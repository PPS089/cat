<template>
  <div :class="['auth-layout', isAdminLogin ? 'auth-layout--admin' : 'auth-layout--user']">
    <RouterView />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useThemeStore } from '@/app/store/modules/theme'

const route = useRoute()
const themeStore = useThemeStore()
const ROOT_CLASSES = ['light', 'dark', 'warm', 'warm-sunrise', 'warm-dusk']

const isAdminLogin = computed(() => route.path.startsWith('/admin/login'))

const enforceAuthTheme = () => {
  const root = document.documentElement
  if (isAdminLogin.value) {
    ROOT_CLASSES.forEach(cls => root.classList.remove(cls))
    root.classList.add('admin-theme')
  } else {
    root.classList.remove('admin-theme')
    themeStore.applyTheme()
  }
}

onMounted(enforceAuthTheme)
watch(
  () => route.fullPath,
  () => enforceAuthTheme(),
)
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(1.25rem, 4vw, 3rem);
  overflow: hidden;
}

.auth-layout--user {
  background: linear-gradient(135deg, #f9fbff 0%, #eef2ff 35%, #fdf2f8 100%);
}

.auth-layout--admin {
  background: radial-gradient(circle at 20% 20%, rgba(246, 199, 141, 0.14), transparent 32%),
    radial-gradient(circle at 80% 10%, rgba(138, 214, 181, 0.14), transparent 30%),
    linear-gradient(145deg, #0b111a 0%, #0f1924 40%, #0d101a 100%);
}

.auth-layout :deep(.auth-shell) {
  width: 100%;
  height: 100%;
}

@media (max-width: 640px) {
  .auth-layout {
    padding: 1rem;
  }
}
</style>
