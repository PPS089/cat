<template>
  <PageContainer :title="t('nav.myPets')" :description="t('message.findYourPerfectCompanion')" padded>
    <template #actions>
      <el-button type="primary" @click="goAdoptPet">
        {{ t('user.goAdoptPet') }}
      </el-button>
    </template>

    <section v-loading="loading" class="pets-section">
      <el-empty v-if="!loading && !hasPets" :description="t('user.noAdoptionRecords')">
        <el-button type="primary" @click="goAdoptPet">{{ t('nav.adoptNow') }}</el-button>
      </el-empty>

      <div v-else class="pets-grid">
        <WebCard
          v-for="pet in pets"
          :key="pet.pid"
          class="pet-card"
        >
          <div class="pet-card__media">
            <img :src="pet.image || fallbackImage" :alt="pet.name" loading="lazy" @error="onImageError" />
            <el-tag :type="pet.status === 'FOSTERING' ? 'warning' : pet.status === 'FOSTER_PENDING' ? 'info' : 'success'" size="small" effect="dark">
              {{ getStatusText(pet.status) }}
            </el-tag>
          </div>
          <div class="pet-card__body">
            <header>
              <h3>{{ pet.name }}</h3>
              <p>{{ pet.breed }}</p>
            </header>
            <ul class="pet-card__meta">
              <li>{{ pet.age }} {{ t('message.yearsOld') }}</li>
              <li>{{ pet.gender }}</li>
              <li v-if="pet.adoptionDate">
                {{ t('message.adoptDate') }}：{{ formatDate(pet.adoptionDate) }}
              </li>
              <li v-if="pet.shelterName">
                {{ t('message.shelter') }}：{{ pet.shelterName }}
              </li>
            </ul>
            <div class="pet-card__actions">
              <el-button
                v-if="pet.status === 'FOSTERING'"
                type="warning"
                size="small"
                @click="confirmEndFoster(pet)"
              >
                {{ t('message.endFoster') }}
              </el-button>
              <el-button
                v-else
                size="small"
                :disabled="pet.status === 'FOSTER_PENDING'"
                @click="openFosterDialog(pet)"
              >
                {{ pet.status === 'FOSTER_PENDING' ? t('user.fosterPending') : t('message.startFoster') }}
              </el-button>
              <el-button size="small" text type="primary" @click="editPet(pet.pid)">
                {{ t('message.edit') }}
              </el-button>
            </div>
          </div>
        </WebCard>
      </div>

      <AppPagination
        v-if="pagination.total > pagination.pageSize"
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[6, 12, 24]"
        layout="total, sizes, prev, pager, next"
        @pageChange="setPage"
        @sizeChange="setPageSize"
      />
    </section>
  </PageContainer>

  <el-dialog
    v-model="dialogVisible"
    :title="t('message.startFoster')"
    width="420px"
  >
    <el-form label-width="120px">
      <el-form-item :label="t('message.selectShelter')">
        <el-select
          v-model="selectedShelter"
          :loading="sheltersLoading"
          :placeholder="t('message.selectShelter')"
          clearable
        >
          <el-option
            v-for="shelter in shelters"
            :key="shelter.sid"
            :label="`${shelter.shelterName} · ${shelter.shelterAddress}`"
            :value="shelter.sid"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeDialog">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="confirmingFoster" @click="submitFoster">
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </el-dialog>

  <PetEditorDialog
    v-if="editorVisible && editingPetId"
    :visible="editorVisible"
    :pet-id="editingPetId"
    @update:visible="handleEditorVisibleChange"
    @saved="handlePetSaved"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import WebCard from '@/shared/components/common/WebCard.vue'
