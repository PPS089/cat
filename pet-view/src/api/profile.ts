import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

 // 从API获取用户完整资料
export const fetchUserProfileFromAPI = async () => {
  try {
    const userId = localStorage.getItem('userId')
    const token = localStorage.getItem('token')
    if (!token || !userId) {
      console.warn('用户未登录')
      return { success: false, message: `${userId}用户未登录` }
    }
    
    // 确保请求携带正确的Authorization头
    const response = await request.get('/user/profile')
    console.log('用户资料API原始响应:', response)
    const result = response.data
    console.log('用户资料API data字段:', result)
  
    return { success: true, data: result }
  }
  catch (error) {
    console.error('从API获取用户资料失败:', error)
    return { success: false, message: '获取用户资料失败' }
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
      { pattern: /^1[3-9]\d{9}$/, message: t('user.phoneInvalid'), trigger: 'blur' }
    ],
    bio: [
      { max: 200, message: t('user.bioMax'), trigger: 'blur' }
    ]
  }))

  const beforeImageUpload = (file: File) => {
    const allowedExts = ['jpg','jpeg','png','gif','webp','bmp']
    const ext = file.name.split('.').pop()?.toLowerCase() || ''
    const isAllowedExt = allowedExts.includes(ext)
    const isLt2M = file.size / 1024 / 1024 < 2

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
    
    // 验证文件
    const isValid = beforeImageUpload(file)
    if (!isValid) return
    
    // 本地预览 - 不立即上传
    pendingAvatarFile.value = file
    
    // 创建本地预览URL
    const reader = new FileReader()
    reader.onload = (e) => {
      profileForm.headPic = e.target?.result as string
    }
    reader.onerror = () => {
      ElMessage.error(t('avatarPreviewFailed'))
      profileForm.headPic = '/src/assets/img/dog.jpg' // 使用默认头像
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
      }, 5000)
    })
  }

