import { ref, reactive, onMounted } from 'vue'
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
  } else {
  }
  
  const form = reactive<LoginForm>({
    username: '',
    password: '',
    rememberMe: false
  })

  const handleLogin = async () => {
    try {
      // 表单验证
      if (!form.username.trim()) {
        ElMessage.warning({
          message: '请输入用户名',
          showClose: true,
          duration: 3000
        });
        return;
      }
      
      if (!form.password.trim()) {
        ElMessage.warning({
          message: '请输入密码',
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
        // 确保使用后端返回的token，不使用用户名作为备用
        const token = loginData.token;
        if (!token) {
          ElMessage.error({
            message: '登录失败：服务器响应异常，请稍后重试',
            showClose: true,
            duration: 5000
          });
          return;
        }
        
        // 清理旧的用户信息和token
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('userId');
        localStorage.removeItem('userName');
        localStorage.removeItem('rememberedUsername');
        localStorage.removeItem('rememberedPassword');
        
        // 统一存储字段名，保持前后端一致
        localStorage.setItem('userId', loginData.userId.toString());
        localStorage.setItem('jwt_token', token); 
        localStorage.setItem('userName', loginData.userName || '');
        localStorage.setItem('rememberedUsername', form.username);
        if (form.rememberMe) {
          localStorage.setItem('rememberedPassword', form.password);
        }
        
        // 立即设置用户信息到store中，避免路由守卫检查失败
        userStore.info.userId = loginData.userId;
        userStore.info.userName = loginData.userName || '';
        
        // 持久化用户信息到localStorage
        localStorage.setItem('userInfo', JSON.stringify(userStore.info));
        
        // 登录成功后立即获取完整用户资料
        console.log('登录成功，开始获取完整用户资料...');
        await userStore.fetchProfile();
        
        // 登录成功提示
        ElMessage.success({
          message: `欢迎回来，${loginData.userName || '用户'}！`,
          showClose: true,
          duration: 2000
        });
        
        // 尝试跳转，添加错误处理
        await router.push(routerPath.value);
        console.log(t('redirectSuccessTo'), routerPath.value);
      } else {
        // 根据不同的错误类型提供更友好的提示
        let errorMessage = '登录失败';
        
        if (loginData.message) {
          if (loginData.message.includes('用户名') || loginData.message.includes('密码')) {
            errorMessage = '用户名或密码错误，请检查后重新输入';
          } else if (loginData.message.includes('不存在')) {
            errorMessage = '用户不存在，请检查用户名或注册新账号';
          }else {
            errorMessage = loginData.message;
          }
        }
        
        ElMessage.error({
          message: errorMessage,
          showClose: true,
          duration: 5000
        });
        
        console.log(t('loginFailedServerReturn'), loginData.message);
      }
    } catch (error: any) {
      console.error('登录请求失败:', error);
      
      // 根据不同的错误类型提供更友好的提示
      let errorMessage = '登录失败，请稍后重试';
      
      if (error.response) {
        // 服务器返回了错误状态码
        if (error.response.status === 401) {
          errorMessage = '用户名或密码错误，请检查后重新输入';
        } else if (error.response.status === 403) {
          errorMessage = '账号已被禁用，请联系管理员';
        } else if (error.response.status === 404) {
          errorMessage = '用户不存在，请检查用户名或注册新账号';
        } else if (error.response.status >= 500) {
          errorMessage = '服务器内部错误，请稍后重试';
        }
      } else if (error.request) {
        // 请求已发出但没有收到响应
        errorMessage = '网络连接失败，请检查网络设置';
      } else if (error.message) {
        if (error.message.includes('Network Error')) {
          errorMessage = '网络连接失败，请检查网络设置';
        } else if (error.message.includes('timeout')) {
          errorMessage = '请求超时，请稍后重试';
        }
      }
      
      ElMessage.error({
        message: errorMessage,
        showClose: true,
        duration: 5000
      });
    }
  }

  const initForm = () => {
    onMounted(() => {
      const rememberedUsername = localStorage.getItem('rememberedUsername');
      const rememberedPassword = localStorage.getItem('rememberedPassword');
      if (rememberedUsername && rememberedPassword) {
        form.username = rememberedUsername;
        form.password = rememberedPassword;
        form.rememberMe = true;
      }
    });
  }

  return {
    // 翻译函数
    t,
    // 响应式数据
    form,
    routerPath,
    // 方法
    handleLogin,
    initForm
  }
}