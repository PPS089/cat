// 通用类型定义

// 宠物性别枚举
export type PetGender = 'male' | 'female' | 'unknown'

// 宠物状态枚举（与后端 PetStatusEnum 保持一致）
export type PetStatus = 'AVAILABLE' | 'FOSTERING' | 'ADOPTED' | 'DECEASED'

// 收容所信息
export interface Shelter {
  sid: number
  shelterName: string
  shelterAddress: string
}

// 统一的宠物信息接口
export interface Pet {
  pid: number
  name: string
  species: string
  breed: string
  age: number
  gender: PetGender
  status: PetStatus
  imgUrl: string | null
  shelterName: string
  shelterAddress: string
  shelter?: Shelter
  adoptionDate?: string
}

// ID 类型
export type Id = string | number

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

// 视图模式
export type ViewMode = 'grid' | 'list' | 'timeline'

// 排序字段
export type SortField = 'record_time' | 'event_type' | 'mood'