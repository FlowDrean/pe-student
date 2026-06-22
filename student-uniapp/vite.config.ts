import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

export default async () => {
  const { default: UnoCSS } = await import('unocss/vite')

  return {
    plugins: [uni(), UnoCSS()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
        '@components': path.resolve(__dirname, 'src/components'),
        '@stores': path.resolve(__dirname, 'src/stores'),
        '@api': path.resolve(__dirname, 'src/api'),
        '@utils': path.resolve(__dirname, 'src/utils'),
        '@composables': path.resolve(__dirname, 'src/composables'),
        '@layouts': path.resolve(__dirname, 'src/layouts'),
      },
    },
    server: {
      port: 3002,
      host: '0.0.0.0',
      proxy: {
        '/api': { target: 'http://localhost:8080', changeOrigin: true },
        '/uploads': { target: 'http://localhost:8080', changeOrigin: true },
      },
    },
  }
}
