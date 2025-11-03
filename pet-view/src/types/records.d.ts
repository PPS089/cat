// 宠物记录相关类型声明

import type { ComputedRef, Ref } from 'vue'

// 媒体文件类型
export interface MediaFile {
  id: number
  media_url: string
  media_type: 'image' | 'video'
  media_name?: string
  uploadTime?: string
  created_at?: string
  updated_at?: string
}

// 文件预览类型
export interface FilePreview {
  file: File
  previewUrl: string  // Data URL for images or blob URL
  thumbnailUrl: string  // Thumbnail URL for display
  mediaType: 'image' | 'video'
  validationError?: string  // 验证错误信息
  isValid: boolean  // 文件是否通过验证
}

// 文件验证配置
export interface FileValidationConfig {
  maxFileSize: number  // 最大文件大小（字节）
  supportedImageFormats: string[]  // 支持的图片格式
  supportedVideoFormats: string[]  // 支持的视频格式
  thumbnailMaxWidth: number  // 缩略图最大宽度
  thumbnailMaxHeight: number  // 缩略图最大高度
}

// 宠物记录类型
export interface PetRecord {
  record_id: number
  pid: number
  event_type: string
  description: string
  record_time: string
  mood?: string
  location?: string
  pet_name?: string
  created_at: string
  updated_at?: string
  media_list?: MediaFile[]
}

// 宠物基本信息类型
export interface PetInfo {
  pid: number
  name: string
  species: string
  breed: string
  type: 'adoption' // 只处理领养记录
  date?: string
}

// 记录表单数据类型
export interface RecordFormData {
  record_id?: string
  pid: string
  event_type: string
  mood: string
  description: string
  location: string
  record_time: string
}

// 筛选条件类型
export interface RecordFilters {
  selectedPet: string
  selectedEventType: string
  selectedMood: string
  selectedDate: string
}

// 视图模式
export type ViewMode = 'grid' | 'list' | 'timeline'

// 排序字段
export type SortField = 'record_time' | 'event_type' | 'mood'



// useRecords返回类型
export interface UseRecordsReturn {
  // 数据状态
  events: Ref<PetRecord[]>
  pets: Ref<PetInfo[]>
  loading: Ref<boolean>
  
  // 筛选状态
  filters: RecordFilters
  viewMode: Ref<ViewMode>
  sortBy: Ref<SortField>
  
  // 提交状态
  isSubmitting: Ref<boolean>
  
  // 模态框状态
  showAddModal: Ref<boolean>
  showEditModal: Ref<boolean>
  showMediaModal: Ref<boolean>
  mediaLoading: Ref<boolean>
  
  // 表单数据
  formData: RecordFormData
  filePreviews: Ref<FilePreview[]>  // 臭上次的 uploadedFiles
  currentMediaList: Ref<MediaFile[]>
  
  // 计算属性
  totalEvents: ComputedRef<number>
  totalMedia: ComputedRef<number>
  filteredEvents: ComputedRef<PetRecord[]>
  sortedEvents: ComputedRef<PetRecord[]>
  groupedEvents: ComputedRef<Record<string, PetRecord[]>>
  
  // 方法
  fetchEvents: () => Promise<void>
  fetchPets: () => Promise<void>
  getPetName: (pid: number, petName?: string) => string
  getMoodEmoji: (mood: string) => string
  getMoodClass: (mood: string | undefined) => Record<string, boolean>
  formatDate: (date: string) => string
  handleFileUpload: (event: Event) => Promise<void>  // 现在是异步的
  removeFile: (index: number) => void
  getFileIcon: (mediaType: string) => string
  formatFileSize: (bytes: number) => string
  openMediaModal: (mediaList: MediaFile[], recordId?: number) => Promise<void>
  closeMediaModal: () => void
  editEvent: (event: PetRecord) => void
  deleteEvent: (recordId: number) => Promise<void>
  saveEvent: () => Promise<void>
  closeModal: () => void
}