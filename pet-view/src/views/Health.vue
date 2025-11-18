<template>
  <div class="health-alerts-container" :class="{ 'dark-theme': themeStore.preferences.theme === 'dark' }">
    <div class="page-header">
      <h1>{{ t('healthAlerts.pageTitle') }}</h1>
      <p>{{ t('healthAlerts.description') }}</p>
    </div>

    <!-- 统计卡片 - 清晰的状态分类 -->
    <div class="stats-grid">
      <div class="stat-card total">
        <div class="stat-icon">📄</div>
        <div class="stat-content">
          <h3>{{ totalAlerts }}</h3>
          <p>{{ t('healthAlerts.totalRecords') }}</p>
        </div>
      </div>
      <div class="stat-card attention">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <h3>{{ attentionAlerts.length }}</h3>
          <p>{{ t('healthAlerts.attentionStatus') }}</p>
        </div>
      </div>
      <div class="stat-card expired">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <h3>{{ expiredAlerts.length }}</h3>
          <p>{{ t('healthAlerts.expiredStatus') }}</p>
        </div>
      </div>
      <div class="stat-card reminded">
        <div class="stat-icon">🔔</div>
        <div class="stat-content">
          <h3>{{ remindedAlerts.length }}</h3>
          <p>{{ t('healthAlerts.remindedStatus') }}</p>
        </div>
      </div>
    </div>

    <!-- 筛选和添加按钮 -->
    <div class="controls-section">
      <div class="filter-group">
        <select v-model="selectedPet" class="filter-select">
          <option value="">{{ t('healthAlerts.allPets') }}</option>
          <option v-for="pet in pets" :key="pet.pid" :value="pet.pid">
            {{ pet.name }}
          </option>
        </select>
        <select v-model="selectedHealthType" class="filter-select">
          <option value="">{{ t('healthAlerts.allTypes') }}</option>
          <option value="VACCINE">{{ t('healthAlerts.vaccine') }}</option>
          <option value="CHECKUP">{{ t('healthAlerts.checkup') }}</option>
          <option value="SURGERY">{{ t('healthAlerts.surgery') }}</option>
          <option value="DISEASE">{{ t('healthAlerts.disease') }}</option>
        </select>
        <select v-model="selectedStatus" class="filter-select">
          <option value="">{{ t('healthAlerts.allStatus') }}</option>
          <option value="pending">{{ t('healthAlerts.pending') }}</option>
          <option value="attention">{{ t('healthAlerts.attention') }}</option>
          <option value="expired">{{ t('healthAlerts.expired') }}</option>
          <option value="reminded">{{ t('healthAlerts.reminded') }}</option>
        </select>
      </div>
      <button class="add-button" @click="() => { closeModal(); showAddModal = true }">
        <span>+</span> {{ t('healthAlerts.addHealthRecord') }}
      </button>
    </div>

    <!-- 健康记录列表 -->
    <div class="alerts-section">
      <div class="section-header">
        <h2>{{ t('healthAlerts.title') }}</h2>
        <div class="view-toggle">
          <button 
            :class="{ active: viewMode === 'list' }" 
            @click="viewMode = 'list'"
          >
            {{ t('healthAlerts.listView') }}
          </button>
          <button 
            :class="{ active: viewMode === 'timeline' }" 
            @click="viewMode = 'timeline'"
          >
            {{ t('healthAlerts.timelineView') }}
          </button>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-if="viewMode === 'list' && filteredAlerts.length > 0" class="alerts-list">
        <div 
          v-for="alert in filteredAlerts" 
          :key="alert.healthId || alert.hid"
          class="alert-card"
          :class="getStatusClass(alert.status)"
        >
          <div class="alert-header">
            <div class="pet-info">
              <h4>{{ getPetName(alert.pid) }}</h4>
              <span v-if="alert.healthType || alert.alertType" class="health-type" :class="((alert.healthType || alert.alertType) || '').toLowerCase()">
                {{ getHealthTypeLabel(alert.healthType || alert.alertType) }}
              </span>
              <span v-else class="health-type unclassified">
                {{ getHealthTypeLabel(null) }}
              </span>
            </div>
            <div class="alert-status" :class="(alert.status || '').toLowerCase()">
              {{ getStatusLabel(alert.status) }}
            </div>
          </div>
          
          <div class="alert-content">
            <p class="description">{{ alert.description || '(未填写)' }}</p>
            <div class="alert-meta">
              <span class="check-date">
                <strong>{{ t('healthAlerts.checkTime') }}:</strong> {{ formatDate(alert.checkDate) }}
              </span>
              <span v-if="alert.reminderTime" class="reminder-time">
                <strong>{{ t('healthAlerts.reminderTime') }}:</strong> {{ formatDate(alert.reminderTime) }}
              </span>
              <span v-if="alert.updatedAt && alert.updatedAt !== alert.checkDate" class="updated-time">
                <strong>{{ t('records.updatedAt') }}:</strong> {{ formatDate(alert.updatedAt) }}
              </span>
            </div>
          </div>

          <div class="alert-actions">
            <button 
              class="action-btn edit-btn"
              @click="editAlert(alert)"
            >
              {{ t('healthAlerts.editRecord') }}
            </button>
            <button 
              class="action-btn delete-btn"
              @click="deleteAlert(alert.healthId || alert.hid)"
            >
              {{ t('healthAlerts.deleteRecord') }}
            </button>
          </div>
        </div>
      </div>

      <!-- 空状态提示 -->
      <div v-else-if="viewMode === 'list' && filteredAlerts.length === 0" class="empty-state">
        <p>📄 {{ t('healthAlerts.noRecords') }}</p>
      </div>

      <!-- 时间线视图 -->
      <div v-else-if="viewMode === 'timeline' && Object.keys(groupedAlerts).length > 0" class="timeline-view">
        <div class="timeline">
          <div 
            v-for="(alerts, date) in groupedAlerts" 
            :key="date"
            class="timeline-group"
          >
            <div class="timeline-date">{{ formatDate(date) }}</div>
            <div class="timeline-items">
              <div 
                v-for="alert in alerts" 
                :key="alert.healthId || alert.hid"
                class="timeline-item"
                :class="getStatusClass(alert.status)"
              >
                <div class="timeline-marker"></div>
                <div class="timeline-content">
                  <div class="timeline-header">
                    <h4>{{ getPetName(alert.pid) }}</h4>
                    <span v-if="alert.healthType || alert.alertType" class="health-type" :class="((alert.healthType || alert.alertType) || '').toLowerCase()">
                      {{ getHealthTypeLabel(alert.healthType || alert.alertType) }}
                    </span>
                    <span v-else class="health-type unclassified">
                      {{ getHealthTypeLabel(null) }}
                    </span>
                  </div>
                  <p class="description">{{ alert.description || t('common.noData') }}</p>
                  <div class="timeline-status">
                    <span :class="(alert.status || '').toLowerCase()">
                      {{ getStatusLabel(alert.status) }}
                    </span>
                  </div>
                  <div v-if="alert.reminderTime" class="timeline-reminder">
                    <strong>{{ t('healthAlerts.reminderTime') }}:</strong> {{ formatDate(alert.reminderTime) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 时间线空状态 -->
      <div v-else class="empty-state">
        <p>📄 {{ t('healthAlerts.noRecords') }}</p>
      </div>
    </div>

    <!-- 添加/编辑模态框 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ showEditModal ? t('healthAlerts.editHealthRecord') : t('healthAlerts.addHealthRecord') }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        
        <form @submit.prevent="saveAlert" class="modal-form">
          <div class="form-group">
            <label>{{ t('healthAlerts.petName') }}<span class="required">*</span></label>
            <select v-model="formData.pid" required class="form-select">
              <option value="">{{ t('healthAlerts.selectPet') }}</option>
              <option v-for="pet in pets" :key="pet.pid" :value="pet.pid">
                {{ pet.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>{{ t('healthAlerts.healthType') }}<span class="required">*</span></label>
            <select v-model="formData.healthType" required class="form-select">
              <option value="">{{ t('healthAlerts.selectType') }}</option>
              <option value="VACCINE">{{ t('healthAlerts.vaccine') }}</option>
              <option value="CHECKUP">{{ t('healthAlerts.checkup') }}</option>
              <option value="SURGERY">{{ t('healthAlerts.surgery') }}</option>
              <option value="DISEASE">{{ t('healthAlerts.disease') }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>{{ t('healthAlerts.checkTime') }}<span class="required">*</span></label>
            <input 
              type="datetime-local" 
              v-model="formData.checkDate" 
              required 
              class="form-input"
            >
          </div>

          <div class="form-group">
                <label>{{ t('healthAlerts.healthStatus') }}<span class="required">*</span></label>
                <select v-model="formData.status" required class="form-select">
                  <option value="attention">{{ t('healthAlerts.attention') }}</option>
                  <option value="expired">{{ t('healthAlerts.expired') }}</option>
                  <option value="reminded">{{ t('healthAlerts.reminded') }}</option>
                </select>
              </div>

          <div class="form-group">
            <label>{{ t('healthAlerts.descriptionLabel') }}<span class="required">*</span></label>
            <textarea 
              v-model="formData.description" 
              :placeholder="t('healthAlerts.descriptionPlaceholder')"
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>

          <div class="form-group" v-if="formData.status === 'attention'">
            <label>{{ t('healthAlerts.reminderTime') }}</label>
            <input 
              type="datetime-local" 
              v-model="formData.reminderTime" 
              class="form-input"
            >
            <small class="form-help">{{ t('healthAlerts.reminderTimeHelp') }}</small>
          </div>

          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeModal">
              {{ t('healthAlerts.cancel') }}
            </button>
            <button type="submit" class="save-btn">
              {{ showEditModal ? t('healthAlerts.update') : t('healthAlerts.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useHealth } from '@/api/health'
import { useWebSocket } from '@/composables/useWebSocket'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'

const { t } = useI18n()
const themeStore = useThemeStore()

// ... 使用组合式函数
const {
  healthAlerts,
  pets,
  loading,
  selectedPet,
  selectedHealthType,
  selectedStatus,
  viewMode,
  showAddModal,
  showEditModal,
  formData,
  totalAlerts,
  attentionAlerts,
  expiredAlerts,
  remindedAlerts,
  filteredAlerts,
  groupedAlerts,
  getPetName,
  getHealthTypeLabel,
  getStatusLabel,
  getStatusClass,
  formatDate,
  editAlert,
  deleteAlert,
  saveAlert,
  closeModal,
  initializeHealthAlerts
} = useHealth()

const { createWebSocketConnection, closeWebSocketConnection, syncHealthAlertsToStorage } = useWebSocket()

onMounted(async () => {
  await initializeHealthAlerts()
  syncHealthAlertsToStorage(healthAlerts.value)
  
  const userStore = useUserStore()
  if (userStore.info.userId) {
    createWebSocketConnection(userStore.info.userId)
  }
})

watch(healthAlerts, (newAlerts) => {
  console.log('健康提醒数据变化，同步到本地存储，新数量:', newAlerts.length)
  syncHealthAlertsToStorage(newAlerts)
}, { deep: true })

onUnmounted(() => {
  closeWebSocketConnection()
})
</script>

<style scoped>
@import '@/styles/Health.css'
</style>
