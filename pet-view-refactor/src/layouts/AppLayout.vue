<template>
  <div class="app-shell">
    <AppHeader />
    <section class="shell-body">
      <AppSidebar />
      <main class="page-container">
        <RouterView />
      </main>
    </section>
    <GlobalNotificationCenter />
    <RealtimeReminderDialog />
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/shared/components/layout/AppHeader.vue'
import AppSidebar from '@/shared/components/layout/AppSidebar.vue'
import GlobalNotificationCenter from '@/shared/components/layout/GlobalNotificationCenter.vue'
import RealtimeReminderDialog from '@/shared/components/layout/RealtimeReminderDialog.vue'
import { useWebSocket } from '@/shared/hooks/useWebSocket'
import { useThemeStore } from '@/app/store/modules/theme'

useWebSocket()

const route = useRoute()
const themeStore = useThemeStore()
const ROOT_CLASSES = ['light', 'dark', 'warm', 'warm-sunrise', 'warm-dusk']

const enforceAdminTheme = () => {
  const root = document.documentElement
  const isAdmin = Boolean(route.meta.requiresAdmin || route.path.startsWith('/admin'))
  if (isAdmin) {
    ROOT_CLASSES.forEach(cls => root.classList.remove(cls))
    root.classList.add('admin-theme')
    return
  }
  root.classList.remove('admin-theme')
  themeStore.applyTheme()
}

onMounted(enforceAdminTheme)
watch(
  () => route.fullPath,
  () => enforceAdminTheme(),
  { immediate: false },
)
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--app-bg-color);
  display: flex;
  flex-direction: column;
  position: relative;
}

.shell-body {
  flex: 1;
  display: grid;
  grid-template-columns: auto 1fr;
  min-height: calc(100vh - 80px);
  background: var(--app-bg-color);
}

.page-container {
  padding: 1.5rem 2rem;
  min-height: calc(100vh - 80px);
  background: var(--app-bg-color);
  overflow-y: auto;
}

@media (max-width: 1024px) {
  .shell-body {
    grid-template-columns: 1fr;
  }
}
</style>
