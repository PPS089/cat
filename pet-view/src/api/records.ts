import { ref, computed, reactive, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import { uploadMediaFiles } from './media'
import type {
  PetRecord,
  PetInfo,
  RecordFormData,
  RecordFilters,
  ViewMode,
  SortField,
  MediaFile,
  FilePreview,
  FileValidationConfig,
  UseRecordsReturn
} from '@/types/records'

// 心情表情
export const useMoodEmoji = () => {
  const { t } = useI18n()
  
  const getMoodEmoji = (mood: string | undefined): string => {
    const emojis: Record<string, string> = {
      [t('happy')]: '😊',
      [t('angry')]: '😠',
      [t('tired')]: '😴',
      [t('active')]: '🐕'
    }
    return mood ? emojis[mood] || '😐' : '😐'
  }

  return { getMoodEmoji }
}

// 心情样式类
export const useMoodClass = () => {
  const { t } = useI18n()
  
  const getMoodClass = (mood: string | undefined): Record<string, boolean> => {
    return {
      'happy': mood === t('happy'),
      'angry': mood === t('angry'),
      'tired': mood === t('tired'),
      'active': mood === t('active')
    }
  }

  return { getMoodClass }
}

// 日期格式化
export const useDateFormatter = () => {
  const formatDate = (dateString: string): string => {
    if (!dateString) return ''
    return new Date(dateString).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return { formatDate }
}

// 文件验证配置
const FILE_VALIDATION_CONFIG: FileValidationConfig = {
  maxFileSize: 52428800,  // 50MB
  supportedImageFormats: ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/jpg'],
  supportedVideoFormats: ['video/mp4', 'video/mpeg', 'video/quicktime', 'video/x-msvideo', 'video/webm'],
  thumbnailMaxWidth: 200,
  thumbnailMaxHeight: 200
}

// 生成图片缩略图
const generateImageThumbnail = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (event) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        if (!ctx) {
          resolve(event.target?.result as string)
          return
        }

        let width = img.width
        let height = img.height
        const maxWidth = FILE_VALIDATION_CONFIG.thumbnailMaxWidth
        const maxHeight = FILE_VALIDATION_CONFIG.thumbnailMaxHeight

        if (width > height) {
          if (width > maxWidth) {
            height = (height * maxWidth) / width
            width = maxWidth
          }
        } else {
          if (height > maxHeight) {
            width = (width * maxHeight) / height
            height = maxHeight
          }
        }

        canvas.width = width
        canvas.height = height
        ctx.drawImage(img, 0, 0, width, height)
        resolve(canvas.toDataURL('image/jpeg', 0.7))
      }
      img.onerror = () => {
        resolve(event.target?.result as string)
      }
      img.src = event.target?.result as string
    }
    reader.onerror = () => reject(new Error('无法读取文件'))
    reader.readAsDataURL(file)
  })
}

// 验证文件
const validateFile = (file: File): { valid: boolean; error?: string } => {
  if (file.size > FILE_VALIDATION_CONFIG.maxFileSize) {
    return {
      valid: false,
      error: `文件过大，最大支持${FILE_VALIDATION_CONFIG.maxFileSize / 1024 / 1024}MB`
    }
  }

  const isImage = FILE_VALIDATION_CONFIG.supportedImageFormats.includes(file.type)
  const isVideo = FILE_VALIDATION_CONFIG.supportedVideoFormats.includes(file.type)

  if (!isImage && !isVideo) {
    return {
      valid: false,
      error: '不支持的文件格式，仅支持常见的图片和视频格式'
    }
  }

  return { valid: true }
}

