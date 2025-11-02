import request from '@/utils/request'


/**
 * 上传媒体文件
 * @param files 文件数组
 * @param recordId 记录ID
 */
export const uploadMediaFiles = async (
  files: File[],
  recordId: string | number
): Promise<any> => {
  const formData = new FormData()
  
  // 添加所有文件
  files.forEach((file) => {
    formData.append('files', file)
  })
  
  // 添加记录ID
  formData.append('recordId', String(recordId))
  
  
  return request.post('/media/upload', formData)
}

/**
 * 获取记录的媒体文件列表
 * @param recordId 记录ID
 */
export const getRecordMedia = async (recordId: string | number): Promise<any> => {
  return request.get(`/media/record/${recordId}`)
}

/**
 * 删除媒体文件
 * @param mediaId 媒体文件ID
 */
export const deleteMediaFile = async (mediaId: number): Promise<any> => {
  return request.delete(`/media/${mediaId}`)
}

/**
 * 获取媒体文件URL
 * @param fileName 文件名
 */
export const getMediaUrl = (fileName: string): string => {
  return `/api/media/download/${fileName}`
}
