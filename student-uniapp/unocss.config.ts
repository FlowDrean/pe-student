import { defineConfig } from 'unocss'
import presetUno from '@unocss/preset-uno'
import presetAttributify from '@unocss/preset-attributify'
import transformerDirectives from '@unocss/transformer-directives'

// Editorial Ink Design System — 复刻 student-app 配色
export default defineConfig({
  presets: [presetUno(), presetAttributify()],
  transformers: [transformerDirectives()],
  theme: {
    colors: {
      // 墨色 Ink 色板
      ink: {
        50: '#f4f4f6',
        100: '#e4e4e7',
        200: '#c9c9cf',
        300: '#a1a1aa',
        400: '#71717a',
        500: '#52525b',
        600: '#3f3f46',
        700: '#27272a',
        800: '#18181b',
        900: '#0f1119',
      },
      // 微信绿 Accent
      accent: {
        50: '#e6f9f0',
        100: '#b3efd1',
        200: '#8de5b9',
        300: '#59d899',
        400: '#38cf81',
        500: '#07C160',
        600: '#06af56',
        700: '#058943',
        800: '#046a34',
        900: '#035127',
      },
      // 琥珀 Amber
      amber: {
        50: '#fffbeb',
        100: '#fef3c7',
        200: '#fde68a',
        300: '#fcd34d',
        400: '#fbbf24',
        500: '#f59e0b',
        600: '#d97706',
      },
      // 珊瑚 Coral
      coral: {
        50: '#fff7ed',
        100: '#ffedd5',
        200: '#fed7aa',
        300: '#fdba74',
        400: '#fb923c',
        500: '#f97316',
        600: '#ea580c',
      },
      // 功能色
      success: '#07C160',
      error: '#dc2626',
      warning: '#f59e0b',
      info: '#3b82f6',
      bg: {
        primary: '#ffffff',
        secondary: '#f4f4f6',
        tertiary: '#f8f9fa',
      },
    },
    fontFamily: {
      display: 'system-ui, -apple-system, sans-serif',
      sans: 'system-ui, -apple-system, sans-serif',
      mono: '"JetBrains Mono", "Courier New", monospace',
    },
    fontSize: {
      '2xs': ['0.625rem', { lineHeight: '0.875rem' }],
      xs: ['0.75rem', { lineHeight: '1rem' }],
      sm: ['0.875rem', { lineHeight: '1.25rem' }],
      base: ['1rem', { lineHeight: '1.5rem' }],
      lg: ['1.125rem', { lineHeight: '1.75rem' }],
      xl: ['1.25rem', { lineHeight: '1.75rem' }],
      '2xl': ['1.5rem', { lineHeight: '2rem' }],
    },
    spacing: {
      0: '0px',
      1: '4px',
      2: '8px',
      3: '12px',
      4: '16px',
      5: '20px',
      6: '24px',
      8: '32px',
      10: '40px',
      12: '48px',
    },
    borderRadius: {
      sm: '4px',
      md: '8px',
      lg: '12px',
      xl: '16px',
      full: '9999px',
    },
    boxShadow: {
      sm: '0 1px 2px 0 rgba(15,17,25,0.04)',
      md: '0 4px 6px -1px rgba(15,17,25,0.06), 0 2px 4px -2px rgba(15,17,25,0.04)',
      glow: '0 0 0 3px rgba(7,193,96,0.2)',
      card: '0 2px 0 0 rgba(15,17,25,0.06)',
    },
  },
  shortcuts: {
    'page': 'min-h-screen bg-ink-50',
    'content-px': 'px-4',
    'card': 'bg-bg-primary rounded-lg shadow-card border-2 border-ink-50',
    'btn-primary': 'bg-accent-500 text-white rounded-full font-600 px-6 py-3 text-center shadow-sm active:bg-accent-600',
    'btn-outline': 'border-2 border-ink-200 rounded-full font-600 px-6 py-3 text-center active:bg-ink-50',
    'text-ellipsis': 'overflow-hidden text-ellipsis whitespace-nowrap',
    'input': 'w-full px-4 py-3 bg-bg-tertiary border-2 border-ink-200 rounded-full text-sm text-ink-800 transition-all focus:border-accent-500 focus:shadow-glow',
    'tag': 'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-500',
    'avatar': 'rounded-full overflow-hidden flex-shrink-0 flex items-center justify-center text-white font-700',
    'skeleton': 'bg-ink-100 animate-pulse rounded-md',
    'divider': 'h-px bg-ink-100 w-full',
  },
})
