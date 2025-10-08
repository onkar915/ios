import { Preferences } from '@capacitor/preferences';
import localforage from 'localforage';
import { Medicine } from '../store/modules/medicines';

// Configure localForage
const store = localforage.createInstance({
  name: 'MedicineTrackerDB',
  storeName: 'medicines',
  driver: [
    localforage.INDEXEDDB,
    localforage.WEBSQL,
    localforage.LOCALSTORAGE
  ]
});

// Proper Capacitor type declarations
declare global {
  interface Window {
    Capacitor?: {
      isNativePlatform: boolean;
      Platform?: {
        isAndroid: boolean;
        isIOS: boolean;
      };
      Plugins?: {
        App: {
          addListener: (event: string, callback: Function) => Promise<any>;
          removeAllListeners: () => Promise<void>;
        };
        Preferences: typeof Preferences;
      };
      Bridge?: {
        eval: (code: string, options: { android: boolean }) => Promise<any>;
      };
    };
  }
}

const storage = {
  async initialize(): Promise<void> {
    try {
      await store.setItem('__test__', 'test');
      await store.removeItem('__test__');
    } catch (error) {
      console.error('Storage initialization failed:', error);
      throw error;
    }
  },

  async setItem<T>(key: string, value: T): Promise<void> {
    const serializedValue = JSON.stringify(value);
    if (window.Capacitor?.isNativePlatform) {
      await Preferences.set({ key, value: serializedValue });
      if (key === 'medicines') {
        await this.syncWithNativePreferences(value as Medicine[]);
      }
    }
    await store.setItem(key, serializedValue);
  },

  async getItem<T>(key: string): Promise<T | null> {
    let value: string | null = null;
    if (window.Capacitor?.isNativePlatform) {
      const result = await Preferences.get({ key });
      value = result.value;
    } else {
      value = await store.getItem<string>(key);
    }
    return value ? JSON.parse(value) : null;
  },

  async checkMedicineExists(medicine: { name: string; dosage: string; schedule: string }): Promise<boolean> {
    const medicines = await this.getItem<Medicine[]>('medicines') || [];
    return medicines.some(m => 
      m.name.toLowerCase() === medicine.name.toLowerCase() &&
      m.dosage.toLowerCase() === medicine.dosage.toLowerCase() &&
      new Date(m.schedule).getTime() === new Date(medicine.schedule).getTime()
    );
  },

  async removeItem(key: string): Promise<void> {
    if (window.Capacitor?.isNativePlatform) {
      await Preferences.remove({ key });
    }
    await store.removeItem(key);
  },

  async clear(): Promise<void> {
    if (window.Capacitor?.isNativePlatform) {
      try {
        const keysResult = await (Preferences as any).keys();
        const keys = keysResult?.keys || [];
        await Promise.all(keys.map((k: string) => Preferences.remove({ key: k })));
      } catch (error) {
        console.error('Error clearing preferences:', error);
      }
    }
    await store.clear();
  },

  async keys(): Promise<string[]> {
    if (window.Capacitor?.isNativePlatform) {
      try {
        const keysResult = await (Preferences as any).keys();
        return keysResult?.keys || [];
      } catch (error) {
        console.error('Error getting keys:', error);
        return [];
      }
    }
    return await store.keys();
  },

  async syncWithNativePreferences(medicines: Medicine[]): Promise<void> {
    if (window.Capacitor?.isNativePlatform && window.Capacitor.Plugins) {
      try {
        // Save to SharedPreferences for widget access
        await Preferences.set({
          key: 'medicines',
          value: JSON.stringify(medicines)
        });
        
        // Notify widget to update if Android
        if (window.Capacitor.Platform?.isAndroid) {
          const { App } = window.Capacitor.Plugins;
          if (App) {
            App.addListener('appStateChange', ({ isActive }: { isActive: boolean }) => {
              if (isActive) {
                this.updateWidget();
              }
            });
          }
        }
      } catch (error) {
        console.error('Error syncing with native preferences:', error);
      }
    }
  },

  async updateWidget(): Promise<void> {
    if (window.Capacitor?.isNativePlatform && 
        window.Capacitor.Platform?.isAndroid && 
        window.Capacitor.Bridge) {
      try {
        await window.Capacitor.Bridge.eval(`
          if (getApplicationContext() != null) {
            MedicineWidget.updateWidgets(getApplicationContext());
          }
        `, { android: true });
      } catch (error) {
        console.error('Error updating widget:', error);
      }
    }
  }
};

// Initialize storage
storage.initialize().catch(console.error);

export default storage;