// 加载用户资料
  const loadUserProfile = async () => {
    try {
      console.log('开始加载用户资料，store.info:', userStore.info)

      // 检查store中是否有有效的用户信息
      if (userStore.info && userStore.info.userId && userStore.info.userId !== 0) {
        console.log('使用store中的用户信息:', userStore.info)
        profileForm.userName = userStore.info.userName || ''
        profileForm.email =  userStore.info.email || ''
        profileForm.phone = userStore.info.phone || ''
        profileForm.headPic = userStore.info.headPic || '/src/assets/img/dog.jpg'
        profileForm.introduce = userStore.info.introduce || ''
      } else {
        console.log('store中用户信息不完整，从API获取...')
        const response = await fetchUserProfileFromAPI()
        console.log('API响应:', response)
        
        if (response.success && response.data) {
          const userData = response.data
          console.log('获取到的用户数据:', userData)

          profileForm.userName = userData.userName || ''
          profileForm.email =  userData.email || ''
          profileForm.phone = userData.phone || ''
          profileForm.headPic = userData.headPic ? `/api/images/${userData.headPic}` : '/src/assets/img/dog.jpg'
          profileForm.introduce = userData.introduce || ''
          
          // 同時更新store中的数据【注意路径~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~】
          userStore.setUserProfile({
            userName: userData.userName,
            headPic: userData.headPic ? `/api/images/${userData.headPic}` : '/src/assets/img/dog.jpg',
            email: userData.email,
            phone: userData.phone,
            introduce: userData.introduce
          })
        } else {
          console.error('API返回数据无效:', response)
          ElMessage.error('获取用户资料失败')
        }
      } 
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error(t('message.getUserInfoFailed'))
    }
  }

  const submitForm = async () => {
    if (!profileFormRef.value) return
    
    await profileFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        loading.value = true
        try {
          // 获取用户当前信息
          const currentUserInfo = userStore.info;
          
          // 只添加发生变更的字段到FormData
          const formData = new FormData()
          
          // 添加文件（如果有）
          if (pendingAvatarFile.value) {
            formData.append('avatar', pendingAvatarFile.value)
          }
          
          // 只有当字段发生变更时才添加到FormData
          if (profileForm.userName && profileForm.userName !== currentUserInfo.userName) {
            formData.append('userName', profileForm.userName)
          }
          
          if (profileForm.email && profileForm.email !== currentUserInfo.email) {
            formData.append('email', profileForm.email)
          }
          
          if (profileForm.phone && profileForm.phone !== currentUserInfo.phone) {
            formData.append('phone', profileForm.phone)
          }
          
          if (profileForm.introduce !== currentUserInfo.introduce) {
            formData.append('introduce', profileForm.introduce || '')
          }
          
          // 如果没有任何字段需要更新且没有文件上传，则提示用户
          if (!pendingAvatarFile.value && formData.entries().next().done) {
            ElMessage.info(t('user.noChanges'))
            loading.value = false
            return
          }
          
          // 提交到后端（Vite代理会自动添加/api前缀）
          const response = await request.put('/user/profile', formData)
          
          if (response.code === 200) {
            // 统一更新用户资料到store
            const profileUpdate = {
              userName: profileForm.userName,
              email: profileForm.email,
              phone: profileForm.phone,
              introduce: profileForm.introduce
            } as any
            
            // 处理头像更新 - 添加可用性验证
            let newAvatarUrl: string | undefined
            if (response.data?.headPic) {
              newAvatarUrl = `/api/images/${response.data.headPic}`
            } else if (pendingAvatarFile.value) {
              // 如果本地上传了头像但后端没返回（使用本地预览的URL）
              newAvatarUrl = profileForm.headPic || undefined
            }
            
            // 如果有新头像，先验证其可用性
            if (newAvatarUrl && newAvatarUrl.trim() !== '') {
              console.log('检测到新头像，开始可用性验证:', newAvatarUrl)
              
              // 给服务器一些处理时间，特别是新上传的图片
              setTimeout(async () => {
                console.log('延迟验证新头像可用性...')
                
                // 测试新头像是否可加载
                const isLoadable = await testImageLoad(newAvatarUrl)
                
                if (isLoadable) {
                  console.log('新头像验证成功，更新store')
                  profileUpdate.headPic = newAvatarUrl
                  userStore.setUserProfile(profileUpdate)
                  ElMessage.success(t('user.profileUpdated'))
                } else {
                  console.log('新头像验证失败，使用默认头像')
                  // 头像验证失败，只更新其他信息，头像保持原样或使用默认头像
                  profileUpdate.headPic = userStore.info.headPic || '/src/assets/img/dog.jpg'
                  userStore.setUserProfile(profileUpdate)
                  ElMessage.warning('头像更新可能存在延迟，如长时间未显示请刷新页面')
                }
                
                // 清空待上传文件
                pendingAvatarFile.value = null
              }, 1000) // 1秒延迟，给服务器处理时间
            } else {
              // 没有头像更新，直接更新其他信息
              userStore.setUserProfile(profileUpdate)
              ElMessage.success(t('user.profileUpdated'))
            }
          } else {
            ElMessage.error(response.message || t('user.updateFailed'))
          }
        } catch (error: any) {
          console.error('资料更新失败:', error)
          ElMessage.error(error.response?.data?.message || t('user.updateFailed'))
        } finally {
          loading.value = false
        }
      }
    })
  }


  function resetForm() {
  // 重置表单到store中的当前状态
  profileForm.userName = userStore.info.userName || ''
  profileForm.email = userStore.info.email || ''
  profileForm.phone = userStore.info.phone || ''
  profileForm.introduce = userStore.info.introduce || ''
  profileForm.headPic = userStore.info.headPic || '/src/assets/img/dog.jpg'
  ElMessage.success(t('user.formReset'))
}

  onMounted(() => {
    // 加载最新数据
    loadUserProfile()
  })

  return {
    // 翻译函数
    t,
    
    // 响应式状态
    profileFormRef,
    loading,
    
    // 表单数据
    profileForm,
    pendingAvatarFile,
    
    // 计算属性
    computedRules,
    
    // 方法
    beforeImageUpload,
    handleImageUpload,
    testImageLoad,
    loadUserProfile,
    submitForm,
    resetForm
  }
}

