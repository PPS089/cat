import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from "@/utils/request";
import type { LoginForm, LoginResponse } from '@/types/login'

/**
 * 用户登录组合式函数
 */
export const useLogin = () => {
  const router = useRouter()
  const route = useRoute()
  const { t } = useI18n()
  const userStore = useUserStore()
  
  const routerPath = ref('/user');
  
  // 设置跳转路径
  if (route.query.from) {
    routerPath.value = route.query.from as string;
  } else if (route.query.redirect) {
    // 处理路由守卫重定向的情况
    routerPath.value = route.query.redirect as string;
  }
  
  const form = reactive<LoginForm>({
    username: '',
    password: '',
    rememberMe: false
  })

  // 记住用户名和密码的初始化
  const initForm = () => {
    const rememberedUsername = localStorage.getItem('rememberedUsername');
    const rememberedPassword = localStorage.getItem('rememberedPassword');
    if (rememberedUsername && rememberedPassword) {
      form.username = rememberedUsername;
      form.password = rememberedPassword;
      form.rememberMe = true;
    }
  }

  // 初始化表单
  initForm()

  const handleLogin = async () => {
    try {
      // 表单验证
      if (!form.username.trim()) {
        ElMessage.warning({
          message: t('login.usernamePlaceholder'),
          showClose: true,
          duration: 3000
        });
        return;
      }
      
      if (!form.password.trim()) {
        ElMessage.warning({
          message: t('login.passwordPlaceholder'),
          showClose: true,
          duration: 3000
        });
        return;
      }
      
      const response = await request.post('/user/login', {
        username: form.username,
        password: form.password
      })
      
      const loginData = response.data as LoginResponse
      
      if (loginData.ok) {
        const accessToken = (loginData as any).accessToken ?? loginData.token
        const refreshToken = (loginData as any).refreshToken ?? null
        if (!accessToken) {
          ElMessage.error({
            message: t('login.networkError'),
            showClose: true,
            duration: 5000
          });
          return;
        }
        
        // 清理旧的用户信息和token
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('userId');
        
        // 只有在用户选择不记住密码时才清除记住的用户名和密码
        if (!form.rememberMe) {
          localStorage.removeItem('rememberedUsername');
          localStorage.removeItem('rememberedPassword');
        }
        
        // 统一存储字段名，保持前后端一致
        localStorage.setItem('userId', loginData.userId.toString());
        localStorage.setItem('jwt_token', accessToken);  // 存储访问token
        
        // 存储刷新token（双token机制）
        if (refreshToken) {
          localStorage.setItem('refresh_token', refreshToken);
          console.log('已存储refresh token');
        } else {
          console.warn('服务器未返回refresh token，将使用单token模式');
        }
        
        if (form.rememberMe) {
          localStorage.setItem('rememberedUsername', form.username);
          localStorage.setItem('rememberedPassword', form.password);
        }
        
        // 立即设置用户信息到store中，避免路由守卫检查失败
        userStore.info.userId = loginData.userId;
        userStore.info.userName = loginData.username || '';
        
        // 持久化用户信息到localStorage
        localStorage.setItem('userInfo', JSON.stringify(userStore.info));
        localStorage.setItem('userId', loginData.userId.toString());
        localStorage.setItem('userName', loginData.username || '');
        
        // 登录成功后立即获取完整用户资料
        try {
          await userStore.fetchProfile();
        } catch (error) {
          console.error('获取用户资料失败，但继续跳转:', error);
          // 即使获取用户资料失败，也确保基本信息已设置
          userStore.info.userId = loginData.userId;
          userStore.info.userName = loginData.username || '';
        }
        
        // 登录成功提示
        ElMessage.success({
          message: `欢迎回来，${loginData.username || '用户'}！`,
          showClose: true,
          duration: 2000
        });
        
        console.log('登录成功，准备跳转到:', routerPath.value);
        console.log('当前用户ID:', localStorage.getItem('userId'));
        console.log('当前Token:', localStorage.getItem('jwt_token'));
        
        await router.push(routerPath.value);
      } 
    } catch (error: any) {
      
      // 根据不同的错误类型提供更友好的提示
      let errorMessage = t('login.error');
      
      
      ElMessage.error({
        message: errorMessage,
        showClose: true,
        duration: 5000
      });
    }
  }



  return {
    // 翻译函数
    t,
    // 响应式数据
    form,
    routerPath,
    // 方法
    handleLogin
  }
}
