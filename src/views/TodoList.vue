<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Medicine List</ion-title>
      </ion-toolbar>
    </ion-header>
    
    <ion-content class="ion-padding">
      <!-- Add new todo form -->
      <ion-item>
        <ion-input 
          v-model="newTodo" 
          placeholder="Add a new medicine" 
          @keyup.enter="addTodo"
        ></ion-input>
        
        <!-- Date and time picker for new todo -->
        <ion-datetime-button datetime="newTodoDateTime"></ion-datetime-button>
        
        <ion-button @click="addTodo" slot="end" fill="clear">
          <ion-icon :icon="addOutline"></ion-icon>
        </ion-button>
      </ion-item>
      
      <!-- Date time picker modal -->
      <ion-modal :keep-contents-mounted="true">
        <ion-datetime 
          id="newTodoDateTime"
          v-model="newTodoDateTime"
          presentation="date-time"
          :show-default-buttons="true"
          :min="minDate"
          :max="maxDate"
        ></ion-datetime>
      </ion-modal>
      
      <!-- Calendar integration button -->
      <ion-button expand="block" @click="openCalendar" class="calendar-btn">
        <ion-icon :icon="calendarOutline" slot="start"></ion-icon>
        Open Calendar
      </ion-button>
      
      <!-- Column headers -->
      <ion-grid class="header-grid">
        <ion-row class="ion-align-items-center header-row">
          <ion-col size="5" class="header-col">
            <ion-label class="header-label">Medicine Name</ion-label>
          </ion-col>
          <ion-col size="4" class="header-col ion-text-center">
            <ion-label class="header-label">Date & Time</ion-label>
          </ion-col>
          <ion-col size="3" class="header-col ion-text-center">
            <ion-label class="header-label">Actions</ion-label>
          </ion-col>
        </ion-row>
      </ion-grid>
      
      <!-- Todo list -->
      <ion-list>
        <ion-item-sliding v-for="todo in todos" :key="todo.id">
          <ion-item>
            <ion-grid class="todo-grid">
              <ion-row class="ion-align-items-center">
                <!-- Text column -->
                <ion-col size="5">
                  <div>
                    <ion-label :class="{ 'completed': todo.completed }">
                      {{ todo.text }}
                    </ion-label>
                  </div>
                </ion-col>
                
                <!-- Date and time column -->
                <ion-col size="4">
                  <div class="date-time-container">
                    <div class="week-display">
                      Week {{ getWeekOfMonth(todo.dateTime) }} of {{ getMonthName(todo.dateTime) }}
                    </div>
                    <div class="datetime-display">
                      <ion-button 
                        fill="clear" 
                        size="small" 
                        @click="editDateTime(todo)"
                        class="edit-datetime-btn"
                      >
                        <ion-icon :icon="calendarOutline" class="calendar-icon"></ion-icon>
                        {{ formatDate(todo.dateTime) }}
                        <span class="time-text">{{ formatTime(todo.dateTime) }}</span>
                      </ion-button>
                    </div>
                  </div>
                  
                  <!-- Date time picker modal for editing -->
                  <ion-modal 
                    :is-open="editingTodoId === todo.id" 
                    @didDismiss="editingTodoId = null"
                    :keep-contents-mounted="true"
                  >
                    <ion-header>
                      <ion-toolbar>
                        <ion-title>Edit Date & Time</ion-title>
                        <ion-buttons slot="end">
                          <ion-button @click="editingTodoId = null">Close</ion-button>
                          <ion-button @click="openNativeCalendar(todo)">Use Calendar App</ion-button>
                        </ion-buttons>
                      </ion-toolbar>
                    </ion-header>
                    <ion-content>
                      <ion-datetime 
                        :value="todo.dateTime"
                        presentation="date-time"
                        :show-default-buttons="true"
                        @ionChange="updateTodoDateTime(todo.id, $event)"
                        :min="minDate"
                        :max="maxDate"
                      ></ion-datetime>
                    </ion-content>
                  </ion-modal>
                </ion-col>
                
                <!-- Action buttons column -->
                <ion-col size="3" class="ion-text-center action-buttons">
                  <!-- <ion-button 
                    fill="clear" 
                    color="primary" 
                    @click="openNativeCalendar(todo)"
                    class="calendar-item-btn"
                    v-if="!todo.completed"
                  >
                    <ion-icon :icon="calendarOutline"></ion-icon>
                  </ion-button> -->
                  
                  <!-- <ion-checkbox 
                    :checked="todo.completed" 
                    @click="toggleTodo(todo.id)"
                    class="todo-checkbox"
                  ></ion-checkbox> -->
                  
                  <ion-button 
                    fill="clear" 
                    color="danger" 
                    @click="deleteTodo(todo.id)"
                    class="delete-btn"
                  >
                    <ion-icon :icon="trashOutline"></ion-icon>
                  </ion-button>
                </ion-col>
              </ion-row>
            </ion-grid>
          </ion-item>
        </ion-item-sliding>
      </ion-list>
      
      <!-- Empty state -->
      <div v-if="todos.length === 0" class="empty-state">
        <ion-icon :icon="checkmarkDoneOutline" size="large"></ion-icon>
        <p>No medicines yet. Add one above!</p>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, 
  IonItem, IonInput, IonButton, IonIcon, IonList, 
  IonItemSliding, IonItemOptions, IonItemOption, IonLabel, 
  IonCheckbox, IonGrid, IonRow, IonCol, toastController,
  IonModal, IonDatetime, IonDatetimeButton, IonButtons
} from '@ionic/vue';
import { 
  addOutline, 
  trashOutline, 
  checkmarkDoneOutline, 
  calendarOutline
} from 'ionicons/icons';
import { Capacitor } from '@capacitor/core';
import TodoWidget from '@/capacitor-plugins/TodoWidget';

