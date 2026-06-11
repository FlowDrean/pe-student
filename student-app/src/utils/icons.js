/* ============================================
   SVG 图标统一管理 — 替代 Emoji
   ============================================ */

// 图标尺寸预设
export const ICON_SIZES = { xs: 14, sm: 16, md: 20, lg: 24, xl: 28 }

// SVG path 数据映射 (Feather Icons style, 24x24 viewBox)
const paths = {
  // 帖子分类
  help: {
    paths: ['M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 012-2h2a2 2 0 012 2v0M9 5h6'],
    stroke: true, fill: false,
  },
  food: {
    paths: ['M18 8h1a4 4 0 010 8h-1M2 8h16v9a4 4 0 01-4 4H6a4 4 0 01-4-4V8zM6 1v3M10 1v3M14 1v3'],
    stroke: true, fill: false,
  },
  study: {
    paths: ['M4 19.5A2.5 2.5 0 016.5 17H20M4 19.5A2.5 2.5 0 006.5 22H20V2H6.5A2.5 2.5 0 004 4.5v15z'],
    stroke: true, fill: false,
  },
  life: {
    paths: ['M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z', 'M9 22V12h6v10'],
    stroke: true, fill: false,
  },
  chat: {
    paths: ['M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z'],
    stroke: true, fill: false,
  },

  // 交易分类
  book: {
    paths: ['M4 19.5A2.5 2.5 0 016.5 17H20M4 19.5A2.5 2.5 0 006.5 22H20V2H6.5A2.5 2.5 0 004 4.5v15z'],
    stroke: true, fill: false,
  },
  electronics: {
    paths: ['M20 16V7a2 2 0 00-2-2H6a2 2 0 00-2 2v9m16 0v4a2 2 0 01-2 2H6a2 2 0 01-2-2v-4m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 16H4'],
    stroke: true, fill: false,
  },
  daily: {
    paths: ['M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4'],
    stroke: true, fill: false,
  },
  clothing: {
    paths: ['M12 3v18M6 3h12a3 3 0 013 3v12a3 3 0 01-3 3H6a3 3 0 01-3-3V6a3 3 0 013-3z'],
    stroke: true, fill: false,
  },
  package: {
    paths: ['M16.5 9.4L7.55 4.24M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z', 'M3.29 7L12 12l8.71-5M12 22V12'],
    stroke: true, fill: false,
  },

  // 通知类型
  heart: {
    paths: ['M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z'],
    stroke: true, fill: false,
  },
  'message-circle': {
    paths: ['M21 11.5a8.38 8.38 0 01-.9 3.8 8.5 8.5 0 01-7.6 4.7 8.38 8.38 0 01-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 01-.9-3.8 8.5 8.5 0 014.7-7.6 8.38 8.38 0 013.8-.9h.5a8.48 8.48 0 018 8v.5z'],
    stroke: true, fill: false,
  },
  user: {
    paths: ['M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2', 'M12 11a4 4 0 100-8 4 4 0 000 8z'],
    stroke: true, fill: false,
  },
  bell: {
    paths: ['M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9', 'M13.73 21a2 2 0 01-3.46 0'],
    stroke: true, fill: false,
  },
  'shopping-cart': {
    paths: ['M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6'],
    stroke: true, fill: false,
  },

  // 菜单图标
  edit: {
    paths: ['M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7', 'M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z'],
    stroke: true, fill: false,
  },
  'file-text': {
    paths: ['M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z', 'M16 2v6h6', 'M10 9h4', 'M10 13h6', 'M10 17h6'],
    stroke: true, fill: false,
  },
  star: {
    paths: ['M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z'],
    stroke: true, fill: false,
  },
  settings: {
    paths: ['M14.7 6.3a1 1 0 000-1.4l-1.4-1.4a1 1 0 00-1.4 0L10.5 4.9a7.43 7.43 0 00-2.1.4L6.9 3.8a1 1 0 00-1.4 0L4.1 5.2a1 1 0 000 1.4l1.5 1.5a7.43 7.43 0 00-.4 2.1l-2 .4a1 1 0 00-.8 1.1l.4 1.9a1 1 0 001.1.8l2-.4a7.43 7.43 0 002.1.4l1.5 1.5a1 1 0 001.4 0l1.4-1.4a1 1 0 000-1.4l-1.5-1.5a7.43 7.43 0 00.4-2.1l2-.4a1 1 0 00.8-1.1l-.4-1.9a1 1 0 00-1.1-.8l-2 .4a7.43 7.43 0 00-2.1-.4z'],
    stroke: true, fill: false,
  },
  trash: {
    paths: ['M3 6h18', 'M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2'],
    stroke: true, fill: false,
  },
  lock: {
    paths: ['M19 11H5a2 2 0 00-2 2v7a2 2 0 002 2h14a2 2 0 002-2v-7a2 2 0 00-2-2z', 'M7 11V7a5 5 0 0110 0v4'],
    stroke: true, fill: false,
  },
  info: {
    paths: ['M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z', 'M12 16v-4', 'M12 8h.01'],
    stroke: true, fill: false,
  },
  home: {
    paths: ['M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z', 'M9 22V12h6v10'],
    stroke: true, fill: false,
  },
  plus: {
    paths: ['M12 5v14M5 12h14'],
    stroke: true, fill: false,
  },
  search: {
    paths: ['M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z'],
    stroke: true, fill: false,
  },
  trending: {
    paths: ['M23 6l-9.5 9.5-5-5L1 18', 'M17 6h6v6'],
    stroke: true, fill: false,
  },
  bell_filled: {
    paths: ['M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0'],
    stroke: false, fill: true,
  },
  star_filled: {
    paths: ['M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z'],
    stroke: false, fill: true,
  },
  heart_filled: {
    paths: ['M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z'],
    stroke: false, fill: true,
  },
}

/**
 * 渲染 SVG 图标 HTML 字符串
 * @param {string} name - 图标名称
 * @param {number} size - 尺寸 (px)
 * @param {string} color - 颜色
 * @param {string} strokeWidth - 描边宽度
 * @returns {object} { viewBox, paths[], stroke?, fill? }
 */
export function getIcon(name, size = 20, color = 'currentColor', strokeWidth = 1.8) {
  const icon = paths[name]
  if (!icon) {
    console.warn(`Icon "${name}" not found`)
    return null
  }

  return {
    viewBox: '0 0 24 24',
    size,
    color,
    strokeWidth,
    ...icon,
  }
}

/**
 * 获取所有图标名称
 */
export function getIconNames() {
  return Object.keys(paths)
}

export default paths
