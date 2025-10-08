<template>
  <ion-badge :color="isReallyOnline ? 'success' : 'danger'">
    {{ isReallyOnline ? 'Online' : 'Offline' }}
  </ion-badge>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useStore } from 'vuex';
import { checkNetworkStatus } from '../utils/network'

export default defineComponent({
  name: 'OfflineStatus',
  setup() {
    const store = useStore();
    const isReallyOnline = computed(() => store.getters['medicines/reliableIsOnline']);
    
    // Periodically verify network status
    setInterval(async () => {
      const isOnline = await checkNetworkStatus();
      store.dispatch('medicines/updateOnlineStatus', isOnline);
    }, 30000); // Check every 30 seconds

    return { isReallyOnline };
  }
});
</script>