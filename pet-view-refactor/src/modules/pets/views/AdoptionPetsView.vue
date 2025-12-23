<template>
  <PageContainer :title="t('nav.adoptAPet')" :description="t('message.findYourPerfectCompanion')">
    <section v-loading="loading" class="pets-section">
      <!-- 筛选栏 -->
      <div class="filter-section">
        <div class="filter-row">
          <!-- 物种筛选 -->
          <el-select 
            v-model="localFilters.species" 
            :placeholder="t('message.selectSpecies')" 
            clearable 
            :loading="speciesLoading"
            @change="handleSpeciesChange"
          >
            <el-option 
              v-for="species in speciesOptions" 
              :key="species.value" 
              :label="species.label" 
              :value="species.value" 
            />
          </el-select>
          
          <!-- 品种筛选 -->
          <el-select 
            v-model="localFilters.breed" 
            :placeholder="t('message.selectBreed')" 
            clearable 
            :disabled="!localFilters.species"
            :loading="breedLoading"
          >
            <el-option 
              v-for="breed in breedOptions" 
              :key="breed" 
              :label="breed" 
              :value="breed" 
            />
          </el-select>
          
          <!-- 性别筛选 -->
          <el-select 
            v-model="localFilters.gender" 
            :placeholder="t('message.selectGender')" 
            clearable
          >
            <el-option 
              v-for="gender in genderOptions" 
              :key="gender.value" 
              :label="gender.label" 
              :value="gender.value" 
            />
          </el-select>
        </div>
        
        <div class="filter-row">
          <!-- 年龄范围筛选 -->
          <div class="age-range">
            <span>{{ t('message.ageRange') }}</span>
            <el-input-number 
              v-model="localFilters.minAge" 
              :placeholder="t('message.minAge')" 
              :min="0" 
              :max="20" 
              controls-position="right" 
              size="small" 
            />
            <span>-</span>
            <el-input-number 
              v-model="localFilters.maxAge" 
              :placeholder="t('message.maxAge')" 
              :min="0" 
              :max="20" 
              controls-position="right" 
              size="small" 
            />
          </div>
          
          <!-- 搜索框 -->
          <el-input 
            v-model="searchKeyword" 
            :placeholder="t('message.searchBreed')" 
            clearable 
            @keyup.enter="applySearch"
          >
            <template #append>
              <el-button @click="applySearch">{{ t('message.search') }}</el-button>
            </template>
          </el-input>
          
          <!-- 清除筛选按钮 -->
          <el-button @click="clearAllFilters">{{ t('message.clearFilters') }}</el-button>
        </div>
      </div>
      
      <el-empty v-if="!loading && !items.length" :description="t('nav.noPetsAvailable')">
        <p>{{ t('nav.allPetsAdopted') }}</p>
      </el-empty>

      <div v-else class="pets-grid">
        <WebCard v-for="pet in items" :key="pet.pid" class="pet-card">
          <div class="pet-card__media" @click="viewDetail(pet.pid)">
            <img :src="resolvePetImage(pet)" :alt="pet.name" loading="lazy" @error="onPetImageError" />
            <div class="pet-card__media-overlay">
              <el-tag type="success" size="small" class="pet-card__status">{{ t('nav.adoptable') }}</el-tag>
            </div>
          </div>

          <div class="pet-card__body">
            <header class="pet-card__header">
              <div>
                <h3>{{ pet.name }}</h3>
                <p>{{ [pet.breed, pet.species].filter(Boolean).join(' · ') }}</p>
              </div>
            </header>

            <div class="pet-card__meta">
              <span class="meta-chip">{{ pet.age }}{{ t('message.yearsOld') }}</span>
              <span class="meta-chip">{{ pet.gender }}</span>
              <span class="meta-chip">{{ pet.shelterName }}</span>
            </div>

            <div class="pet-card__actions">
              <el-button type="primary" @click="adopt(pet)">{{ t('nav.adoptNow') }}</el-button>
              <el-button text @click="viewDetail(pet.pid)">{{ t('nav.viewDetails') }}</el-button>
            </div>
          </div>
        </WebCard>
      </div>

      <AppPagination
        v-if="pagination.total > pagination.pageSize"
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[6, 12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @pageChange="setPage"
        @sizeChange="setPageSize"
      />
    </section>
  </PageContainer>
  <PetDetailDialog
    v-if="detailVisible && selectedPetId"
    :visible="detailVisible"
    :pet-id="selectedPetId"
    @update:visible="handleDetailVisibleChange"
  />
</template>

<script setup lang="ts">
import { onMounted, ref, watch, reactive, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import WebCard from '@/shared/components/common/WebCard.vue'
import AppPagination from '@/shared/components/common/AppPagination.vue'
import { useAdoptablePetsStore } from '@/modules/pets/store/adoptable'
import { petsService } from '@/modules/pets/services/pets'
import type { AdoptablePet, BreedItem, SpeciesItem } from '@/modules/pets/types'
import PetDetailDialog from '@/modules/pets/components/PetDetailDialog.vue'
import dogImage from '@/assets/img/dog.jpg'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const adoptableStore = useAdoptablePetsStore()
const { items, pagination, loading } = storeToRefs(adoptableStore)
const detailVisible = ref(false)
const selectedPetId = ref<number | null>(null)

// 本地筛选状态
const localFilters = reactive({
  species: '',
  breed: '',
  gender: '',
  minAge: undefined as number | undefined,
  maxAge: undefined as number | undefined
})

// 搜索关键词
const searchKeyword = ref('')

const speciesLoading = ref(false)
const breedLoading = ref(false)
const speciesItems = ref<SpeciesItem[]>([])
const breedItems = ref<BreedItem[]>([])

// 物种选项（动态加载）
const speciesOptions = computed(() =>
  speciesItems.value.map(item => ({
    value: item.name,
    label: item.name,
  })),
)

// 性别选项
const genderOptions = [
  { value: '雌', label: t('message.female') },
  { value: '雄', label: t('message.male') }
]

// 品种选项（根据所选物种动态变化）
const breedOptions = computed(() => breedItems.value.map(item => item.name))

const resolveSpeciesId = (speciesName: string) => speciesItems.value.find(s => s.name === speciesName)?.id

const fetchSpeciesOptions = async () => {
  speciesLoading.value = true
  try {
    const res = await petsService.fetchSpecies()
    speciesItems.value = res.code === 200 && Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('[AdoptionPets] fetchSpecies failed', error)
    speciesItems.value = []
  } finally {
    speciesLoading.value = false
  }
}

const fetchBreedOptions = async () => {
  const speciesName = localFilters.species
  if (!speciesName) {
    breedItems.value = []
    return
  }
  const speciesId = resolveSpeciesId(speciesName)
  if (!speciesId) {
    breedItems.value = []
    return
  }
  breedLoading.value = true
  try {
    const res = await petsService.fetchBreedsBySpecies(speciesId)
    breedItems.value = res.code === 200 && Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('[AdoptionPets] fetchBreedsBySpecies failed', error)
    breedItems.value = []
  } finally {
    breedLoading.value = false
  }
}

onMounted(() => {
  adoptableStore.fetchList()
  void fetchSpeciesOptions()
})

const setPage = (page: number) => {
  adoptableStore.setPage(page)
}

const setPageSize = (size: number) => {
  adoptableStore.setPageSize(size)
}

const adopt = (pet: AdoptablePet) => {
  adoptableStore.adoptPet(pet)
}

const resolvePetImage = (pet: AdoptablePet) => pet.image || dogImage

const onPetImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = dogImage
}

const normalizeIdParam = (param: string | string[] | null | undefined): number | null => {
  const raw = Array.isArray(param) ? param[0] : param
  if (!raw) return null
  const value = Number(raw)
  return Number.isNaN(value) ? null : value
}

const syncDetailFromRoute = () => {
  const petId = normalizeIdParam(route.query.pet as any)
  if (petId) {
    selectedPetId.value = petId
    detailVisible.value = true
  } else {
    detailVisible.value = false
    selectedPetId.value = null
  }
}

watch(
  () => route.query.pet,
  () => syncDetailFromRoute(),
  { immediate: true },
)

const removePetQuery = () => {
  const query = { ...route.query }
  delete query.pet
  router.replace({ query })
}

const viewDetail = (pid: number) => {
  if (!pid) return
  const query = { ...route.query, pet: String(pid) }
  router.replace({ query })
}

const handleDetailVisibleChange = (value: boolean) => {
  detailVisible.value = value
  if (!value) {
    selectedPetId.value = null
    removePetQuery()
  }
}

// 处理物种变化
const handleSpeciesChange = async () => {
  // 清除品种选择
  localFilters.breed = ''
  await fetchBreedOptions()
  applyFilters()
}

// 应用筛选条件
const applyFilters = () => {
  const filterObj: Record<string, any> = {}
  
  if (localFilters.species) filterObj.species = localFilters.species
  if (localFilters.breed) filterObj.breed = localFilters.breed
  if (localFilters.gender) filterObj.gender = localFilters.gender
  if (localFilters.minAge !== undefined) filterObj.minAge = localFilters.minAge
  if (localFilters.maxAge !== undefined) filterObj.maxAge = localFilters.maxAge
  
  adoptableStore.setFilters(filterObj)
}

// 应用搜索
const applySearch = () => {
  if (searchKeyword.value) {
    // 将搜索关键词作为品种筛选条件
    localFilters.breed = searchKeyword.value
  } else {
    // 如果搜索框为空，清除品种筛选
    if (localFilters.breed === searchKeyword.value) {
      localFilters.breed = ''
    }
  }
  applyFilters()
}

// 清除所有筛选
const clearAllFilters = () => {
  // 重置本地筛选状态
  localFilters.species = ''
  localFilters.breed = ''
  localFilters.gender = ''
  localFilters.minAge = undefined
  localFilters.maxAge = undefined
  searchKeyword.value = ''
  breedItems.value = []
  
  // 清除store中的筛选条件
  adoptableStore.clearFilters()
}

// 监听筛选条件变化
watch(localFilters, () => {
  // 只有当不是通过搜索框触发的变化时才应用筛选
  if (!searchKeyword.value || localFilters.breed !== searchKeyword.value) {
    applyFilters()
  }
}, { deep: true })

// 监听年龄范围变化，添加验证
watch([() => localFilters.minAge, () => localFilters.maxAge], ([newMin, newMax]) => {
  if (newMin !== undefined && newMax !== undefined && newMin > newMax) {
    // 如果最小年龄大于最大年龄，交换它们
    localFilters.minAge = newMax
    localFilters.maxAge = newMin
  }
})
</script>

<style scoped>
.pets-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.filter-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 1.25rem;
  margin-bottom: 1rem;
}

