export const pending: Record<string, boolean> = {}

export const getRequestKey = (config: any): string => {
  const { url, method, params, data } = config
  return `${url}&${method}&${JSON.stringify(params)}&${JSON.stringify(data)}`
}

export const removePending = (key: string): void => {
  delete pending[key]
}
