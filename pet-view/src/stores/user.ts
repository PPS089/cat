import { defineStore } from 'pinia'
import { reactive } from 'vue'
import type{ userInfo } from '@/types/profile'
import { fetchUserProfileFromAPI } from '@/api/profile'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'


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
      console.error('解析存储的用户信息失败:', e)
      // 清理无效数据
      localStorage.removeItem('userInfo')
      initialInfo = {
        userName: '',
        headPic: '/src/assets/img/dog.jpg',
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
      headPic: '/src/assets/img/dog.jpg',
      userId: storedUserId ? parseInt(storedUserId) : 0,
      email: '',
      phone: '',
      introduce: '',
    }
    if (storedUserId || storedUserName) {
      console.log('从独立字段恢复用户信息:', JSON.stringify(initialInfo, null, 2))
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
      console.log('用户资料请求已在进行中，等待现有请求完成')
      return fetchPromise
    }
    
    // 如果已经有完整的用户信息，直接返回
    if (info.userId && info.userId !== 0 && info.userName && info.userName !== '') {
      console.log('用户资料已存在且完整，跳过请求')
      return Promise.resolve()
    }
    
    // 如果只有userId但其他信息为空，仍然需要获取完整资料
    if (info.userId && info.userId !== 0 && (!info.userName || info.userName === '')) {
      console.log('用户ID存在但信息不完整，需要获取完整资料')
    }
    
    // 设置请求锁
    isFetchingProfile = true
    
    fetchPromise = (async () => {
      try {
        const result = await fetchUserProfileFromAPI()
        if (result.success) {
          console.log('用户数据结构:', JSON.stringify(result.data, null, 2))
          // 更新数据 - 添加防御性检查
          if (typeof result.data === 'object' && result.data !== null) {
            // 清理旧的用户信息，确保使用最新的数据
            info.userName = ''
            info.headPic = '/src/assets/img/dog.jpg'
            info.email = ''
            info.phone = ''
            info.introduce = ''
            
            // 设置新的用户信息
            info.userName = result.data.userName || ''
            info.headPic = result.data.headPic  ? `/api/images/${result.data.headPic}` : '/src/assets/img/dog.jpg'
            info.email = result.data.email || ''
            info.phone = result.data.phone || ''
            info.introduce = result.data.introduce || ''
            // 重要：确保设置userId，这是认证状态的关键
            if (result.data.userId) {
              info.userId = result.data.userId
              console.log('从API获取到userId:', result.data.userId)
            } else {
              console.log('API返回的用户数据中没有userId字段')
            }
          } else {
            console.log('API返回的数据不是对象，可能是:', typeof result.data, result.data)
            // 如果返回的是纯数字，可能是userId
            if (typeof result.data === 'number') {
              console.log('API返回的是数字，假设为userId:', result.data)
              info.userId = result.data
            }
          }
          
          // 持久化到localStorage
          localStorage.setItem('userInfo', JSON.stringify(info))
          console.log('用户资料获取成功 - 用户名:', info.userName, '用户ID:', info.userId)
          console.log('完整的用户信息:', JSON.stringify(info, null, 2))
        } else {
          // 只有在真正获取用户资料失败时才显示错误消息
          if (result.message !== '用户未登录或缺少必要信息') {
            ElMessage.error(result.message || '获取用户资料失败')
          }
        }
      } catch (error) {
        console.error('获取用户资料失败:', error)
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
      console.error('修改密码失败:', error)
      throw error
    }
  }



  // 设置用户资料
  const setUserProfile = (profile: {userName?: string; headPic?: string; email?: string; phone?: string; introduce?: string }) => {
    if (profile.userName) {
      info.userName = profile.userName
      localStorage.setItem('userName', info.userName)
      console.log('用户名已更新:', info.userName)
    }
    if (profile.headPic) {
      info.headPic = profile.headPic
      localStorage.setItem('headPic', info.headPic)
      console.log('头像已更新:', info.headPic)
    }
    if (profile.email !== undefined) {
      info.email = profile.email
      localStorage.setItem('email', profile.email)
      console.log('邮箱已更新:', profile.email)
    }
    if (profile.phone !== undefined) {
      info.phone = profile.phone
      localStorage.setItem('phone', profile.phone)
      console.log('电话已更新:', profile.phone)
    }
    if (profile.introduce !== undefined) {
      info.introduce = profile.introduce
      localStorage.setItem('introduce', profile.introduce)
      console.log('简介已更新:', profile.introduce)
    }
    
    // 同步更新完整的userInfo到localStorage
    localStorage.setItem('userInfo', JSON.stringify(info))
    console.log('完整的用户信息已同步到localStorage:', JSON.stringify(info, null, 2))
  }

  // 设置用户名
  const setUsername = (name: string) => {
    info.userName = name
    localStorage.setItem('userName', name)
  }


  // 必须返回info供组件使用
  return { info, fetchProfile , changePassword , setUserProfile, setUsername}
})

