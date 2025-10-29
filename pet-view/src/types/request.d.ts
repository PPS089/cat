import { AxiosInstance } from 'axios'

declare module 'axios' {
  interface AxiosInstance {
    upload: (url: string, file: File) => Promise<any>
  }
}

export {}