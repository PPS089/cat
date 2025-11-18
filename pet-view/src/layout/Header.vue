<template>
    <header class="top-header">
      <div class="header-left">
        <div class="logo">
          <img src="/pet.png" :alt="t('common.appName')" />
          <span>{{ t('common.appName') }}</span>
        </div>
      </div>
      <div class="header-right">
        <div class="user-dashboard">
          <el-dropdown @command="handleCommand" placement="bottom-end">
            <div class="user-info-trigger">
              <div class="user-avatar">
                <img :src="headPicUrl" :alt="userName"  loading="lazy" @error="handleAvatarError" />
              </div>
              <div class="user-info">
                <span class="username">{{  userName }}</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <div class="user-dropdown-header">
                  <div class="user-avatar-large">
                    <img :src="headPicUrl" :alt="userName"  loading="lazy" @error="handleAvatarError" />
                  </div>
                  <div class="user-details">
                    <div class="username">{{ userName }}</div>
                  </div>
                </div>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  {{ t('user.profile') }}
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  {{ t('settings.title') }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  {{ t('common.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
</template>

<script setup lang="ts">
import { ElIcon } from 'element-plus'
import { User, Setting, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useHeaderActions } from '@/composables/useHeaderActions'
import { computed } from 'vue'
import dogImage from '@/assets/img/dog.jpg'

const { t } = useI18n()

// 使用头部操作
const { handleCommand, headPic, userName } = useHeaderActions()

// 处理头像加载错误
const handleAvatarError = (event: any) => {
  // 如果头像加载失败，显示默认头像
  event.target.src = dogImage
}

// 计算属性，处理头像URL
const headPicUrl = computed(() => {
  // 如果是完整的URL（包含http或https），直接返回
  if (headPic.value && (headPic.value.startsWith('http://') || headPic.value.startsWith('https://'))) {
    return headPic.value
  }
  // 否则返回默认头像
  else {
    return dogImage
  }
})
</script>

<style scoped>
@import '../styles/header.css'
</style>