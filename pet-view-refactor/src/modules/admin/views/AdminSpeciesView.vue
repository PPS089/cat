<template>
  <PageContainer title="物种/品种管理" description="维护物种及其下品种">
    <div class="grid">
      <section class="panel">
        <header class="panel-header">
          <div class="panel-title">物种</div>
          <el-button size="small" type="primary" @click="openCreateSpecies">新增物种</el-button>
        </header>
        <el-table
          :data="speciesList"
          v-loading="speciesLoading"
          highlight-current-row
          row-key="id"
          @row-click="onSpeciesRowClick"
        >
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click.stop="openEditSpecies(row)"
                >编辑</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel">
        <header class="panel-header">
          <div class="panel-title">品种</div>
          <div class="panel-tools">
            <el-select
              v-model="selectedSpeciesId"
              size="small"
              placeholder="选择物种"
              clearable
              filterable
              style="width: 160px"
              @change="onSpeciesSelectChange"
            >
              <el-option v-for="s in speciesList" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-button size="small" type="primary" :disabled="!selectedSpeciesId" @click="openCreateBreed">
              新增品种
            </el-button>
          </div>
        </header>
        <el-table :data="breedList" v-loading="breedLoading" row-key="id">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click.stop="openEditBreed(row)"
                >编辑</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!breedLoading && selectedSpeciesId && breedList.length === 0" description="暂无品种" />
        <el-empty v-if="!selectedSpeciesId" description="请先选择物种" />
      </section>
    </div>

    <el-dialog v-model="speciesDialogVisible" :title="speciesEditingId ? '编辑物种' : '新增物种'" width="520px">
      <el-form ref="speciesFormRef" :model="speciesForm" :rules="speciesRules" label-width="110px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="speciesForm.name" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="speciesForm.description" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="speciesDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingSpecies" @click="saveSpecies">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="breedDialogVisible" :title="breedEditingId ? '编辑品种' : '新增品种'" width="520px">
      <el-form ref="breedFormRef" :model="breedForm" :rules="breedRules" label-width="110px">
        <el-form-item label="物种" prop="speciesId">
          <el-select v-model="breedForm.speciesId" placeholder="请选择物种" filterable style="width: 100%">
            <el-option v-for="s in speciesList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="breedForm.name" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="breedForm.description" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="breedDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingBreed" @click="saveBreed">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import { adminApi } from '@/modules/admin/services/admin'

type Species = { id: number; name: string; description?: string | null }
type Breed = { id: number; speciesId: number; name: string; description?: string | null }

const speciesList = ref<Species[]>([])
const breedList = ref<Breed[]>([])
const speciesLoading = ref(false)
const breedLoading = ref(false)
const savingSpecies = ref(false)
const savingBreed = ref(false)

const selectedSpeciesId = ref<number | null>(null)

const speciesDialogVisible = ref(false)
const speciesEditingId = ref<number | null>(null)
const speciesFormRef = ref<FormInstance>()
const speciesForm = ref<{ name: string; description: string }>({ name: '', description: '' })
const speciesRules: FormRules = {
  name: [{ required: true, message: '请输入物种名称', trigger: 'blur' }],
  description: [{ required: true, whitespace: true, message: '请输入描述', trigger: 'blur' }],
}

const breedDialogVisible = ref(false)
const breedEditingId = ref<number | null>(null)
const breedFormRef = ref<FormInstance>()
const breedForm = ref<{ speciesId: number; name: string; description: string }>({ speciesId: 0, name: '', description: '' })
const breedRules: FormRules = {
  speciesId: [{ required: true, message: '请选择物种', trigger: 'change' }],
  name: [{ required: true, message: '请输入品种名称', trigger: 'blur' }],
  description: [{ required: true, whitespace: true, message: '请输入描述', trigger: 'blur' }],
}

const normalizeDesc = (value?: string) => {
  return (value ?? '').trim()
}

const fetchSpecies = async () => {
  speciesLoading.value = true
  try {
    const res: any = await adminApi.listSpecies()
    speciesList.value = res?.data ?? []
  } finally {
    speciesLoading.value = false
  }
}