import AppPagination from '@/shared/components/common/AppPagination.vue'
import dogImage from '@/assets/img/dog.jpg'
import { useMyPetsStore } from '@/modules/pets/store/myPets'
import type { AdoptedPet } from '@/modules/pets/types'
import PetEditorDialog from '@/modules/pets/components/PetEditorDialog.vue'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const myPetsStore = useMyPetsStore()
const { list, pagination, loading, hasPets, shelters, sheltersLoading } = storeToRefs(myPetsStore)

const pets = computed(() => list.value)
const fallbackImage = dogImage
const dialogVisible = ref(false)
const selectedPet = ref<AdoptedPet | null>(null)
const selectedShelter = ref<number | null>(null)
const confirmingFoster = ref(false)
const editorVisible = ref(false)
const editingPetId = ref<number | null>(null)

onMounted(() => {
  myPetsStore.fetchPets()
})

const normalizeIdParam = (param: string | string[] | null | undefined): number | null => {
  const raw = Array.isArray(param) ? param[0] : param
  if (!raw) return null
  const value = Number(raw)
  return Number.isNaN(value) ? null : value
}

const syncEditorFromRoute = () => {
  const nextId = normalizeIdParam(route.query.edit as any)
  if (nextId) {
    editingPetId.value = nextId
    editorVisible.value = true
  } else {
    editorVisible.value = false
    editingPetId.value = null
  }
}

watch(
  () => route.query.edit,
  () => syncEditorFromRoute(),
  { immediate: true },
)

const removeEditorQuery = () => {
  const query = { ...route.query }
  delete query.edit
  router.replace({ query })
}

const setPage = (page: number) => {
  myPetsStore.setPage(page)
}

const setPageSize = (size: number) => {
  myPetsStore.setPageSize(size)
}

const goAdoptPet = () => {
  router.push('/user/adoption-pets')
}

const editPet = (pid: number) => {
  if (!pid) return
  const query = { ...route.query, edit: String(pid) }
  router.replace({ query })
}

const getStatusText = (status: string) => {
  if (status === 'FOSTERING') return t('user.fostering')
  if (status === 'FOSTER_PENDING') return t('user.fosterPending')
  return t('user.adoptionStatusNormal')
}

const openFosterDialog = async (pet: AdoptedPet) => {
  selectedPet.value = pet
  selectedShelter.value = null
  await myPetsStore.fetchShelters()
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  selectedPet.value = null
  selectedShelter.value = null
}

const submitFoster = async () => {
  if (!selectedPet.value || !selectedShelter.value) {
    ElMessage.warning(t('common.pleaseSelectShelter'))
    return
  }
  confirmingFoster.value = true
  const success = await myPetsStore.startFoster(selectedPet.value.pid, selectedShelter.value)
  confirmingFoster.value = false
  if (success) {
    closeDialog()
  }
}

const confirmEndFoster = async (pet: AdoptedPet) => {
  try {
    await ElMessageBox.confirm(t('message.confirmEndFoster'), t('message.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
    await myPetsStore.endFoster(pet.pid)
  } catch {
    // user canceled
  }
}

const formatDate = (value?: string) => {
  if (!value) return ''
  return new Date(value).toLocaleDateString()
}

const onImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = fallbackImage
}

const handleEditorVisibleChange = (value: boolean) => {
  editorVisible.value = value
  if (!value) {
    editingPetId.value = null
    removeEditorQuery()
  }
}

const handlePetSaved = () => {
  myPetsStore.fetchPets()
}
</script>

<style scoped>
.pets-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.pets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.5rem;
}

.pet-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.pet-card__media {
  position: relative;
}

.pet-card__media img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 12px;
}

.pet-card__media .el-tag {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
}

.pet-card__body header {
  margin-bottom: 0.5rem;
}

.pet-card__body h3 {
  margin: 0;
  font-size: 1.25rem;
}

.pet-card__body p {
  margin: 0;
  color: var(--app-text-secondary);
}

.pet-card__meta {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  padding: 0;
  margin: 0 0 1rem;
  list-style: none;
  color: var(--app-text-secondary);
}

.pet-card__actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
</style>
