<template>
  <PageContainer title="宠物管理" description="新增、更新状态或删除宠物">
    <div class="toolbar">
      <el-select v-model="filterStatus" size="small" placeholder="筛选状态" @change="fetchData(1)">
        <el-option label="全部" value="" />
        <el-option label="未被领养" value="AVAILABLE" />
        <el-option label="已被领养" value="ADOPTED" />
        <el-option label="寄养中" value="FOSTERING" />
      </el-select>
      <el-button type="primary" size="small" @click="openCreate">新增宠物</el-button>
    </div>
    <el-table :data="records" style="width: 100%" v-loading="loading">
      <el-table-column prop="pid" label="ID" width="60" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="species" label="物种" width="100" />
      <el-table-column prop="breed" label="品种" />
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="viewDetail(row)">查看</el-button>
          <el-button
            v-if="row.status === 'AVAILABLE'"
            size="small"
            type="primary"
            @click="openEdit(row)"
          >
            修改信息
          </el-button>
          <el-button
            v-if="row.status === 'AVAILABLE'"
            size="small"
            type="danger"
            @click="remove(row)"
          >
            删除
          </el-button>
          <el-button
            v-else
            size="small"
            type="info"
            disabled
          >
            仅查看
          </el-button>
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

    <el-dialog v-model="detailVisible" title="宠物信息" width="520px" class="pet-detail-dialog">
      <div class="detail-body">
        <div class="detail-row">
          <span class="label">名称</span><span class="value">{{ detail.name || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">物种</span><span class="value">{{ detail.species || '-' }}</span>
          <span class="label">品种</span><span class="value">{{ detail.breed || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">年龄</span><span class="value">{{ detail.age ?? '-' }}</span>
          <span class="label">性别</span><span class="value">{{ detail.gender || '-' }}</span>
          <span class="label">状态</span><span class="value">{{ statusLabel(detail.status) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">收容所</span>
          <div class="value value-multi">
            <div class="value-main">{{ detail.shelterName || '-' }}</div>
            <div v-if="detail.shelterLocation" class="value-sub">位置：{{ detail.shelterLocation }}</div>
          </div>
        </div>
        <div class="detail-row" v-if="detail.status === 'ADOPTED' || detail.status === 'FOSTERING'">
          <span class="label">领养人</span>
          <div class="value value-multi adopter">
            <div class="value-main">{{ detail.adopterName || '-' }}</div>
            <div class="value-sub">
              <span v-if="detail.adopterPhone">电话：{{ detail.adopterPhone }}</span>
              <span v-if="detail.adopterEmail" class="divider">·</span>
              <span v-if="detail.adopterEmail">邮箱：{{ detail.adopterEmail }}</span>
            </div>
            <div v-if="detail.adopterAddress" class="value-sub">{{ detail.adopterAddress }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" plain @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="createVisible"
      :title="isEditing ? '编辑宠物' : '新增宠物'"
      width="540px"
      @close="onFormClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="物种" prop="speciesId">
          <el-select
            v-model="form.speciesId"
            placeholder="请选择物种"
            filterable
            :loading="speciesLoading"
            @change="handleSpeciesChange"
          >
            <el-option v-for="sp in speciesOptions" :key="sp.id" :label="sp.name" :value="sp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种" prop="breedId">
          <el-select
            v-model="form.breedId"
            placeholder="请选择品种"
            filterable
            :disabled="!form.speciesId"
            :loading="breedLoading"
          >
            <el-option v-for="b in breedOptions" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age"><el-input-number v-model="form.age" :min="1" /></el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="雄">雄</el-radio>
            <el-radio label="雌">雌</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="收容所" prop="shelterId">
          <el-select v-model="form.shelterId" placeholder="请选择收容所" filterable>
            <el-option
              v-for="shelter in shelters"
              :key="shelter.sid"
              :label="`${shelter.name} · ${shelter.location}`"
              :value="shelter.sid"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物图片" prop="imageUrl">
          <el-upload
            class="upload-block"
            :show-file-list="false"
            accept="image/*"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
          >
            <el-button :loading="uploading" type="primary" plain>上传图片</el-button>
            <span class="upload-hint">（支持 jpg/png/webp，≤10MB）</span>
          </el-upload>
          <div v-if="form.imageUrl" class="image-preview">
            <img :src="form.imageUrl" alt="预览" @error="onImageError" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="onFormClose">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ isEditing ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElForm, type FormRules } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import AppPagination from '@/shared/components/common/AppPagination.vue'
import { adminApi } from '@/modules/admin/services/admin'
import { petsService } from '@/modules/pets/services/pets'
import request from '@/shared/utils/request'

const records = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ current: 1, size: 10, total: 0 })
const filterStatus = ref('')
const createVisible = ref(false)
const isEditing = ref(false)
const shelters = ref<{ sid: number; name: string; location: string }[]>([])
const uploading = ref(false)
const previousImageUrl = ref<string>('')
const pendingUploadUrl = ref<string>('')
const detailVisible = ref(false)
const detail = ref<any>({})
const form = ref({
  pid: undefined as number | undefined,
  name: '',
  speciesId: undefined as number | undefined,
  breedId: undefined as number | undefined,
  age: 1,
  gender: '雄',
  imageUrl: '',
  shelterId: 1,
})
const speciesOptions = ref<any[]>([])
const breedOptions = ref<any[]>([])
const speciesLoading = ref(false)
const breedLoading = ref(false)
const formRef = ref<InstanceType<typeof ElForm>>()
const rules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  speciesId: [{ required: true, message: '请选择物种', trigger: 'change' }],
  breedId: [{ required: true, message: '请选择品种', trigger: 'change' }],
  age: [
    { required: true, message: '请输入年龄', trigger: 'change' },
    { type: 'number', min: 1, message: '年龄必须大于0', trigger: 'change' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  imageUrl: [{ required: true, message: '请上传图片', trigger: 'change' }],
  shelterId: [{ required: true, message: '请选择收容所', trigger: 'change' }],
}

const fetchData = async (page = pagination.value.current) => {
  loading.value = true
  try {
    const res = await adminApi.listPets(filterStatus.value, page, pagination.value.size)
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

const fetchSpeciesOptions = async () => {
  speciesLoading.value = true
  try {
    const res: any = await adminApi.listSpecies()
    speciesOptions.value = res?.data ?? []
  } finally {
    speciesLoading.value = false
  }
}

const fetchBreedOptions = async (speciesId?: number) => {
  if (!speciesId) {
    breedOptions.value = []
    return
  }
  breedLoading.value = true
  try {
    const res: any = await adminApi.listBreeds(speciesId)
    breedOptions.value = res?.data ?? []
  } finally {
    breedLoading.value = false
  }
}

const handleSpeciesChange = async (speciesId?: number) => {
  form.value.breedId = undefined
  await fetchBreedOptions(speciesId)
  if (!form.value.breedId && breedOptions.value.length) {
    form.value.breedId = breedOptions.value[0].id
  }
}

const loadShelters = async () => {
  const res = await petsService.fetchShelters()
  shelters.value = (res?.data && Array.isArray(res.data) ? res.data : []).map((s: any) => ({
    sid: s.sid ?? s.id,
    name: s.name ?? s.shelterName,
    location: s.location ?? s.shelterAddress,
  }))
}

onMounted(async () => {
  fetchData(1)
  await loadShelters()
  await fetchSpeciesOptions()
})

const statusLabel = (status?: string) => {
  const s = (status || '').toUpperCase()
  if (s === 'AVAILABLE') return '可领养'
  if (s === 'ADOPTED') return '已领养'
  if (s === 'FOSTERING') return '寄养中'
  return status || ''
}
const statusTag = (status?: string) => {
  const s = (status || '').toUpperCase()
  if (s === 'AVAILABLE') return 'success'
  if (s === 'FOSTERING') return 'warning'
  if (s === 'ADOPTED') return 'info'
  return 'info'
}

const openCreate = async () => {
  isEditing.value = false
  createVisible.value = true
  const defaultSpeciesId = speciesOptions.value[0]?.id
  form.value = {
    pid: undefined,
    name: '',
    speciesId: defaultSpeciesId,
    breedId: undefined,
    age: 1,
    gender: '雄',
    imageUrl: '',
    shelterId: shelters.value[0]?.sid || 1,
  }
  await handleSpeciesChange(defaultSpeciesId)
  previousImageUrl.value = ''
  pendingUploadUrl.value = ''
}

const openEdit = async (row: any) => {
  isEditing.value = true
  createVisible.value = true
  const speciesId = row.speciesId ?? row.species_id
  const breedId = row.breedId ?? row.breed_id
  form.value = {
    pid: row.pid,
    name: row.name,
    speciesId,
    breedId,
    age: row.age,
    gender: row.gender,
    imageUrl: row.image || row.imgUrl || row.imageUrl || '',
    shelterId: row.shelterId || row.shelter_id || row.sid,
  }
  await fetchBreedOptions(speciesId)
  if (breedId && breedOptions.value.some(b => b.id === breedId)) {
    form.value.breedId = breedId
  } else {
    form.value.breedId = breedOptions.value[0]?.id
  }
  previousImageUrl.value = form.value.imageUrl
  pendingUploadUrl.value = ''
}

const viewDetail = (row: any) => {
  detail.value = {
    name: row.name,
    species: row.species,
    breed: row.breed,
    age: row.age,
    gender: row.gender,
    status: row.status,
    shelterName: row.shelterName,
    shelterLocation: row.shelterLocation,
    adopterName: row.adopterName,
    adopterPhone: row.adopterPhone,
    adopterEmail: row.adopterEmail,
    adopterAddress: row.adopterAddress || row.adopterLocation,
  }
  detailVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  if (isEditing.value && form.value.pid) {
    await adminApi.updatePet(form.value.pid, {
      name: form.value.name,
      speciesId: form.value.speciesId,
      breedId: form.value.breedId,
      age: form.value.age,
      gender: form.value.gender,
      imageUrl: form.value.imageUrl,
      shelterId: form.value.shelterId,
    })
    if (previousImageUrl.value && previousImageUrl.value !== form.value.imageUrl) {
      try {
        await request.delete('/media/raw', { params: { url: previousImageUrl.value } })
      } catch (e) {
        console.warn('删除旧图片失败', e)
      }
    }
    ElMessage.success('更新成功')
  } else {
    await adminApi.createPet({
      name: form.value.name,
      speciesId: form.value.speciesId,
      breedId: form.value.breedId,
      age: form.value.age,
      gender: form.value.gender,
      imageUrl: form.value.imageUrl,
      shelterId: form.value.shelterId,
    })
    ElMessage.success('创建成功')
  }
  previousImageUrl.value = form.value.imageUrl
  pendingUploadUrl.value = ''
  createVisible.value = false
  fetchData(1)
}

const remove = async (row: any) => {
  await ElMessageBox.confirm('确定删除该宠物？', '提示', { type: 'warning' })
  await adminApi.deletePet(row.pid)
  ElMessage.success('删除成功')
  fetchData()
}

const onImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = ''
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
      form.value.imageUrl = url
      pendingUploadUrl.value = url
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

const resetForm = () => {
  const defaultSpeciesId = speciesOptions.value[0]?.id
  form.value = {
    pid: undefined,
    name: '',
    speciesId: defaultSpeciesId,
    breedId: undefined,
    age: 1,
    gender: '雄',
    imageUrl: '',
    shelterId: shelters.value[0]?.sid || 1,
  }
  handleSpeciesChange(defaultSpeciesId)
}

const onFormClose = async () => {
  createVisible.value = false
  if (pendingUploadUrl.value && pendingUploadUrl.value !== previousImageUrl.value) {
    try {
      await request.delete('/media/raw', { params: { url: pendingUploadUrl.value } })
    } catch (e) {
      console.warn('取消时清理未保存图片失败', e)
    }
  }
  pendingUploadUrl.value = ''
  resetForm()
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
.image-preview img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--app-border-color);
}

.pet-detail-dialog :deep(.el-dialog__body) {
  padding-top: 12px;
}
.detail-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
  color: var(--app-text-primary);
}
.detail-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 12px;
  line-height: 1.6;
}
.detail-row .label {
  color: #d59a5c;
  min-width: 52px;
  font-weight: 500;
}
.detail-row .value {
  flex: 1 1 auto;
}
.value-multi {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.value-main {
  font-weight: 600;
}
.value-sub {
  color: var(--app-text-secondary);
  font-size: 13px;
}
.adopter .value-main {
  font-weight: 600;
}
.adopter .divider {
  margin: 0 6px;
  color: var(--app-text-secondary);
}
</style>
