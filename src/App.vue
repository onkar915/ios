<template>
  <ion-app>
    <ion-router-outlet />
    <OfflineStatus />
  </ion-app>
</template>

<script lang="ts">
import { IonApp, IonRouterOutlet } from '@ionic/vue';
import { defineComponent, onMounted, onUnmounted } from 'vue';
import { useStore } from 'vuex';
import { App } from '@capacitor/app';

export default defineComponent({
  name: 'App',
  components: { IonApp, IonRouterOutlet },
  setup() {
    const store = useStore();

    const updateNetworkStatus = () => {
      const isOnline = navigator.onLine;
      store.dispatch('medicines/updateOnlineStatus', isOnline);
      
      if (!isOnline) {
        fetch('https://httpbin.org/status/200', { mode: 'no-cors' })
          .then(() => store.dispatch('medicines/updateOnlineStatus', true))
          .catch(() => store.dispatch('medicines/updateOnlineStatus', false));
      }
    };

    const handleMedicineAction = (action: string, medicineId: string) => {
      if (action === 'taken') {
        store.dispatch('medicines/markAsTaken', medicineId);
      } else if (action === 'missed') {
        store.dispatch('medicines/markAsMissed', medicineId);
      }
      window.history.replaceState({}, document.title, window.location.pathname);
    };

    const setupWidgetListeners = async () => {
      if (!window.Capacitor?.isNativePlatform) return;

      try {
        // Handle direct URL opens
        App.addListener('appUrlOpen', ({ url }: { url: string }) => {
          try {
            const urlObj = new URL(url);
            const action = urlObj.searchParams.get('action');
            const medicineId = urlObj.searchParams.get('medicineId');
            if (action && medicineId) handleMedicineAction(action, medicineId);
          } catch (error) {
            console.error('Error handling appUrlOpen:', error);
          }
        });

        // Handle app coming to foreground
        App.addListener('appStateChange', ({ isActive }: { isActive: boolean }) => {
          if (isActive) {
            const urlObj = new URL(window.location.href);
            const action = urlObj.searchParams.get('action');
            const medicineId = urlObj.searchParams.get('medicineId');
            if (action && medicineId) handleMedicineAction(action, medicineId);
          }
        });

        // Handle initial launch URL
        const launchUrl = await App.getLaunchUrl();
        if (launchUrl?.url) {
          try {
            const urlObj = new URL(launchUrl.url);
            const action = urlObj.searchParams.get('action');
            const medicineId = urlObj.searchParams.get('medicineId');
            if (action && medicineId) handleMedicineAction(action, medicineId);
          } catch (error) {
            console.error('Error handling launch URL:', error);
          }
        }
      } catch (error) {
        console.error('Error setting up widget listeners:', error);
      }
    };

    const setupWidgetActionListener = () => {
      window.addEventListener('widgetAction', (event: any) => {
        const { action, medicineId } = event.detail;
        if (action === 'taken') {
          store.dispatch('medicines/markAsTaken', medicineId);
        } else if (action === 'missed') {
          store.dispatch('medicines/markAsMissed', medicineId);
        }
      });
    };

    onMounted(() => {
      window.addEventListener('online', updateNetworkStatus);
      window.addEventListener('offline', updateNetworkStatus);
      updateNetworkStatus();
      setupWidgetListeners();
      setupWidgetActionListener();
    });

    onUnmounted(() => {
      window.removeEventListener('online', updateNetworkStatus);
      window.removeEventListener('offline', updateNetworkStatus);
      window.removeEventListener('widgetAction', setupWidgetActionListener);
      App.removeAllListeners().catch(console.error);
    });

    return {};
  }
});
</script>