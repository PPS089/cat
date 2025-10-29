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
                <img :src="headPic" :alt="userName"  loading="lazy" />
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
                    <img :src="headPic" :alt="userName"  loading="lazy" />
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

const { t } = useI18n()


// 使用头部操作
const { handleCommand,headPic,userName} = useHeaderActions()



</script>

<style scoped>
@import '../styles/header.css'
</style>
