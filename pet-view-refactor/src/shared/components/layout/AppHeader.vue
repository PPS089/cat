<template>
  <header class="app-header">
    <div class="brand" @click="goHome">
      <img src="/pet.png" alt="logo" />
      <p>{{ t('common.appName') }}</p>
    </div>
    <div class="header-actions">
      <el-select v-if="!isAdmin" v-model="locale" class="lang-select" size="small">
        <el-option label="中文" value="zh-CN" />
        <el-option label="English" value="en-US" />
      </el-select>
      <el-button v-if="!isAdmin" circle size="small" @click="toggleTheme">
        <el-icon>
          <Moon v-if="isDark" />
          <Sunny v-else />
        </el-icon>
      </el-button>
      <el-dropdown @command="handleCommand" placement="bottom-end">
        <div class="user-trigger">
          <img :src="avatarUrl" :alt="userName" decoding="async" fetchpriority="high" @error="handleAvatarError" />
          <span>{{ userName || t('common.user') }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <template v-if="!isAdmin">
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                {{ t('user.profile') }}
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                {{ t('settings.title') }}
              </el-dropdown-item>
            </template>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              {{ t('common.logout') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ArrowDown, User, Setting, SwitchButton, Moon, Sunny } from '@element-plus/icons-vue'
import { useUserStore } from '@/app/store/modules/user'
import { useTheme } from '@/shared/hooks/useTheme'
import { useLocale } from '@/shared/hooks/useLocale'
import { resolveAssetUrl } from '@/shared/utils/url'
import dogImage from '@/assets/img/dog.jpg'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const { currentLocale, setLocale } = useLocale()
const { isDark, toggle } = useTheme()
const isAdmin = computed(() => userStore.isAdmin)

const locale = computed({
  get: () => currentLocale.value,
  set: value => setLocale(value),
})

const userName = computed(() => userStore.info.userName)
const avatarUrl = computed(() => {
  const url = userStore.info.headPic
  if (!url) {
    return dogImage
  }
  const resolved = resolveAssetUrl(url)
  return resolved || dogImage
})

const toggleTheme = () => toggle()

const goHome = () => {
  if (userStore.hasValidSession) {
    router.push(userStore.isAdmin ? '/admin' : '/user')
  } else {
    router.push('/')
  }
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    const wasAdmin = userStore.isAdmin
    userStore.logout()
    router.push(wasAdmin ? '/admin/login' : '/login')
    return
  }
  if (isAdmin.value) return
  router.push(`/user/${command}`)
}

const handleAvatarError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = dogImage
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1.5rem;
  height: 80px;
  border-bottom: 1px solid var(--app-border-color);
  background: var(--app-surface-color);
  backdrop-filter: blur(12px);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.brand img {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  object-fit: contain;
  object-position: center;
}

.brand p {
  margin: 0;
  font-weight: 600;
  color: var(--app-text-color);
  letter-spacing: 0.01em;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  cursor: pointer;
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  background: var(--button-bg);
  color: var(--button-text);
  border: 1px solid var(--button-border);
  transition: background 0.2s ease, color 0.2s ease;
}

.user-trigger img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.user-trigger:hover {
  background: var(--button-hover-bg);
  color: var(--app-primary);
}

.lang-select {
  width: 120px;
}
</style>
