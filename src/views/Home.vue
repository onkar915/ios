<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Medicine Tracker</ion-title>
        <ion-buttons slot="end">
          <ion-badge v-if="pendingSyncCount > 0" color="danger">
            {{ pendingSyncCount }}
          </ion-badge>
          <OfflineStatus />
          <ion-button @click="syncData" :disabled="!isOnline || isSyncing">
            <ion-icon slot="icon-only" :icon="refreshOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding">
      <MedicineForm />
      <ion-refresher slot="fixed" @ionRefresh="handleRefresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>
      <MedicineList />
    </ion-content>
  </ion-page>
</template>

<script lang="ts">
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, 
  IonButton, IonIcon, IonButtons, IonBadge, IonRefresher, 
  IonRefresherContent 
} from '@ionic/vue'
import { defineComponent, computed, ref } from 'vue'
import { useStore } from 'vuex'
import { refreshOutline } from 'ionicons/icons'
import MedicineForm from '../components/MedicineForm.vue'
import MedicineList from '../components/MedicineList.vue'
import OfflineStatus from './OfflineStatus.vue'

export default defineComponent({
  name: 'HomePage',
  components: { 
    IonPage, IonHeader, IonToolbar, IonTitle, IonContent, 
    IonButton, IonIcon, IonButtons, IonBadge,
    IonRefresher, IonRefresherContent,
    MedicineForm, MedicineList, OfflineStatus 
  },
  setup() {
    const store = useStore()
    const isSyncing = ref(false)
    
    const isOnline = computed(() => store.getters['medicines/isOnline'])
    const pendingSyncCount = computed(() => store.getters['medicines/pendingSyncCount'])
    const lastSync = computed(() => store.getters['medicines/lastSync'])

    const syncData = async () => {
      isSyncing.value = true
      try {
        await store.dispatch('medicines/syncData')
      } finally {
        isSyncing.value = false
      }
    }

    const handleRefresh = async (event: CustomEvent) => {
      await syncData()
      event.detail.complete()
    }

    return { 
      isOnline,
      pendingSyncCount,
      lastSync,
      isSyncing,
      syncData,
      handleRefresh,
      refreshOutline
    }
  }
})
</script>