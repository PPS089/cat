<template>
  <div class="unauthorized-container">
    <div class="unauthorized-content">
      <div class="error-icon">
        <el-icon :size="80">
          <CircleCloseFilled />
        </el-icon>
      </div>
      <h1 class="error-title">{{ t('error.unauthorized') || '401 - 无权限' }}</h1>
      <p class="error-description">
        {{ reason || t('error.unauthorizedDescription') || '您没有权限访问此资源或需要重新登录' }}
      </p>
      
      <div class="error-details" v-if="showDetails">
        <div class="detail-item">
          <span class="label">错误代码：</span>
          <span class="value">{{ errorCode }}</span>
        </div>
        <div class="detail-item" v-if="errorMessage">
          <span class="label">错误信息：</span>
          <span class="value">{{ errorMessage }}</span>
        </div>
      </div>

      <div class="action-buttons">
        <el-button type="primary" size="large" @click="handleLogin">
          {{ t('error.goToLogin') || '重新登录' }}
        </el-button>
        <el-button size="large" @click="handleBack">
          {{ t('error.goBack') || '返回首页' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { CircleCloseFilled } from '@element-plus/icons-vue'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const reason = computed(() => {
  return (route.query.reason as string) || ''
})

const errorCode = computed(() => {
  return (route.query.code as string) || '401'
})

const errorMessage = computed(() => {
  return (route.query.message as string) || ''
})

const showDetails = computed(() => {
  return !!(errorCode.value || errorMessage.value)
})

const handleLogin = () => {
  const currentPath = route.path
  router.push({
    path: currentPath.includes('/admin') ? '/admin/login' : '/login',
    query: { redirect: currentPath },
  })
}

const handleBack = () => {
  const isAdmin = route.path.includes('/admin')
  router.push(isAdmin ? '/admin' : '/user')
}
</script>

<style scoped lang="css">
.unauthorized-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.unauthorized-content {
  background: white;
  border-radius: 12px;
  padding: 60px 40px;
  text-align: center;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.error-icon {
  margin-bottom: 30px;
  color: #f56c6c;
}

.error-icon :deep(.el-icon) {
  display: inline-flex;
}

.error-title {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 16px 0;
}

.error-description {
  font-size: 16px;
  color: #606266;
  margin: 0 0 30px 0;
  line-height: 1.6;
}

.error-details {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin: 30px 0;
  text-align: left;
}

.detail-item {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item .label {
  font-weight: 600;
  color: #303133;
  min-width: 100px;
}

.detail-item .value {
  color: #606266;
  word-break: break-all;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 30px;
}

.action-buttons :deep(.el-button) {
  min-width: 140px;
}

@media (max-width: 600px) {
  .unauthorized-content {
    padding: 40px 20px;
  }

  .error-title {
    font-size: 24px;
  }

  .error-description {
    font-size: 14px;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
