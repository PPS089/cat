declare module '*.vue' {
  import { DefineComponent } from 'vue';
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

declare module '@/utils/request' {
  import type { AxiosInstance } from 'axios';
  const request: AxiosInstance;
  export default request;
}