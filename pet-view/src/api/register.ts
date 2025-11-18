import { ref } from "vue"
import { ElMessage } from "element-plus"
import { User, Phone, Lock, Message } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import request from "@/utils/request"
import { useRouter } from "vue-router"
import type { RegisterForm } from '@/types/register'

/**
 * 用户注册组合式函数
 */
export const useRegister = () => {
  const { t, locale } = useI18n()
  const router = useRouter()
  
  const loading = ref(false)
  const currentLanguage = ref(locale.value)
  
  const form = ref<RegisterForm>({
    username: "",
    phone: "",
    email: "",
    password: "",
    confirmPassword: ""
  })

  // 返回 t 函数供模板使用
  const translate = (key: string) => t(key)

  // 跳转到登录页面
  const login = () => {
    router.push('/login')
  }

  // 表单验证规则
  const rules = {
    username: [{ required: true, message: t('register.usernameRequired'), trigger: "blur" }],
    phone: [
      { required: true, message: t('register.phoneRequired'), trigger: "blur" },
      {
        validator: (_rule: any, value: string, callback: Function) => {
          if (!/^1[3-9]\d{9}$/.test(value)) {
            callback(new Error(t('register.phoneInvalid')))
          } else {
            callback()
          }
        },
        trigger: "blur"
      }
    ],
    email: [
      { required: true, message: t('register.emailRequired'), trigger: "blur" },
      {
        validator: (_rule: any, value: string, callback: Function) => {
          if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
            callback(new Error(t('register.emailInvalid')))
          } else {
            callback()
          }
        },
        trigger: "blur"
      }
    ],
    password: [{ required: true, message: t('register.passwordRequired'), trigger: "blur" }],
    confirmPassword: [
      { required: true, message: t('register.confirmPasswordRequired'), trigger: "blur" },
      {
        validator: (_rule: any, value: string, callback: Function) => {
          if (value !== form.value.password) {
            callback(new Error(t('register.passwordMismatch')))
          } else {
            callback()
          }
        },
        trigger: "blur"
      }
    ]
  }

  // 提交注册表单
  const onSubmit = async () => {
    loading.value = true
    
    if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
      ElMessage.error(t('register.phoneFormatError'))
      loading.value = false
      return
    }
    
    if (!/^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![a-z0-9]+$)\S{6,12}$/.test(form.value.password)) {
      ElMessage.error(t('register.passwordFormatError'))
      loading.value = false
      return
    }

    try {
      const response = await request.post("/user", {
        username: form.value.username,
        phone: form.value.phone,
        email: form.value.email,
        password: form.value.password
      })
      
      // 检查响应状态
      if (response.code === 200) {
        ElMessage.success(t('register.success'))
        router.push('/login')
      } else {
        ElMessage.error(response.message || t('register.error'))
      }
    } catch (err: any) {
      ElMessage.error(err.response?.data?.message || err.message || t('register.error'))
    } finally {
      loading.value = false
    }
  }

  return {
    // 数据
    form,
    loading,
    currentLanguage,
    rules,
    
    // 图标
  User,
  Phone,
  Lock,
  Message,
    
    // 方法
    login,
    onSubmit,
    
    // 翻译函数
    t: translate
  }
}