.filter-row {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 1rem;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-row > * {
  flex: 1;
}

.age-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.age-range span {
  white-space: nowrap;
}

.pets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.35rem;
}

.pet-card {
  padding: 0;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.02);
  box-shadow: 0 22px 55px rgba(2, 6, 23, 0.18);
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.pet-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 30px 70px rgba(2, 6, 23, 0.22);
}

.pet-card__media {
  position: relative;
  height: 180px;
  cursor: pointer;
  border-bottom: 1px solid var(--app-border-color);
}

.pet-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(1.02);
  transition: transform 320ms ease;
  filter: saturate(1.02) contrast(1.02);
}

.pet-card:hover .pet-card__media img {
  transform: scale(1.07);
}

.pet-card__media::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(2, 6, 23, 0) 35%, rgba(2, 6, 23, 0.68));
  pointer-events: none;
}

.pet-card__media-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 0.9rem;
  z-index: 1;
}

.pet-card__status {
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(34, 197, 94, 0.14);
}

.pet-card__body {
  padding: 1.1rem 1.15rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.pet-card__header h3 {
  margin: 0;
  font-size: 1.15rem;
  letter-spacing: 0.01em;
}

.pet-card__header p {
  margin: 0.25rem 0 0;
  color: var(--app-text-secondary);
}

.pet-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  border: 1px solid var(--app-border-color);
  background: rgba(95, 111, 255, 0.08);
  color: var(--app-text-secondary);
  font-size: 0.85rem;
  font-weight: 600;
}

.pet-card__actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  justify-content: space-between;
}

.pet-card__actions :deep(.el-button--primary) {
  border-radius: 999px;
  padding: 0.55rem 1.15rem;
}

.pet-card__actions :deep(.el-button.is-text) {
  font-weight: 650;
}
</style>
