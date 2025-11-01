import request from '@/utils/request'
import type { PetStatus, PetGender,petDetailResponse} from '@/types/petdetail'



// 宠物状态和性别映射工具函数

// 状态类型映射 - 返回UI组件类型
export const getStatusType = (status: PetStatus | string): 'success' | 'info' | 'warning' | 'danger' => {
  switch (status) {
    case 'available': return 'success'    // 可领养 -> 成功状态
    case 'adopted': return 'info'           // 已领养 -> 信息状态
    case 'fostered': return 'warning'     // 寄养中 -> 警告状态
    case 'pending': return 'warning'      // 待处理 -> 警告状态
    case 'quarantine': return 'danger'    // 隔离中 -> 危险状态
    default: return 'info'                  // 默认 -> 信息状态
  }
}

// 状态标签映射 - 返回显示标签
export const getStatusLabel = (status: PetStatus | string): string => {
  const statusKey = status.toUpperCase()
  switch (statusKey) {
    case 'UNADOPTED': return '可领养'
    case 'ADOPTED': return '已领养'
    case 'FOSTERING': return '寄养中'
    default: return status
  }
}

// 性别标签映射 - 返回显示标签
export const getGenderLabel = (gender: PetGender | string): string => {
  switch (gender) {
    case 'male': return '公'
    case 'female': return '母'
    case 'unknown': return '未知'
    default: return gender
  }
}



export const fetchPetDetail = async (pid: number)=>{
  // 根据ID获取宠物详情 - 直接返回后端JSON数据
    try {
      console.log(`正在获取ID为 ${pid} 的宠物详情`)
      
      // 获取数据
      const response = await request.get(`/pets/details/${pid}`)
      
      if (response.code === 200) {
        const result = response.data as petDetailResponse
        console.log('成功获取宠物数据:', response.data)
        return result
      }
      console.log(`在自定义宠物中未找到ID为 ${pid} 的宠物`)
      return null
    } catch (error) {
      console.error('根据ID获取宠物时出错:', error)
      return null
    }
  }