import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword, fetchLoginHistory, clearLoginHistory } from '@/modules/settings/services/settings'
import type { LoginHistoryItem, ChangePasswordPayload } from '@/modules/settings/types'
import { useUserStore } from '@/app/store/modules/user'

type TabName = 'security' | 'history'

export function useSettings() {
  const { t } = useI18n()
  const router = useRouter()
  const userStore = useUserStore()
  const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)\S{6,12}$/

  const activeTab = ref<TabName>('security')

  const passwordForm = reactive<ChangePasswordPayload>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const passwordLoading = ref(false)

  const loginHistory = ref<LoginHistoryItem[]>([])
  const historyLoading = ref(false)
  const historyClearing = ref(false)

  const validatePasswordForm = () => {
    if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
      ElMessage.error(t('settings.passwordInfoRequired'))
      return false
    }
    if (passwordForm.currentPassword.length < 6 || passwordForm.currentPassword.length > 12) {
      ElMessage.error(t('settings.passwordLengthError'))
      return false
    }
    if (!PASSWORD_REGEX.test(passwordForm.newPassword)) {
      ElMessage.error(t('settings.passwordStrengthError') || t('settings.passwordLengthError'))
      return false
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      ElMessage.error(t('settings.passwordMismatch'))
      return false
    }
    return true
  }

  const handleChangePassword = async () => {
    if (!validatePasswordForm()) return
    passwordLoading.value = true
    try {
      await changePassword(passwordForm)
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      ElMessage.success(t('settings.passwordChanged'))
      const wasAdmin = userStore.isAdmin
      userStore.logout()
      router.push(wasAdmin ? '/admin/login' : '/login')
    } catch (error: any) {
      console.error('[Settings] change password failed', error)
    } finally {
      passwordLoading.value = false
    }
  }

  const loadLoginHistory = async () => {
    historyLoading.value = true
    try {
      loginHistory.value = await fetchLoginHistory()
    } catch (error: any) {
      loginHistory.value = []
      console.error('[Settings] load login history failed', error)
    } finally {
      historyLoading.value = false
    }
  }

  const handleClearHistory = async () => {
    if (!loginHistory.value.length) return
    historyClearing.value = true
    try {
      await clearLoginHistory()
      loginHistory.value = []
      ElMessage.success(t('settings.historyCleared'))
    } catch (error: any) {
      console.error('[Settings] clear login history failed', error)
    } finally {
      historyClearing.value = false
    }
  }

  const formatDateTime = (input: string) => {
    const date = new Date(input)
    if (Number.isNaN(date.getTime())) return input
    return new Intl.DateTimeFormat(undefined, {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date)
  }

  onMounted(() => {
    loadLoginHistory()
  })

  return {
    t,
    activeTab,
    passwordForm,
    passwordLoading,
    loginHistory,
    historyLoading,
    historyClearing,
    handleChangePassword,
    handleClearHistory,
    loadLoginHistory,
    formatDateTime,
  }
}
