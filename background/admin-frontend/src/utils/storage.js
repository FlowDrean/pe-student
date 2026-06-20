const PREFIX = 'pe_admin_'

export const storage = {
  get(key) {
    try {
      const raw = localStorage.getItem(PREFIX + key)
      return raw ? JSON.parse(raw) : null
    } catch {
      return localStorage.getItem(PREFIX + key)
    }
  },
  set(key, value) {
    const val = typeof value === 'string' ? value : JSON.stringify(value)
    localStorage.setItem(PREFIX + key, val)
  },
  remove(key) {
    localStorage.removeItem(PREFIX + key)
  },
}
