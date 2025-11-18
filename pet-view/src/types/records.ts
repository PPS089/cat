// 宠物记录相关类型声明

import type { ComputedRef, Ref } from 'vue'
import type { MediaFile, FilePreview, ViewMode, SortField } from './common'
import type { ApiResult, PageResult } from './api'

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

// 事件记录结果
export type EventRecordResult = ApiResult<PetRecord>

// 事件记录分页结果
export type EventRecordPageResult = PageResult<PetRecord>

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
  currentMediaIndex: Ref<number> // 添加当前媒体索引
  
  // 表单数据
  formData: RecordFormData
  filePreviews: Ref<FilePreview[]>  // 臭上次的 uploadedFiles
  isDragOver: Ref<boolean>
  currentMediaList: Ref<MediaFile[]>
  
  // 计算属性
  totalEvents: ComputedRef<number>
  totalMedia: ComputedRef<number>
  filteredEvents: ComputedRef<PetRecord[]>
  sortedEvents: ComputedRef<PetRecord[]>
  groupedEvents: ComputedRef<{ [key: string]: PetRecord[] }>
  
  // 方法
  fetchEvents: () => Promise<void>
  fetchPets: () => Promise<void>
  getPetName: (pid: number, petName?: string) => string
  getMoodEmoji: (mood: string) => string
  getMoodClass: (mood: string | undefined) => Record<string, boolean>
  formatDate: (date: string) => string
  handleFileUpload: (event: Event) => Promise<void>  // 现在是异步的
  handleDragOver: (event: DragEvent) => void
  handleDragLeave: () => void
  handleDrop: (event: DragEvent) => Promise<void>
  removeFile: (index: number) => void
  getFileIcon: (mediaType: string) => string
  formatFileSize: (bytes: number) => string
  openMediaModal: (mediaList: MediaFile[], recordId?: number) => Promise<void>
  refreshMediaList: (recordId: number) => Promise<void>
  closeMediaModal: () => void
  nextMedia: () => void // 添加切换方法
  prevMedia: () => void
  selectMedia: (index: number) => void
  editEvent: (event: PetRecord) => void
  deleteEvent: (recordId: number) => Promise<void>
  deleteMediaFile: (mid: number) => Promise<void>
  saveEvent: () => Promise<void>
  closeModal: () => void
}