interface Todo {
  id: number;
  text: string;
  completed: boolean;
  dateTime: string;
}

const newTodo = ref('');
const newTodoDateTime = ref(getDefaultDateTime());
const todos = ref<Todo[]>([]);
const editingTodoId = ref<number | null>(null);

// Date constraints
const minDate = new Date().toISOString();
const maxDate = new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString();

// Check if we're on Android
const isAndroid = computed(() => {
  return Capacitor.isNativePlatform() && Capacitor.getPlatform() === 'android';
});

// Get default date time (next hour)
function getDefaultDateTime(): string {
  const now = new Date();
  now.setHours(now.getHours() + 1);
  now.setMinutes(0);
  now.setSeconds(0);
  return now.toISOString();
}

// Load todos
onMounted(() => {
  const savedTodos = localStorage.getItem('todos');
  if (savedTodos) {
    todos.value = JSON.parse(savedTodos);
    
    // Ensure all todos have a dateTime property
    todos.value = todos.value.map(todo => ({
      ...todo,
      dateTime: todo.dateTime || getDefaultDateTime()
    }));
  }
});

// Update saveTodos to handle completion status
// In your saveTodos function
const saveTodos = async () => {
  const todosJson = JSON.stringify(todos.value.map(todo => ({
    text: todo.text,
    completed: todo.completed,
    id: todo.id,
    dateTime: todo.dateTime
  })));
  
  // Web fallback
  localStorage.setItem('todos', todosJson);
  
  if (Capacitor.isNativePlatform()) {
    try {
      if (Capacitor.getPlatform() === 'android') {
        // Android implementation
        if ((window as any).AndroidBridge) {
          (window as any).AndroidBridge.updateWidgetData(todosJson);
        }
      } else if (Capacitor.getPlatform() === 'ios') {
        // iOS implementation
        await TodoWidget.saveTodos({ todos: todosJson });
        await TodoWidget.updateWidget();
      }
    } catch (e) {
      console.error('Widget update failed:', e);
    }
  }
};

// Add todo function
const addTodo = async () => {
  if (newTodo.value.trim() === '') return;
  
  todos.value.push({
    id: Date.now(),
    text: newTodo.value.trim(),
    completed: false,
    dateTime: newTodoDateTime.value
  });
  
  newTodo.value = '';
  newTodoDateTime.value = getDefaultDateTime();
  await saveTodos();
};

// Toggle todo completion
const toggleTodo = (id: number) => {
  const todo = todos.value.find(t => t.id === id);
  if (todo) {
    todo.completed = !todo.completed;
    saveTodos();
  }
};

// Delete todo
const deleteTodo = async (id: number) => {
  todos.value = todos.value.filter(t => t.id !== id);
  saveTodos();
};

// Open datetime editor for a todo
const editDateTime = (todo: Todo) => {
  editingTodoId.value = todo.id;
};

// Update todo datetime
const updateTodoDateTime = (id: number, event: any) => {
  const todo = todos.value.find(t => t.id === id);
  if (todo && event.detail.value) {
    todo.dateTime = event.detail.value as string;
    saveTodos();
  }
  editingTodoId.value = null;
};