// 文件上传管理
export const useFileUpload = () => {
  const filePreviews = ref<FilePreview[]>([])

  const handleFileUpload = async (event: Event): Promise<void> => {
    const target = event.target as HTMLInputElement
    const files = Array.from(target.files || [])
    
    console.log(`用户选择了 ${files.length} 个文件`)

    if (filePreviews.value.length + files.length > 5) {
      ElMessage.error('最多只能上传5个文件')
      return
    }

    for (const file of files) {
      console.log(`处理文件: ${file.name}, 大小: ${file.size}, 类型: ${file.type}`)
      
      const validation = validateFile(file)
      if (!validation.valid) {
        ElMessage.error(`文件 "${file.name}" 验证失败：${validation.error}`)
        continue
      }

      try {
        const mediaType = file.type.startsWith('image/') ? 'image' : 'video'
        let previewUrl = URL.createObjectURL(file)
        let thumbnailUrl = mediaType === 'image' ? await generateImageThumbnail(file) : previewUrl

        filePreviews.value.push({
          file,
          previewUrl,
          thumbnailUrl,
          mediaType,
          isValid: true
        })

        console.log(`文件 "${file.name}" 已添加到预览列表`)
        ElMessage.success(`文件 "${file.name}" 添加成功`)
      } catch (error) {
        console.error(`处理文件 "${file.name}" 失败:`, error)
        ElMessage.error(`文件 "${file.name}" 处理失败：${error instanceof Error ? error.message : '未知错误'}`)
      }
    }

    target.value = ''
    console.log(`当前预览列表中共有 ${filePreviews.value.length} 个文件`)
  }

  const removeFile = (index: number): void => {
    const preview = filePreviews.value[index]
    if (preview && preview.previewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(preview.previewUrl)
    }
    filePreviews.value.splice(index, 1)
    ElMessage.success('文件已删除')
  }

  const clearAllFiles = (): void => {
    filePreviews.value.forEach((preview) => {
      if (preview.previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(preview.previewUrl)
      }
    })
    filePreviews.value = []
  }

  const getUploadFiles = (): File[] => {
    const files = filePreviews.value.map((preview) => preview.file)
    console.log(`获取上传文件列表，共 ${files.length} 个文件`)
    return files
  }

  const getFileIcon = (mediaType: string): string => {
    if (mediaType === 'image') return '🖼️'
    if (mediaType === 'video') return '🎬'
    return '📄'
  }

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
  }

  return {
    filePreviews,
    handleFileUpload,
    removeFile,
    clearAllFiles,
    getUploadFiles,
    getFileIcon,
    formatFileSize
  }
}

// 媒体查看管理
export const useMediaModal = () => {
  const showMediaModal = ref(false)
  const currentMediaList = ref<MediaFile[]>([])
  const mediaLoading = ref(false)
  const currentMediaIndex = ref(0) // 添加当前媒体索引

  const openMediaModal = async (mediaList: MediaFile[], recordId?: number): Promise<void> => {
    currentMediaIndex.value = 0 // 重置索引
    
    if (recordId) {
      mediaLoading.value = true
      try {
        const response = await request.get(`/media/record/${recordId}`)
        if (response.code === 200 && response.data) {
          const mediaFiles = Array.isArray(response.data) ? response.data : [response.data]
          currentMediaList.value = mediaFiles.map((m: any) => ({
            id: m.mid,
            media_url: `/api${m.filePath}`,
            media_type: m.mediaType,
            media_name: m.fileName,
            updated_at: m.updatedAt
          }))
        } else {
          currentMediaList.value = []
        }
      } catch (error) {
        console.error(`加载媒体文件失败:`, error)
        ElMessage.error('加载媒体文件失败')
        currentMediaList.value = []
      } finally {
        mediaLoading.value = false
      }
    } else {
      currentMediaList.value = mediaList || []
    }
    
    showMediaModal.value = true
  }

  const closeMediaModal = (): void => {
    showMediaModal.value = false
    currentMediaList.value = []
    currentMediaIndex.value = 0
  }
  
  // 添加切换媒体的方法
  const nextMedia = (): void => {
    if (currentMediaList.value.length > 1) {
      currentMediaIndex.value = (currentMediaIndex.value + 1) % currentMediaList.value.length
    }
  }
  
  const prevMedia = (): void => {
    if (currentMediaList.value.length > 1) {
      currentMediaIndex.value = (currentMediaIndex.value - 1 + currentMediaList.value.length) % currentMediaList.value.length
    }
  }
  
  const selectMedia = (index: number): void => {
    if (index >= 0 && index < currentMediaList.value.length) {
      currentMediaIndex.value = index
    }
  }

  return {
    showMediaModal,
    currentMediaList,
    mediaLoading,
    currentMediaIndex, // 导出当前媒体索引
    openMediaModal,
    closeMediaModal,
    nextMedia, // 导出切换方法
    prevMedia,
    selectMedia
  }
}

