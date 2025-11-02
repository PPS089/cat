import request from '@/utils/request'
import type {  PetGender,petDetailResponse} from '@/types/petdetail'






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
        console.log('成功获取宠物数据:', response.data)
        return result
      }
      return null
    } catch (error) {
      console.error('根据ID获取宠物时出错:', error)
      return null
    }
  }