import { ref, computed, reactive, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
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

/**
 * 获取心情表情
 */
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

/**
 * 获取心情样式类
 */
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

/**
 * 日期格式化
 */
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

/**
 * 文件验证配置
 */
const FILE_VALIDATION_CONFIG: FileValidationConfig = {
  maxFileSize: 52428800,  // 50MB
  supportedImageFormats: ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/jpg'],
  supportedVideoFormats: ['video/mp4', 'video/mpeg', 'video/quicktime', 'video/x-msvideo', 'video/webm'],
  thumbnailMaxWidth: 200,
  thumbnailMaxHeight: 200
}

/**
 * 生成图片缩略图
 */
const generateImageThumbnail = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (event) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        if (!ctx) {
          resolve(event.target?.result as string)  // 如果失败则返回原始URL
          return
        }

        // 计算缩放尺寸
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
        resolve(canvas.toDataURL('image/jpeg', 0.7))  // 使用JPEG格式，质量70%
      }
      img.onerror = () => {
        resolve(event.target?.result as string)  // 如果失败则返回原始URL
      }
      img.src = event.target?.result as string
    }
    reader.onerror = () => reject(new Error('无法读取文件'))
    reader.readAsDataURL(file)
  })
}

/**
 * 验证文件
 */
const validateFile = (file: File): { valid: boolean; error?: string } => {
  // 检查文件大小
  if (file.size > FILE_VALIDATION_CONFIG.maxFileSize) {
    return {
      valid: false,
      error: `文件过大，最大支持${FILE_VALIDATION_CONFIG.maxFileSize / 1024 / 1024}MB`
    }
  }

  // 检查文件类型
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

/**
 * 文件上传管理（增强版本）
 */
export const useFileUpload = () => {
  const filePreviews = ref<FilePreview[]>([])

  /**
   * 处理文件上传
   */
  const handleFileUpload = async (event: Event): Promise<void> => {
    const target = event.target as HTMLInputElement
    const files = Array.from(target.files || [])

    // 检查文件数量限制
    if (filePreviews.value.length + files.length > 5) {
      ElMessage.error('最多只能上传5个文件')
      return
    }

    // 处理每个文件
    for (const file of files) {
      // 验证文件
      const validation = validateFile(file)
      if (!validation.valid) {
        ElMessage.error(`文件 "${file.name}" 验证失败：${validation.error}`)
        continue
      }

      try {
        // 获取媒体类型
        const mediaType = file.type.startsWith('image/') ? 'image' : 'video'

        // 生成预览URL
        let previewUrl = URL.createObjectURL(file)
        let thumbnailUrl = previewUrl

        // 对于图片，生成缩略图
        if (mediaType === 'image') {
          thumbnailUrl = await generateImageThumbnail(file)
        } else {
          // 对于视频，使用原始对象URL作为预览
          thumbnailUrl = previewUrl
        }

        // 添加到预览列表
        filePreviews.value.push({
          file,
          previewUrl,
          thumbnailUrl,
          mediaType,
          isValid: true
        })

        ElMessage.success(`文件 "${file.name}" 添加成功`)
      } catch (error) {
        ElMessage.error(`文件 "${file.name}" 处理失败：${error instanceof Error ? error.message : '未知错误'}`)
      }
    }

    // 清空input，允许再次选择同一文件
    target.value = ''
  }

  /**
   * 删除文件预览
   */
  const removeFile = (index: number): void => {
    const preview = filePreviews.value[index]
    if (preview) {
      // 释放对象URL
      if (preview.previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(preview.previewUrl)
      }
    }
    filePreviews.value.splice(index, 1)
    ElMessage.success('文件已删除')
  }

  /**
   * 清空所有文件
   */
  const clearAllFiles = (): void => {
    filePreviews.value.forEach((preview) => {
      if (preview.previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(preview.previewUrl)
      }
    })
    filePreviews.value = []
  }

  /**
   * 获取待上传的文件数组
   */
  const getUploadFiles = (): File[] => {
    return filePreviews.value.map((preview) => preview.file)
  }

  /**
   * 获取文件图标
   */
  const getFileIcon = (mediaType: string): string => {
    if (mediaType === 'image') {
      return '🖼️'
    } else if (mediaType === 'video') {
      return '🎬'
    }
    return '📄'
  }

  /**
   * 格式化文件大小
   */
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

/**
 * 媒体查看管理
 */
export const useMediaModal = () => {
  const showMediaModal = ref(false)
  const currentMediaList = ref<MediaFile[]>([])
  const mediaLoading = ref(false)

  const openMediaModal = async (mediaList: MediaFile[], recordId?: number): Promise<void> => {
    // 如果传入了recordId，则从后端加载该记录的媒体列表
    if (recordId) {
      mediaLoading.value = true
      try {
        const response = await request.get(`/media/record/${recordId}`)
        console.log(`后端返回的媒体数据:`, response)
        if (response.code === 200 && response.data) {
          // response.data 是 MediaFileVo[] 数组，需要转换为前端的 MediaFile 格式
          const mediaFiles = Array.isArray(response.data) ? response.data : [response.data]
          currentMediaList.value = mediaFiles.map((m: any) => ({
            id: m.mid || m.id,
            media_url: `/api${m.filePath || m.mediaUrl || m.media_url}`,  // 添加 /api 前缀，映射 filePath → media_url
            media_type: m.mediaType || m.media_type || 'image',
            media_name: m.fileName || m.name || ''
          }))
          console.log(`已加载记录 ${recordId} 的媒体文件:`, currentMediaList.value)
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
      // 如果没有传recordId，则使用传入的mediaList
      currentMediaList.value = mediaList || []
    }
    
    showMediaModal.value = true
  }

  const closeMediaModal = (): void => {
    showMediaModal.value = false
    currentMediaList.value = []
  }

  return {
    showMediaModal,
    currentMediaList,
    mediaLoading,
    openMediaModal,
    closeMediaModal
  }
}

/**
 * 事件数据获取
 */
export const useEventData = () => {
  const { t } = useI18n()

  
  // 事件数据
  const events = ref<PetRecord[]>([])
  const loading = ref(false)

  const fetchEvents = async (): Promise<void> => {
    console.log(t('fetchEventsFunctionStart'))
    loading.value = true
    try {
      const response = await request.get('/events')
      console.log(t('fetchEventsApiResponse'), response)
      
      // 适配实际的API响应格式 - 根据实际响应结构调整判断条件
      if (response.code === 200 && response.data) {
        console.log('fetchEvents: 事件数量:', response.data.length)
        // 转换后端数据格式到前端格式，并验证数据完整性
        const newEvents = response.data.map((event: any) => {
          // 验证必需字段
          const recordId = event.record_id || event.eid || event.id
          if (!recordId) {
            console.warn('Event missing record_id:', event)
            return null
          }
          
          // 验证时间字段
          const recordTime = event.record_time || event.eventTime || event.event_time
          if (!recordTime) {
            console.warn('Event missing record_time:', event)
            return null
          }
          
          return {
            record_id: recordId,
            pid: event.pid || 0,
            event_type: event.eventType || event.event_type || '未知',
            description: event.description || '',
            record_time: recordTime,
            mood: event.mood || '',
            location: event.location || '',
            pet_name: event.pet_name || '',
            created_at: event.createdAt || event.created_at || recordTime,
            // 使用后端返回的 mediaList，如果没有则使用旧的 mediaUrl 兼容
            media_list: event.mediaList && Array.isArray(event.mediaList) 
              ? event.mediaList.map((m: any) => ({
                  id: m.mid || m.id || 1,
                  media_url: `/api${m.filePath || m.media_url || m.url}`,
                  media_type: m.mediaType || m.media_type || 'image',
                  media_name: m.fileName || m.media_name || ''
                }))
              : (event.mediaUrl ? [{ 
                  id: 1, 
                  media_url: event.mediaUrl, 
                  media_type: event.mediaType || 'image',
                  media_name: ''
                }] : [])
          }
        }).filter(Boolean) // 过滤掉无效的事件
        
    
        events.value = newEvents
        console.log('fetchEvents: 更新事件数据后，新事件数量:', events.value.length)
      } else {
        events.value = []
        console.warn(t('fetchEventsApiResponseFormatIncorrect'))
      }
    } catch (error) {
      console.error('fetchEvents: 获取事件失败:', error)
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

/**
 * 宠物数据获取 - 只获取领养记录和pets表判断是否被领养，跟寄养没关系
 */
export const usePetData = () => {
  const { t } = useI18n()
  const userStore = useUserStore()
  
  // 宠物数据
  const pets = ref<PetInfo[]>([])
  const loading = ref(false)

  const fetchPets = async (): Promise<void> => {
   

    loading.value = true
    try {
      console.log(t('fetchPetsStartGettingPetData'))
      console.log(t('fetchPetsCurrentUserInfo'), {
        user_id: userStore.info.userId,
        userName: userStore.info.userName,
        token: localStorage.getItem('jwt_token') || localStorage.getItem('token')
      })
      
      // 只获取领养记录 - 事件记录只需要判断宠物是否被领养
      console.log(t('fetchPetsReadyToCallApiUserAdoptions'))
      const adoptionData = await request.get('/user/adoptions', {
        params: {
          current_page: 1,
          per_page: 100  // 获取足够多的记录以确保包含所有相关宠物
        }
      })
      
      console.log('fetchPets: API响应数据:', JSON.stringify(adoptionData, null, 2))
      

      
      // 1. 标准格式: { code: 200, data: { records: [...] } }
      if (adoptionData.code === 200 ) {
         const records = adoptionData.data.records
       
     
      
      if (records && Array.isArray(records) && records.length > 0) {
        console.log(t('fetchPetsStartProcessingAdoptionRecords'))
        const adoptionPets = records.map((record: any) => ({
          pid: record.pid || record.id,
          name: record.name || record.petName,
          species: record.breed || record.species || '未知',
          breed: record.breed || record.species || '未知',
          type: 'adoption' as const,
          date: record.adoptionDate || record.adoptDate || record.date
        }))
        console.log('fetchPets: 映射后的宠物数组:', adoptionPets)
        pets.value = adoptionPets
        console.log('fetchPets: 最终pets.value数量:', pets.value.length)
      }
     } else {
        pets.value = []
        console.log('fetchPets: 没有获取到领养记录数据')
      }
    } catch (error) {
      ElMessage.error(t('api.getPetsFailed'))
      console.error('Error fetching pets:', error)
      pets.value = []
    } finally {
      loading.value = false
      console.log('fetchPets: 最终pets.value:', pets.value)
    }
  }

  const getPetName = (pid: number, petName?: string): string => {
    console.log(`getPetName: 查找PID ${pid}, 可用宠物数量: ${pets.value.length}, 宠物列表:`, pets.value.map(p => ({pid: p.pid, name: p.name})))
    const pet = pets.value.find(p => p.pid === pid)
    if (pet) {
      console.log(`getPetName: PID ${pid} 找到宠物: ${pet.name}`)
      return pet.name
    }
    
    // 如果找不到宠物，但有提供宠物名字，则使用提供的名字
    if (petName) {
      console.log(`getPetName: PID ${pid} 未找到宠物，使用提供的名字: ${petName}`)
      return petName
    }
    
    console.log(`getPetName: PID ${pid} 未找到宠物且没有提供名字，返回未知宠物`)
    return t('api.unknownPet')
  }

  return {
    pets,
    loading,
    fetchPets,
    getPetName
  }
}

/**
 * 事件操作管理
 */
export const useEventOperations = (eventData?: { fetchEvents: () => Promise<void> }) => {
  const { t } = useI18n()
  const { fetchEvents } = eventData || useEventData()

  const deleteEvent = async (recordId: number): Promise<void> => {
    try {
      // 验证 recordId 是否有效
      if (!recordId || recordId === undefined || recordId <= 0) {
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
      console.log('deleteEvent: 开始重新获取事件数据...')
      await fetchEvents()
      console.log('deleteEvent: 事件数据重新获取完成')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(t('api.deleteFailed'))
        console.error('Error deleting event:', error)
      }
    }
  }

  const saveEvent = async (
    formData: RecordFormData,
    isEdit: boolean
  ): Promise<void> => {
    try {
      // 转换日期时间格式：将 ISO 格式 (2025-10-24T17:07) 转换为后端支持的格式 (2025-10-24 17:07:00)
      const recordTimeISO = formData.record_time; // 如: "2025-10-24T17:07"
      const recordTimeFormatted = recordTimeISO ? recordTimeISO.replace('T', ' ') + ':00' : '';
      
      // 转换前端字段到后端API字段格式
      const data = {
        pid: formData.pid,
        eventType: formData.event_type,
        recordTime: recordTimeFormatted, // 使用格式化后的时间
        description: formData.description,
        mood: formData.mood || '',
        location: formData.location || ''
      }

      // 1. 先创建或更新事件记录
      let recordId: number | null = null
      if (isEdit && formData.record_id) {
        await request.put(`/events/${formData.record_id}`, data)
        recordId = Number(formData.record_id)
        ElMessage.success(t('api.updateSuccess'))
      } else {
        const response = await request.post('/events', data)
        recordId = response.data?.eid || response.data?.record_id || response.data?.id
        ElMessage.success(t('api.addSuccess'))
      }

      // 2. 然后上传文件（如果有的话）
      // NOTE: 这里使用空数组作为默认值，实际的文件列表由useRecords中的getUploadFiles提供
      const uploadFiles: File[] = [] // 占位符，实际值会在useRecords中处理
      if (uploadFiles && uploadFiles.length > 0 && recordId) {
        try {
          console.log(`上传流程：开始上传${uploadFiles.length}个文件...`)
          await uploadMediaFiles(uploadFiles, recordId)
          ElMessage.success('流程完成：事件和媒体文件上传成功')
        } catch (uploadError) {
          console.warn('流程警告：事件保存成功，但媒体文件上传失败', uploadError)
          ElMessage.warning('事件保存成功，但媒体文件上传失败')
        }
      }

      // 3. 缓冲时间后刷新事件列表
      console.log('saveEvent: 开始重新获取事件数据...')
      setTimeout(async () => {
        await fetchEvents()
        console.log('saveEvent: 事件数据重新获取完成')
      }, 500)
    } catch (error) {
      ElMessage.error(isEdit ? t('api.updateFailed') : t('api.addFailed'))
      console.error('Error saving event:', error)
      if ((error as any).response) {
        console.error('Error response:', (error as any).response.data)
        console.error('Error status:', (error as any).response.status)
      }
      throw error
    }
  }

  return {
    deleteEvent,
    saveEvent
  }
}

/**
 * 筛选和排序逻辑
 */
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

/**
 * 排序逻辑
 */
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

/**
 * 时间线分组
 */
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

/**
 * 表单管理
 */
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
    // 修复日期格式转换：处理后端返回的 "2025-10-24 17:07:00" 格式
    let formattedDateTime = ''
    try {
      if (event.record_time) {
        // 替换空格为 T，使其成为有效的 ISO 格式
        const isoString = event.record_time.replace(' ', 'T')
        const date = new Date(isoString)
        if (!isNaN(date.getTime())) {
          formattedDateTime = date.toISOString().slice(0, 16)
        } else {
          console.warn('Invalid date format:', event.record_time)
          // 尝试直接解析原始格式
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

/**
 * 主组合式函数 - 记录管理
 */
export const useRecords = (): UseRecordsReturn => {
  // 基础数据
  const { events, loading, fetchEvents } = useEventData()
  const { pets, fetchPets, getPetName } = usePetData()
  
  // 工具函数
  const { getMoodEmoji } = useMoodEmoji()
  const { getMoodClass } = useMoodClass()
  const { formatDate } = useDateFormatter()
  
  // 文件和模态框管理
  const { filePreviews, handleFileUpload, removeFile, clearAllFiles, getUploadFiles, getFileIcon, formatFileSize } = useFileUpload()
  const { showMediaModal, currentMediaList, mediaLoading, openMediaModal, closeMediaModal } = useMediaModal()
  
  // 筛选和排序
  const { filters, filteredEvents } = useRecordFilters(events)
  const { sortBy, sortedEvents } = useRecordSorting(filteredEvents)
  const { groupedEvents } = useTimelineGrouping(sortedEvents)
  
  // 表单管理
  const { formData, resetForm, populateForm } = useRecordForm()
  
  // 操作管理 - 确保使用相同的事件数据实例
  const { deleteEvent } = useEventOperations({ fetchEvents })
  
  // 模态框状态
  const showAddModal = ref(false)
  const showEditModal = ref(false)
  
  // 视图模式
  const viewMode = ref<ViewMode>('grid')
  
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
  
  // 保存事件（包装saveEvent以適配组件使用）
  const saveEventHandler = async (): Promise<void> => {
    try {
      // 转换日期时间格式·将 ISO 格式 (2025-10-24T17:07) 转换为后端支持的格式 (2025-10-24 17:07:00)
      const recordTimeISO = formData.record_time
      const recordTimeFormatted = recordTimeISO ? recordTimeISO.replace('T', ' ') + ':00' : ''
        
      // 转换前端字段到后端API字段格式
      const data = {
        pid: formData.pid,
        eventType: formData.event_type,
        recordTime: recordTimeFormatted,
        description: formData.description,
        mood: formData.mood || '',
        location: formData.location || ''
      }
  
      // 1. 先创建或更新事件记录
      let recordId: number | null = null
      if (showEditModal.value && formData.record_id) {
        await request.put(`/events/${formData.record_id}`, data)
        recordId = Number(formData.record_id)
        ElMessage.success('事件更新成功')
      } else {
        const response = await request.post('/events', data)
        recordId = response.data?.eid || response.data?.record_id || response.data?.id
        ElMessage.success('事件添加成功')
      }
  
      // 2. 然后上传文件（如果有的话）
      const uploadFiles = getUploadFiles()
      if (uploadFiles && uploadFiles.length > 0 && recordId) {
        try {
          console.log(`流头模元：开始上传${uploadFiles.length}个流传文件...`)
          await uploadMediaFiles(uploadFiles, recordId)
          ElMessage.success('事件保存成功，媒体文件上传完成')
        } catch (uploadError) {
          console.warn('流头模块警告：事件添加成功，但流传文件上传失败', uploadError)
          ElMessage.warning('事件添加成功，但流传文件上传失败')
        }
      }
  
      // 3. 缓冲时间后刷新事件列表
      console.log('saveEventHandler: 开始重新获取事件数据...')
      setTimeout(async () => {
        await fetchEvents()
        console.log('saveEventHandler: 事件数据重新获取完成')
        closeModal()
      }, 500)
    } catch (error) {
      ElMessage.error(showEditModal.value ? '事件更新失败' : '事件添加失败')
      console.error('Error saving event:', error)
      if ((error as any).response) {
        console.error('Error response:', (error as any).response.data)
        console.error('Error status:', (error as any).response.status)
      }
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
    editEvent,
    deleteEvent,
    saveEvent: saveEventHandler,
    closeModal
  }
}