const fetchBreeds = async (speciesId: number) => {
  breedLoading.value = true
  try {
    const res: any = await adminApi.listBreeds(speciesId)
    breedList.value = res?.data ?? []
  } finally {
    breedLoading.value = false
  }
}

const selectSpecies = async (speciesId: number) => {
  selectedSpeciesId.value = speciesId
  await fetchBreeds(speciesId)
}

const onSpeciesSelectChange = async (value: number | null) => {
  const id = Number(value ?? 0)
  if (!Number.isFinite(id) || id <= 0) {
    selectedSpeciesId.value = null
    breedList.value = []
    return
  }
  await selectSpecies(id)
}

const onSpeciesRowClick = async (row: Species) => {
  await selectSpecies(row.id)
}

const openCreateSpecies = () => {
  speciesEditingId.value = null
  speciesForm.value = { name: '', description: '' }
  speciesDialogVisible.value = true
}

const openEditSpecies = (row: Species) => {
  speciesEditingId.value = row.id
  speciesForm.value = { name: row.name ?? '', description: row.description ?? '' }
  speciesDialogVisible.value = true
}

const saveSpecies = async () => {
  const valid = await speciesFormRef.value?.validate().catch(() => false)
  if (!valid) return
  savingSpecies.value = true
  try {
    const payload = { name: speciesForm.value.name.trim(), description: normalizeDesc(speciesForm.value.description) }
    if (speciesEditingId.value) {
      await adminApi.updateSpecies(speciesEditingId.value, payload)
      ElMessage.success('物种已更新')
    } else {
      await adminApi.createSpecies(payload)
      ElMessage.success('物种已创建')
    }
    speciesDialogVisible.value = false
    const keepId = selectedSpeciesId.value
    await fetchSpecies()
    if (keepId && speciesList.value.some(s => s.id === keepId)) {
      await selectSpecies(keepId)
    } else {
      const first = speciesList.value[0]
      if (first) {
        await selectSpecies(first.id)
      } else {
        selectedSpeciesId.value = null
        breedList.value = []
      }
    }
  } finally {
    savingSpecies.value = false
  }
}

const openCreateBreed = () => {
  const fallbackSpeciesId = selectedSpeciesId.value ?? speciesList.value[0]?.id ?? null
  if (!fallbackSpeciesId) {
    ElMessage.warning('请先创建物种')
    return
  }
  breedEditingId.value = null
  breedForm.value = { speciesId: fallbackSpeciesId, name: '', description: '' }
  breedDialogVisible.value = true
}

const openEditBreed = (row: Breed) => {
  breedEditingId.value = row.id
  breedForm.value = { speciesId: row.speciesId ?? 0, name: row.name ?? '', description: row.description ?? '' }
  breedDialogVisible.value = true
}

const saveBreed = async () => {
  const speciesId = Number(breedForm.value.speciesId ?? 0)
  if (!Number.isFinite(speciesId) || speciesId <= 0) {
    ElMessage.warning('请选择物种')
    return
  }
  const valid = await breedFormRef.value?.validate().catch(() => false)
  if (!valid) return
  savingBreed.value = true
  try {
    const payload = { name: breedForm.value.name.trim(), description: normalizeDesc(breedForm.value.description) }
    if (breedEditingId.value) {
      await adminApi.updateBreed(speciesId, breedEditingId.value, payload)
      ElMessage.success('品种已更新')
    } else {
      await adminApi.createBreed(speciesId, payload)
      ElMessage.success('品种已创建')
    }
    breedDialogVisible.value = false
    if (selectedSpeciesId.value !== speciesId) {
      selectedSpeciesId.value = speciesId
    }
    await fetchBreeds(speciesId)
  } finally {
    savingBreed.value = false
  }
}

onMounted(async () => {
  await fetchSpecies()
  const first = speciesList.value[0]
  if (first) await selectSpecies(first.id)
})
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.panel {
  border: 1px solid var(--app-border-color);
  border-radius: 12px;
  padding: 12px;
  background: var(--app-surface-color);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-tools {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.panel-title {
  font-weight: 600;
  color: var(--app-text-primary);
}

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
