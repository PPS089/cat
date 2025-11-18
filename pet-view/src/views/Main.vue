<template>
  <div class="user-layout">
    <!-- 顶部导航栏 -->
    <Header />
    <div class="layout-container">
      <!-- 左侧导航栏 -->
      <aside class="sidebar-container">
        <Aside />
      </aside>
      <!-- 右侧内容区 -->
      <main class="main-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Aside from '../layout/Aside.vue'
import Header from '../layout/Header.vue'
import { useUserLayout } from '../api/main'

// 使用用户布局逻辑
useUserLayout()
</script>

<style scoped>
.user-layout {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.sidebar-container {
  width: 260px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-right: none;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.08);
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  overflow-y: hidden;
  z-index: 10;
}

.main-content {
  flex: 1;
  min-width: 0;
  padding: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  margin-left: 260px;
  height: calc(100vh - 80px);
  overflow-y: hidden;
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: transparent;
  padding: 1rem;
  box-sizing: border-box;
  max-width: 100%;
  height: 100%;
}

/* 暗色主题支持 */
:deep(.dark) .user-layout {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

:deep(.dark) .sidebar-container {
  background: rgba(30, 30, 40, 0.95);
  border-right: none;
}

/* 确保布局高度正确 */
html, body, #app {
  height: 100%;
  overflow: hidden;
}

.user-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-container {
  flex: 1;
  display: flex;
  overflow: hidden;
  height: calc(100vh - 80px);
  margin-top: 80px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-container {
    flex-direction: column;
  }
  
  .sidebar-container {
    width: 100%;
    height: auto;
    max-height: 200px;
    overflow-y: hidden;
  }
  
/* 小屏幕上移除宽度限制 */
  }
  
  .content-wrapper {
    padding: 0.75rem;
   box-sizing: border-box;
}

/* 平滑滚动 */
.content-wrapper {
  scroll-behavior: smooth;
}

/* 自定义滚动条 */
.content-wrapper::-webkit-scrollbar {
  width: 6px;
}

.content-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