// 事件数据获取
export const useEventData = () => {
  const { t } = useI18n()
  const events = ref<PetRecord[]>([])
  const loading = ref(false)

  const fetchEvents = async (): Promise<void> => {
    loading.value = true
    try {
      const response = await request.get('/events')
      
      if (response.code === 200 && response.data) {
        events.value = response.data.map((event: any) => ({
          record_id: event.eid,
          pid: event.pid,
          event_type: event.eventType,
          description: event.description,
          record_time: event.eventTime,
          mood: event.mood,
          location: event.location,
          pet_name: event.pet_name || '',
          created_at: event.createdAt || event.eventTime,
          updated_at: event.updatedAt || event.createdAt || event.eventTime,
          media_list: event.mediaList ? event.mediaList.map((m: any) => ({
            id: m.mid,
            media_url: `/api${m.filePath}`,
            media_type: m.mediaType,
            media_name: m.fileName,
            updated_at: m.updatedAt
          })) : []
        }))
      } else {
        events.value = []
      }
    } catch (error) {
      console.error('获取事件失败:', error)
      ElMessage.error(t('api.getEventsFailed'))
      events.value = []
    } finally {
      loading.value = false
    }
  }

  return {
    events,
    loading,
    fetchEvents
  }
}

// 宠物数据获取
export const usePetData = () => {
  const { t } = useI18n()
  const pets = ref<PetInfo[]>([])
  const loading = ref(false)

  const fetchPets = async (): Promise<void> => {
    loading.value = true
    try {
      const adoptionData = await request.get('/user/adoptions', {
        params: {
          current_page: 1,
          per_page: 100
        }
      })
      
      if (adoptionData.code === 200 && adoptionData.data?.records) {
        pets.value = adoptionData.data.records.map((record: any) => ({
          pid: record.pid || record.id,
          name: record.name || record.petName,
          species: record.breed || record.species || '未知',
          breed: record.breed || record.species || '未知',
          type: 'adoption' as const,
          date: record.adoptionDate || record.adoptDate || record.date
        }))
      } else {
        pets.value = []
      }
    } catch (error) {
      ElMessage.error(t('api.getPetsFailed'))
      console.error('Error fetching pets:', error)
      pets.value = []
    } finally {
      loading.value = false
    }
  }

  const getPetName = (pid: number, petName?: string): string => {
    const pet = pets.value.find(p => p.pid === pid)
    if (pet) {
      return pet.name
    }
    
    if (petName) {
      return petName
    }
    
    return t('api.unknownPet')
  }

  return {
    pets,
    loading,
    fetchPets,
    getPetName
  }
}

