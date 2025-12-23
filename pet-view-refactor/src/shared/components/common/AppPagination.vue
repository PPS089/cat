<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="internalCurrentPage"
      v-model:page-size="internalPageSize"
      :page-sizes="pageSizes"
      :total="total"
      background
      :layout="layout"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

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
  pageSizes: () => [6, 12, 24],
  layout: 'total, sizes, prev, pager, next',
})

const emit = defineEmits<Emits>()

const internalCurrentPage = computed({
  get: () => props.currentPage,
  set: value => emit('update:currentPage', value),
})

const internalPageSize = computed({
  get: () => props.pageSize,
  set: value => emit('update:pageSize', value),
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
}
</style>
