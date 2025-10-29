import './style.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from "element-plus"
import "element-plus/dist/index.css"
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import qs from "qs";
import i18n from './locales'
import ThemePlugin from './plugins/theme'


const pinia = createPinia()
const app = createApp(App)


//全局配置ico
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}


//全局绑定qs
app.config.globalProperties.qs = qs

// 使用主题插件（在挂载前使用，确保主题立即生效）
app.use(ThemePlugin)

app.use(pinia).use(ElementPlus).use(i18n).use(router).mount('#app')
