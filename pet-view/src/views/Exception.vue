<template>
  <div class="forbidden-container">
    <div class="forbidden-content">
      <h1 class="error-code">403</h1>
      <h2 class="error-title">访问被拒绝</h2>
      <p class="error-message">抱歉，您还没有登录,3秒后自动跳转</p>
    </div>
  </div>
</template>


<script setup lang="ts">
import { exception } from '@/api/exception'

// 使用403页面逻辑
const { initRedirectTimer } = exception()

// 初始化跳转定时器
initRedirectTimer()
</script>


<style>
@import '@/styles/exception.css';
</style>
