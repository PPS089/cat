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
    console.log('检测到from参数，跳转到路径:', routerPath.value);
  } else {
    console.log('没有from参数，使用默认路径:', routerPath.value);
  }
  
  const form = reactive<LoginForm>({
    username: '',
    password: '',
    rememberMe: false
  })

  const handleLogin = async () => {
    try {
      
      const response = await request.post('/user/login', {
        username: form.username,
        password: form.password
      })

      console.log('登录响应数据:', response);

      // 后端返回格式为 {code: 200, msg: 'success', data: {实际数据}}
      
      const loginData = response.data as LoginResponse
      
      if (loginData.ok) {
        // 确保使用后端返回的token，不使用用户名作为备用
        const token = loginData.token;
        if (!token) {
          console.error('登录响应中没有token字段');
          ElMessage.error('登录失败：服务器未返回token');
          return;
        }
        
        // 统一存储字段名，保持前后端一致
        localStorage.setItem('userId', loginData.userId.toString());
        localStorage.setItem('jwt_token', token);  // 改为jwt_token，与请求头一致
        // 同时保持向后兼容
        localStorage.setItem('token', token);
        localStorage.setItem('rememberedUsername', form.username);
        if (form.rememberMe) {
          localStorage.setItem('rememberedPassword', form.password);
        }
        
        // 立即设置用户信息到store中，避免路由守卫检查失败
        userStore.info.userId = loginData.userId;
        userStore.info.userName = loginData.userName || '';
        
        // 登录成功后立即获取完整用户资料
        console.log('登录成功，开始获取完整用户资料...');
        await userStore.fetchProfile();
        
        // 登录历史记录已由后端自动处理，这里可以添加额外的日志记录
        console.log('用户登录成功，登录历史已记录到数据库，用户ID:', loginData.userId);
        
        // 尝试跳转，添加错误处理
        await router.push(routerPath.value);
        console.log(t('redirectSuccessTo'), routerPath.value);
      } else {
        ElMessage.error(loginData.message || t('loginFailed'));
        console.log(t('loginFailedServerReturn'), loginData.message);
      }
    } catch (error: any) {
      console.error('登录请求失败:', error);
      ElMessage.error(error.message || '网络错误，请检查网络连接');
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