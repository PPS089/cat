<template>
  <div class="pagination-container" :class="{ 'dark': isDark }">
    <el-pagination
      v-model:current-page="internalCurrentPage"
      v-model:page-size="internalPageSize"
      :page-sizes="pageSizes"
      :total="total"
      :background="true"
      :layout="layout"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

interface Props {
  currentPage: number
  pageSize: number
  total: number
  pageSizes?: number[]
  layout?: string
}

interface Emits {
  (e: 'update:currentPage', value: number): void
  (e: 'update:pageSize', value: number): void
  (e: 'pageChange', value: number): void
  (e: 'sizeChange', value: number): void
}

const props = withDefaults(defineProps<Props>(), {
  pageSizes: () => [5, 10, 20, 50],
  layout: 'total, sizes, prev, pager, next, jumper'
})

const emit = defineEmits<Emits>()

const themeStore = useThemeStore()
const isDark = computed(() => themeStore.preferences.theme === 'dark')

const internalCurrentPage = computed({
  get: () => props.currentPage,
  set: (value) => emit('update:currentPage', value)
})

const internalPageSize = computed({
  get: () => props.pageSize,
  set: (value) => emit('update:pageSize', value)
})

const handleCurrentChange = (val: number) => {
  emit('pageChange', val)
}

const handleSizeChange = (val: number) => {
  emit('sizeChange', val)
}
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
  padding: 1rem 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 亮色主题下的分页样式 */
.pagination-container :deep(.el-pagination) {
  --el-pagination-bg-color: #ffffff;
  --el-pagination-button-bg-color: #f8f9fa;
  --el-pagination-button-disabled-bg-color: #e9ecef;
  --el-pagination-text-color: #495057;
  --el-pagination-button-text-color: #495057;
  --el-pagination-button-disabled-text-color: #6c757d;
  --el-pagination-border-color: #dee2e6;
  --el-pagination-hover-color: #3498db;
  --el-pagination-button-border-radius: 6px;
}

/* 暗色主题下的分页样式 */
.pagination-container.dark :deep(.el-pagination) {
  --el-pagination-bg-color: rgba(52, 73, 94, 0.8);
  --el-pagination-button-bg-color: rgba(44, 62, 80, 0.9);
  --el-pagination-button-disabled-bg-color: rgba(52, 73, 94, 0.5);
  --el-pagination-text-color: #ecf0f1;
  --el-pagination-button-text-color: #ecf0f1;
  --el-pagination-button-disabled-text-color: #95a5a6;
  --el-pagination-border-color: rgba(255, 255, 255, 0.2);
  --el-pagination-hover-color: #3498db;
  --el-pagination-button-border-radius: 6px;
}

/* 分页容器背景样式 - 暗色主题 */
.pagination-container.dark {
  background: linear-gradient(135deg, rgba(52, 73, 94, 0.8) 0%, rgba(44, 62, 80, 0.9) 100%);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  margin: 2rem 1rem 0;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 分页按钮悬停效果 */
.pagination-container :deep(.el-pagination button:hover) {
  transform: translateY(-1px);
  transition: all 0.2s ease;
}

.pagination-container.dark :deep(.el-pagination button:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

/* 当前页码高亮样式 */
.pagination-container :deep(.el-pager li.active) {
  background: linear-gradient(135deg, #3498db, #2980b9) !important;
  border-color: #3498db !important;
  color: white !important;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(52, 152, 219, 0.3);
}

.pagination-container.dark :deep(.el-pager li.active) {
  background: linear-gradient(135deg, #3498db, #2980b9) !important;
  border-color: #3498db !important;
  color: white !important;
  box-shadow: 0 4px 15px rgba(52, 152, 219, 0.5);
}

/* 页码项悬停效果 */
.pagination-container :deep(.el-pager li:hover:not(.active)) {
  background-color: #f8f9fa !important;
  color: #3498db !important;
}

.pagination-container.dark :deep(.el-pager li:hover:not(.active)) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: #3498db !important;
}

/* 禁用状态的按钮样式 */
.pagination-container :deep(.el-pagination button:disabled) {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-container.dark :deep(.el-pagination button:disabled) {
  opacity: 0.4;
}

/* 输入框样式 */
.pagination-container :deep(.el-pagination__editor) {
  background-color: #ffffff;
  border: 1px solid #dee2e6;
  border-radius: 4px;
}

.pagination-container.dark :deep(.el-pagination__editor) {
  background-color: rgba(52, 73, 94, 0.8);
  border-color: rgba(255, 255, 255, 0.2);
  color: #ecf0f1;
}

/* 下拉选择框样式 */
.pagination-container :deep(.el-pagination .el-select .el-input__inner) {
  background-color: #ffffff;
  border-color: #dee2e6;
  color: #495057;
}

.pagination-container.dark :deep(.el-pagination .el-select .el-input__inner) {
  background-color: rgba(52, 73, 94, 0.8);
  border-color: rgba(255, 255, 255, 0.2);
  color: #ecf0f1;
}

/* 总条数文字样式 */
.pagination-container :deep(.el-pagination__total) {
  color: #495057;
  font-weight: 500;
}

.pagination-container.dark :deep(.el-pagination__total) {
  color: #ecf0f1;
}

/* 分页组件加载状态 */
.pagination-container :deep(.el-pagination.is-loading) {
  opacity: 0.7;
  pointer-events: none;
}

/* 分页组件焦点状态 */
.pagination-container :deep(.el-pagination button:focus-visible),
.pagination-container :deep(.el-pagination .el-pager li:focus-visible) {
  outline: 2px solid #3498db;
  outline-offset: 2px;
}

.pagination-container.dark :deep(.el-pagination button:focus-visible),
.pagination-container.dark :deep(.el-pagination .el-pager li:focus-visible) {
  outline-color: #5dade2;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pagination-container {
    margin: 2rem 1rem 0;
    padding: 1rem;
  }

  .pagination-container :deep(.el-pagination) {
    --el-pagination-sizes-width: 100px;
  }

  .pagination-container :deep(.el-pagination .el-select) {
    width: 100px;
  }
}

@media (max-width: 480px) {
  .pagination-container {
    margin: 1.5rem 0.5rem 0;
    padding: 0.5rem;
  }

  .pagination-container :deep(.el-pagination) {
    --el-pagination-button-width: 32px;
    --el-pagination-button-height: 32px;
    --el-pagination-font-size: 12px;
  }

  .pagination-container :deep(.el-pagination .btn-prev),
  .pagination-container :deep(.el-pagination .btn-next) {
    padding: 0 8px;
  }

  .pagination-container :deep(.el-pagination .el-pager li) {
    min-width: 32px;
    height: 32px;
    line-height: 32px;
  }
}
</style>
