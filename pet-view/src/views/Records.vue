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
              <span v-if="event.updated_at && event.updated_at !== event.created_at" class="updated-time">
                {{ t('records.updatedAt') }} {{ formatDate(event.updated_at) }}
              </span>
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
        
        <form class="modal-form">
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
                {{ filePreviews.length >= 5 ? t('common.maxFilesExceeded') : t('records.supportedFormats') }}
              </div>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeModal" :disabled="isSubmitting">
                  {{ t('records.cancel') }}
                </button>
                <button type="button" class="save-btn" @click="saveEvent" :disabled="isSubmitting">
                  {{ isSubmitting ? (showEditModal ? t('records.updating') : t('records.saving')) : (showEditModal ? t('records.update') : t('records.save')) }}
                </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 媒体查看模态框 -->
    <Teleport to="body">
      <Transition name="media-viewer">
        <div 
          v-if="showMediaModal" 
          class="media-viewer-overlay" 
          @click="closeMediaModal"
          @keydown.esc="closeMediaModal"
          @keydown.left="prevMedia"
          @keydown.right="nextMedia"
          tabindex="-1"
        >
          <div class="media-viewer-content" @click.stop>
            <!-- 关闭按钮 -->
            <button class="media-viewer-close" @click="closeMediaModal">×</button>
            
            <!-- 图片显示区域 -->
            <div class="media-viewer-main" v-if="currentMediaList.length > 0">
              <!-- 左箭头 -->
              <button 
                v-if="currentMediaList.length > 1" 
                class="media-nav-btn media-nav-prev"
                @click.stop="prevMedia"
                :aria-label="t('records.previousMedia')"
              >
                ‹
              </button>
              
              <!-- 媒体内容 -->
              <div class="media-display">
                <Transition name="media-fade" mode="out-in">
                  <img 
                    v-if="currentMediaList[currentMediaIndex]?.media_type === 'image'" 
                    :key="`img-${currentMediaIndex}`"
                    :src="currentMediaList[currentMediaIndex].media_url" 
                    :alt="currentMediaList[currentMediaIndex].media_name || `${t('records.media')} ${currentMediaIndex + 1}`"
                    class="media-image"
                  >
                  <video 
                    v-else-if="currentMediaList[currentMediaIndex]?.media_type === 'video'" 
                    :key="`video-${currentMediaIndex}`"
                    :src="currentMediaList[currentMediaIndex].media_url"
                    controls
                    class="media-video"
                    :autoplay="true"
                  ></video>
                </Transition>
              </div>
              
              <!-- 右箭头 -->
              <button 
                v-if="currentMediaList.length > 1" 
                class="media-nav-btn media-nav-next"
                @click.stop="nextMedia"
                :aria-label="t('records.nextMedia')"
              >
                ›
              </button>
            </div>
            
            <!-- 媒体信息和缩略图 -->
            <div class="media-viewer-footer" v-if="currentMediaList.length > 0">
              <div class="media-info">
                <h4>{{ currentMediaList[currentMediaIndex]?.media_name || `${t('records.media')} ${currentMediaIndex + 1}` }}</h4>
                <p v-if="currentMediaList[currentMediaIndex]?.created_at">
                  {{ t('records.createdAt') }}: {{ formatDate(currentMediaList[currentMediaIndex].created_at || '') }}
                </p>
                <p v-if="currentMediaList[currentMediaIndex]?.updated_at && currentMediaList[currentMediaIndex].updated_at !== (currentMediaList[currentMediaIndex].created_at || '')">
                  {{ t('records.updatedAt') }}: {{ formatDate(currentMediaList[currentMediaIndex].updated_at || '') }}
                </p>
                <p>
                  {{ t('records.mediaIndex', { current: currentMediaIndex + 1, total: currentMediaList.length }) }}
                </p>
              </div>
              
              <!-- 缩略图导航 -->
              <div class="media-thumbnails" v-if="currentMediaList.length > 1">
                <div 
                  v-for="(media, index) in currentMediaList" 
                  :key="index"
                  class="thumbnail-item"
                  :class="{ active: index === currentMediaIndex }"
                  @click.stop="selectMedia(index)"
                  :aria-label="`${t('records.media')} ${index + 1}`"
                >
                  <img 
                    v-if="media.media_type === 'image'" 
                    :src="media.media_url" 
                    :alt="media.media_name || `${t('records.media')} ${index + 1}`"
                  >
                  <div v-else class="video-thumbnail">
                    <span class="video-icon">🎬</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 空状态 -->
            <div class="media-empty" v-else>
              <p>{{ t('records.noMediaFiles') }}</p>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

  </div>
</template>

<script setup lang="ts">
import { onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRecords } from '@/api/records'
import { useThemeStore } from '@/stores/theme'

const { t } = useI18n()

const themeStore = useThemeStore()


// 使用组合式函数
const {
  // 数据状态
  pets,
  
  // 筛选状态
  filters,
  viewMode,
  sortBy,
  
  // 提交状态
  isSubmitting,
  
  // 模态框状态
  showAddModal,
  showEditModal,
  showMediaModal,
  currentMediaIndex, // 添加当前媒体索引
  
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
  nextMedia, // 添加切换方法
  prevMedia,
  selectMedia,
  editEvent,
  deleteEvent,
  saveEvent,
  closeModal
} = useRecords()

 onMounted(async () => {
    // 先获取宠物数据
    await fetchPets()
    
    // 等待DOM更新确保宠物数据已反应到pets.value中
    await nextTick()
    
    // 再获取事件数据，这样事件映射时宠物数据已可用
    await fetchEvents()
  })
</script>


<style scoped>
@import '../styles/records.css'
</style>