// Format date for display
const formatDate = (dateTime: string): string => {
  if (!dateTime) return 'Select date';
  
  const date = new Date(dateTime);
  return date.toLocaleDateString(undefined, { 
    day: '2-digit', 
    month: '2-digit', 
    year: 'numeric' 
  });
};

// Format time for display
const formatTime = (dateTime: string): string => {
  if (!dateTime) return '';
  
  const date = new Date(dateTime);
  return date.toLocaleTimeString(undefined, { 
    hour: '2-digit', 
    minute: '2-digit',
    hour12: true 
  });
};

// Get week of the month (1-5)
const getWeekOfMonth = (dateTime: string): number => {
  if (!dateTime) return 0;
  
  const date = new Date(dateTime);
  const firstDayOfMonth = new Date(date.getFullYear(), date.getMonth(), 1);
  const firstDayOfWeek = firstDayOfMonth.getDay() || 7; // Convert Sunday (0) to 7
  
  const offset = ((firstDayOfWeek + 6) % 7); // Adjust so Monday is first day of week
  return Math.ceil((date.getDate() + offset) / 7);
};

// Get month name
const getMonthName = (dateTime: string): string => {
  if (!dateTime) return '';
  
  const date = new Date(dateTime);
  return date.toLocaleDateString(undefined, { month: 'long' });
};

// Open native calendar for a specific todo
const openNativeCalendar = async (todo: Todo) => {
  try {
    if (isAndroid.value && (window as any).AndroidBridge && (window as any).AndroidBridge.openCalendarForEvent) {
      const startDate = new Date(todo.dateTime);
      
      // Open calendar with the todo's date and details
      (window as any).AndroidBridge.openCalendarForEvent(
        startDate.getTime(),
        todo.text,
        `Medicine Reminder: ${todo.text}`
      );
      
      showToast('Opening calendar...');
    } else {
      showToast('Calendar integration is only available on Android');
    }
  } catch (error) {
    console.error('Error opening calendar:', error);
    showToast('Failed to open calendar');
  }
};

// Open native calendar app
const openCalendar = async () => {
  try {
    if (isAndroid.value && (window as any).AndroidBridge && (window as any).AndroidBridge.openCalendar) {
      (window as any).AndroidBridge.openCalendar();
      showToast('Opening calendar...');
    } else {
      showToast('This feature is only available on Android');
    }
  } catch (error) {
    console.error('Error opening calendar:', error);
    showToast('Failed to open calendar. Make sure you have a calendar app installed.');
  }
};

// Show toast notification
const showToast = async (message: string) => {
  const toast = await toastController.create({
    message,
    duration: 2000,
    position: 'bottom'
  });
  await toast.present();
};
</script>

<style scoped>
.completed {
  text-decoration: line-through;
  color: var(--ion-color-medium);
}

.empty-state {
  text-align: center;
  margin-top: 50px;
  color: var(--ion-color-medium);
}

.empty-state ion-icon {
  color: var(--ion-color-success);
}

/* Grid styling */
.todo-grid {
  width: 100%;
  padding: 0;
}

.todo-checkbox {
  --size: 24px;
  margin: 0 8px 0 0;
}

.delete-btn, .calendar-item-btn {
  --padding-start: 8px;
  --padding-end: 8px;
  margin: 0;
}

.calendar-btn {
  margin: 16px 0;
}

ion-label {
  margin: 0;
  padding: 12px 0;
}

ion-col {
  display: flex;
  align-items: center;
}

/* Header row styling */
.header-grid {
  margin-top: 20px;
  margin-bottom: 10px;
  border-bottom: 1px solid var(--ion-color-light-shade);
  padding-bottom: 8px;
}

.header-row {
  font-weight: bold;
}

.header-col {
  padding: 0 8px;
}

.header-label {
  font-weight: 600;
  color: var(--ion-color-dark);
}

/* Action buttons grouping */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

/* Date and time styling */
.date-time-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.week-display {
  font-size: 12px;
  font-weight: bold;
  color: var(--ion-color-primary);
  margin-bottom: 4px;
}

.datetime-display {
  display: flex;
  align-items: center;
}

.edit-datetime-btn {
  --padding-start: 0;
  --padding-end: 0;
  margin: 0;
  font-size: 14px;
}

.calendar-icon {
  font-size: 14px;
  margin-right: 4px;
  color: #333333;
}

.time-text {
  margin-left: 4px;
  font-weight: normal;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .header-col {
    font-size: 12px;
  }
  
  .edit-datetime-btn {
    font-size: 12px;
  }
  
  .week-display {
    font-size: 10px;
  }
}
</style>