// 事件操作管理
export const useEventOperations = (eventData?: { fetchEvents: () => Promise<void> }) => {
  const { t } = useI18n()
  const { fetchEvents } = eventData || useEventData()

  const deleteEvent = async (recordId: number): Promise<void> => {
    try {
      if (!recordId || recordId <= 0) {
        console.error('Invalid recordId:', recordId)
        ElMessage.error('无效的记录ID')
        return
      }
      
      await ElMessageBox.confirm(t('common.confirmDeleteEvent'), t('common.deleteConfirmation'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
      
      await request.delete(`/events/${recordId}`)
      ElMessage.success(t('api.deleteSuccess'))
      await fetchEvents()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(t('api.deleteFailed'))
        console.error('Error deleting event:', error)
      }
    }
  }

  return {
    deleteEvent
  }
}

// 筛选和排序逻辑
export const useRecordFilters = (events: Ref<PetRecord[]>) => {
  const filters = reactive<RecordFilters>({
    selectedPet: '',
    selectedEventType: '',
    selectedMood: '',
    selectedDate: ''
  })

  const filteredEvents = computed(() => {
    let filtered = [...events.value]

    if (filters.selectedPet) {
      filtered = filtered.filter(event => event.pid === Number(filters.selectedPet))
    }

    if (filters.selectedEventType) {
      filtered = filtered.filter(event => event.event_type === filters.selectedEventType)
    }

    if (filters.selectedMood) {
      filtered = filtered.filter(event => event.mood === filters.selectedMood)
    }

    if (filters.selectedDate) {
      filtered = filtered.filter(event => 
        new Date(event.record_time).toDateString() === new Date(filters.selectedDate).toDateString()
      )
    }

    return filtered
  })

  return {
    filters,
    filteredEvents
  }
}

// 排序逻辑
export const useRecordSorting = (events: Ref<PetRecord[]>) => {
  const sortBy = ref<SortField>('record_time')

  const sortedEvents = computed(() => {
    const sorted = [...events.value]
    
    sorted.sort((a, b) => {
      switch (sortBy.value) {
        case 'event_type':
          return a.event_type.localeCompare(b.event_type)
        case 'mood':
          return (a.mood || '').localeCompare(b.mood || '')
        case 'record_time':
        default:
          return new Date(b.record_time).getTime() - new Date(a.record_time).getTime()
      }
    })
    
    return sorted
  })

  return {
    sortBy,
    sortedEvents
  }
}

// 时间线分组
export const useTimelineGrouping = (events: Ref<PetRecord[]>) => {
  const groupedEvents = computed(() => {
    const groups: Record<string, PetRecord[]> = {}
    events.value.forEach(event => {
      const date = new Date(event.record_time).toDateString()
      if (!groups[date]) {
        groups[date] = []
      }
      groups[date].push(event)
    })
    return groups
  })

  return { groupedEvents }
}

// 表单管理
export const useRecordForm = () => {
  const formData = reactive<RecordFormData>({
    record_id: '',
    pid: '',
    event_type: '',
    mood: '',
    description: '',
    location: '',
    record_time: ''
  })

  const resetForm = (): void => {
    Object.assign(formData, {
      record_id: '',
      pid: '',
      event_type: '',
      mood: '',
      description: '',
      location: '',
      record_time: ''
    })
  }

  const populateForm = (event: PetRecord): void => {
    let formattedDateTime = ''
    try {
      if (event.record_time) {
        const isoString = event.record_time.replace(' ', 'T')
        const date = new Date(isoString)
        if (!isNaN(date.getTime())) {
          formattedDateTime = date.toISOString().slice(0, 16)
        } else {
          const parts = event.record_time.split(' ')
          if (parts.length === 2) {
            formattedDateTime = `${parts[0]}T${parts[1].slice(0, 5)}`
          }
        }
      }
    } catch (error) {
      console.error('Error parsing date:', error, event.record_time)
    }
    
    Object.assign(formData, {
      ...event,
      record_time: formattedDateTime
    })
  }

  return {
    formData,
    resetForm,
    populateForm
  }
}

// 主组合式函数 - 记录管理
export const useRecords = (): UseRecordsReturn => {
  // 基础数据
  const { events, loading, fetchEvents } = useEventData()
  const { pets, fetchPets, getPetName } = usePetData()
  
  // 工具函数
  const { getMoodEmoji } = useMoodEmoji()
  const { getMoodClass } = useMoodClass()
  const { formatDate } = useDateFormatter()
  
  // 文件和模态框管理
  const { 
    filePreviews, 
    handleFileUpload, 
    removeFile, 
    clearAllFiles, 
    getUploadFiles, 
    getFileIcon, 
    formatFileSize 
  } = useFileUpload()
  
  const { 
    showMediaModal, 
    currentMediaList, 
    mediaLoading, 
    currentMediaIndex, // 添加当前媒体索引
    openMediaModal, 
    closeMediaModal,
    nextMedia, // 添加切换方法
    prevMedia,
    selectMedia
  } = useMediaModal()
  
  // 筛选和排序
  const { filters, filteredEvents } = useRecordFilters(events)
  const { sortBy, sortedEvents } = useRecordSorting(filteredEvents)
  const { groupedEvents } = useTimelineGrouping(sortedEvents)
  
  // 表单管理
  const { formData, resetForm, populateForm } = useRecordForm()
  
  // 操作管理
  const { deleteEvent } = useEventOperations({ fetchEvents })
  
  // 模态框状态
  const showAddModal = ref(false)
  const showEditModal = ref(false)
  
  // 视图模式
  const viewMode = ref<ViewMode>('grid')
  
  // 提交状态
  const isSubmitting = ref(false)
  
  // 计算属性
  const totalEvents = computed(() => events.value.length)
  const totalMedia = computed(() => 
    events.value.reduce((total, event) => total + (event.media_list?.length || 0), 0)
  )
  
  // 表单操作
  const editEvent = (event: PetRecord): void => {
    populateForm(event)
    showEditModal.value = true
  }
  
  const closeModal = (): void => {
    showAddModal.value = false
    showEditModal.value = false
    clearAllFiles()
    resetForm()
  }
  
  // 保存事件
  const saveEventHandler = async (): Promise<void> => {
    // 防止重复提交
    if (isSubmitting.value) {
      ElMessage.warning('正在保存中，请勿重复提交')
      return
    }
    
    isSubmitting.value = true
    
    try {
      const recordTimeISO = formData.record_time
      const recordTimeFormatted = recordTimeISO ? recordTimeISO.replace('T', ' ') + ':00' : ''
        
      const data = {
        pid: formData.pid,
        eventType: formData.event_type,
        recordTime: recordTimeFormatted,
        description: formData.description,
        mood: formData.mood || '',
        location: formData.location || ''
      }
  
      let recordId: number | null = null
      if (showEditModal.value && formData.record_id) {
        await request.put(`/events/${formData.record_id}`, data)
        recordId = Number(formData.record_id)
        ElMessage.success('事件更新成功')
      } else {
        const response = await request.post('/events', data)
        recordId = response.data?.eid
        ElMessage.success('事件添加成功')
      }
  
      // 只有在成功创建/更新事件后才上传媒体文件
      if (recordId) {
        const uploadFiles = getUploadFiles()
        console.log(`准备上传的文件数量: ${uploadFiles ? uploadFiles.length : 0}`)
        
        if (uploadFiles && uploadFiles.length > 0) {
          try {
            // 确保所有文件一次性上传，并关联到同一个记录ID
            console.log('开始上传媒体文件...')
            const uploadResult = await uploadMediaFiles(uploadFiles, recordId)
            console.log('媒体文件上传结果:', uploadResult)
            ElMessage.success('事件保存成功，媒体文件上传完成')
          } catch (uploadError) {
            console.warn('流程警告：事件保存成功，但媒体文件上传失败', uploadError)
            ElMessage.warning('事件保存成功，但媒体文件上传失败')
          }
        } else {
          console.log('没有文件需要上传')
        }
      }
      
      // 所有操作完成后，刷新数据并关闭模态框
      await fetchEvents()
      closeModal()
    } catch (error) {
      ElMessage.error(showEditModal.value ? '事件更新失败' : '事件添加失败')
      console.error('Error saving event:', error)
    } finally {
      // 确保无论成功还是失败，都重置提交状态
      isSubmitting.value = false
    }
  }

  return {
    // 数据状态
    events,
    pets,
    loading,
    
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
    mediaLoading,
    currentMediaIndex, // 导出当前媒体索引
    
    // 表单数据
    formData,
    filePreviews,
    currentMediaList,
    
    // 计算属性
    totalEvents,
    totalMedia,
    filteredEvents,
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
    nextMedia, // 导出切换方法
    prevMedia,
    selectMedia,
    editEvent,
    deleteEvent,
    saveEvent: saveEventHandler,
    closeModal
  }
}
