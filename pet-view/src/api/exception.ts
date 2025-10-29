import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

// 403页面组合式函数
export const exception = () => {
  // 获取路由实例
  const router = useRouter();
  const route = useRoute();
  let timerId: NodeJS.Timeout | null = null;

  // 立即跳转
  const immediateRedirect = () => {
    // 清除定时器（防止重复跳转）
    if (timerId) {
      clearTimeout(timerId);
    }
    const routerPath = ref(route.query.from as string);
    router.push({ path: '/login', query: { from: routerPath.value } });
  };

  // 延迟跳转逻辑
  const initRedirectTimer = () => {
    onMounted(() => {
      // 启动延迟定时器
      timerId = setTimeout(immediateRedirect, 3 * 1000);
    });

    // 组件卸载时清除定时器（防止内存泄漏）
    onUnmounted(() => {
      if (timerId) {
        clearTimeout(timerId);
      }
    });
  };

  return {
    // 方法
    immediateRedirect,
    initRedirectTimer
  };
}