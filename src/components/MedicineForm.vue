<template>
  <ion-card>
    <ion-card-header>
      <ion-card-title>Add Medicine</ion-card-title>
    </ion-card-header>
    <ion-card-content>
      <ion-item>
        <ion-label position="stacked">Name*</ion-label>
        <ion-input 
          v-model="name" 
          type="text" 
          required
          :class="{ 'ion-invalid': nameError }"
          @ionBlur="validateName()"
        ></ion-input>
        <ion-note slot="error" v-if="nameError">Medicine name is required</ion-note>
      </ion-item>

      <ion-item>
        <ion-label position="stacked">Dosage*</ion-label>
        <ion-input 
          v-model="dosage" 
          type="text" 
          required
          :class="{ 'ion-invalid': dosageError }"
          @ionBlur="validateDosage()"
        ></ion-input>
        <ion-note slot="error" v-if="dosageError">Dosage is required</ion-note>
      </ion-item>

      <ion-item>
        <ion-label position="stacked">Schedule*</ion-label>
        <ion-datetime 
          v-model="schedule" 
          presentation="time"
          :class="{ 'ion-invalid': scheduleError }"
          @ionBlur="validateSchedule()"
        ></ion-datetime>
        <ion-note slot="error" v-if="scheduleError">Please select a time</ion-note>
      </ion-item>

      <ion-button 
        expand="block" 
        @click="addMedicine"
        :disabled="!formValid || isAdding || !isInitialized"
      >
        <ion-spinner v-if="isAdding" name="crescent"></ion-spinner>
        <span v-else>Add Medicine</span>
      </ion-button>
      
      <ion-note v-if="errorMessage" color="danger" class="status-message">
        {{ errorMessage }}
      </ion-note>
      <ion-note v-if="successMessage" color="success" class="status-message">
        {{ successMessage }}
      </ion-note>
      <ion-note v-if="isOffline" color="warning" class="status-message">
        {{ networkStatusMessage }}
      </ion-note>
      <ion-note v-if="pendingSyncCount > 0" color="medium" class="status-message">
        {{ pendingSyncCount }} medicine(s) waiting to sync
      </ion-note>
      <ion-note v-if="!isInitialized" color="medium" class="status-message">
        Initializing application...
      </ion-note>
    </ion-card-content>
  </ion-card>
</template>

<script lang="ts">
import { 
  IonCard, IonCardHeader, IonCardTitle, IonCardContent, 
  IonItem, IonLabel, IonInput, IonDatetime, 
  IonButton, IonNote, IonSpinner 
} from '@ionic/vue';
import { ref, computed, onMounted, watch } from 'vue';
import { useStore } from 'vuex';

export default {
  name: 'MedicineForm',
  components: { 
    IonCard, IonCardHeader, IonCardTitle, IonCardContent, 
    IonItem, IonLabel, IonInput, IonDatetime, 
    IonButton, IonNote, IonSpinner 
  },
  setup() {
    const store = useStore();
    const name = ref('');
    const dosage = ref('');
    const schedule = ref(new Date().toISOString());
    const errorMessage = ref('');
    const successMessage = ref('');
    const isAdding = ref(false);
    const nameError = ref(false);
    const dosageError = ref(false);
    const scheduleError = ref(false);

    onMounted(() => {
      store.dispatch('medicines/initialize');
    });

    const isOffline = computed(() => !store.getters['medicines/reliableIsOnline']);
    const pendingSyncCount = computed(() => store.getters['medicines/pendingSyncCount']);
    const isInitialized = computed(() => store.getters['medicines/isInitialized']);
    const networkStatusMessage = computed(() => 
      store.getters['medicines/networkChecked'] 
        ? 'You are offline. Medicine will sync when you reconnect.'
        : 'Checking network connection...'
    );

    const formValid = computed(() => 
      name.value.trim() && 
      dosage.value.trim() && 
      schedule.value &&
      !nameError.value && 
      !dosageError.value && 
      !scheduleError.value
    );

    const validateName = () => {
      nameError.value = !name.value.trim();
    };

    const validateDosage = () => {
      dosageError.value = !dosage.value.trim();
    };

    const validateSchedule = () => {
      scheduleError.value = !schedule.value;
    };

    const addMedicine = async () => {
      errorMessage.value = '';
      successMessage.value = '';
      isAdding.value = true;
      
      validateName();
      validateDosage();
      validateSchedule();
      
      if (!formValid.value) {
        errorMessage.value = 'Please fill all required fields correctly';
        isAdding.value = false;
        return;
      }
      
      try {
        const medicine = {
          name: name.value.trim(),
          dosage: dosage.value.trim(),
          schedule: schedule.value
        };
        
        await store.dispatch('medicines/addMedicine', medicine);
        successMessage.value = 'Medicine added successfully!';
        resetForm();
      } catch (error: any) {
        if (error.message === 'DUPLICATE_MEDICINE') {
          errorMessage.value = 'This medicine already exists with the same dosage and schedule';
        } else {
          errorMessage.value = 'Failed to add medicine. Please try again.';
        }
      } finally {
        isAdding.value = false;
      }
    };

    const resetForm = () => {
      name.value = '';
      dosage.value = '';
      schedule.value = new Date().toISOString();
      nameError.value = false;
      dosageError.value = false;
      scheduleError.value = false;
      setTimeout(() => {
        successMessage.value = '';
      }, 3000);
    };

    // Watch for network changes to trigger sync
    watch(isOffline, (newVal) => {
      if (!newVal) {
        store.dispatch('medicines/syncData');
      }
    });

    return { 
      name, dosage, schedule, 
      addMedicine, isOffline,
      formValid, errorMessage,
      successMessage, isAdding,
      nameError, dosageError, scheduleError,
      validateName, validateDosage, validateSchedule,
      pendingSyncCount, networkStatusMessage,
      isInitialized
    };
  }
};
</script>

<style scoped>
.status-message {
  display: block;
  margin-top: 12px;
  text-align: center;
}

.ion-invalid {
  --highlight-color: var(--ion-color-danger);
}
</style>