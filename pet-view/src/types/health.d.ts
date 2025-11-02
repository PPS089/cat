// 健康提醒相关类型定义

// 领养记录类型
export interface AdoptionRecord {
  aid?: number;                    // 领养记录ID
  adoptionDate?: string;           // 领养日期
  pid: number;                     // 宠物ID
  name: string;                    // 宠物名称
  breed: string;                   // 品种
  age?: number;                    // 年龄
  gender?: string;                 // 性别
  image?: string;                  // 宠物图片
  sid?: number;                    // 收容所ID
  sname?: string;                  // 收容所名称
  location?: string;               // 收容所地址
  isFostering?: boolean;           // 是否正在寄养
  petStatus?: string;              // 宠物状态
  species?: string;                 // 物种（前端添加的字段）
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number;
  data: T;
  message?: string;
}

// 分页响应类型
export interface PageResponse<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 领养记录分页响应
export interface AdoptionPageResponse extends ApiResponse<PageResponse<AdoptionRecord>> {}

// 健康提醒类型（与后端 pet_health 表结构对应）
export interface HealthAlert {
  healthId?: number;               // 健康检查ID（后端主键）
  pid: number;                     // 宠物ID
  checkDate: string;               // 检查日期（后端改为 check_date）
  healthType: 'VACCINE' | 'CHECKUP' | 'SURGERY' | 'DISEASE' | string;  // 健康类型（后端改为 health_type）
  description: string;             // 健康描述（后端改为 description）
  reminderTime?: string | null;    // 提醒时间
  status: 'normal' | 'critical' | string;  // 状态（后端改为 normal/critical）
  createdAt?: string;              // 创建时间
  updatedAt?: string;              // 更新时间
  handler?: string;                // 处理人（可选）
  
  // 为了兼容旧字段名的映射（临时保留）
  hid?: number;                    // 已弃用，使用 healthId
  alertType?: string;              // 已弃用，使用 healthType
  time?: string;                   // 已弃用，使用 checkDate
  content?: string;                // 已弃用，使用 description
}

// 健康提醒表单数据类型
export interface HealthAlertForm {
  healthId?: number;               // 健康检查ID（编辑时用）
  pid: number;                     // 宠物ID
  healthType: string;              // 健康类型
  checkDate: string;               // 检查日期
  description: string;             // 健康描述
  reminderTime: string;            // 提醒时间
  status: string;                  // 状态（normal/critical）
}