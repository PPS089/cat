<template>
  <PageContainer :title="t('nav.eventRecords')" :description="t('message.findYourPerfectCompanion')">
    <template #actions>
      <el-tooltip v-if="!pets.length" :content="t('message.pleaseAdoptPetInstead')" placement="bottom">
        <span>
          <el-button type="primary" disabled>+ {{ t('records.addEvent') }}</el-button>
        </span>
      </el-tooltip>
      <el-button v-else type="primary" @click="openCreate">+ {{ t('records.addEvent') }}</el-button>
    </template>

    <section class="events-page" v-loading="loading">
      <section class="events-surface stats-surface">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">📅</div>
            <div class="stat-content">
              <h3>{{ totalEvents }}</h3>
              <p>{{ t('records.totalEvents') }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">🐾</div>
            <div class="stat-content">
              <h3>{{ petsCount }}</h3>
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
      </section>

      <section class="events-surface filter-surface">
        <div class="filter-layout">
          <el-form class="events-filter-form" label-position="top">
            <el-form-item :label="t('records.pet')" class="events-filter-item">
              <el-select v-model="filters.selectedPet" :placeholder="t('records.allPets')" clearable>
                <el-option value="" :label="t('records.allPets')" />
                <el-option v-for="pet in pets" :key="pet.pid" :label="pet.name" :value="pet.pid" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('records.eventType')" class="events-filter-item">
              <el-select v-model="filters.selectedEventType" :placeholder="t('records.allEvents')" clearable>
                <el-option value="" :label="t('records.allEvents')" />
                <el-option v-for="option in eventTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('records.mood')" class="events-filter-item">
              <el-select v-model="filters.selectedMood" :placeholder="t('records.allMood')" clearable>
                <el-option value="" :label="t('records.allMood')" />
                <el-option v-for="option in moodOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('records.selectDate')" class="events-filter-item">
              <el-date-picker v-model="filters.selectedDate" type="date" value-format="YYYY-MM-DD" clearable style="width: 100%" />
            </el-form-item>
          </el-form>

          <div class="filter-actions">
            <el-segmented v-model="viewMode" :options="viewModeOptions" />
            <el-button text @click="resetFilters">{{ t('common.reset') }}</el-button>
          </div>
        </div>
      </section>

      <section class="events-surface content-surface">
        <el-empty v-if="!loading && !filteredEvents.length" :description="t('records.noRecords')" />
        <div v-else-if="viewMode === 'grid'" class="events-grid">
          <WebCard
            v-for="event in filteredEvents"
            :key="event.id"
            class="event-card"
            :class="getMoodClass(event.mood)"
          >
            <div class="event-header">
              <div class="pet-info">
                <h4>{{ getPetName(event.petId, event.petName) }}</h4>
                <span class="event-type">{{ event.eventType }}</span>
              </div>
              <div class="event-mood" v-if="event.mood">
                <span class="mood-emoji">{{ getMoodEmoji(event.mood) }}</span>
                <span class="mood-text">{{ event.mood }}</span>
              </div>
            </div>
            <div class="event-content">
              <p class="description">{{ event.description }}</p>
              <div v-if="event.location" class="location">
                📍 {{ event.location }}
              </div>
            </div>
            <div class="event-media" v-if="event.media.length">
              <div class="media-preview">
                <div
                  v-for="media in event.media.slice(0, 3)"
                  :key="media.id"
                  class="media-item"
                  @click="openMedia(event)"
                >
                  <img v-if="media.type === 'image'" :src="media.url" :alt="media.name || event.eventType" />
                  <div v-else class="video-thumbnail">🎬</div>
                </div>
                <div
                  v-if="event.media.length > 3"
                  class="media-count"
                  @click="openMedia(event)"
                >
                  +{{ event.media.length - 3 }}
                </div>
              </div>
            </div>
            <div class="event-footer">
              <div class="event-time">
                <span>{{ formatDate(event.recordTime) }}</span>
                <span v-if="event.updatedAt && event.updatedAt !== event.createdAt" class="updated-time">
                  {{ t('records.updatedAt') }} {{ formatDate(event.updatedAt) }}
                </span>
              </div>
              <div class="event-actions">
                <el-button size="small" text type="primary" @click="editEvent(event)">
                  {{ t('records.edit') }}
                </el-button>
                <el-button size="small" text type="danger" @click="deleteEvent(event.id)">
                  {{ t('records.delete') }}
                </el-button>
              </div>
            </div>
          </WebCard>
        </div>

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
            v-for="event in filteredEvents"
            :key="event.id"
            class="list-item"
            :class="getMoodClass(event.mood)"
          >
            <div class="col-pet">
              <h4>{{ getPetName(event.petId, event.petName) }}</h4>
            </div>
            <div class="col-type">
              <span class="event-type-badge">{{ event.eventType }}</span>
            </div>
            <div class="col-mood">
              <span v-if="event.mood" class="mood-indicator">
                <span class="mood-emoji">{{ getMoodEmoji(event.mood) }}</span>
                {{ event.mood }}
              </span>
            </div>
            <div class="col-description">
              <p>{{ event.description }}</p>
              <small v-if="event.location">📍 {{ event.location }}</small>
            </div>
            <div class="col-time">
              {{ formatDate(event.recordTime) }}
            </div>
            <div class="col-actions">
              <el-button size="small" text type="primary" @click="editEvent(event)">
                {{ t('records.edit') }}
              </el-button>
              <el-button size="small" text type="danger" @click="deleteEvent(event.id)">
                {{ t('records.delete') }}
              </el-button>
            </div>
          </div>
        </div>

        <div v-else class="timeline-view">
          <div
            v-for="(items, date) in groupedEvents"
            :key="date"
            class="timeline-group"
          >
            <div class="timeline-date">{{ date }}</div>
            <div class="timeline-items">
              <div
                v-for="event in items"
                :key="event.id"
                class="timeline-item"
                :class="getMoodClass(event.mood)"
              >
                <div class="timeline-marker" />
                <div class="timeline-content">
                  <div class="timeline-header">
                    <h4>{{ getPetName(event.petId, event.petName) }}</h4>
                    <span class="event-type-badge">{{ event.eventType }}</span>
                    <span v-if="event.mood" class="mood-indicator">
                      <span class="mood-emoji">{{ getMoodEmoji(event.mood) }}</span>
                      {{ event.mood }}
                    </span>
                  </div>
                  <p>{{ event.description }}</p>
                  <div v-if="event.location" class="location">📍 {{ event.location }}</div>
                  <div class="timeline-footer">
                    <span>{{ formatDate(event.recordTime) }}</span>
                    <div class="timeline-actions">
                      <el-button size="small" text type="primary" @click="editEvent(event)">
                        {{ t('records.edit') }}
                      </el-button>
                      <el-button size="small" text type="danger" @click="deleteEvent(event.id)">
                        {{ t('records.delete') }}
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </section>
  </PageContainer>

  <el-dialog
    v-model="editorVisible"
    :title="form.recordId ? t('records.editEvent') : t('records.addEvent')"
    class="event-editor-dialog"
    width="640px"
    :close-on-click-modal="false"
    @close="closeEditor"
  >
    <div class="modal-form">
      <el-form label-width="110px">
        <el-form-item :label="t('records.pet')">
          <el-select v-model="form.pid" :placeholder="t('records.selectPet')" filterable>
            <el-option v-for="pet in pets" :key="pet.pid" :label="pet.name" :value="String(pet.pid)" />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('records.eventType')">
          <el-select v-model="form.eventType" :placeholder="t('records.selectType')">
            <el-option v-for="option in eventTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('records.mood')">
          <el-select v-model="form.mood" :placeholder="t('records.selectMood')">
            <el-option v-for="option in moodOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('records.eventTime')">
          <el-date-picker
            v-model="form.recordTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm"
            :placeholder="t('records.selectDate')"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item :label="t('records.location')">
          <el-input v-model="form.location" :placeholder="t('records.locationOptional')" />
        </el-form-item>

        <el-form-item :label="t('records.detailedDescription')">
          <el-input
            v-model="form.description"
            type="textarea"
            :placeholder="t('records.pleaseEnterDescription')"
            :rows="3"
          />
        </el-form-item>

        <el-form-item v-if="form.recordId && existingMedia.length" :label="t('records.existingMedia')">
          <div class="existing-media-grid">
            <div v-for="media in existingMedia" :key="media.id" class="existing-media-item">
              <div class="media-thumb" @click="openMediaById(form.recordId || 0)">
                <img v-if="media.type === 'image'" :src="media.url" :alt="media.name" />
                <div v-else class="video-placeholder">🎬</div>
              </div>
              <el-button
                circle
                size="small"
                type="danger"
                plain
                @click="removeMedia(media.id)"
                :loading="isSubmitting"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item :label="t('records.mediaFiles')">
          <div
            class="upload-area"
            :class="{ 'drag-over': isDragOver }"
            @click="triggerFileDialog"
            @dragover.prevent="() => setDragState(true)"
            @dragleave.prevent="() => setDragState(false)"
            @drop.prevent="onDrop"
          >
            <input
              ref="fileInputRef"
              class="file-input"
              type="file"
              multiple
              accept="image/*,video/*"
              :disabled="filePreviews.length >= maxFiles"
              @change="onFileChange"
            />
            <p>{{ t('records.clickToSelectOrDrag') }}</p>
            <small>{{ t('records.supportedFormats') }}</small>
          </div>
          <div v-if="filePreviews.length" class="file-preview-grid">
            <div v-for="(preview, index) in filePreviews" :key="preview.url" class="file-preview-item">
              <img v-if="preview.type === 'image'" :src="preview.url" :alt="preview.file.name" />
              <div v-else class="video-preview">🎬</div>
              <div class="file-info">
                <span class="file-name">{{ preview.file.name }}</span>
                <small>{{ formatFileSize(preview.file.size) }}</small>
              </div>
              <el-button type="danger" text @click="removeFile(index)" :disabled="isSubmitting">
                {{ t('records.deleteFile') }}
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="closeEditor" :disabled="isSubmitting">{{ t('records.cancel') }}</el-button>
      <el-button type="primary" :loading="isSubmitting" @click="persistEvent">
        {{ form.recordId ? t('records.update') : t('records.save') }}
      </el-button>
    </template>
  </el-dialog>

  <Teleport to="body">
    <Transition name="media-viewer">
      <div v-if="mediaVisible" class="media-viewer-overlay" @click="closeMediaViewer">
        <div class="media-viewer-content" @click.stop>
          <button class="media-viewer-close" @click="closeMediaViewer">×</button>
          <div class="media-viewer-main" v-if="mediaItems.length">
            <button v-if="mediaItems.length > 1" class="media-nav-btn media-nav-prev" @click="prevMedia">‹</button>
            <div class="media-display-wrapper">
              <div class="media-display">
                <img v-if="currentMedia?.type === 'image'" :src="currentMedia.url" :alt="currentMedia.name" />
                <video
                  v-else
                  ref="videoRef"
                  controls
                  playsinline
                  preload="metadata"
                  :muted="isMuted"
                  :src="currentMedia?.url"
                  @play="onVideoPlay"
                  @pause="onVideoPause"
                  @loadedmetadata="onVideoLoaded"
                />
              </div>
              <div v-if="isVideoMedia" class="video-controls">
                <button class="video-control-btn" type="button" @click="togglePlay">
                  {{ isVideoPlaying ? t('records.pauseVideo') : t('records.playVideo') }}
                </button>
                <button class="video-control-btn" type="button" @click="toggleMute">
                  {{ isMuted ? t('records.unmuteVideo') : t('records.muteVideo') }}
                </button>
                <label class="volume-control">
                  {{ t('records.volume') }}
                  <input
                    type="range"
                    min="0"
                    max="1"
                    step="0.05"
                    :value="volume"
                    @input="onVolumeInput"
                  />
                </label>
              </div>
            </div>
            <button v-if="mediaItems.length > 1" class="media-nav-btn media-nav-next" @click="nextMedia">›</button>
          </div>
          <div class="media-viewer-footer" v-if="mediaItems.length">
            <p>{{ currentMedia?.name }}</p>
            <p>{{ t('records.mediaIndex', { current: mediaIndex + 1, total: mediaItems.length }) }}</p>
            <div class="media-thumbnails" v-if="mediaItems.length > 1">
              <div
                v-for="(media, index) in mediaItems"
                :key="media.id"
                class="thumbnail-item"
                :class="{ active: index === mediaIndex }"
                @click="selectMedia(index)"
              >
                <img v-if="media.type === 'image'" :src="media.url" :alt="media.name" />
                <div v-else class="video-thumbnail">🎬</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { Close } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import WebCard from '@/shared/components/common/WebCard.vue'
import { useEventsStore } from '@/modules/events/store/events'

const { t } = useI18n()
const eventsStore = useEventsStore()

const {
  loading,
  filters,
  viewMode,
  totalEvents,
  totalMedia,
  pets,
  filteredEvents,
  groupedEvents,
  editorVisible,
  form,
  existingMedia,
  filePreviews,
  isDragOver,
  isSubmitting,
  mediaVisible,
  mediaItems,
  mediaIndex,
} = storeToRefs(eventsStore)

const petsCount = computed(() => pets.value.length)
const currentMedia = computed(() => mediaItems.value[mediaIndex.value])
const maxFiles = 5
const fileInputRef = ref<HTMLInputElement | null>(null)
const videoRef = ref<HTMLVideoElement | null>(null)
const isVideoPlaying = ref(false)
const isMuted = ref(false)
const volume = ref(1)
const lastVolume = ref(1)
const isVideoMedia = computed(() => currentMedia.value?.type === 'video')

const eventTypeOptions = computed(() => [
  { value: '喂食', label: t('records.feeding') },
  { value: '洗澡', label: t('records.bathing') },
  { value: '散步', label: t('records.walking') },
  { value: '看兽医', label: t('records.veterinary') },
  { value: '玩耍', label: t('records.playing') },
  { value: '打疫苗', label: t('records.vaccination') },
])

const viewModeOptions = computed(() => [
  { label: t('records.gridView'), value: 'grid' },
  { label: t('records.listView'), value: 'list' },
  { label: t('records.timeline'), value: 'timeline' },
])

const moodOptions = computed(() => [
  { value: t('records.happy'), label: t('records.happy') },
  { value: t('records.angry'), label: t('records.angry') },
  { value: t('records.tired'), label: t('records.tired') },
  { value: t('records.active'), label: t('records.active') },
])

const syncVideoState = () => {
  const video = videoRef.value
  if (!video) return
  video.volume = volume.value
  video.muted = isMuted.value
}

const stopVideo = () => {
  const video = videoRef.value
  if (video) {
    video.pause()
    video.currentTime = 0
  }
  isVideoPlaying.value = false
}

const resetVideoState = () => {
  stopVideo()
  isMuted.value = false
  volume.value = 1
  lastVolume.value = 1
  nextTick(syncVideoState)
}

watch(currentMedia, () => {
  resetVideoState()
})

watch(mediaVisible, visible => {
  if (!visible) {
    stopVideo()
  }
})

const onVideoPlay = () => {
  isVideoPlaying.value = true
}

const onVideoPause = () => {
  isVideoPlaying.value = false
}

const togglePlay = () => {
  const video = videoRef.value
  if (!video) return
  if (video.paused) {
    video.play()
  } else {
    video.pause()
  }
}

const handleVolumeChange = (value: number) => {
  const clamped = Math.min(1, Math.max(0, value))
  volume.value = clamped
  const video = videoRef.value
  if (video) {
    video.volume = clamped
    video.muted = clamped === 0
  }
  if (clamped === 0) {
    isMuted.value = true
  } else {
    isMuted.value = false
    lastVolume.value = clamped
  }
}

const onVolumeInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  handleVolumeChange(Number(target.value))
}

