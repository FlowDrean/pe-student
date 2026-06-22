const PREFIX = 'student_app_'

export const storage = {
  get(key: string): any {
    try {
      const raw = uni.getStorageSync(PREFIX + key)
      return raw ? JSON.parse(raw) : null
    } catch {
      return uni.getStorageSync(PREFIX + key)
    }
  },

  set(key: string, value: any) {
    const val = typeof value === 'string' ? value : JSON.stringify(value)
    uni.setStorageSync(PREFIX + key, val)
  },

  remove(key: string) {
    uni.removeStorageSync(PREFIX + key)
  },

  clear() {
    uni.clearStorageSync()
  },
}

export default storage
