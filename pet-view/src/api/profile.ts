import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import dogImage from '@/assets/img/dog.jpg'

// 常量定义
const DEFAULT_AVATAR = dogImage
const API_BASE = '/api/images/'
const MAX_FILE_SIZE = 2 * 1024 * 1024 // 2MB
const ALLOWED_FILE_TYPES = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp']
const PHONE_REGEX = /^1[3-9]\d{9}$/
const AVATAR_VALIDATION_TIMEOUT = 5000
const AVATAR_UPDATE_DELAY = 1000

// 从API获取用户完整资料
export const fetchUserProfileFromAPI = async (): Promise<{ success: boolean; data: any; message: string }> => {
  try {
    const userId = localStorage.getItem('userId')
    const token = localStorage.getItem('jwt_token')
    if (!token || !userId) {
      console.warn('用户未登录或缺少必要信息')
      return { success: false, data: null, message: '用户未登录或缺少必要信息' }
    }
    
    const response = await request.get('/user/profile')
    const result = response.data
  
    return { success: true, data: result, message: '获取用户资料成功' }
  }
  catch (error: any) {
    
    
    // 检查是否是401/403错误（未授权）
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      return { success: false, data: null, message: '用户未登录或缺少必要信息' }
    }
    
    return { success: false, data: null, message: '获取用户资料失败' }
  }
}

/**
 * 用户资料管理组合式函数
 */
