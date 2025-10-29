/**
 * 宠物健康提醒管理模块
 */

import { ref, computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { ElMessageBox } from 'element-plus'
/**
 * 健康提醒模块类型导出
 * 供其他组件使用，确保类型一致性
 */
import type { AdoptionRecord, AdoptionPageResponse, HealthAlert, HealthAlertForm } from '@/types/health'

/**
 * 健康提醒管理组合式函数
 * 提供宠物健康提醒的完整管理功能，包括：
 * - 健康提醒的增删改查
 * - 宠物关联管理
 * - 状态筛选和分组显示
 * - 模态框交互
 */
export const useHealth = () => {
  const { t } = useI18n()
  
  // ===== 数据状态管理 =====
  /** 健康提醒列表数据 */
  const healthAlerts = ref<HealthAlert[]>([])
  /** 用户关联的宠物列表 */
  const pets = ref<AdoptionRecord[]>([])
  /** 页面加载状态 */
  const loading = ref(false)

  // ===== 筛选条件 =====
  /** 选中的宠物ID，用于筛选特定宠物的健康提醒 */
  const selectedPet = ref('')
  /** 选中的健康类型，如疫苗、体检等 */
  const selectedHealthType = ref('')
  /** 选中的状态：待处理、已完成、已过期 */
  const selectedStatus = ref('')

  // ===== 显示模式 =====
  /** 视图模式：list(列表) 或 timeline(时间线) */
  const viewMode = ref('list')

  // ===== 模态框状态 =====
  /** 显示添加健康提醒模态框 */
  const showAddModal = ref(false)
  /** 显示编辑健康提醒模态框 */
  const showEditModal = ref(false)

  
  /** 健康提醒表单数据 */
  const formData: HealthAlertForm = reactive({
    healthId: '',         // 健康提醒ID
    pid: '',              // 宠物ID
    healthType: '',       // 提醒类型：疫苗、体检、手术、疾病
    checkDate: '',        // 检查日期
    description: '',      // 详细描述
    reminderTime: '',     // 提前提醒时间
    status: 'normal'      // 状态：normal/critical
  })

  
  /**
   * 显示添加健康提醒模态框
   * 检查用户是否有关联宠物，如果没有则提示先领养或寄养
   */
  const showAddModalDialog = () => {
    if (pets.value.length === 0) {
      ElMessage.warning(t('noAssociatedPetPleaseAdoptOrFoster'))
      return
    }
    showAddModal.value = true
  }



  // ===== 计算属性 =====
  
  /** 所有健康记录的总数 */
  const totalAlerts = computed(() => healthAlerts.value.length)

  /** 正常状态的健康记录列表 */
  const normalAlerts = computed(() => 
    healthAlerts.value.filter(alert => alert.status === 'normal')
  )

  /** 严重状态的健康记录列表 */
  const criticalAlerts = computed(() => 
    healthAlerts.value.filter(alert => alert.status === 'critical')
  )

  // 为了向后兼容，保留旧名称的计算属性（但指向新的数据）
  const pendingAlerts = computed(() => normalAlerts.value)
  const completedAlerts = computed(() => criticalAlerts.value)
  const expiredAlerts = computed(() => criticalAlerts.value)

  /**
   * 根据筛选条件过滤后的健康提醒列表
   * 支持按宠物、健康类型、状态进行筛选，并按时间倒序排列
   */
  const filteredAlerts = computed(() => {
    let filtered = healthAlerts.value

    // 按宠物筛选
    if (selectedPet.value) {
      filtered = filtered.filter(alert => alert.pid === Number(selectedPet.value))
    }

    // 按健康类型筛选
    if (selectedHealthType.value) {
      filtered = filtered.filter(alert => alert.healthType === selectedHealthType.value)
    }

    // 按状态筛选
    if (selectedStatus.value) {
      filtered = filtered.filter(alert => alert.status === selectedStatus.value)
    }

    // 按时间倍序排列，最新的在前
    return filtered.sort((a, b) => new Date(b.checkDate).getTime() - new Date(a.checkDate).getTime())
  })

  /**
   * 按日期分组的健康提醒数据
   * 用于时间线视图显示，将提醒按日期分组
   */
  const groupedAlerts = computed(() => {
    const groups: Record<string, HealthAlert[]> = {}
    filteredAlerts.value.forEach(alert => {
      // 使用检查日期或提醒时间作为分组依据
      const date = new Date(alert.checkDate).toDateString()
      if (!groups[date]) {
        groups[date] = []
      }
      groups[date].push(alert)
    })
    return groups
  })

  // ===== API请求方法 =====
  
  /**
   * 获取健康提醒列表
   * 适配多种后端返回的数据格式，确保兼容性
   * 只显示用户拥有的宠物的健康提醒
   */
  const fetchHealthAlerts = async () => {
    loading.value = true
    try {
      const response = await request.get('/user/health-alerts')
      
      console.log('健康警报API原始响应:', response)
      console.log('健康警报API响应数据:', JSON.stringify(response, null, 2))

      // 先获取用户关联的宠物列表
      await fetchPets()
      
      // 获取用户拥有的宠物ID列表，用于过滤
      const userPetIds = pets.value.map(pet => pet.pid)
      console.log('用户拥有的宠物ID列表:', userPetIds)
      
      // ===== 适配后端返回的数据格式 =====
      // 格式: {code: 200, msg: "success", data: {alerts: [...]}}
      // 后端返回的字段是驼峰式（camelCase）：healthId, pid, checkDate, healthType, description, reminderTime, status
      if (response && (response as any).data && (response as any).data.alerts && Array.isArray((response as any).data.alerts)) {
        const allAlerts = (response as any).data.alerts
      
        // 过滤只显示用户拥有的宠物的健康提醒
        healthAlerts.value = allAlerts
          .filter((alert: any) => {
            const alertPid = Number(alert.pid)
            const isUserPet = userPetIds.includes(alertPid)
            console.log(`过滤检查 - alertPid: ${alertPid}, 用户宠物: ${isUserPet}`)
            return isUserPet
          })
          .map((alert: any) => {
            console.log('健康提醒alert数据:', alert)
            // ===== 直接映射后端返回的驼峰式字段 =====
            // 后端返回：healthId, pid, checkDate, healthType, description, reminderTime, status, createdAt, updatedAt
            return {
              healthId: alert.healthId,                    // 健康提醒ID
              pid: Number(alert.pid),                      // 宠物ID
              checkDate: alert.checkDate,                  // 检查日期
              healthType: alert.healthType,                // 健康类型（VACCINE/CHECKUP/SURGERY/DISEASE）
              description: alert.description,              // 健康状况描述
              reminderTime: alert.reminderTime,            // 提醒时间
              status: alert.status,                        // 状态（normal/critical）
              createdAt: alert.createdAt,                  // 创建时间
              updatedAt: alert.updatedAt,                  // 更新时间
              // 兼容旧字段名（如果需要的话）
              hid: alert.healthId,
              alertType: alert.healthType,
              time: alert.checkDate,
              content: alert.description
            } as HealthAlert
          })
      }

      console.log('过滤后的健康警报数据:', healthAlerts.value)
    } catch (error) {
      ElMessage.error(t('api.getPetListFailed'))
      healthAlerts.value = []
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户关联的宠物列表
   * 通过用户的领养记录来获取宠物信息
   */
  const fetchPets = async () => {
    try {
      let adoptionsResponse
      
      // 获取用户的领养记录
      try {
        adoptionsResponse = await request.get('/user/adoptions', {
          params: {
            current_page: 1,
            per_page: 100
          }
        })
      } catch (adoptionError) {
        ElMessage.error(t('api.getAdoptionRecordsFailed') + ': ' + ((adoptionError as any).message || t('unknownError')))
        adoptionsResponse = { success: false, data: { adoptions: [] } }
      }
      
      // 检查响应结构是否有效
      if (!adoptionsResponse) {
        adoptionsResponse = { success: false, data: { adoptions: [] } }
      }
      
      const petsList: AdoptionRecord[] = []
      
      console.log('领养记录API响应:', adoptionsResponse)
      
      // ===== 处理标准API响应格式：{code: 200, data: {records: [...]}} =====
      if (adoptionsResponse.code === 200) {
        const adoptions = (adoptionsResponse as AdoptionPageResponse).data.records
        console.log('处理领养记录数据:', adoptions)
      
        // 遍历领养记录，提取宠物信息
        adoptions.forEach((adoption: AdoptionRecord) => {
          if (adoption.pid) {
            petsList.push({
              pid: Number(adoption.pid), // 确保转换为数字类型
              name: adoption.name,
              species: adoption.species || '',
              breed: adoption.breed
            })
          }
        })
      }
      
      console.log('提取的宠物列表:', petsList)
      // 更新宠物列表数据
      pets.value = petsList
    } catch (error) {
      console.error(t('api.getPetListFailed'), error)
      ElMessage.error(t('api.getPetListFailed'))
      pets.value = []
    }
  }

  /**
   * 更新健康提醒状态
   * @param healthId 健康提醒ID
   * @param status 新状态：normal/critical
   */
  const updateAlertStatus = async (healthId: number, status: 'normal' | 'critical') => {
    try {
      await request.put(`/user/health-alerts/${healthId}/status?status=${status}`)
      await fetchHealthAlerts() // 重新获取最新数据
      ElMessage.success(t('api.updateSuccess'))
    } catch (error) {
      ElMessage.error(t('api.updateFailed'))
    }
  }

  /**
   * 删除健康提醒
   * 包含用户确认对话框，防止误删
   * 有效性验证：确保 healthId 是有效的整数，不是 undefined 或空字符串
   * @param healthId 要删除的健康提醒ID
   */
  const deleteAlert = async (healthId: number | string | undefined) => {
    try {
      // ===== 参数有效性验证 =====
      // 不能是 undefined、null 或空字符串
      if (!healthId || healthId === '' || healthId === 'undefined') {
        ElMessage.error('错误：无效的记录ID，无法进行删除')
        console.error('删除失败，接收到一个无效的ID:', healthId, '类型:', typeof healthId)
        return
      }
      
      // 仅数字比较
      const validId = Number(healthId)
      if (isNaN(validId) || validId <= 0) {
        ElMessage.error('错误：不是有效的整数ID')
        console.error('删除失败，转换到不有效整数:', healthId)
        return
      }
      
      // 显示删除确认对话框
      await ElMessageBox.confirm(
        t('api.confirmDeleteHealthAlert'),
        t('api.deleteConfirmation'),
        {
          confirmButtonText: t('api.confirm'),
          cancelButtonText: t('api.cancel'),
          type: 'warning'
        }
      )
      
      // 用户确认后执行删除操作，使用有效ID
      await request.delete(`/user/health-alerts/${validId}`)
      await fetchHealthAlerts() // 重新获取数据
      ElMessage.success(t('api.deleteSuccess'))
    } catch (error) {
      // 用户取消删除不显示错误信息
      if (error !== 'cancel') {
        ElMessage.error(t('api.deleteFailed'))
        console.error('删除健康提醒失败:', error)
      }
    }
  }

  /**
   * 保存健康提醒（新增或更新）
   * 包含表单验证、时间格式化和API请求
   */
  const saveAlert = async () => {
    try {
     
      // ===== 表单验证 =====
      if (!formData.pid) {
        ElMessage.error(t('api.pleaseSelectPet'))
        return
      }

      // ===== 时间格式化处理 =====
      // 格式化时间为后端亟望的格式 yyyy-MM-dd HH:mm
      let formattedCheckDate = formData.checkDate
      if (formData.checkDate && formData.checkDate.includes('T')) {
        // 将 datetime-local 格式 (yyyy-MM-ddTHH:mm) 转换为后端格式 (yyyy-MM-dd HH:mm)
        formattedCheckDate = formData.checkDate.replace('T', ' ')
      }

      // 格式化提醒时间为后端期望的格式 yyyy-MM-dd HH:mm
      let formattedReminderTime = formData.reminderTime
      if (formData.reminderTime && formData.reminderTime.includes('T')) {
        formattedReminderTime = formData.reminderTime.replace('T', ' ')
      }

      // ===== 构建请求数据 =====
      const payload = {
        pid: formData.pid,
        healthType: formData.healthType,
        checkDate: formattedCheckDate,
        reminderTime: formattedReminderTime || null,
        status: formData.status,
        description: formData.description
      }
      
      console.log('保存健康提醒执效数据：', {
        原始表单数据: formData,
        格式化后数据: payload,
        是更新: !!formData.healthId
      })

      // ===== 发送API请求 =====
      let response
      if (formData.healthId) {
        // 更新现有提醒
        response = await request.put(`/user/health-alerts/${formData.healthId}`, payload)
      } else {
        // 创建新提醒
        response = await request.post('/user/health-alerts', payload)
      }

      // ===== 处理响应结果 =====
      if (response.data) {
        ElMessage.success(formData.healthId ? t('api.updateHealthAlertSuccess') : t('api.createHealthAlertSuccess'))
        showAddModal.value = false
        showEditModal.value = false
        await fetchHealthAlerts() // 重新获取数据
      } else {
        ElMessage.error(t('api.operationFailed'))
      }
    } catch (error) {
      console.error(t('api.saveHealthAlertFailed'), error)
      ElMessage.error(t('api.saveHealthAlertFailed'))
    }
  }

  /**
   * 关闭Modal并重置表单数据
   * 清空所有表单字段，恢复到初始状态
   */
  const closeModal = () => {
    showAddModal.value = false
    showEditModal.value = false
    
    // ===== 重置表单数据到初始状态 =====
    formData.healthId = ''
    formData.pid = ''
    formData.healthType = ''
    formData.checkDate = ''
    formData.reminderTime = ''
    formData.status = 'normal'
    formData.description = ''
  }

  /**
   * 初始化健康提醒模块
   * 先加载宠物数据，再加载健康提醒（因为健康提醒需要基于用户宠物过滤）
   */
  const initializeHealthAlerts = async () => {
    console.log('开始初始化健康提醒数据')
    
    // 先获取宠物数据
    await fetchPets()
    console.log('宠物数据加载完成:', pets.value)
    
    // 只有在宠物数据加载成功后才获取健康提醒
    if (pets.value.length > 0) {
      await fetchHealthAlerts()
      console.log('健康提醒数据加载完成:', healthAlerts.value)
    } else {
      console.log('没有宠物数据，不加载健康提醒')
      healthAlerts.value = []
    }
    
    console.log('initializeHealthAlerts: 初始化完成，宠物数量:', pets.value.length, '健康提醒数量:', healthAlerts.value.length)
  }

  // ===== 工具函数 =====
  
  /**
   * 获取状态对应的CSS样式类
   * 用于状态标签的样式控制，支持新旧状态值
   * @param status 状态值 (normal/critical 或旧值 pending/completed/expired)
   * @returns 样式类对象
   */
  const getStatusClass = (status: string): Record<string, boolean> => {
    // 支持新状态值映射
    const normalizeStatus = status === 'normal' ? 'normal' : status === 'critical' ? 'critical' : status
    return {
      'pending': normalizeStatus === 'normal',
      'completed': normalizeStatus === 'normal',
      'expired': normalizeStatus === 'critical',
      'normal': normalizeStatus === 'normal',
      'critical': normalizeStatus === 'critical'
    }
  }

  /**
   * 获取健康类型的中文标签
   * 将英文类型转换为中文显示，处理 null 值
   * @param type 健康类型
   * @returns 中文标签
   */
  const getHealthTypeLabel = (type: string | null | undefined): string => {
    if (!type) return t('healthAlerts.noType', '未分类')  // 处理 null 或 undefined
    const typeMap: Record<string, string> = {
      'VACCINE': t('healthAlerts.vaccine'),   // 疫苗
      'CHECKUP': t('healthAlerts.checkup'),   // 体棈
      'SURGERY': t('healthAlerts.surgery'),   // 手术
      'DISEASE': t('healthAlerts.disease')    // 疾病
    }
    return typeMap[type] || type
  }

  /**
   * 获取状态的中文标签
   * 支持新旧状态值的映射
   * @param status 状态值 (normal/critical 或 pending/completed/expired)
   * @returns 中文标签
   */
  const getStatusLabel = (status: string | null | undefined): string => {
    if (!status) return t('healthAlerts.unknown', '未知')
    const statusMap: Record<string, string> = {
      'normal': t('healthAlerts.normal'),          // 正常
      'critical': t('healthAlerts.critical'),      // 严重
      'pending': t('healthAlerts.pending'),        // 待处理 (兼容旧值)
      'completed': t('healthAlerts.completed'),    // 已完成 (兼容旧值)
      'expired': t('healthAlerts.expired')         // 已过期 (兼容旧值)
    }
    return statusMap[status] || status
  }

  /**
   * 格式化日期字符串为中文格式
   * 处理 null 或 undefined 值
   * @param dateString 日期字符串
   * @returns 格式化后的中文日期
   */
  const formatDate = (dateString: string | null | undefined): string => {
    if (!dateString) return '未设置'
    
    try {
      const date = new Date(dateString)
      // 检查日期是否有效
      if (isNaN(date.getTime())) {
        return dateString
      }
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',   // 年
        month: '2-digit',  // 月
        day: '2-digit',    // 日
        hour: '2-digit',   // 时
        minute: '2-digit'  // 分
      })
    } catch (error) {
      return dateString // 解析失败时返回原字符串
    }
  }

  /**
   * 编辑健康提醒
   * 将提醒数据填充到表单中，显示编辑模态框
   * 重要：需要将后端返回的日期格式从“2025-10-30 10:14”转换为“2025-10-30T10:14”
   * @param alert 要编辑的健康提醒对象
   */
  const editAlert = (alert: HealthAlert) => {
    // 
    formData.healthId = alert.healthId || alert.hid || ''
    formData.pid = Number(alert.pid) || ''
    formData.healthType = alert.healthType || alert.alertType || ''
    
    // 转换日期格式："日期 时间".replace(' ', 'T')
    // 窻上："2025-10-30 10:14" → "2025-10-30T10:14"
    formData.checkDate = (alert.checkDate || alert.time || '').replace(' ', 'T')
    formData.reminderTime = (alert.reminderTime || '').replace(' ', 'T')
    formData.status = alert.status || 'normal'
    formData.description = alert.description || alert.content || ''
    
    console.log('编辑提醒数据:', {
      healthId: formData.healthId,
      pid: formData.pid,
      healthType: formData.healthType,
      checkDate: formData.checkDate,
      reminderTime: formData.reminderTime,
      status: formData.status,
      description: formData.description
    })
    
    // 显示编辑模态框
    showEditModal.value = true
  }

  /**
   * 根据宠物ID获取宠物名称
   * @param pid 宠物ID
   * @returns 宠物名称，未找到时返回'未知宠物'
   */
  const getPetName = (pid: number): string => {
    console.log(`getPetName被调用，参数pid: ${pid} (类型: ${typeof pid})`)
    console.log(`当前宠物列表:`, pets.value)
    const pet = pets.value.find(p => {
      console.log(`比较: p.pid=${p.pid} (类型: ${typeof p.pid}) === pid=${pid} (类型: ${typeof pid})`, p.pid === pid)
      return p.pid === pid
    })
    const result = pet ? pet.name : '未知宠物'
    console.log(`getPetName返回结果: ${result}`)
    return result
  }

  // ===== 返回对象：供组件使用的数据和方法 =====
  return {
    // ===== 数据状态 =====
    /** 健康提醒列表数据 */
    healthAlerts,
    /** 用户关联的宠物列表 */
    pets,
    /** 页面加载状态 */
    loading,
    
    // ===== 筛选条件 =====
    /** 选中的宠物ID */
    selectedPet,
    /** 选中的健康类型 */
    selectedHealthType,
    /** 选中的状态 */
    selectedStatus,
    
    // ===== 显示模式 =====
    /** 视图模式：list(列表) 或 timeline(时间线) */
    viewMode,
    
    // ===== 模态框状态 =====
    /** 显示添加健康提醒模态框 */
    showAddModal,
    /** 显示编辑健康提醒模态框 */
    showEditModal,
    // ===== 表单数据 =====
    /** 健康提醒表单数据 */
    formData,
    /** 所有健康记录总数 */
    totalAlerts,
    /** 正常状态的健康记录 */
    normalAlerts,
    /** 严重状态的健康记录 */
    criticalAlerts,
    /** 待处理的健康提醒列表（兼容旧名称） */
    pendingAlerts,
    /** 已完成的健康提醒列表（兼容旧名称） */
    completedAlerts,
    /** 已过期的健康提醒列表（兼容旧名称） */
    expiredAlerts,
    /** 根据筛选条件过滤后的健康提醒列表 */
    filteredAlerts,
    /** 按日期分组的健康提醒数据 */
    groupedAlerts,
    
    // ===== 方法函数 =====
    /** 显示添加健康提醒模态框 */
    showAddModalDialog,
    /** 更新健康提醒状态 */
    updateAlertStatus,
    /** 删除健康提醒 */
    deleteAlert,
    /** 保存健康提醒（新增或更新） */
    saveAlert,
    /** 关闭模态框并重置表单 */
    closeModal,
    /** 初始化健康提醒模块 */
    initializeHealthAlerts,
    /** 获取状态对应的CSS样式类 */
    getStatusClass,
    /** 获取健康类型的中文标签 */
    getHealthTypeLabel,
    /** 获取状态的中文标签 */
    getStatusLabel,
    /** 格式化日期字符串为中文格式 */
    formatDate,
    /** 编辑健康提醒 */
    editAlert,
    /** 根据宠物ID获取宠物名称 */
    getPetName
  }
}