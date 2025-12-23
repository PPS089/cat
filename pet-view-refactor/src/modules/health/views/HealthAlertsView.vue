<template>
  <PageContainer :title="t('nav.healthManagement')" :description="t('healthAlerts.subtitle')">
    <template #actions>
      <el-tooltip v-if="!pets.length" :content="t('message.pleaseAdoptPetInstead')" placement="bottom">
        <span>
          <el-button type="primary" disabled>+ {{ t('healthAlerts.addHealthRecord') }}</el-button>
        </span>
      </el-tooltip>
      <el-button v-else type="primary" @click="handleCreate">+ {{ t('healthAlerts.addHealthRecord') }}</el-button>
    </template>

    <section class="health-page" v-loading="loading">
      <WebCard class="stats-card">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">📄</div>
            <div>
              <p>{{ t('healthAlerts.totalRecords') }}</p>
              <h3>{{ stats.total }}</h3>
            </div>
          </div>
          <div class="stat-card attention">
            <div class="stat-icon">⚠️</div>
            <div>
              <p>{{ t('healthAlerts.attentionStatus') }}</p>
              <h3>{{ stats.attention }}</h3>
            </div>
          </div>
          <div class="stat-card expired">
            <div class="stat-icon">⏰</div>
            <div>
              <p>{{ t('healthAlerts.expiredStatus') }}</p>
              <h3>{{ stats.expired }}</h3>
            </div>
          </div>
          <div class="stat-card reminded">
            <div class="stat-icon">🔔</div>
            <div>
              <p>{{ t('healthAlerts.remindedStatus') }}</p>
              <h3>{{ stats.reminded }}</h3>
            </div>
          </div>
        </div>
      </WebCard>

      <WebCard class="filters-card">
        <div class="filters">
          <el-select v-model="filters.petId" :placeholder="t('healthAlerts.allPets')" clearable>
            <el-option :label="t('healthAlerts.allPets')" value="" />
            <el-option v-for="pet in pets" :key="pet.pid" :label="pet.name" :value="String(pet.pid)" />
          </el-select>
          <el-select v-model="filters.healthType" :placeholder="t('healthAlerts.allTypes')" clearable>
            <el-option :label="t('healthAlerts.allTypes')" value="" />
            <el-option v-for="option in healthTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
          <el-select v-model="filters.status" :placeholder="t('healthAlerts.allStatus')" clearable>
            <el-option :label="t('healthAlerts.allStatus')" value="" />
            <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </div>
        <div class="view-toggle">
          <button type="button" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
            {{ t('healthAlerts.listView') }}
          </button>
          <button type="button" :class="{ active: viewMode === 'timeline' }" @click="viewMode = 'timeline'">
            {{ t('healthAlerts.timelineView') }}
          </button>
        </div>
      </WebCard>

      <WebCard class="records-card">
        <div v-if="viewMode === 'list'">
          <div v-if="filteredAlerts.length" class="alert-list">
            <article
              v-for="alert in filteredAlerts"
              :key="alert.healthId"
              class="alert-card"
              :class="alert.status"
            >
              <header class="alert-header">
                <div>
                  <h4>{{ getPetName(alert.pid) }}</h4>
                  <span class="type-chip">{{ getHealthTypeLabel(alert.healthType) }}</span>
                </div>
                <el-tag size="small" :type="statusType(alert.status)">
                  {{ getStatusLabel(alert.status) }}
                </el-tag>
              </header>
              <p class="alert-description">{{ alert.description || t('common.noData') }}</p>
              <dl class="alert-meta">
                <div>
                  <dt>{{ t('healthAlerts.checkTime') }}</dt>
                  <dd>{{ formatDate(alert.checkDate) }}</dd>
                </div>
                <div v-if="alert.reminderTime">
                  <dt>{{ t('healthAlerts.reminderTime') }}</dt>
                  <dd>{{ formatDate(alert.reminderTime) }}</dd>
                </div>
              </dl>
              <footer class="alert-actions">
                <el-button text size="small" type="primary" @click="handleEdit(alert)">
                  {{ t('healthAlerts.editRecord') }}
                </el-button>
                <el-button text size="small" type="danger" @click="deleteAlert(alert.healthId)">
                  {{ t('healthAlerts.deleteRecord') }}
                </el-button>
              </footer>
            </article>
          </div>
          <el-empty v-else :description="t('healthAlerts.noRecords')" />
        </div>

        <div v-else>
          <div v-if="hasTimeline" class="timeline">
            <div v-for="(items, date) in groupedAlerts" :key="date" class="timeline-day">
              <div class="timeline-date">{{ formatDate(date) }}</div>
              <div class="timeline-items">
                <article
                  v-for="alert in items"
                  :key="alert.healthId"
                  class="timeline-item"
                  :class="alert.status"
                >
                  <div class="marker" />
                  <div class="timeline-content">
                    <header>
                      <h4>{{ getPetName(alert.pid) }}</h4>
                      <span class="type-chip">{{ getHealthTypeLabel(alert.healthType) }}</span>
                      <el-tag size="small" :type="statusType(alert.status)">
                        {{ getStatusLabel(alert.status) }}
                      </el-tag>
                    </header>
                    <p>{{ alert.description || t('common.noData') }}</p>
                    <small>{{ formatDate(alert.checkDate) }}</small>
                  </div>
                </article>
              </div>
            </div>
          </div>
          <el-empty v-else :description="t('healthAlerts.noRecords')" />
        </div>
      </WebCard>
    </section>
  </PageContainer>

  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="540px"
    :close-on-click-modal="false"
    @close="closeDialog"
  >
    <el-form label-width="110px">
      <el-form-item :label="t('healthAlerts.petName')">
        <el-select v-model="form.pid" :placeholder="t('healthAlerts.selectPet')">
          <el-option v-for="pet in pets" :key="pet.pid" :label="pet.name" :value="pet.pid" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('healthAlerts.healthType')">
        <el-select v-model="form.healthType" :placeholder="t('healthAlerts.selectType')">
          <el-option v-for="option in healthTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('healthAlerts.healthStatus')">
        <el-select v-model="form.status">
          <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('healthAlerts.checkTime')">
        <el-date-picker
          v-model="form.checkDate"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm"
          :placeholder="t('healthAlerts.selectCheckTime')"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item :label="t('healthAlerts.reminderTime')" v-if="showReminderField">
        <el-date-picker
          v-model="form.reminderTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm"
          :placeholder="t('healthAlerts.selectReminderTime')"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item :label="t('healthAlerts.descriptionLabel')">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          :placeholder="t('healthAlerts.descriptionPlaceholder')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeDialog">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" @click="saveAlert">
        {{ dialogMode === 'create' ? t('healthAlerts.save') : t('common.update') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import WebCard from '@/shared/components/common/WebCard.vue'
import { useHealthAlertsStore } from '@/modules/health/store/healthAlerts'

const { t } = useI18n()
const healthStore = useHealthAlertsStore()
const {
  stats,
  filteredAlerts,
  groupedAlerts,
  filters,
  viewMode,
  loading,
  dialogVisible,
  dialogMode,
  form,
  healthTypeOptions,
  statusOptions,
  showReminderField,
  pets,
  submitting,
} = storeToRefs(healthStore)

const hasTimeline = computed(() => Object.keys(groupedAlerts.value).length > 0)
const dialogTitle = computed(() =>
  dialogMode.value === 'create'
    ? t('healthAlerts.addHealthRecord')
    : t('healthAlerts.editHealthRecord'),
)

const handleCreate = () => healthStore.openCreate()
const handleEdit = (alert: any) => healthStore.openEdit(alert)

const formatDate = (value?: string | null) => healthStore.formatDate(value)
const getPetName = (pid: number) => healthStore.getPetName(pid)
const getHealthTypeLabel = (value: string) => {
  const option = healthTypeOptions.value.find(item => item.value === value)
  return option?.label || value || t('healthAlerts.noType')
}
const getStatusLabel = (value?: string | null) => healthStore.getStatusLabel(value)
const statusType = (status?: string | null) => {
  const map: Record<string, 'warning' | 'danger' | 'success' | 'info'> = {
    attention: 'warning',
    expired: 'danger',
    reminded: 'success',
    pending: 'info',
  }
  return map[status || ''] || 'info'
}

const { closeDialog, saveAlert, deleteAlert } = healthStore

onMounted(() => {
  healthStore.bootstrap()
  healthStore.ensureRealtime()
})

onBeforeUnmount(() => {
  healthStore.disposeRealtime()
})
</script>

<style scoped>
.health-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-md);
  background: var(--app-bg-color);
}

