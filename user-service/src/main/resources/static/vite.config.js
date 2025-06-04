import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    outDir: '../../../target/classes/static',
    assetsDir: 'assets',
    manifest: true,
    rollupOptions: {
      input: {
        main: 'src/main.js',
        index: 'index.html',
        login: 'login.html',
        register: 'register.html'
      }
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  define: {
    'process.env': {
      VITE_API_BASE_URL: 'http://localhost:8080',
      VITE_APP_TITLE: '历史投票系统'
    }
  }
}) 