export const useProfile = () => {
  const { t } = useI18n()
  const userStore = useUserStore()
  const profileFormRef = ref()
  const loading = ref(false)

  interface Form {
    userName?: string;
    email?: string;
    phone?: string;
    headPic?: string;
    introduce?: string;
  }

  const profileForm = reactive<Form>({
    userName: '',
    email: '',
    phone: '',
    headPic: '',
    introduce: ''
  })

  // 存储待上传的头像文件
  const pendingAvatarFile = ref<File | null>(null)

  // 创建计算属性版本的rules，确保t函数在组件生命周期中正确调用
  const computedRules = computed(() => ({
    username: [
      { required: true, message: t('user.usernameRequired'), trigger: 'blur' },
      { min: 3, max: 20, message: t('user.usernameLength'), trigger: 'blur' }
    ],
    email: [
      { required: true, message: t('user.emailRequired'), trigger: 'blur' },
      { type: 'email', message: t('user.emailInvalid'), trigger: 'blur' }
    ],
    phone: [
      { pattern: PHONE_REGEX, message: t('user.phoneInvalid'), trigger: 'blur' }
    ],
    bio: [
      { max: 200, message: t('user.bioMax'), trigger: 'blur' }
    ]
  }))

  const beforeImageUpload = (file: File) => {
    const ext = file.name.split('.').pop()?.toLowerCase() || ''
    const isAllowedExt = ALLOWED_FILE_TYPES.includes(ext)
    const isLt2M = file.size < MAX_FILE_SIZE

    if (!isAllowedExt) {
      ElMessage.error(t('message.avatarFormatError'))
      return false
    }
    if (!isLt2M) {
      ElMessage.error(t('message.avatarSizeError'))
      return false
    }
    return true
  }

  // 处理头像上传
  const handleImageUpload = async (options: any) => {
    const file = options.file
    
    if (!beforeImageUpload(file)) return
    
    pendingAvatarFile.value = file
    
    const reader = new FileReader()
    reader.onload = (e) => {
      profileForm.headPic = e.target?.result as string
    }
    reader.onerror = () => {
      ElMessage.error(t('avatarPreviewFailed'))
      profileForm.headPic = DEFAULT_AVATAR
    }
    reader.readAsDataURL(file)
  }

  /**
   * 测试图片是否可加载
   * @param url 图片URL
   * @returns 是否可加载
   */
  const testImageLoad = (url: string): Promise<boolean> => {
    return new Promise((resolve) => {
      const testImg = new Image()
      
      testImg.onload = () => {
        console.log('头像测试加载成功:', url)
        resolve(true)
      }

      testImg.onerror = () => {
        console.log('头像测试加载失败:', url)
        resolve(false)
      }

      testImg.src = url

      // 设置超时
      setTimeout(() => {
        console.log('头像测试加载超时:', url)
        resolve(false)
      }, AVATAR_VALIDATION_TIMEOUT)
    })
  }

  // 填充表单数据
  const fillProfileForm = (userData: any, isFromAPI = false) => {
    profileForm.userName = userData.userName || ''
    profileForm.email = userData.email || ''
    profileForm.phone = userData.phone || ''
    profileForm.introduce = userData.introduce || ''
    
    if (isFromAPI && userData.headPic) {
      profileForm.headPic = `${API_BASE}${userData.headPic}`
    } else {
      profileForm.headPic = userData.headPic || DEFAULT_AVATAR
    }
  }

  // 加载用户资料
  const loadUserProfile = async () => {
    try {
      if (userStore.info && userStore.info.userId && userStore.info.userId !== 0) {
        fillProfileForm(userStore.info)
      } else {
        const response = await fetchUserProfileFromAPI()
        
        if (response.success && response.data) {
          const userData = response.data

          fillProfileForm(userData, true)
          
          // 同时更新store中的数据
          userStore.setUserProfile({
            userName: userData.userName,
            headPic: userData.headPic ? `${API_BASE}${userData.headPic}` : DEFAULT_AVATAR,
            email: userData.email,
            phone: userData.phone,
            introduce: userData.introduce
          })
        } else {
          ElMessage.error('获取用户资料失败')
        }
      } 
    } catch (error) {
      ElMessage.error(t('message.getUserInfoFailed'))
    }
  }

  
  
  const submitForm = async () => {
    if (!profileFormRef.value) return
    
    await profileFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        loading.value = true
        try {
          const formData = new FormData()
          
          // 添加文件（如果有）
          if (pendingAvatarFile.value) {
            formData.append('avatar', pendingAvatarFile.value)
          }
          
          // 添加用户信息字段
          if (profileForm.userName) {
            formData.append('userName', profileForm.userName)
          }
          if (profileForm.phone) {
            formData.append('phone', profileForm.phone)
          }
          if (profileForm.introduce) {
            formData.append('introduce', profileForm.introduce)
          }
          if (profileForm.email) {
            formData.append('email', profileForm.email)
          }
          
          const response = await request.put('/user/profile', formData)
          
          if (response.code === 200) {
            const profileUpdate = {
              userName: profileForm.userName,
              email: profileForm.email,
              phone: profileForm.phone,
              introduce: profileForm.introduce
            } as any
            
            // 处理头像更新
            let newAvatarUrl: string | undefined
            if (response.data?.headPic) {
              newAvatarUrl = `${API_BASE}${response.data.headPic}`
            } else if (pendingAvatarFile.value) {
              newAvatarUrl = profileForm.headPic || undefined
            }
            
            if (newAvatarUrl?.trim()) {
                setTimeout(async () => {
                  const isLoadable = await testImageLoad(newAvatarUrl)
                
                if (isLoadable) {
                  profileUpdate.headPic = newAvatarUrl
                  userStore.setUserProfile(profileUpdate)
                  ElMessage.success(t('user.profileUpdated'))
                } else {
                  profileUpdate.headPic = userStore.info.headPic || DEFAULT_AVATAR
                  userStore.setUserProfile(profileUpdate)
                  ElMessage.warning('头像更新可能存在延迟，如长时间未显示请刷新页面')
                }
                
                pendingAvatarFile.value = null
              }, AVATAR_UPDATE_DELAY)
            } else {
              userStore.setUserProfile(profileUpdate)
              ElMessage.success(t('user.profileUpdated'))
            }
          } else {
            ElMessage.error(response.message || t('user.updateFailed'))
          }
        } catch (error: any) {
          ElMessage.error(error.response?.data?.message || t('user.updateFailed'))
        } finally {
          loading.value = false
        }
      }
    })
  }

  function resetForm() {
    fillProfileForm(userStore.info)
    ElMessage.success(t('user.formReset'))
  }

  onMounted(() => {
    loadUserProfile()
  })

  return {
    t,
    profileFormRef,
    loading,
    profileForm,
    pendingAvatarFile,
    computedRules,
    beforeImageUpload,
    handleImageUpload,
    testImageLoad,
    loadUserProfile,
    submitForm,
    resetForm
  }
}