const toggleMute = () => {
  const video = videoRef.value
  if (!video) return
  if (isMuted.value) {
    isMuted.value = false
    const restored = lastVolume.value || 1
    volume.value = restored
    video.muted = false
    video.volume = restored
  } else {
    isMuted.value = true
    lastVolume.value = volume.value || 1
    volume.value = 0
    video.muted = true
  }
}

onMounted(() => {
  eventsStore.bootstrap()
})

const openCreate = () => eventsStore.openCreate()
const editEvent = (event: any) => eventsStore.openEdit(event)
const deleteEvent = (id: number) => eventsStore.deleteEvent(id)
const persistEvent = () => eventsStore.persistEvent()
const closeEditor = () => eventsStore.closeEditor()
const removeMedia = (id: number) => eventsStore.removeMedia(id)
const openMedia = (event: any) => eventsStore.openMediaViewer(event)
const openMediaById = (id: number) => {
  if (!id) return
  const record = filteredEvents.value.find(item => item.id === id)
  if (record) {
    eventsStore.openMediaViewer(record)
  }
}
const closeMediaViewer = () => {
  stopVideo()
  eventsStore.closeMediaViewer()
}
const nextMedia = () => eventsStore.nextMedia()
const prevMedia = () => eventsStore.prevMedia()
const selectMedia = (index: number) => eventsStore.selectMedia(index)
const setDragState = (state: boolean) => eventsStore.setDragState(state)
const triggerFileDialog = () => {
  if (filePreviews.value.length >= maxFiles) return
  fileInputRef.value?.click()
}

const onFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files?.length) {
    eventsStore.addFiles(target.files)
    target.value = ''
  }
}

const onDrop = (event: DragEvent) => {
  setDragState(false)
  const files = event.dataTransfer?.files
  if (files?.length) {
    eventsStore.addFiles(files)
  }
}

const onRemoveFile = (index: number) => eventsStore.removeFile(index)

const getPetName = (petId: number, fallback?: string) => eventsStore.getPetName(petId, fallback)
const getMoodEmoji = (mood?: string) => eventsStore.getMoodEmoji(mood)
const getMoodClass = (mood?: string) => eventsStore.getMoodClass(mood)
const formatDate = (value: string) => eventsStore.formatDate(value)
const formatFileSize = (bytes: number) => `${(bytes / 1024 / 1024).toFixed(2)} MB`
const onVideoLoaded = () => syncVideoState()
const resetFilters = () => eventsStore.resetFilters()

const removeFile = (index: number) => onRemoveFile(index)
</script>

<style scoped>
.events-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.events-surface {
  border-radius: var(--app-radius-lg);
  border: 1px solid var(--app-border-color);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.04), transparent 55%), var(--app-bg-color);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
  padding: 1.25rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 1rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 1rem;
  border-radius: var(--app-radius-md);
  border: 1px solid var(--app-border-color);
  background: rgba(255, 255, 255, 0.04);
  box-shadow: 0 18px 45px rgba(2, 6, 23, 0.12);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(95, 111, 255, 0.18), rgba(95, 111, 255, 0.05));
  border: 1px solid rgba(95, 111, 255, 0.22);
  font-size: 1.4rem;
}

