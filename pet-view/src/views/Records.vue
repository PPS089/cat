<template>
  <div class="events-container" :class="{ 'dark-theme': themeStore.preferences.theme === 'dark' }">
    <div class="page-header">
      <h1>{{ t('records.pageTitle') }}</h1>
      <p>{{ t('records.pageSubtitle') }}</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📅</div>
        <div class="stat-content">
          <h3>{{ totalEvents }}</h3>
          <p>{{ t('records.totalEvents') }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🐕</div>
        <div class="stat-content">
          <h3>{{ pets.length }}</h3>
          <p>{{ t('records.totalPets') }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📸</div>
        <div class="stat-content">
          <h3>{{ totalMedia }}</h3>
          <p>{{ t('records.totalMedia') }}</p>
        </div>
      </div>
    </div>

    <!-- 控制栏 -->

    <!-- 控制栏 -->
    <div class="controls-section">
      <div class="filter-group">
        <div style="position: relative;">
          <select v-model="filters.selectedPet" class="filter-select" :disabled="pets.length === 0">
            <option value="">{{ pets.length === 0 ? t('records.loading') : t('records.allPets') }}</option>
            <option v-for="pet in pets" :key="pet.pid" :value="pet.pid">
              {{ pet.name }}
            </option>
          </select>
          <small v-if="pets.length > 0" style="position: absolute; top: -20px; left: 0; color: #666;">
            {{ t('records.foundPets', { count: pets.length }) }}
          </small>
        </div>
        <select v-model="filters.selectedEventType" class="filter-select">
          <option value="">{{ t('records.allEvents') }}</option>
          <option value="喂食">{{ t('records.feeding') }}</option>
          <option value="洗澡">{{ t('records.bathing') }}</option>
          <option value="散步">{{ t('records.walking') }}</option>
          <option value="看兽医">{{ t('records.veterinary') }}</option>
          <option value="玩耍">{{ t('records.playing') }}</option>
          <option value="打疫苗">{{ t('records.vaccination') }}</option>
        </select>
        <select v-model="filters.selectedMood" class="filter-select">
          <option value="">{{ t('records.allMood') }}</option>
          <option value="开心">{{ t('records.happy') }}</option>
          <option value="生气">{{ t('records.angry') }}</option>
          <option value="疲倦">{{ t('records.tired') }}</option>
          <option value="活跃">{{ t('records.active') }}</option>
        </select>
        <input 
          type="date" 
          v-model="filters.selectedDate" 
          class="filter-input"
          :placeholder="t('records.selectDate')"
        >
      </div>
      <button class="add-button" @click="showAddModal = true">
        <span>+</span> {{ t('records.addEvent') }}
      </button>
    </div>

    <!-- 视图切换 -->
    <div class="view-controls">
      <div class="view-toggle">
        <button 
          :class="{ active: viewMode === 'grid' }" 
          @click="viewMode = 'grid'"
        >
          {{ t('records.gridView') }}
        </button>
        <button 
          :class="{ active: viewMode === 'list' }" 
          @click="viewMode = 'list'"
        >
          {{ t('records.listView') }}
        </button>
        <button 
          :class="{ active: viewMode === 'timeline' }" 
          @click="viewMode = 'timeline'"
        >
          {{ t('records.timeline') }}
        </button>
      </div>
      <div class="sort-controls">
        <select v-model="sortBy" class="sort-select">
          <option value="record_time">{{ t('records.sortByTime') }}</option>
          <option value="event_type">{{ t('records.sortByType') }}</option>
          <option value="mood">{{ t('records.sortByMood') }}</option>
        </select>
      </div>
    </div>

    <!-- 事件内容 -->
    <div class="events-content">
      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="events-grid">
        <div 
          v-for="event in sortedEvents" 
          :key="event.record_id"
          class="event-card"
          :class="event.mood ? getMoodClass(event.mood) : {}"
        >
          <div class="event-header">
            <div class="pet-info">
              <h4>{{ getPetName(event.pid, event.pet_name) }}</h4>
              <span class="event-type">{{ event.event_type }}</span>
            </div>
            <div class="event-mood" v-if="event.mood">
              <span class="mood-emoji">{{ getMoodEmoji(event.mood) }}</span>
              <span class="mood-text">{{ event.mood }}</span>
            </div>
          </div>

          <div class="event-content">
            <p class="description">{{ event.description }}</p>
            <div v-if="event.location" class="location">
              <span class="location-icon">📍</span>
              {{ event.location }}
            </div>
          </div>

          <div class="event-media" v-if="event.media_list && event.media_list.length > 0">
            <div class="media-preview">
              <div 
                v-for="media in event.media_list.slice(0, 3)" 
                :key="media.id"
                class="media-item"
              >
                <img 
                  v-if="media.media_type === 'image'" 
                  :src="media.media_url" 
                  :alt="event.event_type"
                  @click="openMediaModal([], event.record_id)"
                  style="cursor: pointer;"
                >
                <video 
                  v-else-if="media.media_type === 'video'" 
                  :src="media.media_url"
                  @click="openMediaModal([], event.record_id)"
                  style="cursor: pointer;"
                ></video>
              </div>
              <div 
                v-if="event.media_list.length > 3" 
                class="media-count"
                @click="openMediaModal([], event.record_id)"
                style="cursor: pointer;"
              >
                +{{ event.media_list.length - 3 }}
              </div>
            </div>
          </div>

          <div class="event-footer">
            <div class="event-time">
              <span class="record-time">{{ formatDate(event.record_time) }}</span>
              <span class="created-time">{{ t('records.recordedAt') }} {{ formatDate(event.created_at) }}</span>
            </div>
            <div class="event-actions">
              <button class="action-btn edit-btn" @click="editEvent(event)">
                {{ t('records.edit') }}
              </button>
              <button class="action-btn delete-btn" @click="deleteEvent(event.record_id)" :disabled="!event.record_id">
                {{ t('records.delete') }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else-if="viewMode === 'list'" class="events-list">
        <div class="list-header">
          <div class="col-pet">{{ t('records.pet') }}</div>
          <div class="col-type">{{ t('records.eventType') }}</div>
          <div class="col-mood">{{ t('records.mood') }}</div>
          <div class="col-description">{{ t('records.description') }}</div>
          <div class="col-time">{{ t('records.time') }}</div>
          <div class="col-actions">{{ t('records.actions') }}</div>
        </div>
        <div 
          v-for="event in sortedEvents" 
          :key="event.record_id"
          class="list-item"
          :class="event.mood ? getMoodClass(event.mood) : {}"
        >
          <div class="col-pet">
            <div class="pet-info">
              <h4>{{ getPetName(event.pid, event.pet_name) }}</h4>
            </div>
          </div>
          <div class="col-type">
            <span class="event-type-badge">{{ event.event_type }}</span>
          </div>
          <div class="col-mood">
            <span v-if="event.mood" class="mood-indicator">
              <span class="mood-emoji">{{ getMoodEmoji(event.mood) }}</span>
              {{ event.mood }}
            </span>
          </div>
          <div class="col-description">
            <p class="description-text">{{ event.description }}</p>
            <div v-if="event.location" class="location-small">
              📍 {{ event.location }}
            </div>
          </div>
          <div class="col-time">
            <span class="time-text">{{ formatDate(event.record_time) }}</span>
          </div>
          <div class="col-actions">
            <button class="action-btn edit-btn" @click="editEvent(event)">
              {{ t('records.edit') }}
            </button>
            <button class="action-btn delete-btn" @click="deleteEvent(event.record_id)" :disabled="!event.record_id">
                {{ t('records.delete') }}
              </button>
          </div>
        </div>
      </div>

      <!-- 时间线视图 -->
      <div v-else class="timeline-view">
        <div class="timeline">
          <div 
            v-for="(events, date) in groupedEvents" 
            :key="date"
            class="timeline-group"
          >
            <div class="timeline-date">{{ formatDate(date) }}</div>
            <div class="timeline-items">
              <div 
                v-for="event in events" 
                :key="event.record_id"
                class="timeline-item"
                :class="event.mood ? getMoodClass(event.mood) : {}"
              >
                <div class="timeline-marker"></div>
                <div class="timeline-content">
                  <div class="timeline-header">
                    <h4>{{ getPetName(event.pid, event.pet_name) }}</h4>
                    <span class="event-type-badge">{{ event.event_type }}</span>
                    <span v-if="event.mood" class="mood-indicator">
                      <span class="mood-emoji">{{ getMoodEmoji(event.mood) }}</span>
                      {{ event.mood }}
                    </span>
                  </div>
                  <p class="description">{{ event.description }}</p>
                  <div v-if="event.location" class="location">
                    📍 {{ event.location }}
                  </div>
                  <div class="timeline-footer">
                    <span class="time-text">{{ formatDate(event.record_time) }}</span>
                    <div class="timeline-actions">
                      <button class="action-btn edit-btn" @click="editEvent(event)">
                        {{ t('records.edit') }}
                      </button>
                      <button class="action-btn delete-btn" @click="deleteEvent(event.record_id)" :disabled="!event.record_id">
                        {{ t('records.delete') }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑模态框 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ showEditModal ? t('records.editEvent') : t('records.addEvent') }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        
        <form @submit.prevent="saveEvent" class="modal-form">
          <div class="form-group">
            <label>{{ t('records.pet') }}</label>
            <select v-model="formData.pid" required class="form-select">
              <option value="">{{ t('records.selectPet') }}</option>
              <option v-for="pet in pets" :key="pet.pid" :value="pet.pid">
                {{ pet.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>{{ t('records.eventType') }}</label>
            <select v-model="formData.event_type" required class="form-select">
              <option value="">{{ t('records.selectType') }}</option>
              <option value="喂食">{{ t('records.feeding') }}</option>
              <option value="洗澡">{{ t('records.bathing') }}</option>
              <option value="散步">{{ t('records.walking') }}</option>
              <option value="看兽医">{{ t('records.veterinary') }}</option>
              <option value="玩耍">{{ t('records.playing') }}</option>
              <option value="打疫苗">{{ t('records.vaccination') }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>{{ t('records.mood') }}</label>
            <select v-model="formData.mood" class="form-select">
              <option value="">{{ t('records.selectMood') }}</option>
              <option value="开心">{{ t('records.happy') }}</option>
              <option value="生气">{{ t('records.angry') }}</option>
              <option value="疲倦">{{ t('records.tired') }}</option>
              <option value="活跃">{{ t('records.active') }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>{{ t('records.eventTime') }}</label>
            <input 
              type="datetime-local" 
              v-model="formData.record_time" 
              required 
              class="form-input"
            >
          </div>

          <div class="form-group">
            <label>{{ t('records.location') }}</label>
            <input 
              type="text" 
              v-model="formData.location" 
              :placeholder="t('records.locationOptional')"
              class="form-input"
            >
          </div>

          <div class="form-group">
            <label>{{ t('records.detailedDescription') }}</label>
            <textarea 
              v-model="formData.description" 
              rows="4" 
              :placeholder="t('records.pleaseEnterDescription')"
              required
              class="form-textarea"
            ></textarea>
          </div>

          <div class="form-group">
            <label>{{ t('records.mediaFiles') }}</label>
            <div class="media-upload">
              <div class="upload-area">
                <input 
                  type="file" 
                  multiple 
                  accept="image/*,video/*" 
                  @change="handleFileUpload"
                  class="file-input"
                  id="media-file-input"
                >
                <label for="media-file-input" class="upload-label">
                  <div class="upload-icon">📁</div>
                  <div class="upload-text">
                    {{ t('records.clickToSelectOrDrag') }}
                  </div>
                  <div class="upload-hint">
                    {{ t('records.supportedFormats') }}
                  </div>
                </label>
              </div>
            </div>
            <div v-if="filePreviews.length > 0" class="uploaded-files">
              <div class="uploaded-files-title">{{ t('records.uploadedFiles') }} ({{ filePreviews.length }}/5)</div>
              <div class="file-preview-grid">
                <div v-for="(preview, index) in filePreviews" :key="index" class="file-preview-item">
                  <!-- 文件缩略 -->
                  <div class="preview-thumbnail">
                    <img 
                      v-if="preview.mediaType === 'image'" 
                      :src="preview.thumbnailUrl" 
                      :alt="preview.file.name"
                      class="preview-image"
                    >
                    <div v-else class="preview-video-placeholder">
                      <span class="video-icon">🎬</span>
                    </div>
                  </div>
                  
                  <!-- 文件信息 -->
                  <div class="file-info-full">
                    <div class="file-name-container">
                      <span class="file-icon">{{ getFileIcon(preview.mediaType) }}</span>
                      <span class="file-name" :title="preview.file.name">{{ preview.file.name }}</span>
                    </div>
                    <div class="file-size-type">
                      <span class="file-size">{{ formatFileSize(preview.file.size) }}</span>
                      <span class="file-type">{{ preview.mediaType === 'image' ? '(图片)' : '(视频)' }}</span>
                    </div>
                  </div>
                  
                  <!-- 删除按钮 -->
                  <button 
                    type="button" 
                    @click="removeFile(index)" 
                    class="remove-file-btn"
                    :title="t('records.deleteFile')"
                  >
                    ×
                  </button>
                </div>
              </div>
              <div class="upload-hint-text">
                {{ filePreviews.length >= 5 ? '已到达最大文件数限制' : `还可上传 ${5 - filePreviews.length} 个文件` }}
              </div>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeModal">
              {{ t('records.cancel') }}
            </button>
            <button type="submit" class="save-btn">
              {{ showEditModal ? t('records.update') : t('records.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 媒体查看模态框 -->
    <div v-if="showMediaModal" class="media-modal-overlay" @click="closeMediaModal">
      <div class="media-modal-content" @click.stop>
        <div class="media-modal-header">
          <h3>{{ t('records.mediaViewer') }}</h3>
          <button class="close-btn" @click="closeMediaModal">×</button>
        </div>
        <div v-if="mediaLoading" class="media-loading">
          <p>正在加载媒体文件...</p>
        </div>
        <div v-else-if="currentMediaList.length === 0" class="media-empty">
          <p>此事件没有媒体文件</p>
        </div>
        <div v-else class="media-carousel">
          <div v-for="(media, index) in currentMediaList" :key="index" class="media-item">
            <img 
              v-if="media.media_type === 'image'" 
              :src="media.media_url" 
              :alt="`Media ${index + 1}`"
            >
            <video 
              v-else-if="media.media_type === 'video'" 
              :src="media.media_url"
              controls
            ></video>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRecords } from '@/api/records'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'

const { t } = useI18n()

const userStore = useUserStore()
const themeStore = useThemeStore()

// 同步用户状态
  const syncUserState = () => {
    if (typeof window === 'undefined') return // SSR保护
    
    // 统一token键名，优先使用jwt_token，回退到token
    const token = localStorage.getItem('jwt_token') || localStorage.getItem('token')
    const username = localStorage.getItem('userName') || localStorage.getItem('username')
    const userId = localStorage.getItem('userId') || localStorage.getItem('userid')
    
    console.log('同步用户状态:', { token: !!token, username, userId })
    
    if (token && username && userId) {
      // 手动设置用户状态
      userStore.info.userName = username
      userStore.info.userId = parseInt(userId)
      console.log('用户状态同步成功:', { userName: username, userId: parseInt(userId) })
    } else {
      console.log('用户状态不完整，需要重新登录', { token, username, userId })
      // 清除不一致的状态
      if (!token) {
        localStorage.removeItem('userName')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('userid')
        userStore.info.userName = ''
        userStore.info.userId = 0
      }
    }
  }

// 使用组合式函数
const {
  // 数据状态
  pets,
  
  // 筛选状态
  filters,
  viewMode,
  sortBy,
  
  // 模态框状态
  showAddModal,
  showEditModal,
  showMediaModal,
  mediaLoading,
  
  // 表单数据
  formData,
  filePreviews,
  currentMediaList,
  
  // 计算属性
  totalEvents,
  totalMedia,
  sortedEvents,
  groupedEvents,
  
  // 方法
  fetchEvents,
  fetchPets,
  getPetName,
  getMoodEmoji,
  getMoodClass,
  formatDate,
  handleFileUpload,
  removeFile,
  getFileIcon,
  formatFileSize,
  openMediaModal,
  closeMediaModal,
  editEvent,
  deleteEvent,
  saveEvent,
  closeModal
} = useRecords()

// 生命周期
onMounted(async () => {
  syncUserState()
  
  // 检查是否真正登录（客户端环境）
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('jwt_token')
    if (!token) {
      // 可以在这里添加跳转到登录页的逻辑
      // router.push('/login')
      return
    }
  }
  
  try {
    // 先获取宠物数据
    await fetchPets()
    
    // 等待DOM更新确保宠物数据已反应到pets.value中
    await nextTick()
    
    // 再获取事件数据，这样事件映射时宠物数据已可用
    await fetchEvents()
  } catch (error) {
    console.error('records.vue: 数据获取失败:', error)
  }
})
</script>


<style scoped>
@import '../styles/records.css'
</style>
