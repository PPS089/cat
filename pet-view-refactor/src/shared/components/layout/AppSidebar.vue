<template>
  <aside class="app-sidebar">
    <nav>
      <template v-for="section in currentSections" :key="section.titleKey">
        <p class="section-title">{{ t(section.titleKey) }}</p>
        <ul>
          <li v-for="item in section.items" :key="item.path">
            <RouterLink :to="item.path" class="nav-link" :class="{ active: isActive(item.path) }">
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
              <span>{{ t(item.labelKey) }}</span>
              <span v-if="shouldShowBadge(item.badgeKey)" class="badge">
                {{ formatBadge(item.badgeKey) }}
              </span>
            </RouterLink>
          </li>
        </ul>
      </template>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { navigationSections, adminNavigationSections } from '@/app/config/navigation'
import { useNavStatsStore } from '@/app/store/modules/navStats'
import { useUserStore } from '@/app/store/modules/user'
import { NAV_STATS_REFRESH_EVENT } from '@/shared/utils/events'

type BadgeKey = 'petCount' | 'adoptionCount' | 'fosterCount' | 'healthAlertCount' | 'eventsCount'

const { t } = useI18n()
const route = useRoute()
const navStats = useNavStatsStore()
const userStore = useUserStore()
let refreshTimer: ReturnType<typeof setTimeout> | null = null

const currentSections = computed(() =>
  userStore.isAdmin ? adminNavigationSections : navigationSections,
)

const badgeMap = computed(() => ({
  petCount: navStats.petCount ?? 0,
  adoptionCount: navStats.adoptionCount ?? 0,
  fosterCount: navStats.fosterCount ?? 0,
  healthAlertCount: navStats.healthAlertCount ?? 0,
  eventsCount: navStats.eventsCount ?? 0,
}))

const isActive = (path: string) => route.path === path

const ensureStats = () => {
  if (!userStore.hasValidSession || userStore.isAdmin) {
    navStats.reset()
    return
  }
  navStats.fetchAll()
}

const scheduleRefresh = () => {
  if (refreshTimer) return
  refreshTimer = window.setTimeout(() => {
    refreshTimer = null
    ensureStats()
  }, 250)
}

const handleNavStatsRefresh = (_event?: Event) => {
  if (!userStore.hasValidSession || userStore.isAdmin) return
  scheduleRefresh()
}

onMounted(() => {
  ensureStats()
  window.addEventListener(NAV_STATS_REFRESH_EVENT, handleNavStatsRefresh)
})

onBeforeUnmount(() => {
  window.removeEventListener(NAV_STATS_REFRESH_EVENT, handleNavStatsRefresh)
  if (refreshTimer) {
    clearTimeout(refreshTimer)
    refreshTimer = null
  }
})

watch(
  () => [userStore.hasValidSession, userStore.isAdmin],
  () => ensureStats(),
)

const shouldShowBadge = (key?: BadgeKey) => {
  if (!key) return false
  return (badgeMap.value[key] ?? 0) > 0
}

const formatBadge = (key?: BadgeKey) => {
  if (!key) return ''
  const value = badgeMap.value[key] ?? 0
  return value > 99 ? '99+' : String(value)
}
</script>

<style scoped>
.app-sidebar {
  width: 260px;
  background: var(--app-surface-color);
  border-right: 1px solid var(--app-border-color);
  color: var(--app-text-color);
  height: calc(100vh - 80px);
  position: sticky;
  top: 80px;
  overflow-y: auto;
  padding: 1.5rem 1rem;
  box-shadow: 4px 0 20px rgba(15, 23, 42, 0.03);
}

.section-title {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--app-text-secondary);
  margin: 1.5rem 0 0.5rem;
}

ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.65rem 0.85rem;
  color: var(--app-text-color);
  border-radius: 12px;
  transition: background 0.2s, color 0.2s;
}

.nav-link.active,
.nav-link:hover {
  background: rgba(99, 102, 241, 0.15);
  color: var(--app-primary);
}

.badge {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--sidebar-badge-bg);
  color: var(--sidebar-badge-color);
  font-size: 0.8rem;
  font-weight: 600;
}

@media (max-width: 1024px) {
  .app-sidebar {
    display: none;
  }
}
</style>