.stat-content h3 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--app-text-color);
}

.stat-content p {
  margin: 0;
  color: var(--app-text-secondary);
}

.events-filter-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.filter-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

:deep(.events-filter-item .el-form-item__label) {
  color: var(--app-text-secondary);
}

:deep(.events-filter-item .el-input__wrapper),
:deep(.events-filter-item .el-select .el-input__wrapper) {
  background: var(--app-bg-color);
}

.filter-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.6rem;
  padding-bottom: 0.25rem;
}

.events-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.25rem;
}

.event-card {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.02);
  box-shadow: 0 18px 50px rgba(2, 6, 23, 0.18);
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.event-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 26px 60px rgba(2, 6, 23, 0.22);
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.75rem;
}

.pet-info h4 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--app-text-color);
}

.pet-info .event-type {
  display: inline-block;
  margin-top: 0.15rem;
  padding: 0.15rem 0.65rem;
  border-radius: 999px;
  background: rgba(95, 111, 255, 0.1);
  color: var(--app-primary);
  font-size: 0.85rem;
}

.event-mood {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  color: var(--app-text-secondary);
}

.event-content {
  color: var(--app-text-secondary);
  min-height: 48px;
}

.location {
  display: inline-flex;
  gap: 0.35rem;
  color: var(--app-text-secondary);
  font-size: 0.9rem;
}

