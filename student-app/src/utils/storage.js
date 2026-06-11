/* ============================================
   本地存储封装
   ============================================ */

const PREFIX = 'student_app_'

const storage = {
  get(key) {
    try {
      const raw = localStorage.getItem(PREFIX + key)
      if (raw === null) return null
      return JSON.parse(raw)
    } catch {
      return null
    }
  },

  set(key, value) {
    try {
      localStorage.setItem(PREFIX + key, JSON.stringify(value))
    } catch {
      // 存储满时静默失败
      console.warn('localStorage is full')
    }
  },

  remove(key) {
    localStorage.removeItem(PREFIX + key)
  },

  clear() {
    const keys = Object.keys(localStorage).filter((k) => k.startsWith(PREFIX))
    keys.forEach((k) => localStorage.removeItem(k))
  },

  // 带过期时间的存储
  setWithExpiry(key, value, ttlMs) {
    const item = {
      value,
      expiry: Date.now() + ttlMs,
    }
    this.set(key, item)
  },

  getWithExpiry(key) {
    const item = this.get(key)
    if (!item) return null
    if (Date.now() > item.expiry) {
      this.remove(key)
      return null
    }
    return item.value
  },
}

export default storage
