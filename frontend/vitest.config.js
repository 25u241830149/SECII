import { fileURLToPath, URL } from 'node:url'

import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vitest/config'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      reportsDirectory: './coverage',
      include: [
        'src/api/**/*.ts',
        'src/components/CampusHubLogo.vue',
        'src/components/CategoryNav.vue',
        'src/components/ChatPanel.vue',
        'src/components/OrderStatusBadge.vue',
        'src/components/ReviewForm.vue',
        'src/components/ReviewList.vue',
        'src/components/TaskCard.vue',
        'src/components/TaskCommentSection.vue',
        'src/views/auth/LoginView.vue',
        'src/views/auth/RegisterView.vue',
        'src/views/home/Home.vue',
        'src/views/message/MessageCenter.vue',
        'src/views/order/OrderList.vue',
        'src/views/report/ReportCreate.vue',
        'src/views/task/TaskDetail.vue',
        'src/views/admin/ReportCenter.vue',
      ],
      thresholds: {
        statements: 78,
        branches: 68,
        functions: 72,
        lines: 81,
      },
    },
  },
})
