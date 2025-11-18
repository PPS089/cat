import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
/**
 * 头部操作的组合式函数
 * 提供导航、登出等操作
 */
export const useHeaderActions = () => {
  const router = useRouter()
  const { t } = useI18n()
  const userStore = useUserStore()

  /**
   * 下拉菜单处理
   * @param command 菜单命令
   */
  const handleCommand = (command: string) => {
    switch (command) {
      case 'profile':
        router.push('/user/profile')
        break
      case 'settings':
        router.push('/user/settings')
        break
      case 'logout':
        logout()
        break
    }
  }

  /**
   * 登出处理
   */
  const logout = async () => {
    try {
      await ElMessageBox.confirm(t('common.logoutConfirm'), t('common.logout'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })

      // 清除本地存储的用户信息
        localStorage.removeItem('jwt_token')
        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
        localStorage.removeItem('userId')
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('rememberedPassword')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userName')
        localStorage.removeItem('headPic')
        localStorage.removeItem('email')
        localStorage.removeItem('phone')
        localStorage.removeItem('introduce')
        userStore.resetUser()
      ElMessage.success(t('common.logoutSuccess'))
      router.push('/login')
    } catch (error) {
      // 用户取消退出，不做任何处理
      console.log('用户取消登出')
    }
  }

  /**
   * 用户数据
   * 获取用户信息
   */
  const userName = computed(() => userStore.info.userName)
  const headPic=computed(() => userStore.info.headPic)

  return {
    handleCommand,
    logout,
    headPic,

    userName
  }
}
