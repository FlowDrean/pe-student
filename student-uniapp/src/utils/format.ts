/* ============================================
   格式化工具 — uni-app 版
   ============================================ */

// 相对时间
export function timeAgo(dateStr: string | number | Date): string {
  const now = Date.now()
  const date = new Date(dateStr).getTime()
  const diff = now - date

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < week) return `${Math.floor(diff / day)}天前`
  if (diff < month) return `${Math.floor(diff / week)}周前`

  return formatDate(dateStr)
}

// 格式化日期
export function formatDate(dateStr: string | number | Date, fmt: string = 'YYYY-MM-DD HH:mm'): string {
  const d = new Date(dateStr)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')

  return fmt
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
}

// 数字缩写
export function formatNumber(num: number): string {
  if (num < 1000) return String(num)
  if (num < 10000) return (num / 1000).toFixed(1) + 'k'
  if (num < 100000) return (num / 10000).toFixed(1) + 'w'
  return '10w+'
}

// 价格格式化
export function formatPrice(price: number): string {
  if (price === 0) return '免费'
  return `¥${Number(price).toFixed(2)}`
}

// 隐藏手机号中间4位
export function maskPhone(phone: string): string {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}
