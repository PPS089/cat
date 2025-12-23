/**
 * 宠物状态管理工具类
 * 统一处理前后端的状态映射，避免状态转换逻辑分散
 */

// 后端直接状态
export const PetStatusEnum = {
  AVAILABLE: 'AVAILABLE' as const,      // 可领养
  ADOPTED: 'ADOPTED' as const,           // 已被领养
  FOSTERING: 'FOSTERING' as const,       // 寄养中
}

export const FosterStatusEnum = {
  PENDING: 'PENDING' as const,           // 待审核
  APPROVED: 'APPROVED' as const,         // 已批准
  ONGOING: 'ONGOING' as const,           // 进行中
  COMPLETED: 'COMPLETED' as const,       // 已完成
  REJECTED: 'REJECTED' as const,         // 已拒绝
}

export const AdoptionStatusEnum = {
  PENDING: 'PENDING' as const,           // 待审核
  APPROVED: 'APPROVED' as const,         // 已批准
  REJECTED: 'REJECTED' as const,         // 已拒绝
}

/**
 * 前端展示状态（组合状态）
 * 用于UI层展示，基于后端的petStatus/fosterStatus/adoptionStatus组合而成
 */
export const DisplayStatusEnum = {
  AVAILABLE: 'AVAILABLE' as const,              // 可领养
  ADOPTED: 'ADOPTED' as const,                  // 已被领养
  FOSTERING: 'FOSTERING' as const,              // 寄养中
  FOSTER_PENDING: 'FOSTER_PENDING' as const,    // 寄养待审核
  FOSTER_REJECTED: 'FOSTER_REJECTED' as const,  // 寄养被拒
  ADOPTION_PENDING: 'ADOPTION_PENDING' as const, // 领养待审核
  ADOPTION_APPROVED: 'ADOPTION_APPROVED' as const, // 领养已批准
  ADOPTION_REJECTED: 'ADOPTION_REJECTED' as const, // 领养被拒
}

/**
 * 根据宠物状态、领养状态、寄养状态计算显示状态
 * @param petStatus 宠物状态
 * @param adoptionStatus 领养状态
 * @param fosterStatus 寄养状态
 * @returns 前端显示状态
 */
export function resolveDisplayStatus(
  petStatus?: string,
  adoptionStatus?: string,
  fosterStatus?: string,
): string {
  const pet = petStatus?.toUpperCase()
  const adoption = adoptionStatus?.toUpperCase()
  const foster = fosterStatus?.toUpperCase()

  // 【优先级规则】
  // 1. 如果有激活的寄养记录，显示寄养相关状态
  if (foster === FosterStatusEnum.PENDING) {
    return DisplayStatusEnum.FOSTER_PENDING
  }
  if (foster === FosterStatusEnum.APPROVED || foster === FosterStatusEnum.ONGOING) {
    return DisplayStatusEnum.FOSTERING
  }
  if (foster === FosterStatusEnum.REJECTED) {
    return DisplayStatusEnum.FOSTER_REJECTED
  }

  // 2. 如果有激活的领养记录，显示领养相关状态
  if (adoption === AdoptionStatusEnum.PENDING) {
    return DisplayStatusEnum.ADOPTION_PENDING
  }
  if (adoption === AdoptionStatusEnum.APPROVED) {
    return DisplayStatusEnum.ADOPTION_APPROVED
  }

  // 3. 使用宠物自身状态
  if (pet === PetStatusEnum.ADOPTED) {
    return DisplayStatusEnum.ADOPTED
  }
  if (pet === PetStatusEnum.FOSTERING) {
    return DisplayStatusEnum.FOSTERING
  }

  // 4. 默认返回可领养状态
  return DisplayStatusEnum.AVAILABLE
}

/**
 * 检查状态是否为活跃状态（需要禁用某些操作）
 */
export function isActiveStatus(status: string): boolean {
  const normalized = status?.toUpperCase()
  return (
    normalized === DisplayStatusEnum.FOSTER_PENDING ||
    normalized === DisplayStatusEnum.FOSTERING ||
    normalized === DisplayStatusEnum.ADOPTION_PENDING ||
    normalized === DisplayStatusEnum.ADOPTION_APPROVED ||
    normalized === DisplayStatusEnum.ADOPTED
  )
}

/**
 * 检查宠物是否可以创建领养申请
 */
export function canCreateAdoption(displayStatus: string): boolean {
  const normalized = displayStatus?.toUpperCase()
  return (
    normalized === DisplayStatusEnum.AVAILABLE ||
    normalized === DisplayStatusEnum.ADOPTION_REJECTED ||
    normalized === DisplayStatusEnum.FOSTER_REJECTED
  )
}

/**
 * 检查宠物是否可以创建寄养申请
 */
export function canCreateFoster(displayStatus: string): boolean {
  const normalized = displayStatus?.toUpperCase()
  return (
    normalized === DisplayStatusEnum.AVAILABLE ||
    normalized === DisplayStatusEnum.ADOPTION_REJECTED ||
    normalized === DisplayStatusEnum.FOSTER_REJECTED
  )
}

/**
 * 获取状态的中文标签
 */
export function getStatusLabel(status: string): string {
  const normalized = status?.toUpperCase()
  const labelMap: Record<string, string> = {
    [DisplayStatusEnum.AVAILABLE]: '可领养',
    [DisplayStatusEnum.ADOPTED]: '已被领养',
    [DisplayStatusEnum.FOSTERING]: '寄养中',
    [DisplayStatusEnum.FOSTER_PENDING]: '寄养待审核',
    [DisplayStatusEnum.FOSTER_REJECTED]: '寄养被拒',
    [DisplayStatusEnum.ADOPTION_PENDING]: '领养待审核',
    [DisplayStatusEnum.ADOPTION_APPROVED]: '领养已批准',
  }
  return labelMap[normalized] || '未知状态'
}
