<template>
  <PageContainer :title="t('nav.myAdoptions')" :description="t('user.viewAdoptionHistory')">
    <section v-loading="loading" class="adoptions-section">
      <div class="filter-bar">
        <el-segmented
          v-model="filterMode"
          :options="[
            { label: t('user.adoptionStatusNormal') || '已通过', value: 'APPROVED' },
            { label: t('user.adoptionStatusPending') || '审核中', value: 'PENDING' },
            { label: t('user.adoptionStatusRejected') || '已拒绝', value: 'REJECTED' },
          ]"
          @change="handleFilterChange"
        />
      </div>
      <el-empty v-if="!loading && !filteredRecords.length" :description="t('user.noAdoptionRecords')">
        <el-button type="primary" @click="goAdoptPet">{{ t('user.browseAdoptablePets') }}</el-button>
      </el-empty>

      <div v-else class="adoptions-grid">
        <WebCard v-for="adoption in filteredRecords" :key="adoption.id" class="adoption-card">
          <div class="adoption-card__media">
            <img
              :src="adoption.pet.image || fallbackImage"
              :alt="adoption.pet.name"
              loading="lazy"
              @error="onImageError"
            />
          </div>
          <div class="adoption-card__body">
            <header>
              <h3>{{ adoption.pet.name }}</h3>
              <p>{{ adoption.pet.breed }}</p>
            </header>
            <ul>
              <li>{{ adoption.pet.age }} {{ t('message.yearsOld') }} · {{ adoption.pet.gender }}</li>
              <li>{{ t('message.shelter') }}：{{ adoption.shelter.sname || t('message.unknown') }}</li>
              <li v-if="adoption.adoptDate">{{ t('message.adoptDate') }}：{{ formatDate(adoption.adoptDate) }}</li>
              <li>{{ t('user.status') }}：{{ getStatusText(adoption.adoptionStatus || adoption.pet.status) }}</li>
              <li v-if="(adoption.adoptionStatus || '').toUpperCase() === 'REJECTED' && adoption.reviewNote">
                {{ t('user.rejectionReason') }}：{{ adoption.reviewNote }}
              </li>
            </ul>
            <div class="adoption-card__actions">
              <el-button size="small" type="primary" @click="viewTimeline(adoption)">
                {{ t('user.viewAdoptionTimeline') }}
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
        layout="total, sizes, prev, pager, next"
        :page-sizes="[8, 16, 24]"
        @pageChange="setPage"
        @sizeChange="setPageSize"
      />
    </section>
  </PageContainer>

  <el-dialog
    v-model="timelineVisible"
    :title="`${timelinePet.name || ''} ${t('user.adoptionTimeline')}`"
    width="520px"
  >
    <el-timeline v-loading="timelineLoading">
      <el-timeline-item
        v-for="(event, index) in timeline"
        :key="index"
        :type="resolveEventType(event.action)"
      >
        <div class="timeline-entry">
          <span class="timeline-entry__label">
            {{ getActionLabel(event.action) }}
            <template v-if="event.description">
              · {{ event.description }}
            </template>
          </span>
          <span class="timeline-entry__time">
            {{ formatDateTime(event.timestamp) }}
          </span>
        </div>
      </el-timeline-item>
    </el-timeline>
    <template #footer>
      <el-button @click="closeTimeline">{{ t('common.close') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import WebCard from '@/shared/components/common/WebCard.vue'
import AppPagination from '@/shared/components/common/AppPagination.vue'
import dogImage from '@/assets/img/dog.jpg'
import { useAdoptionsStore } from '@/modules/pets/store/adoptions'
import type { AdoptionRecord } from '@/modules/pets/types'

const { t } = useI18n()
const router = useRouter()
const adoptionsStore = useAdoptionsStore()
const {
  records,
  pagination,
  loading,
  timeline,
  timelineVisible,
  timelineLoading,
  timelinePet,
} = storeToRefs(adoptionsStore)

const fallbackImage = dogImage
const filterMode = ref<'APPROVED' | 'PENDING' | 'REJECTED'>('APPROVED')
const filteredRecords = computed(() => {
  const status = filterMode.value
  return records.value.filter(r => {
    const resolved = (r.adoptionStatus || r.pet.status || '').toUpperCase()
    return resolved === status
  })
})

onMounted(() => {
  adoptionsStore.setStatusFilter(filterMode.value)
})

const handleFilterChange = (value: 'APPROVED' | 'PENDING' | 'REJECTED') => {
  filterMode.value = value
  adoptionsStore.setStatusFilter(value)
}

const goAdoptPet = () => {
  router.push('/user/adoption-pets')
}

const setPage = (page: number) => {
  adoptionsStore.setPage(page)
}

const setPageSize = (size: number) => {
  adoptionsStore.setPageSize(size)
}

const getStatusText = (status?: string) => {
  const normalized = (status || '').toUpperCase()
  if (normalized === 'PENDING') return t('user.adoptionStatusPending') // 申请中
  if (normalized === 'APPROVED' || normalized === 'ADOPTED') return t('user.adoptionStatusNormal') // 领养成功
  if (normalized === 'REJECTED') return t('user.adoptionStatusRejected') // 已拒绝
  if (normalized === 'FOSTERING') return t('user.adoptionStatusFostering') // 寄养中
  return t('user.status')
}

const viewTimeline = (adoption: AdoptionRecord) => {
  adoptionsStore.fetchTimeline(adoption.pet.pid, adoption.pet.name)
}

const closeTimeline = () => {
  adoptionsStore.closeTimeline()
}

const resolveEventType = (action: string) => {
  if (action === 'foster_started') return 'warning'
  if (action === 'foster_ended') return 'success'
  return 'primary'
}

const getActionLabel = (action: string) => {
  const map: Record<string, string> = {
    adopted: t('user.adopted'),
    foster_started: t('user.fosterStart'),
    foster_ended: t('user.fosterEnded'),
  }
  return map[action] || action
}

const formatDate = (value?: string) => {
  if (!value) return ''
  return new Date(value).toLocaleDateString()
}

const formatDateTime = (value?: string) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes(),
  )}:${pad(date.getSeconds())}`
}

const onImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = fallbackImage
}
</script>

<style scoped>
.adoptions-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.filter-bar {
  display: flex;
  justify-content: flex-end;
}

.adoptions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.adoption-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.adoption-card__media img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 12px;
}

.adoption-card__body header {
  margin-bottom: 0.5rem;
}

.adoption-card__body h3 {
  margin: 0;
  font-size: 1.25rem;
}

.adoption-card__body p {
  margin: 0;
  color: var(--app-text-secondary);
}

.adoption-card__body ul {
  list-style: none;
  margin: 0 0 1rem;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  color: var(--app-text-secondary);
}

.adoption-card__actions {
  display: flex;
  justify-content: flex-end;
}

.timeline-entry {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  color: var(--dialog-text, var(--app-text-color, #1f2937));
}

.timeline-entry__label {
  font-size: 0.95rem;
}

.timeline-entry__time {
  font-size: 0.85rem;
  color: var(--app-text-secondary, #64748b);
}

:global(.el-dialog__body .el-timeline-item) {
  padding-bottom: 1.25rem;
}
</style>
