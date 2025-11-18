import request from '@/utils/request'
import type { PetGender } from '@/types/common'
import type { petDetailResponse } from '@/types/petdetail'






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
      // 获取数据
      const response = await request.get(`/pets/details/${pid}`)
      
      if (response.code === 200) {
        const result = response.data as petDetailResponse
        return result
      }
      return null
    } catch (error) {
      return null
    }
  }
