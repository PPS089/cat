import { createApp } from 'vue'
import type { App as VueApp } from 'vue'
import App from '@/App.vue'
import { setupRouter } from './router'
import { setupStore } from './store'
import { setupI18n } from './plugins/i18n'
import { setupElementPlus } from './plugins/element'

async function setupPlugins(app: VueApp) {
  setupStore(app)
  setupElementPlus(app)
  const i18n = setupI18n(app)
  await setupRouter(app)
  await i18n
}

export async function bootstrapApp() {
  const app = createApp(App)
  await setupPlugins(app)
  app.mount('#app')
}
