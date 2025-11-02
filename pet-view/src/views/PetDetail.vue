<template>
  <!-- 宠物详情页面容器 - 支持深色主题 -->
  <div class="pet-detail-container" :class="{ 'dark': themeStore.preferences.theme === 'dark' }">
    <!-- 页面头部区域 -->
    <header class="pet-detail-header">
      <!-- 页面标题 -->
      <h1>{{ t('nav.viewDetails') || 'Pet Details' }}</h1>
      <!-- 返回按钮 -->
      <el-button class="back-btn" @click="$router.back()">
        {{ t('message.back') }}
      </el-button>
    </header>
    
    <!-- 主要内容区域 - 加载状态控制 -->
    <main class="pet-detail-main">
      <!-- 宠物信息存在时显示详细内容 -->
      <div v-if="pet" class="pet-detail-content">
        <!-- 宠物基本信息区域 -->
        <div class="pet-info-section">
          <!-- 宠物头部信息 - 名称和状态 -->
          <div class="pet-header">
            <!-- 宠物名称 -->
            <h2>{{ pet.name }}</h2>
            <!-- 宠物状态标签 -->
            <el-tag type="success">可领养</el-tag>
          </div>
          
          <!-- 宠物详细信息网格布局 -->
          <div class="pet-details-grid">
            <!-- 物种种类 -->
            <div class="detail-item">
              <span class="label">{{ t('pets.species') || 'Species' }}：</span>
              <span class="value">{{ pet.species }}</span>
            </div>
            <!-- 品种信息 -->
            <div class="detail-item">
              <span class="label">{{ t('nav.breed')  }}：</span>
              <span class="value">{{ pet.breed }}</span>
            </div>
            <!-- 年龄信息 -->
            <div class="detail-item">
              <span class="label">{{ t('nav.age')  }}：</span>
              <span class="value">{{ pet.age }} {{ t('message.yearsOld') }}</span>
            </div>
            <!-- 性别信息 -->
            <div class="detail-item">
              <span class="label">{{ t('nav.gender') }}：</span>
              <span class="value">{{ getGenderLabel(pet.gender) }}</span>
            </div>
          </div>
          
          <!-- 收容所信息区域 -->
          <div class="shelter-section">
            <h3>{{ t('nav.shelter') }}</h3>
            <div class="shelter-details">
              <!-- 收容所名称 -->
              <div class="detail-item">
                <span class="label">{{ t('nav.shelter') || 'Shelter' }}：</span>
                <span class="value">{{ pet.shelterName || t('message.unknown') }}</span>
              </div>
              <!-- 收容所地址 -->
              <div class="detail-item">
                <span class="label">{{ t('nav.address') || 'Address' }}：</span>
                <span class="value">{{ pet.shelterAddress || t('message.unknown') }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮区域 -->
        <div class="pet-actions">
          <!-- 返回按钮 -->
          <el-button @click="$router.back()">
            {{ t('message.back') }}
          </el-button>
        </div>
      </div>
      
      <!-- 未找到宠物时的显示内容 -->
      <div v-else-if="!pet" class="no-pet-found">
        <el-empty :description="t('message.petNotFound') || 'Pet not found'">
          <el-button type="primary" @click="$router.push('/user/adoption-pets')">
            {{ t('nav.browseAdoptablePets') || 'Browse Pets' }}
          </el-button>
        </el-empty>
      </div>
    </main>
  </div>
</template>


<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useThemeStore } from '@/stores/theme'
import { getGenderLabel, fetchPetDetail } from '@/api/petdetail'
import type{ petDetailResponse } from '../types/petdetail'

// 路由相关
const route = useRoute()

// 国际化翻译函数
const { t } = useI18n()

// 主题存储
const themeStore = useThemeStore()

// 宠物数据 - 使用any类型接收原始JSON数据
  const pet = ref<petDetailResponse | null>(null)

// 计算属性：宠物ID
const petId = computed(() => {
  const id  = route.params.id
  return typeof id === 'string' ? parseInt(id) : Array.isArray(id) ? parseInt(id[0]) : 0
})


// 组件挂载时获取宠物详情
onMounted(async () => {
  const data = await fetchPetDetail(petId.value)
  pet.value = data // 直接存储原始JSON数据
})
</script>

<style scoped >
@import '@/styles/petdetail.css';
</style>