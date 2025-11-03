import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), tailwindcss()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // 确保所有请求头都被正确转发
        configure: (proxy) => {
          proxy.on('proxyReq', function (proxyReq, req) {
            // 转发所有请求头
            Object.keys(req.headers).forEach(key => {
              const headerValue = req.headers[key];
              if (headerValue !== undefined) {
                proxyReq.setHeader(key, headerValue);
              }
            });
          });
        }
      },
      '/api/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  }
})