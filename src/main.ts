import { createApp } from 'vue'
import App from './App.vue'
import { IonicVue } from '@ionic/vue'
import router from './router'
import store from './store'


// Core Ionic CSS
import '@ionic/vue/css/core.css'
import '@ionic/vue/css/normalize.css'
import '@ionic/vue/css/structure.css'
import '@ionic/vue/css/typography.css'

const app = createApp(App)
  .use(IonicVue)
  .use(router)
  .use(store)

router.isReady().then(() => {
  // Register service worker after router is ready
  if ('serviceWorker' in navigator && import.meta.env.PROD) {
    navigator.serviceWorker.register('/service-worker.js')
      .then(registration => {
        console.log('SW registered:', registration)
      })
      .catch(error => {
        console.log('SW registration failed:', error)
      })
  }
  
  app.mount('#app')
})