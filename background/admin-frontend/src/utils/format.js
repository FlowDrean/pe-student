/**
 * 格式化日期字符串，将 ISO 格式中的 T 替换为空格，截掉毫秒
 */
export function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}
