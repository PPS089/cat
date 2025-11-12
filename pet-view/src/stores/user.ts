import { defineStore } from 'pinia'
import { reactive } from 'vue'
import type{ userInfo } from '@/types/profile'
import { fetchUserProfileFromAPI } from '@/api/profile'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import dogImage from '@/assets/img/dog.jpg'


export const useUserStore = defineStore('user', () => {
  // 从localStorage读取初始数据
  const storedInfo = localStorage.getItem('userInfo')
  const storedUserId = localStorage.getItem('userId')
  const storedUserName = localStorage.getItem('userName')
  
  // 如果有存储的用户信息，使用存储的，否则创建初始对象
  let initialInfo: userInfo
  if (storedInfo) {
    try {
      initialInfo = JSON.parse(storedInfo)
      // 确保userId存在且有效
      if (!initialInfo.userId && storedUserId) {
        initialInfo.userId = parseInt(storedUserId)
      }
    } catch (e) {
      // 清理无效数据
      localStorage.removeItem('userInfo')
      initialInfo = {
        userName: '',
        headPic: dogImage,
        userId: storedUserId ? parseInt(storedUserId) : 0,
        email: '',
        phone: '',
        introduce: '',
      }
    }
  } else {
    // 如果没有存储的完整用户信息，尝试从独立字段恢复
    initialInfo = {
      userName: storedUserName || '',
      headPic: dogImage,
      userId: storedUserId ? parseInt(storedUserId) : 0,
      email: '',
      phone: '',
      introduce: '',
    }
    
  }

  // 状态管理
  const info = reactive<userInfo>(initialInfo)
  
  // 请求锁，防止重复请求
  let isFetchingProfile = false
  let fetchPromise: Promise<void> | null = null
  
  //从API获取用户资料
  const fetchProfile = async () => {
    // 如果已经在请求中，返回现有的promise
    if (isFetchingProfile && fetchPromise) {
      return fetchPromise
    }
    
    // 如果已经有完整的用户信息，直接返回
    if (info.userId && info.userId !== 0 && info.userName && info.userName !== '') {
      return Promise.resolve()
    }
    
    // 如果只有userId但其他信息为空，仍然需要获取完整资料
    
    
    // 设置请求锁
    isFetchingProfile = true
    
    fetchPromise = (async () => {
      try {
        const result = await fetchUserProfileFromAPI()
        if (result.success) {
          // 更新数据 - 添加防御性检查
          if (typeof result.data === 'object' && result.data !== null) {
            // 清理旧的用户信息，确保使用最新的数据
            info.userName = ''
            info.headPic = dogImage
            info.email = ''
            info.phone = ''
            info.introduce = ''
            
            // 设置新的用户信息
            info.userName = result.data.userName || ''
            info.headPic = result.data.headPic  ? `/api/images/${result.data.headPic}` : dogImage
            info.email = result.data.email || ''
            info.phone = result.data.phone || ''
            info.introduce = result.data.introduce || ''
            // 重要：确保设置userId，这是认证状态的关键
            if (result.data.userId) {
              info.userId = result.data.userId
            }
          } else {
            // 如果返回的是纯数字，可能是userId
            if (typeof result.data === 'number') {
              info.userId = result.data
            }
          }
          
          // 持久化到localStorage
          localStorage.setItem('userInfo', JSON.stringify(info))
        } else {
          // 只有在真正获取用户资料失败时才显示错误消息
          if (result.message !== '用户未登录或缺少必要信息') {
            ElMessage.error(result.message || '获取用户资料失败')
          }
        }
      } catch (error) {
        ElMessage.error('获取用户资料失败')
      } finally {
        // 释放请求锁
        isFetchingProfile = false
        fetchPromise = null
      }
    })()
    
    return fetchPromise
  }


  // 修改密码（使用JWT认证）
  const changePassword = async (passwordForm: { currentPassword: string; newPassword: string; confirmPassword: string }) => {
    try {
      const response = await request.put('/user/updatePassword', {
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
      })

      if (response.code === 200) {
        return { success: true, message: '密码修改成功' }
      } else {
        throw new Error(response?.message || '密码修改失败')
      }
    } catch (error) {
      
      throw error
    }
  }



  // 设置用户资料
  const setUserProfile = (profile: {userName?: string; headPic?: string; email?: string; phone?: string; introduce?: string }) => {
      if (profile.userName) {
        info.userName = profile.userName
        localStorage.setItem('userName', info.userName)
      }
      if (profile.headPic) {
        info.headPic = profile.headPic
        localStorage.setItem('headPic', info.headPic)
      }
      if (profile.email !== undefined) {
        info.email = profile.email
        localStorage.setItem('email', profile.email)
      }
      if (profile.phone !== undefined) {
        info.phone = profile.phone
        localStorage.setItem('phone', profile.phone)
      }
      if (profile.introduce !== undefined) {
        info.introduce = profile.introduce
        localStorage.setItem('introduce', profile.introduce)
      }
      
      // 同步更新完整的userInfo到localStorage
      localStorage.setItem('userInfo', JSON.stringify(info))
    }

  // 设置用户名
  const setUsername = (name: string) => {
    info.userName = name
    localStorage.setItem('userName', name)
  }


  // 必须返回info供组件使用
  return { info, fetchProfile , changePassword , setUserProfile, setUsername}
})
