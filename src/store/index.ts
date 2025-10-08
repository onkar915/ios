import { createStore } from 'vuex'
import medicines from './modules/medicines'

export interface RootState {
  // Add root state types here if needed
}

export default createStore<RootState>({
  modules: {
    medicines
  }
})