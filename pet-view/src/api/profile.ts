import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import dogImage from '@/assets/img/dog.jpg'

// 常量定义
const DEFAULT_AVATAR = dogImage
// 图片基础路径不再直接使用
const MAX_FILE_SIZE = 100 * 1024 * 1024 // 100MB 
const ALLOWED_FILE_TYPES = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp']
const PHONE_REGEX = /^1[3-9]\d{9}$/
const AVATAR_VALIDATION_TIMEOUT = 5000

// 从API获取用户完整资料
  export const fetchUserProfileFromAPI = async (): Promise<{ success: boolean; data: any; message: string }> => {
  try {
    const token = localStorage.getItem('jwt_token')
    if (!token) {
      return { success: false, data: null, message: '用户未登录或缺少必要信息' }
    }
    const response = await request.get('/user/profile')
    const result = response.data
    return { success: true, data: result, message: '获取用户资料成功' }
  } catch (error: any) {
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

  // 处理图片加载错误
  const handleImageError = () => {
    // 只有当当前显示的不是默认头像时，才切换到默认头像
    if (profileForm.headPic && profileForm.headPic !== DEFAULT_AVATAR) {
      profileForm.headPic = DEFAULT_AVATAR
    }
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
        resolve(true)
      }

      testImg.onerror = () => {
        resolve(false)
      }

      testImg.src = url

      // 设置超时
      setTimeout(() => {
        resolve(false)
      }, AVATAR_VALIDATION_TIMEOUT)
    })
  }

  // 填充表单数据
  const fetchAvatarUrl = async (fileNameOrUrl: string): Promise<string> => {
    try {
      // 如果已经是完整的URL（包含http或https），直接返回
      if (fileNameOrUrl.startsWith('http') || fileNameOrUrl.startsWith('https')) {
        return fileNameOrUrl;
      }
      // 如果是base64数据URI，直接返回
      if (fileNameOrUrl.startsWith('data:image')) {
        return fileNameOrUrl;
      }
      // 如果是文件名，构造本地访问URL
      if (fileNameOrUrl) {
        return `/images/${fileNameOrUrl}`;
      }
      // 否则返回默认头像
      return DEFAULT_AVATAR;
    } catch {
      return DEFAULT_AVATAR;
    }
  };

  const fillProfileForm = async (userData: any, isFromAPI = false) => {
    profileForm.userName = userData.userName || '';
    profileForm.email = userData.email || '';
    profileForm.phone = userData.phone || '';
    profileForm.introduce = userData.introduce || '';
    
    if (isFromAPI && userData.headPic) {
      profileForm.headPic = await fetchAvatarUrl(userData.headPic);
    } else {
      // 如果userData.headPic为空或未定义，使用默认头像
      profileForm.headPic = userData.headPic || DEFAULT_AVATAR;
    }
  }

  // 加载用户资料
  const loadUserProfile = async () => {
    try {
      await userStore.fetchProfile()
      await fillProfileForm(userStore.info, true)
    } catch (error) {
      ElMessage.error(t('message.getUserInfoFailed'))
      // 加载失败时使用默认头像
      profileForm.headPic = DEFAULT_AVATAR;
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
          
          // 检查响应是否成功
          if (response && response.code === 200) {
            // 请求成功，处理响应数据
            const profileUpdate = {
              userName: profileForm.userName,
              email: profileForm.email,
              phone: profileForm.phone,
              introduce: profileForm.introduce
            } as any
            
            // 处理头像更新
            // 优先使用服务器返回的头像URL，避免将base64数据存储到localStorage
            if (response?.data?.headPic) {
              profileUpdate.headPic = response.data.headPic;
            } else {
              profileUpdate.headPic = DEFAULT_AVATAR;
            }
            
            // 只存储服务器返回的头像URL，不存储base64数据
            userStore.setUserProfile(profileUpdate);
            ElMessage.success(t('user.profileUpdated'));
            
            // 清除待上传的头像文件引用
            pendingAvatarFile.value = null;
          } else {
            // 请求失败，显示错误信息
            throw new Error(response?.message || t('user.updateFailed'))
          }
        } catch (error: any) {
          ElMessage.error(error.message || error.response?.data?.message || t('user.updateFailed'))
        } finally {
          loading.value = false
        }
      }
    })
  }

  async function resetForm() {
    await fillProfileForm(userStore.info)
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
    handleImageError,
    testImageLoad,
    loadUserProfile,
    submitForm,
    resetForm
  }
}