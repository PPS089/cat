import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import {request} from '../utils'
import type { LoginHistoryItem, PasswordForm, Preferences } from '@/types/setting'
import { onMounted } from 'vue'
import router from '@/router'

/**
 * 用户设置组合式函数
 */
export const useSettings = () => {
  const { t, locale } = useI18n()
  const userStore = useUserStore()
  const themeStore = useThemeStore()

    // 登录历史数据
  const loginHistory = ref<LoginHistoryItem[]>([])

  // 响应式状态
  const activeTab = ref('login-history')
  const passwordLoading = ref(false)
  const languageLoading = ref(false)
  const themeLoading = ref(false)
  const preferencesLoading = ref(false)

  // 密码表单
  const passwordForm = reactive<PasswordForm>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  // 主题颜色选项
  const themeColors = [
    { name: 'default', value: '#409EFF' },
    { name: 'green', value: '#67C23A' },
    { name: 'orange', value: '#E6A23C' },
    { name: 'red', value: '#F56C6C' },
    { name: 'purple', value: '#9B59B6' }
  ]

  // 个人偏好设置
  const preferences = reactive<Preferences>({
    language: 'zh-CN',
    theme: 'light',
    primaryColor: '#409EFF',
    fontSize: 14,
    animations: true,
    compactMode: false
  })


  // 修改密码
  const changePassword = async () => {
    if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
      ElMessage.error(t('settings.passwordRequired'))
      return
    }

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      ElMessage.error(t('settings.passwordMismatch'))
      return
    }

    if (passwordForm.newPassword.length < 6) {
      ElMessage.error(t('settings.passwordLengthError'))
      return
    }

    passwordLoading.value = true
    try {
      await userStore.changePassword(passwordForm)
      // 清空表单
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      //跳转到登录页
      ElMessage.success(t('settings.passwordChanged'))
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('userId');
      localStorage.removeItem('rememberedUsername');
      localStorage.removeItem('rememberedPassword');
      router.push('/login')
    } catch (error: any) {
      ElMessage.error(t('settings.passwordChangeFailed') + (error.message ? ': ' + error.message : ''))
    } finally {
      passwordLoading.value = false
    }
  }

    // 获取登录历史
  const fetchLoginHistory = async () => {
    try {
      
      const response = await request.get('/login-history')
      
      if(response.code == 200) {
          loginHistory.value = response.data.map((item: any) => ({
            id: item.id,
            userId: item.userId,
            loginTime: item.loginTime,
            device: item.device || 'Unknown Device',
            location: item.location || 'Unknown Location',
            ipAddress: item.ipAddress || '0.0.0.0',
            status: item.status || 'SUCCESS'
      }))
          console.log('登录历史处理完成，记录数:', loginHistory.value.length)
        } else {
          console.warn('登录历史数据不是数组格式，使用空数组')
          loginHistory.value = []
        }
    } catch (error: any) {
      console.error('获取登录历史失败:', error)
      loginHistory.value = []
      // 更友好的错误处理
      if (error.message === '未授权') {
        ElMessage.warning(t('settings.pleaseLoginToViewHistory') || '请先登录后查看登录历史')
      } else {
        ElMessage.error(t('settings.networkErrorUnableToGetLoginHistory'))
      }
    }
  }

  // 清除登录历史
  const clearLoginHistory = async () => {
    try {
      const response = await request.delete('/login-history/clear')

      if (response.code === 200) {
        // 后端返回Result.success()，data为null，不需要调用
        ElMessage.success(t('settings.historyCleared'))
        loginHistory.value = []
      } else {
        ElMessage.error(t('settings.clearHistoryFailed') + ': ' + response.message)
      }
    } catch (error) {
      console.error('清除登录历史错误:', error)
      ElMessage.error(t('settings.clearHistoryFailed'))
    }
  }


  // 加载偏好设置
  const loadPreferences = async () => {
    try {
      console.log(t('settings.loadingPreferences'))
      await themeStore.loadPreferences()
      console.log(t('settings.preferencesLoaded'))

      // 同步本地状态
      preferences.language = themeStore.preferences.language
      preferences.theme = themeStore.preferences.theme
      preferences.primaryColor = themeStore.preferences.primaryColor
      preferences.fontSize = themeStore.preferences.fontSize
      preferences.animations = themeStore.preferences.animations
      preferences.compactMode = themeStore.preferences.compactMode

      console.log(t('settings.preferencesSynced'))
    } catch (error) {
      console.error('加载偏好设置错误:', error)
      ElMessage.error(t('settings.loadPreferencesFailed'))
    }
  }

  // 保存语言设置
  const saveLanguage = async () => {
    languageLoading.value = true
    try {
      // 直接更新主题存储
      await themeStore.updatePreference('language', preferences.language)
      // 更新i18n语言
      locale.value = preferences.language
      ElMessage.success(t('settings.languageSaved'))
    } catch (error) {
      console.error(t('languageSaveFailed'), error)
      ElMessage.error(t('settings.savePreferencesFailed') || t('saveFailed'))
    } finally {
      languageLoading.value = false
    }
  }


  // 保存主题设置
  const saveThemeSettings = async () => {
    themeLoading.value = true
    try {
      await themeStore.updatePreferences({
        theme: preferences.theme,
        primaryColor: preferences.primaryColor
      })
      ElMessage.success(t('settings.themeSaved'))
    } catch (error) {
      ElMessage.error(t('settings.themeSaveFailed'))
    } finally {
      themeLoading.value = false
    }
  }

  // 保存界面设置
  const saveInterfaceSettings = async () => {
    preferencesLoading.value = true
    try {
      await themeStore.updatePreferences({
        fontSize: preferences.fontSize,
        animations: preferences.animations,
        compactMode: preferences.compactMode
      })
      ElMessage.success(t('settings.interfaceSettingsSaved'))
    } catch (error) {
      ElMessage.error(t('settings.saveInterfaceSettingsFailed'))
    } finally {
      preferencesLoading.value = false
    }
  }

  // 格式化日期时间
  const formatDateTime = (dateString: string) => {
    const date = new Date(dateString)
    const now = new Date()
    const diff = now.getTime() - date.getTime()

    // 小于1分钟
    if (diff < 60000) {
      return t('settings.justNow')
    }

    // 小于1小时
    if (diff < 3600000) {
      const minutes = Math.floor(diff / 60000)
      return `${minutes}${t('settings.minutesAgo')}`
    }

    // 小于24小时
    if (diff < 86400000) {
      const hours = Math.floor(diff / 3600000)
      return `${hours}${t('settings.hoursAgo')}`
    }

    // 小于7天
    if (diff < 604800000) {
      const days = Math.floor(diff / 86400000)
      return `${days}${t('settings.daysAgo')}`
    }

    // 超过7天，显示具体日期
    return date.toLocaleDateString(preferences.language === 'zh-CN' ? 'zh-CN' : 'en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }


  
onMounted(() => {
    loadPreferences()
    fetchLoginHistory()
  })

  return {
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
    
    // 方法
    changePassword,
    saveLanguage,
    saveThemeSettings,
    saveInterfaceSettings,
    clearLoginHistory,
    formatDateTime,
    loadPreferences,
    fetchLoginHistory
  }
}