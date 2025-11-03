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
  // 检查是否有文件需要上传
  if (!files || files.length === 0) {
    console.warn('没有文件需要上传')
    return { success: true, message: '没有文件需要上传' }
  }

  const formData = new FormData()
  
  // 添加所有文件，确保使用相同的字段名
  files.forEach((file) => {
    formData.append('files', file)
  })
  
  // 添加记录ID，确保所有文件关联到同一个记录
  formData.append('recordId', String(recordId))
  
  // 添加一个标识，表明这是批量上传
  formData.append('batchUpload', 'true')
  
  console.log(`开始批量上传 ${files.length} 个文件到记录 ${recordId}`)
  
  try {
    const response = await request.post('/media/upload', formData)
    console.log('批量上传完成，响应:', response)
    return response
  } catch (error) {
    console.error('批量上传失败:', error)
    throw error
  }
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
