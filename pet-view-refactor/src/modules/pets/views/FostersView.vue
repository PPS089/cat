<template>
  <PageContainer :title="t('user.fosterRecords')" :description="t('user.viewFosterHistory')">
    <section v-loading="loading" class="foster-section">
      <div class="filter-bar">
        <el-segmented
          v-model="filterMode"
          :options="[
            { label: t('user.fosterInProgress') || '寄养中', value: 'ONGOING' },
            { label: t('user.pendingApproval') || '待审核', value: 'PENDING' },
            { label: t('user.onlyRejected') || '已拒绝', value: 'REJECTED' },
            { label: t('common.all') || '全部', value: 'ALL' },
          ]"
          @change="handleFilterChange"
        />
      </div>
      <el-empty v-if="!loading && !filteredRecords.length" :description="t('user.noFosterRecords')" />

      <div v-else class="foster-grid">
        <WebCard v-for="record in filteredRecords" :key="record.id" class="foster-card">
          <div class="foster-card__media">
            <img :src="record.pet.image || fallbackImage" :alt="record.pet.name" loading="lazy" @error="onImageError" />
            <el-tag :type="getStatusTagType(record.status)" effect="dark" size="small">
              {{ getStatusLabel(record.status) }}
            </el-tag>
          </div>
          <div class="foster-card__body">
            <header>
              <h3>{{ record.pet.name }}</h3>
              <p>{{ record.pet.breed }}</p>
            </header>
            <ul>
              <li>{{ record.pet.age }} {{ t('message.yearsOld') }} · {{ record.pet.gender }}</li>
              <li>{{ t('user.fosterStart') }}：{{ formatDate(record.createTime) }}</li>
              <li>{{ t('user.status') }}：{{ getStatusLabel(record.status) }}</li>
              <li v-if="!isActiveStatus(record.status) && record.updateTime">
                {{ t('user.fosterEnd') }}：{{ formatDate(record.updateTime) }}
              </li>
              <li v-if="record.status === 'REJECTED' && record.reviewNote">
                {{ t('user.rejectionReason') }}：{{ record.reviewNote }}
              </li>
              <li>{{ t('user.shelter') }}：{{ record.shelter?.name || t('user.unknown') }}</li>
            </ul>
            <div class="foster-card__actions">
              <template v-if="isActiveStatus(record.status)">
                <el-tooltip :content="t('user.cannotDeleteActiveFoster')" placement="top">
                  <span>
                    <el-button type="danger" size="small" disabled>
                      {{ t('user.deleteRecord') }}
                    </el-button>
                  </span>
                </el-tooltip>
              </template>
              <el-button
                v-else
                type="danger"
                size="small"
                @click="removeRecord(record)"
              >
                {{ t('user.deleteRecord') }}
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
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @pageChange="setPage"
        @sizeChange="setPageSize"
      />
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import WebCard from '@/shared/components/common/WebCard.vue'
import AppPagination from '@/shared/components/common/AppPagination.vue'
import dogImage from '@/assets/img/dog.jpg'
import { useFostersStore } from '@/modules/pets/store/fosters'
import type { FosterRecord } from '@/modules/pets/types'

const { t } = useI18n()
const fostersStore = useFostersStore()
const { records, pagination, loading } = storeToRefs(fostersStore)
const fallbackImage = dogImage
const filterMode = ref<'ONGOING' | 'PENDING' | 'REJECTED' | 'ALL'>('ONGOING')
const filteredRecords = computed(() => {
  const status = filterMode.value
  if (status === 'ALL') return records.value
  return records.value.filter(r => r.status === status)
})

onMounted(() => {
  fostersStore.setStatusFilter(filterMode.value)
})

const handleFilterChange = (value: 'ONGOING' | 'PENDING' | 'REJECTED' | 'ALL') => {
  filterMode.value = value
  fostersStore.setStatusFilter(value)
}

const setPage = (page: number) => {
  fostersStore.setPage(page)
}

const setPageSize = (size: number) => {
  fostersStore.setPageSize(size)
}

const formatDate = (value?: string | null) => {
  if (!value) return ''
  return new Date(value).toLocaleDateString()
}

const removeRecord = (record: FosterRecord) => {
  if (!isEndedStatus(record.status)) {
    ElMessage.warning(t('user.cannotDeleteActiveFoster'))
    return
  }
  fostersStore.deleteRecord(record.id)
}

const onImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = fallbackImage
}

type FosterStatus = FosterRecord['status']

const isActiveStatus = (status: FosterStatus) =>
  status === 'APPROVED' || status === 'ONGOING' || status === 'PENDING'
const isEndedStatus = (status: FosterStatus) =>
  status === 'COMPLETED' || status === 'REJECTED'

const getStatusLabel = (status: FosterStatus) => {
  const map: Record<FosterStatus, string> = {
    PENDING: t('user.pendingApproval'),
    APPROVED: t('user.approved'),
    ONGOING: t('user.ongoing'),
    COMPLETED: t('user.fosterEnded'),
    REJECTED: t('user.rejected'),
  }
  return map[status] || status
}

const getStatusTagType = (status: FosterStatus) => {
  if (status === 'PENDING') return 'info'
  if (status === 'APPROVED' || status === 'ONGOING') return 'warning'
  if (status === 'REJECTED') return 'danger'
  if (status === 'COMPLETED') return 'success'
  return 'info'
}
</script>

<style scoped>
.foster-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.filter-bar {
  display: flex;
  justify-content: flex-end;
}

.foster-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.foster-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.foster-card__media {
  position: relative;
}

.foster-card__media img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 12px;
}

.foster-card__media .el-tag {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
}

.foster-card__body header {
  margin-bottom: 0.5rem;
}

.foster-card__body h3 {
  margin: 0;
}

.foster-card__body p {
  margin: 0;
  color: var(--app-text-secondary);
}

.foster-card__body ul {
  list-style: none;
  padding: 0;
  margin: 0 0 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  color: var(--app-text-secondary);
}

.foster-card__actions {
  display: flex;
  justify-content: flex-end;
}
</style>
