<template>
  <div class="settings-container">
    <div class="page-header">
      <h1>{{ t('settings.title') }}</h1>
    </div>

    <div class="settings-content">
      <el-tabs v-model="activeTab" class="settings-tabs">
        <!-- 账号安全 -->
        <el-tab-pane :label="t('settings.accountSecurity')" name="security">
          <div class="tab-content">
            <!-- 修改密码 -->
            <div class="settings-section">
              <div class="section-header">
                <el-icon class="section-icon"><Lock /></el-icon>
                <div class="section-title">
                  <h3>{{ t('settings.changePassword') }}</h3>
                <p>{{ t('settings.changePasswordTip') }}</p>
                </div>
              </div>
              <el-form :model="passwordForm" label-width="120px" style="max-width: 500px">
                <el-form-item :label="t('settings.currentPassword')">
                  <el-input 
                    v-model="passwordForm.currentPassword" 
                    type="password" 
                    :placeholder="t('settings.currentPasswordPlaceholder')"
                    show-password />
                </el-form-item>
                <el-form-item :label="t('settings.newPassword')">
                  <el-input 
                    v-model="passwordForm.newPassword" 
                    type="password" 
                    :placeholder="t('settings.newPasswordPlaceholder')"
                    show-password />
                </el-form-item>
                <el-form-item :label="t('settings.confirmPassword')">
                  <el-input 
                    v-model="passwordForm.confirmPassword" 
                    type="password" 
                    :placeholder="t('settings.confirmPasswordPlaceholder')"
                    show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword" :loading="passwordLoading">
                    {{ t('settings.changePassword') }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 安全提示 -->
            <div class="settings-section">
              <div class="section-header">
                <el-icon class="section-icon"><Warning /></el-icon>
                <div class="section-title">
                  <h3>{{ t('settings.securityTips') }}</h3>
                <p>{{ t('settings.securityTipsSubtitle') }}</p>
                </div>
              </div>
              <div class="security-tips">
                <div class="tip-item">
                  <el-icon><Check /></el-icon>
                  <span>{{ t('settings.securityTip1') }}</span>
                </div>
                <div class="tip-item">
                  <el-icon><Check /></el-icon>
                  <span>{{ t('settings.securityTip2') }}</span>
                </div>
                <div class="tip-item">
                  <el-icon><Check /></el-icon>
                  <span>{{ t('settings.securityTip3') }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane> 

        <!-- 登录历史 -->
        <el-tab-pane :label="t('settings.loginHistory')" name="login-history">
          <div class="tab-content">
            <div class="settings-section">
              <div class="section-header">
                <el-icon class="section-icon"><Clock /></el-icon>
                <div class="section-title">
                  <h3>{{ t('settings.recentLoginRecords') }}</h3>
                <p>{{ t('settings.viewLoginHistory') }}</p>
                </div>
              </div>
              <div class="login-history-list">
                <div v-for="record in loginHistory" :key="record.id" class="history-item">
                  <div class="history-main">
                    <div class="history-time">
                      <el-icon><Calendar /></el-icon>
                      <span>{{ formatDateTime(record.loginTime) }}</span>
                    </div>
                    <div class="history-device">
                      <el-icon><Monitor /></el-icon>
                      <span>{{ record.device }}</span>
                    </div>
                    <div class="history-location">
                      <el-icon><Location /></el-icon>
                      <span>{{ record.location || t('settings.unknownLocation') }}</span>
                    </div>
                  </div>
                  <div class="history-side">
                    <div class="history-ip">
                      <span>IP: {{ record.ipAddress }}</span>
                    </div>
                    <el-tag :type="record.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
                      <el-icon><CircleCheck v-if="record.status === 'SUCCESS'" /><CircleClose v-else /></el-icon>
                      {{ record.status === 'SUCCESS' ? t('settings.loginSuccess') : t('settings.loginFailed') }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div class="load-more" v-if="loginHistory.length >= 10">
                <el-button text type="danger" @click="clearLoginHistory" style="margin-left: 1rem;">{{ t('settings.clearHistory') }}</el-button>
              </div>
              <div v-else-if="loginHistory.length > 0" class="load-more">
                <el-button text type="danger" @click="clearLoginHistory">{{ t('settings.clearHistory') }}</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 个人喜好 -->
        <el-tab-pane :label="t('settings.preferences')" name="preferences">
          <div class="tab-content">
            <!-- 语言设置 -->
            <div class="settings-section">
              <div class="section-header">
                <el-icon class="section-icon"><ChatLineRound /></el-icon>
                <div class="section-title">
                  <h3>{{ t('settings.languageSettings') }}</h3>
                <p>{{ t('settings.languageSettingsSubtitle') }}</p>
                  
                </div>
              </div>
              <el-form label-width="120px">
                <el-form-item :label="t('settings.interfaceLanguage')">
                  <el-radio-group v-model="preferences.language">
                    <el-radio label="zh-CN">{{ t('settings.chineseSimplified') }}</el-radio>
                    <el-radio label="en-US">{{ t('settings.english') }}</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveLanguage" :loading="languageLoading">
                    <el-icon><Check /></el-icon>
                    {{ t('settings.saveLanguageSettings') }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 主题设置 -->
            <div class="settings-section">
              <div class="section-header">
                <el-icon class="section-icon"><Sunny /></el-icon>
                <div class="section-title">
                  <h3>{{ t('settings.themeAppearance') }}</h3>
                  <p>{{ t('settings.themeAppearanceSubtitle') }}</p>
                </div>
              </div>
              <el-form label-width="120px">
                <el-form-item :label="t('settings.themeMode')">
                  <el-radio-group v-model="preferences.theme">
                    <el-radio label="light">
                      <el-icon><Sunny /></el-icon>
                      {{ t('settings.lightTheme') }}
                    </el-radio>
                    <el-radio label="dark">
                      <el-icon><Moon /></el-icon>
                      {{ t('settings.darkTheme') }}
                    </el-radio>
                    <el-radio label="auto">
                      <el-icon><View /></el-icon>
                      {{ t('settings.followSystem') }}
                    </el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item :label="t('settings.themeColor')">
                  <div class="theme-colors">
                    <div 
                      v-for="color in themeColors" 
                      :key="color.value"
                      class="theme-color-item"
                      :class="{ active: preferences.primaryColor === color.value }"
                      :style="{ backgroundColor: color.value }"
                      @click="preferences.primaryColor = color.value">
                      <el-icon v-if="preferences.primaryColor === color.value"><Check /></el-icon>
                    </div>
                  </div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveThemeSettings" :loading="themeLoading">
                    <el-icon><Check /></el-icon>
                    {{ t('settings.saveThemeSettings') }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 界面设置 -->
            <div class="settings-section">
              <div class="section-header">
                <el-icon class="section-icon"><Grid /></el-icon>
                <div class="section-title">
                  <h3>{{ t('settings.interfaceSettings') }}</h3>
        <p>{{ t('settings.interfaceSettingsSubtitle') }}</p>
                </div>
              </div>
              <el-form label-width="120px">
                <el-form-item :label="t('settings.fontSize')">
                  <el-slider 
                    v-model="preferences.fontSize" 
                    :min="12" 
                    :max="18" 
                    :step="1"
                    show-input
                    show-stops />
                </el-form-item>
                <el-form-item :label="t('settings.animationEffects')">
                  <el-switch v-model="preferences.animations" />
                </el-form-item>
                <el-form-item :label="t('settings.compactMode')">
                  <el-switch v-model="preferences.compactMode" />
                  <span class="form-hint">{{ t('settings.compactModeHint') }}</span>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveInterfaceSettings" :loading="preferencesLoading">
                    <el-icon><Check /></el-icon>
                    {{ t('settings.saveInterfaceSettings') }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>


          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  Clock, 
  Calendar, 
  Monitor, 
  Location, 
  CircleCheck, 
  CircleClose, 
  ChatLineRound, 
  Sunny, 
  Moon, 
  View, 
  Check, 
  Grid,
  Lock,
  Warning
} from '@element-plus/icons-vue'
import { useSettings } from '@/api/setting'
import { onMounted } from 'vue'


// 使用 settings 组合式函数
const {
  // 翻译函数
  t,
  
  // 响应式状态
  activeTab,
  passwordLoading,
  languageLoading,
  themeLoading,
  preferencesLoading,
  
  // 表单数据
  passwordForm,
  preferences,
  
  // 选项数据
  themeColors,
  loginHistory,
  
  // 计算属性
  
  // 方法
  changePassword,
  saveLanguage,
  saveThemeSettings,
  saveInterfaceSettings,
  clearLoginHistory,
  formatDateTime,
  loadPreferences,
  fetchLoginHistory
} = useSettings()

onMounted(() => {
    loadPreferences()
    fetchLoginHistory()
  })
 

</script>


<style scoped>
@import '@/styles/setting.css';
</style>