.stat-card h3 {
  margin: 0;
  font-size: 1.5rem;
}

.stat-card p {
  margin: 0;
  color: var(--app-text-secondary);
}

.filters-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.filters :deep(.el-select) {
  min-width: 200px;
}

.view-toggle {
  display: inline-flex;
  align-self: flex-end;
  border: 1px solid var(--app-border-color);
  border-radius: 999px;
  padding: 0.15rem;
  background: var(--app-bg-color);
  gap: 0.25rem;
}

.view-toggle button {
  border: none;
  background: transparent;
  padding: 0.35rem 1rem;
  border-radius: 999px;
  cursor: pointer;
  color: var(--app-text-secondary);
  font-weight: 600;
}

.view-toggle button.active {
  background: var(--app-primary);
  color: #fff;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.alert-card {
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-md);
  padding: 1rem;
  background: var(--app-surface-color);
  box-shadow: var(--app-shadow-card);
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.alert-header h4 {
  margin: 0;
}

.type-chip {
  display: inline-flex;
  align-items: center;
  margin-top: 0.25rem;
  padding: 0.1rem 0.75rem;
  border-radius: 999px;
  font-size: 0.75rem;
  background: rgba(95, 111, 255, 0.12);
  color: var(--app-primary);
}

.alert-description {
  margin: 0 0 0.75rem;
  color: var(--app-text-secondary);
}

.alert-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin: 0;
}

.alert-meta dt {
  font-weight: 600;
}

.alert-meta dd {
  margin: 0;
  color: var(--app-text-secondary);
}

.alert-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  margin-top: 0.75rem;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.timeline-day {
  border-left: 2px solid var(--app-border-color);
  padding-left: 1.5rem;
}

.timeline-date {
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--app-text-secondary);
}

.timeline-item {
  position: relative;
  margin-bottom: 1rem;
}

.timeline-item .marker {
  position: absolute;
  left: -1.55rem;
  top: 0.35rem;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--app-primary);
}

.timeline-content {
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-md);
  padding: 0.75rem;
  background: var(--app-surface-color);
}

.timeline-content header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.records-card {
  padding: 1.5rem;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
  }
  .view-toggle {
    align-self: stretch;
    justify-content: space-between;
  }
}
</style>
