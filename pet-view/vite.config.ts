import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    // 允许外部访问，解决502错误
    host: '0.0.0.0',
    // 允许通过 Cloudflare Tunnel 访问，简化配置支持所有主机
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 本地后端地址
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
        target: 'ws://localhost:8080', // 本地后端 WebSocket 地址
        ws: true,
        changeOrigin: true
      }
    }
  }
})