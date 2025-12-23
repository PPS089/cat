import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadFile } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/app/store/modules/user'
import dogImage from '@/assets/img/dog.jpg'
import { updateProfile } from '@/modules/profile/services/profile'
import type { UserInfo } from '@/modules/profile/types'
import { resolveAssetUrl } from '@/shared/utils/url'

const MAX_FILE_SIZE = 10 * 1024 * 1024
const ALLOWED_TYPES = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg']

function resolveAvatar(value?: string | null) {
  if (!value) return dogImage
  const normalized = resolveAssetUrl(value)
  return normalized || dogImage
}

export function useProfileForm() {
  const { t } = useI18n()
  const userStore = useUserStore()

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const saving = ref(false)
  const isDirty = ref(false)
  const avatarPreview = ref<string>(resolveAvatar(userStore.info.headPic))
  const pendingAvatar = ref<File | null>(null)
  const submitError = ref('')
  let previewUrl: string | null = null

  const formModel = reactive({
    userName: '',
    email: '',
    phone: '',
    introduce: '',
  })

  const applyUserInfo = (info: Partial<UserInfo> | null) => {
    formModel.userName = info?.userName || ''
    formModel.email = info?.email || ''
    formModel.phone = info?.phone || ''
    formModel.introduce = info?.introduce || ''
    if (!pendingAvatar.value) {
      avatarPreview.value = resolveAvatar(info?.headPic)
    }
    isDirty.value = false
    submitError.value = ''
  }

  applyUserInfo(userStore.info)

  const rules = computed<FormRules>(() => ({
    userName: [
      { required: true, message: t('user.usernameRequired'), trigger: 'blur' },
      { min: 1, max: 10, message: t('user.usernameLength'), trigger: 'blur' },
    ],
    email: [
      { type: 'email', message: t('user.emailInvalid'), trigger: 'blur' },
    ],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: t('user.phoneInvalid'), trigger: 'blur' }],
    introduce: [{ max: 200, message: t('user.bioMax'), trigger: 'blur' }],
  }))

  const loadProfile = async () => {
    if (userStore.profileLoaded) {
      // 资料已在路由守卫/登录后加载，避免重复请求导致头像二次加载与闪烁
      return
    }
    loading.value = true
    try {
      await userStore.fetchProfile()
      applyUserInfo(userStore.info)
    } catch (error: any) {
      console.error('[Profile] load profile failed', error)
      applyUserInfo(null)
    } finally {
      loading.value = false
    }
  }

  const validateFile = (file: File) => {
    const extension = file.name.split('.').pop()?.toLowerCase() || ''
    if (!ALLOWED_TYPES.includes(extension)) {
      ElMessage.error(t('message.avatarFormatError'))
      return false
    }
    if (file.size > MAX_FILE_SIZE) {
      ElMessage.error(t('message.avatarSizeError'))
      return false
    }
    return true
  }

  const handleAvatarSelect = (uploadFile: UploadFile) => {
    const file = uploadFile.raw
    if (!file) return false
    if (!validateFile(file)) return false
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl)
      previewUrl = null
    }
    previewUrl = URL.createObjectURL(file)
    avatarPreview.value = previewUrl
    pendingAvatar.value = file
    isDirty.value = true
    submitError.value = ''
    return false
  }

  const markDirty = () => {
    isDirty.value = true
  }

  const clearPendingAvatar = () => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl)
      previewUrl = null
    }
    pendingAvatar.value = null
  }

  const submit = async () => {
    if (!formRef.value || !isDirty.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    submitError.value = ''
    try {
      const formData = new FormData()
      formData.append('userName', formModel.userName || '')
      formData.append('email', formModel.email || '')
      formData.append('phone', formModel.phone || '')
      formData.append('introduce', formModel.introduce || '')
      if (pendingAvatar.value) {
        formData.append('avatar', pendingAvatar.value)
      }

      const updated = await updateProfile(formData)
      userStore.applyProfileUpdate(updated)
      clearPendingAvatar()
      applyUserInfo(userStore.info)
      ElMessage.success(t('user.profileUpdated'))
    } catch (error: any) {
      submitError.value = error?.message || t('user.updateFailed')
      console.error('[Profile] update profile failed', error)
    } finally {
      saving.value = false
    }
  }

  const reset = () => {
    clearPendingAvatar()
    applyUserInfo(userStore.info)
  }

  onMounted(loadProfile)

  onBeforeUnmount(() => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl)
      previewUrl = null
    }
  })

  return {
    t,
    formRef,
    formModel,
    rules,
    loading,
    saving,
    avatarPreview,
    isDirty,
    submitError,
    handleAvatarSelect,
    markDirty,
    submit,
    reset,
  }
}
