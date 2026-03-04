import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import fs from 'fs'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    {
      name: 'clean-dist',
      buildStart() {
        const distPath = resolve(__dirname, 'dist')
        if (fs.existsSync(distPath)) {
          fs.rmSync(distPath, { recursive: true, force: true })
        }
      }
    }
  ],
  server: {
    host: '0.0.0.0',
    port: 5174,
    https: false,
    allowedHosts: true
  }
})
