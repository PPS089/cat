import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { dispatchNavStatsRefresh } from '@/shared/utils/events'
import { eventsService, mapRecord, mapMedia } from '../services/events'
import type {
  FilePreview,
  MediaEntry,
  PetSummary,
  RecordEvent,
  RecordFilters,
  RecordForm,
  ViewMode,
} from '../types'

const MAX_FILES = 5
const MAX_FILE_SIZE = 524288500
const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/jpg']
const VIDEO_TYPES = ['video/mp4', 'video/mpeg', 'video/quicktime', 'video/x-msvideo', 'video/webm']

const createFilters = (): RecordFilters => ({
  selectedPet: '',
  selectedEventType: '',
  selectedMood: '',
  selectedDate: '',
})

const createForm = (): RecordForm => ({
  recordId: null,
  pid: '',
  eventType: '',
  mood: '',
  description: '',
  location: '',
  recordTime: '',
})

export const useEventsStore = defineStore('events', () => {
  const { t } = useI18n()
  const events = ref<RecordEvent[]>([])
  const pets = ref<PetSummary[]>([])
  const loading = ref(false)
  const filters = reactive(createFilters())
  const viewMode = ref<ViewMode>('grid')

  const editorVisible = ref(false)
  const form = reactive(createForm())
  const existingMedia = ref<MediaEntry[]>([])
  const isSubmitting = ref(false)

  const filePreviews = ref<FilePreview[]>([])
  const isDragOver = ref(false)

  const mediaVisible = ref(false)
  const mediaItems = ref<MediaEntry[]>([])
  const mediaIndex = ref(0)

  const totalEvents = computed(() => events.value.length)
  const totalMedia = computed(() =>
    events.value.reduce((sum, item) => sum + item.media.length, 0),
  )

  const filteredEvents = computed(() => {
    let list = [...events.value]
    if (filters.selectedPet) {
      list = list.filter(event => event.petId === Number(filters.selectedPet))
    }
    if (filters.selectedEventType) {
      list = list.filter(event => event.eventType === filters.selectedEventType)
    }
    if (filters.selectedMood) {
      list = list.filter(event => event.mood === filters.selectedMood)
    }
    if (filters.selectedDate) {
      const base = new Date(filters.selectedDate).toDateString()
      list = list.filter(event => new Date(event.recordTime).toDateString() === base)
    }
    return list
  })

  const groupedEvents = computed(() => {
    return filteredEvents.value.reduce<Record<string, RecordEvent[]>>((groups, event) => {
      const key = new Date(event.recordTime).toDateString()
      groups[key] = groups[key] || []
      groups[key].push(event)
      return groups
    }, {})
  })

  const resetFilters = () => {
    Object.assign(filters, createFilters())
  }

  const resetForm = () => {
    Object.assign(form, createForm())
    existingMedia.value = []
    clearFiles()
  }

  const formatForInput = (value?: string) => {
    if (!value) return ''
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return ''
    return date.toISOString().slice(0, 16)
  }

  const formatPayloadTime = (value: string) => (value ? value.replace('T', ' ') + ':00' : '')

  const addFiles = async (files: FileList | File[]) => {
    const next = Array.from(files || [])
    if (!next.length) return
    if (filePreviews.value.length + next.length > MAX_FILES) {
      ElMessage.error(t('common.maxFilesExceeded'))
      return
    }

    next.forEach(file => {
      if (file.size > MAX_FILE_SIZE) {
        ElMessage.error(t('records.fileSizeLimit'))
        return
      }
      const type = file.type.startsWith('image/')
        ? 'image'
        : file.type.startsWith('video/')
          ? 'video'
          : ''
      if (!type || (!IMAGE_TYPES.includes(file.type) && !VIDEO_TYPES.includes(file.type))) {
        ElMessage.error(t('records.invalidFormat'))
        return
      }
      const url = URL.createObjectURL(file)
      filePreviews.value.push({ file, url, type })
    })
  }

  const removeFile = (index: number) => {
    const preview = filePreviews.value[index]
    if (preview) {
      URL.revokeObjectURL(preview.url)
      filePreviews.value.splice(index, 1)
    }
  }

  const clearFiles = () => {
    filePreviews.value.forEach(preview => URL.revokeObjectURL(preview.url))
    filePreviews.value = []
    isDragOver.value = false
  }

  const fetchPets = async () => {
    try {
      const response = await eventsService.fetchPets()
      if (response.code === 200 && response.data?.records) {
        pets.value = response.data.records.map((item: any) => ({
          pid: item.pid,
          name: item.name,
        }))
      } else {
        pets.value = []
      }
    } catch {
      pets.value = []
    }
  }

  const fetchEvents = async () => {
    loading.value = true
    try {
      const response = await eventsService.fetchEvents()
      if (response.code === 200 && Array.isArray(response.data)) {
        events.value = response.data.map(mapRecord)
      } else {
        events.value = []
      }
    } catch (error) {
      events.value = []
      console.error('[Records] fetchEvents failed', error)
    } finally {
      loading.value = false
    }
  }

  const bootstrap = async () => {
    await Promise.all([fetchPets(), fetchEvents()])
  }

  const openCreate = () => {
    resetForm()
    editorVisible.value = true
  }

  const openEdit = async (event: RecordEvent) => {
    resetForm()
    form.recordId = event.id
    form.pid = String(event.petId)
    form.eventType = event.eventType
    form.mood = event.mood || ''
    form.description = event.description
    form.location = event.location || ''
    form.recordTime = formatForInput(event.recordTime)
    editorVisible.value = true
    await loadMedia(event.id)
  }

  const closeEditor = () => {
    editorVisible.value = false
    resetForm()
  }

  const persistEvent = async () => {
    if (isSubmitting.value) return
    if (!form.pid || !form.eventType || !form.description || !form.recordTime) {
      ElMessage.error(t('records.pleaseCompleteForm'))
      return
    }
    isSubmitting.value = true
    const payload = {
      pid: form.pid,
      eventType: form.eventType,
      recordTime: formatPayloadTime(form.recordTime),
      description: form.description,
      mood: form.mood,
      location: form.location,
    }
    try {
      let recordId = form.recordId
      if (recordId) {
        await eventsService.updateEvent(recordId, payload)
      } else {
        const response = await eventsService.createEvent(payload)
        recordId = response.data?.eid ?? null
      }
      if (recordId) {
        await eventsService.uploadMedia(
          recordId,
          filePreviews.value.map(preview => preview.file),
        )
      }
      ElMessage.success(recordId === form.recordId ? t('records.updateSuccess') : t('records.saveSuccessful'))
      await fetchEvents()
      dispatchNavStatsRefresh({ reason: 'events-change' })
      closeEditor()
    } catch (error) {
      console.error('[Records] save failed', error)
    } finally {
      isSubmitting.value = false
    }
  }

  const deleteEvent = async (recordId: number) => {
    if (!recordId) return
    try {
      await ElMessageBox.confirm(t('records.deleteConfirm'), t('common.deleteConfirmation'), {
        type: 'warning',
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
      })
      await eventsService.deleteEvent(recordId)
      ElMessage.success(t('records.deleteSuccess'))
      await fetchEvents()
      dispatchNavStatsRefresh({ reason: 'events-change' })
    } catch (error) {
      if (error !== 'cancel') {
        console.error('[Records] delete failed', error)
      }
    }
  }

  const loadMedia = async (recordId: number) => {
    if (!recordId) {
      existingMedia.value = []
      return
    }
    try {
      const response = await eventsService.fetchRecordMedia(recordId)
      const list = Array.isArray(response.data) ? response.data : response.data ? [response.data] : []
      existingMedia.value = list.map(mapMedia)
    } catch {
      existingMedia.value = []
    }
  }

  const removeMedia = async (mediaId: number) => {
    try {
      await eventsService.deleteMedia(mediaId)
      ElMessage.success(t('records.deleteMediaSuccess'))
      if (form.recordId) {
        await loadMedia(form.recordId)
        await fetchEvents()
      }
    } catch (error) {
      console.error('[Records] delete media failed', error)
    }
  }

  const openMediaViewer = async (record: RecordEvent) => {
    mediaIndex.value = 0
    try {
      await loadMedia(record.id)
      mediaItems.value = existingMedia.value.length ? existingMedia.value : record.media
    } catch {
      mediaItems.value = record.media
    }
    mediaVisible.value = mediaItems.value.length > 0
  }

  const closeMediaViewer = () => {
    mediaVisible.value = false
    mediaItems.value = []
    mediaIndex.value = 0
  }

  const nextMedia = () => {
    if (!mediaItems.value.length) return
    mediaIndex.value = (mediaIndex.value + 1) % mediaItems.value.length
  }

  const prevMedia = () => {
    if (!mediaItems.value.length) return
    mediaIndex.value =
      (mediaIndex.value - 1 + mediaItems.value.length) % mediaItems.value.length
  }

  const selectMedia = (index: number) => {
    if (index >= 0 && index < mediaItems.value.length) {
      mediaIndex.value = index
    }
  }

  const getPetName = (petId: number, fallback?: string) => {
    const pet = pets.value.find(item => item.pid === petId)
    return pet?.name || fallback || t('api.unknownPet')
  }

  const formatDate = (value: string) => {
    if (!value) return ''
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value
    return date.toLocaleString()
  }

  const getMoodEmoji = (mood?: string) => {
    const map: Record<string, string> = {
      [t('records.happy')]: '😊',
      [t('records.angry')]: '😠',
      [t('records.tired')]: '😴',
      [t('records.active')]: '🐾',
    }
    return mood ? map[mood] || '😐' : '😐'
  }

  const getMoodClass = (mood?: string) => ({
    happy: mood === t('records.happy'),
    angry: mood === t('records.angry'),
    tired: mood === t('records.tired'),
    active: mood === t('records.active'),
  })

  return {
    // state
    events,
    pets,
    loading,
    filters,
    resetFilters,
    viewMode,
    editorVisible,
    form,
    existingMedia,
    filePreviews,
    isDragOver,
    isSubmitting,
    mediaVisible,
    mediaItems,
    mediaIndex,
    // computed
    totalEvents,
    totalMedia,
    filteredEvents,
    groupedEvents,
    // actions
    bootstrap,
    fetchEvents,
    fetchPets,
    openCreate,
    openEdit,
    closeEditor,
    persistEvent,
    deleteEvent,
    removeMedia,
    openMediaViewer,
    closeMediaViewer,
    nextMedia,
    prevMedia,
    selectMedia,
    loadMedia,
    addFiles,
    removeFile,
    clearFiles,
    getPetName,
    formatDate,
    getMoodEmoji,
    getMoodClass,
    setDragState: (state: boolean) => {
      isDragOver.value = state
    },
  }
})
