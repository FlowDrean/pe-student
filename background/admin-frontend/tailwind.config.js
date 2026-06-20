/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: {
          50: '#f4f4f6',
          100: '#e4e4e8',
          200: '#bfc0c8',
          300: '#9496a4',
          400: '#6e7080',
          500: '#4a4d5c',
          600: '#343646',
          700: '#252733',
          800: '#1a1c24',
          900: '#0f1119',
        },
        accent: {
          50: '#e8f8ef',
          100: '#c7f0d8',
          200: '#9ae6b9',
          300: '#67d997',
          400: '#3dcc78',
          500: '#07C160',
          600: '#06ad56',
          700: '#05964a',
          800: '#04783b',
        },
        amber: {
          50: '#fffbeb',
          100: '#fef3c7',
          200: '#fde68a',
          300: '#fcd34d',
          400: '#fbbf24',
          500: '#f59e0b',
          600: '#d97706',
        },
        coral: {
          50: '#fff5f0',
          100: '#ffe8db',
          200: '#ffc9b0',
          300: '#ffa07a',
          400: '#ff7a4d',
          500: '#f97316',
          600: '#ea580c',
        },
      },
      fontFamily: {
        display: ['DM Serif Display', 'Noto Serif SC', 'serif'],
        body: ['DM Sans', 'PingFang SC', 'Microsoft YaHei', 'sans-serif'],
        mono: ['JetBrains Mono', 'SF Mono', 'monospace'],
      },
      boxShadow: {
        'card': '0 2px 0 0 rgba(26, 28, 36, 0.08)',
        'card-hover': '0 4px 0 0 rgba(26, 28, 36, 0.12)',
        'button': '0 3px 0 0 rgba(26, 28, 36, 0.25)',
      },
    },
  },
  plugins: [],
}