.media-preview {
  display: flex;
  gap: 0.5rem;
}

.media-item {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--app-border-color);
  cursor: pointer;
}

.media-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 12px;
  border: 1px dashed var(--app-border-color);
  color: var(--app-primary);
  cursor: pointer;
}

.event-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  color: var(--app-text-secondary);
  font-size: 0.9rem;
}

.event-actions {
  display: flex;
  gap: 0.25rem;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.list-header,
.list-item {
  display: grid;
  grid-template-columns: 1.2fr 0.9fr 0.8fr 1.4fr 1fr 0.8fr;
  gap: 0.75rem;
  align-items: center;
}

.list-header {
  padding: 0.5rem 0.75rem;
  color: var(--app-text-secondary);
  font-size: 0.85rem;
  text-transform: uppercase;
}

.list-item {
  padding: 0.85rem 0.75rem;
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-md);
}

.event-type-badge {
  display: inline-block;
  padding: 0.2rem 0.75rem;
  border-radius: 999px;
  background: rgba(95, 111, 255, 0.1);
  color: var(--app-primary);
  font-size: 0.85rem;
}

.mood-indicator {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  color: var(--app-text-secondary);
}

.timeline-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.timeline-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.timeline-date {
  font-weight: 600;
  color: var(--app-text-color);
}

