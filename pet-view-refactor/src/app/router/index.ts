import type { App } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { setupRouterGuards } from './guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export async function setupRouter(app: App) {
  setupRouterGuards(router)
  app.use(router)
  await router.isReady()
}

export default router
