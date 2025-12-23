<template>
  <PageContainer title="寄养审核" description="审批和管理寄养记录">
    <div class="toolbar">
      <el-select v-model="filterStatus" size="small" placeholder="筛选状态" @change="fetchData(1)">
        <el-option label="全部" value="" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过(进行中)" value="ONGOING" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
    </div>
    <el-table :data="records" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="pet.name" label="宠物" />
      <el-table-column prop="pet.breed" label="品种" />
      <el-table-column prop="applicantName" label="申请人" width="120" />
      <el-table-column prop="applicantPhone" label="手机号" width="140" />
      <el-table-column prop="shelter.name" label="收容所" />
      <el-table-column prop="startDate" label="开始时间" width="160">
        <template #default="{ row }">{{ formatDate(row.startDate) }}</template>
      </el-table-column>
      <el-table-column prop="endDate" label="结束时间" width="160">
        <template #default="{ row }">{{ formatDate(row.endDate) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" type="success" :disabled="!isPending(row.status)" @click="approve(row)">通过</el-button>
          <el-button size="small" type="danger" :disabled="!isPending(row.status)" @click="reject(row)">拒绝</el-button>
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
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import AppPagination from '@/shared/components/common/AppPagination.vue'
import { adminApi } from '@/modules/admin/services/admin'

const records = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ current: 1, size: 10, total: 0 })
const filterStatus = ref('PENDING')

const fetchData = async (page = pagination.value.current) => {
  loading.value = true
  try {
    const res = await adminApi.listFosters(filterStatus.value, page, pagination.value.size)
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
const isPending = (status?: string) => (status || '').toUpperCase() === 'PENDING'
const statusLabel = (status?: string) => {
  const s = (status || '').toUpperCase()
  if (s === 'PENDING') return '待审核'
  if (s === 'APPROVED' || s === 'ONGOING') return '进行中'
  if (s === 'COMPLETED') return '已完成'
  if (s === 'REJECTED') return '已拒绝'
  return status || ''
}
const statusTag = (status?: string) => {
  const s = (status || '').toUpperCase()
  if (s === 'PENDING') return 'info'
  if (s === 'APPROVED' || s === 'ONGOING') return 'warning'
  if (s === 'COMPLETED') return 'success'
  if (s === 'REJECTED') return 'danger'
  return 'info'
}

const approve = async (row: any) => {
  await adminApi.approveFoster(row.id)
  row.status = 'ONGOING' // 本地立即更新，防止界面仍显示待审核
  ElMessage.success('已通过')
  fetchData()
}
const reject = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝寄养', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: '请填写拒绝原因',
      inputType: 'textarea',
      inputValidator: (val: string) => (val && val.trim() ? true : '拒绝原因不能为空'),
    })
    await adminApi.rejectFoster(row.id, value)
    row.status = 'REJECTED' // 本地立即更新，防止界面仍显示待审核
    ElMessage.success('已拒绝')
    fetchData()
  } catch {
    // 用户取消/关闭弹窗
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>
