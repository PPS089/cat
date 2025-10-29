// 请求优化工具函数 - 防止重复请求

// 存储待处理请求的键值对
export const pending: Record<string, boolean> = {}

// 生成请求的唯一键
export const getRequestKey = (config: any): string => {
  const { url, method, params, data } = config
  return `${url}&${method}&${JSON.stringify(params)}&${JSON.stringify(data)}`
}

// 检查请求是否已存在
export const checkPending = (key: string): boolean => {
  return !!pending[key]
}

// 移除待处理请求
export const removePending = (key: string): void => {
  delete pending[key]
}