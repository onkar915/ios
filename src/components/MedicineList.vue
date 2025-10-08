<template>
  <div>
    <ion-list>
      <ion-item v-for="medicine in medicines" :key="medicine.id">
        <ion-label>
          <h2>{{ medicine.name }}</h2>
          <p>Dosage: {{ medicine.dosage }}</p>
          <p>Next dose: {{ formatTime(medicine.schedule) }}</p>
          <p>Added: {{ formatDate(medicine.createdAt) }}</p>
        </ion-label>
        <ion-badge v-if="isPendingSync(medicine.id)" color="warning" slot="end">
          <ion-icon :icon="timeOutline"></ion-icon>
        </ion-badge>
        <ion-badge v-else color="success" slot="end">
          <ion-icon :icon="checkmarkDoneOutline"></ion-icon>
        </ion-badge>
      </ion-item>
    </ion-list>
    
    <ion-note v-if="medicines.length === 0 && isInitialized" color="medium" class="empty-message">
      No medicines added yet.
    </ion-note>
    <ion-note v-if="!isInitialized" color="medium" class="empty-message">
      Loading medicines...
    </ion-note>
  </div>
</template>

<script lang="ts">
import { 
  IonList, IonItem, IonLabel, IonBadge, IonIcon, IonNote 
} from '@ionic/vue'
import { computed, defineComponent, onMounted } from 'vue'
import { useStore } from 'vuex'
import { timeOutline, checkmarkDoneOutline } from 'ionicons/icons'
import { Medicine } from '../store/modules/medicines'

export default defineComponent({
  name: 'MedicineList',
  components: { 
    IonList, IonItem, IonLabel, IonBadge, IonIcon, IonNote 
  },
  setup() {
    const store = useStore()
    
    onMounted(() => {
      if (!store.getters['medicines/isInitialized']) {
        store.dispatch('medicines/initialize');
      }
    });
    
    const medicines = computed(() => store.getters['medicines/allMedicines'])
    const isInitialized = computed(() => store.getters['medicines/isInitialized'])
    const pendingSyncItems = computed(() => store.getters['medicines/pendingSyncItems'])
    
    const isPendingSync = (id: string) => {
      return pendingSyncItems.value.some((m: Medicine) => m.id === id);
    };
    
    const formatDate = (dateString: string) => {
      return new Date(dateString).toLocaleDateString()
    }
    
    const formatTime = (dateString: string) => {
      return new Date(dateString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
    
    return { 
      medicines, 
      formatDate, 
      formatTime,
      timeOutline,
      checkmarkDoneOutline,
      isInitialized,
      isPendingSync
    }
  }
})
</script>

<style scoped>
ion-badge {
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

ion-badge ion-icon {
  font-size: 16px;
}

.empty-message {
  display: block;
  text-align: center;
  margin-top: 20px;
  padding: 16px;
}
</style>