.timeline-items {
  border-left: 2px solid var(--app-border-color);
  padding-left: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.timeline-item {
  position: relative;
  padding: 0.75rem 0.75rem 0.75rem 0;
}

.timeline-marker {
  position: absolute;
  left: -26px;
  top: 0.9rem;
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

.timeline-header {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-wrap: wrap;
}

.timeline-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  color: var(--app-text-secondary);
}

.upload-area {
  width: 100%;
  border: 1px dashed var(--app-border-color);
  border-radius: var(--app-radius-md);
  padding: 1.25rem;
  text-align: center;
  color: var(--app-text-secondary);
  transition: border-color 0.2s, background 0.2s;
  cursor: pointer;
}

.upload-area.drag-over {
  border-color: var(--app-primary);
  background: rgba(95, 111, 255, 0.08);
  color: var(--app-primary);
}

.file-input {
  display: none;
}

.file-preview-grid,
.existing-media-grid {
  margin-top: 1rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
}

.media-thumb {
  width: 100%;
  height: 140px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

:deep(.event-editor-dialog .el-dialog__body) {
  padding-top: 0.5rem;
}

.file-preview-item,
.existing-media-item {
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-md);
  padding: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background: var(--app-bg-color);
}

:deep(.existing-media-item .el-button) {
  align-self: flex-end;
}

.media-thumb,
.file-preview-item img,
.video-preview,
.media-display img,
.media-display video {
  width: 100%;
  border-radius: 10px;
  object-fit: cover;
}

.video-placeholder,
.video-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  background: rgba(15, 23, 42, 0.08);
  border-radius: 12px;
  font-size: 2rem;
}

.file-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: var(--app-text-secondary);
}

.media-viewer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
}

.media-viewer-content {
  background: var(--app-surface-color);
  border-radius: var(--app-radius-lg);
  padding: 1rem;
  width: min(90vw, 800px);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  position: relative;
}

.media-viewer-close {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  border: none;
  background: rgba(15, 23, 42, 0.15);
  color: #fff;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
}

.media-viewer-main {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.media-display-wrapper {
  flex: 1;
  min-height: 320px;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.media-display {
  flex: 1;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.media-display img,
.media-display video {
  max-height: 420px;
  width: 100%;
  border-radius: var(--app-radius-md);
  object-fit: contain;
}

.video-controls {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  flex-wrap: wrap;
}

.video-control-btn {
  border: 1px solid var(--app-border-color);
  background: var(--app-bg-color);
  color: var(--app-text-color);
  padding: 0.45rem 1rem;
  border-radius: 999px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.video-control-btn:hover {
  background: var(--app-primary);
  color: #fff;
}

.volume-control {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: var(--app-text-secondary);
}

.volume-control input[type='range'] {
  width: 140px;
}

.media-nav-btn {
  border: none;
  background: var(--app-primary);
  color: #fff;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  cursor: pointer;
}

.media-viewer-footer {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  color: var(--app-text-secondary);
}

.media-thumbnails {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.thumbnail-item {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  border: 2px solid transparent;
  overflow: hidden;
  cursor: pointer;
}

.thumbnail-item.active {
  border-color: var(--app-primary);
}

.file-preview-item img,
.existing-media-item img,
.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-viewer-enter-active,
.media-viewer-leave-active {
  transition: opacity 0.2s ease;
}

.media-viewer-enter-from,
.media-viewer-leave-to {
  opacity: 0;
}

.happy {
  border-color: rgba(52, 211, 153, 0.35) !important;
}

.angry {
  border-color: rgba(248, 113, 113, 0.35) !important;
}

.tired {
  border-color: rgba(148, 163, 184, 0.45) !important;
}

.active {
  border-color: rgba(129, 140, 248, 0.45) !important;
}

@media (max-width: 1024px) {
  .list-header,
  .list-item {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }

  .filter-actions {
    justify-content: space-between;
    padding-bottom: 0;
  }

  .media-viewer-content {
    width: 95vw;
  }
}
</style>
