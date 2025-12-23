<template>
  <div class="not-found">
    <div class="not-found__card">
      <el-result icon="warning" title="页面不存在" sub-title="你访问的地址不存在或已被移除">
        <template #extra>
          <el-button type="primary" @click="goHome">返回首页</el-button>
          <el-button @click="goBack">返回上一页</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/')
}
</script>

<style scoped>
.not-found {
  width: min(560px, 100%);
}

.not-found__card {
  width: 100%;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 24px;
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.18);
  padding: clamp(1.25rem, 3vw, 1.75rem);
}
</style>
