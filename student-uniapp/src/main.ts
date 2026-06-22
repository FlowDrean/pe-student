import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'
import App from './App.vue'
import 'uno.css'
import './styles/global.css'

// 修复 wot-design-uni 在小程序端的 __global 兼容性问题
if (typeof (globalThis as any).__global === 'undefined') {
  ;(globalThis as any).__global = globalThis
}

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  pinia.use(
    createPersistedState({
      storage: {
        getItem(key: string): string | null {
          return uni.getStorageSync(key)
        },
        setItem(key: string, value: string) {
          uni.setStorageSync(key, value)
        },
      },
    }),
  )
  app.use(pinia)
  return { app }
}
