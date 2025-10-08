import storage from '../../plugins/storage';
import { Module } from 'vuex';
import { v4 as uuidv4 } from 'uuid';
import { checkNetworkStatus } from '../../utils/network';

export interface Medicine {
  id: string;
  name: string;
  dosage: string;
  schedule: string;
  createdAt: string;
  updatedAt: string;
  isSynced?: boolean;
  pendingSync?: boolean;
}

interface MedicinesState {
  medicines: Medicine[];
  lastSync: string | null;
  isOnline: boolean;
  pendingSyncQueue: Medicine[];
  networkChecked: boolean;
  initialized: boolean;
}

const medicinesModule: Module<MedicinesState, any> = {
  namespaced: true,
  state: (): MedicinesState => ({
    medicines: [],
    lastSync: null,
    isOnline: navigator.onLine,
    pendingSyncQueue: [],
    networkChecked: false,
    initialized: false
  }),
  mutations: {
    ADD_MEDICINE(state, medicine: Medicine) {
      // Check if medicine already exists before adding
      const exists = state.medicines.some(m => m.id === medicine.id);
      if (!exists) {
        state.medicines.push(medicine);
        if (medicine.pendingSync) {
          state.pendingSyncQueue.push(medicine);
        }
      }
    },
    SET_MEDICINES(state, medicines: Medicine[]) {
      state.medicines = medicines;
      // Rebuild pending sync queue
      state.pendingSyncQueue = medicines.filter(m => m.pendingSync);
    },
    UPDATE_MEDICINE(state, updatedMedicine: Medicine) {
      state.medicines = state.medicines.map(m => 
        m.id === updatedMedicine.id ? updatedMedicine : m
      );
    },
    SET_ONLINE_STATUS(state, status: boolean) {
      state.isOnline = status;
    },
    SET_LAST_SYNC(state, timestamp: string) {
      state.lastSync = timestamp;
    },
    REMOVE_FROM_SYNC_QUEUE(state, medicineId: string) {
      state.pendingSyncQueue = state.pendingSyncQueue.filter(m => m.id !== medicineId);
      state.medicines = state.medicines.map(m => 
        m.id === medicineId ? {...m, pendingSync: false, isSynced: true} : m
      );
    },
    SET_NETWORK_CHECKED(state, checked: boolean) {
      state.networkChecked = checked;
    },
    SET_INITIALIZED(state, value: boolean) {
      state.initialized = value;
    },
    // Add these to your mutations:
MARK_AS_TAKEN(state, medicineId: string) {
  state.medicines = state.medicines.map(m => 
    m.id === medicineId ? { ...m, lastTaken: new Date().toISOString() } : m
  );
},
MARK_AS_MISSED(state, medicineId: string) {
  state.medicines = state.medicines.map(m => 
    m.id === medicineId ? { ...m, lastMissed: new Date().toISOString() } : m
  );
},
  },
  actions: {
    async initialize({ commit, dispatch }) {
      try {
        const medicines = await storage.getItem<Medicine[]>('medicines') || [];
        
        // Normalize the data (in case of duplicates)
        const uniqueMedicines = medicines.reduce((acc, current) => {
          const x = acc.find(item => item.id === current.id);
          if (!x) {
            return acc.concat([current]);
          } else {
            return acc;
          }
        }, [] as Medicine[]);

        commit('SET_MEDICINES', uniqueMedicines);
        await dispatch('verifyNetworkStatus');
        commit('SET_INITIALIZED', true);
        
        // Trigger sync if online
        if (this.state.medicines.isOnline) {
          await dispatch('syncData');
        }
      } catch (error) {
        console.error('Initialization error:', error);
        commit('SET_INITIALIZED', true);
      }
    },

    async verifyNetworkStatus({ commit, state }) {
      const isOnline = await checkNetworkStatus();
      commit('SET_ONLINE_STATUS', isOnline);
      commit('SET_NETWORK_CHECKED', true);
      
      // If we just came online, trigger sync
      if (isOnline && !state.isOnline && state.pendingSyncQueue.length > 0) {
        await this.dispatch('medicines/syncData');
      }
      
      return isOnline;
    },

    async addMedicine({ commit, state, dispatch }, medicineData) {
      try {
        const isOnline = await dispatch('verifyNetworkStatus');
        
        // Check for duplicates based on content, not ID
        const duplicateExists = await storage.checkMedicineExists(medicineData);
        if (duplicateExists) {
          throw new Error('DUPLICATE_MEDICINE');
        }

        const newMedicine: Medicine = {
          ...medicineData,
          id: uuidv4(),
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          isSynced: isOnline,
          pendingSync: !isOnline
        };

        commit('ADD_MEDICINE', newMedicine);
        await storage.setItem('medicines', state.medicines);
        
        if (isOnline) {
          commit('SET_LAST_SYNC', new Date().toISOString());
        }
        
        return newMedicine;
      } catch (error) {
        console.error('Error adding medicine:', error);
        throw error;
      }
    },

    async syncData({ commit, state, dispatch }) {
      if (!state.isOnline) return;

      try {
        // Process each item in the queue
        for (const medicine of [...state.pendingSyncQueue]) {
          try {
            // Verify it doesn't already exist in the remote storage
            const exists = await storage.checkMedicineExists(medicine);
            if (!exists) {
              // Update the medicine to mark as synced
              const updatedMedicine = {
                ...medicine,
                isSynced: true,
                pendingSync: false,
                updatedAt: new Date().toISOString()
              };
              
              // Update in local state
              commit('UPDATE_MEDICINE', updatedMedicine);
              commit('REMOVE_FROM_SYNC_QUEUE', medicine.id);
              
              // Save to storage
              await storage.setItem('medicines', state.medicines);
            } else {
              // If it exists, just mark as synced
              commit('REMOVE_FROM_SYNC_QUEUE', medicine.id);
            }
          } catch (error) {
            console.error('Error syncing medicine:', medicine.name, error);
          }
        }

        commit('SET_LAST_SYNC', new Date().toISOString());
      } catch (error) {
        console.error('Sync error:', error);
      }
    },

    async updateOnlineStatus({ dispatch }) {
      await dispatch('verifyNetworkStatus');
    },
    // Add these to your actions:
async markAsTaken({ commit, state }, medicineId: string) {
  commit('MARK_AS_TAKEN', medicineId);
  await storage.setItem('medicines', state.medicines);
  if (window.Capacitor?.isNativePlatform) {
    await storage.updateWidget();
  }
},

async markAsMissed({ commit, state }, medicineId: string) {
  commit('MARK_AS_MISSED', medicineId);
  await storage.setItem('medicines', state.medicines);
  if (window.Capacitor?.isNativePlatform) {
    await storage.updateWidget();
  }
},


  },
  getters: {
    allMedicines: (state) => state.medicines,
    isOnline: (state) => state.isOnline,
    reliableIsOnline: (state) => state.networkChecked ? state.isOnline : navigator.onLine,
    pendingSyncCount: (state) => state.pendingSyncQueue.length,
    lastSync: (state) => state.lastSync,
    isInitialized: (state) => state.initialized,
    pendingSyncItems: (state) => state.pendingSyncQueue
  }
};

export default medicinesModule;