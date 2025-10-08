// public/sw.js
const CACHE_NAME = 'medicine-tracker-v2'
const ASSETS_TO_CACHE = [
  '/',
  '/index.html',
  '/manifest.json',
  '/js/app.js',
  '/css/app.css',
  '/favicon.ico'
]

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(ASSETS_TO_CACHE))
      .then(() => self.skipWaiting())
  )
})

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cache => {
          if (cache !== CACHE_NAME) {
            return caches.delete(cache)
          }
        })
      )
    })
  )
})

self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return
  
  event.respondWith(
    caches.match(event.request)
      .then(cachedResponse => {
        // Return cached response if found
        if (cachedResponse) {
          return cachedResponse
        }
        
        // Otherwise fetch from network
        return fetch(event.request)
          .then(response => {
            // Cache the response if it's successful
            if (response && response.status === 200) {
              const responseToCache = response.clone()
              caches.open(CACHE_NAME)
                .then(cache => cache.put(event.request, responseToCache))
            }
            return response
          })
          .catch(() => {
            // If offline and no cache, return a fallback
            if (event.request.headers.get('accept').includes('text/html')) {
              return caches.match('/index.html')
            }
          })
      })
  )
})

// Background sync registration
self.addEventListener('sync', event => {
  if (event.tag === 'sync-medicines') {
    event.waitUntil(
      // In a real app, you would implement actual sync logic here
      console.log('Background sync for medicines')
    )
  }
})