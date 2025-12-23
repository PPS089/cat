<template>
  <PageContainer title="文章管理" description="新增、编辑或删除文章">
    <div class="toolbar">
      <el-select v-model="filterStatus" size="small" placeholder="筛选状态" @change="fetchData(1)">
        <el-option label="全部" value="" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
      </el-select>
      <el-button type="primary" size="small" @click="openCreate">新增文章</el-button>
    </div>
    <el-table :data="records" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="author" label="作者" width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusCode(row.status) === 'PUBLISHED' ? 'success' : 'info'">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180">
        <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="edit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <AppPagination
      :current-page="pagination.current"
      :page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @pageChange="page => fetchData(page)"
    />

    <el-dialog
      v-model="formVisible"
      :title="form.id ? '编辑文章' : '新增文章'"
      width="640px"
      @close="onFormClose"
    >
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="作者"><el-input v-model="form.author" /></el-form-item>
        <el-form-item label="封面">
          <el-upload
            class="upload-block"
            :show-file-list="false"
            accept="image/*"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
          >
            <el-button :loading="uploading" type="primary" plain>上传封面</el-button>
            <span class="upload-hint">（支持 jpg/png/webp，≤10MB）</span>
          </el-upload>
          <div v-if="form.coverImage" class="cover-preview">
            <img :src="form.coverImage" alt="封面预览" @error="onImageError" />
          </div>
        </el-form-item>
        <el-form-item label="内容"><el-input type="textarea" :rows="6" v-model="form.content" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="onFormClose">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import AppPagination from '@/shared/components/common/AppPagination.vue'
import { adminApi } from '@/modules/admin/services/admin'
import request from '@/shared/utils/request'

const records = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ current: 1, size: 10, total: 0 })
const filterStatus = ref('')
const formVisible = ref(false)
const uploading = ref(false)
const form = ref<any>({
  id: null,
  title: '',
  author: '',
  content: '',
  coverImage: '',
  status: 'PUBLISHED',
})
const previousCoverImage = ref<string>('')
const pendingCoverImage = ref<string>('')

const fetchData = async (page = pagination.value.current) => {
  loading.value = true
  try {
    const res = await adminApi.listArticles(filterStatus.value, page, pagination.value.size)
    records.value = res?.data?.records ?? []
    pagination.value = {
      current: res?.data?.current ?? page,
      size: res?.data?.size ?? pagination.value.size,
      total: res?.data?.total ?? 0,
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchData(1))

const formatDate = (value?: string) => (value ? new Date(value).toLocaleString() : '')
const statusCode = (status: any) => {
  if (status === undefined || status === null) return ''
  if (typeof status === 'string') return status.toUpperCase()
  if (typeof status === 'object') {
    if ('code' in status) return String((status as any).code).toUpperCase()
    if ('status' in status) return String((status as any).status).toUpperCase()
  }
  return String(status).toUpperCase()
}
const statusLabel = (status?: string) => (statusCode(status) === 'PUBLISHED' ? '已发布' : '草稿')

const openCreate = () => {
  form.value = {
    id: null,
    title: '',
    author: '',
    content: '',
    coverImage: '',
    status: 'PUBLISHED',
  }
  previousCoverImage.value = ''
  pendingCoverImage.value = ''
  formVisible.value = true
}

const edit = (row: any) => {
  form.value = { ...row, status: statusCode(row.status) || 'DRAFT' }
  previousCoverImage.value = row.coverImage || ''
  pendingCoverImage.value = ''
  formVisible.value = true
}

const save = async () => {
  const payload = { ...form.value, status: statusCode(form.value.status) || 'PUBLISHED' }
  await adminApi.saveArticle(payload)
  if (previousCoverImage.value && previousCoverImage.value !== form.value.coverImage) {
    try {
      await request.delete('/media/raw', { params: { url: previousCoverImage.value } })
    } catch (e) {
      console.warn('删除旧封面失败', e)
    }
  }
  ElMessage.success(payload.id ? '更新成功' : '创建成功')
  previousCoverImage.value = form.value.coverImage
  pendingCoverImage.value = ''
  formVisible.value = false
  fetchData()
}

const remove = async (row: any) => {
  await ElMessageBox.confirm('确认删除该文章？', '提示', { type: 'warning' })
  await adminApi.deleteArticle(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

const beforeImageUpload = (file: File) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('仅支持图片格式')
    return false
  }
  const MAX_SIZE = 10 * 1024 * 1024
  if (file.size > MAX_SIZE) {
    ElMessage.error('图片大小需小于 10MB')
    return false
  }
  return true
}

const handleImageUpload = async (options: UploadRequestOptions) => {
  const formData = new FormData()
  formData.append('file', options.file)
  uploading.value = true
  try {
    const res: any = await request.post('/media/upload/raw', formData)
    const url = res?.data ?? res
    if (url) {
      form.value.coverImage = url
      pendingCoverImage.value = url
      ElMessage.success('上传成功')
      options.onSuccess?.(res as any)
    } else {
      throw new Error('未获取到文件地址')
    }
  } catch (error) {
    options.onError?.(error as any)
  } finally {
    uploading.value = false
  }
}

const onImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = ''
}

const onFormClose = async () => {
  formVisible.value = false
  if (pendingCoverImage.value && pendingCoverImage.value !== previousCoverImage.value) {
    try {
      await request.delete('/media/raw', { params: { url: pendingCoverImage.value } })
    } catch (e) {
      console.warn('取消时清理未保存封面失败', e)
    }
  }
  pendingCoverImage.value = ''
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  justify-content: space-between;
  margin-bottom: 12px;
}

.upload-block {
  margin-bottom: 6px;
}
.upload-hint {
  margin-left: 8px;
  color: var(--app-text-secondary);
  font-size: 12px;
}
.cover-preview img {
  width: 160px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--app-border-color);
  margin-top: 6px;
}
</style>
