<template>
  <div class="adopt-pet-container" :class="{ 'dark': themeStore.preferences.theme === 'dark' }">
    <!-- 组件加载确认 -->

    <header class="adopt-pet-header">
      <h1>{{ t('nav.adoptCenter') }}</h1>
    </header>
    
    <main class="adopt-pet-main">

      <div class="pets-grid">
        <div v-for="pet in availablePets" :key="pet.pid" class="pet-card">
          <div class="pet-header">
            <h3>{{ pet.name }}</h3>
            <el-tag type="success">
              {{ t('nav.adoptable') }}
            </el-tag>
          </div>
          
          <div class="pet-info">
            <div class="info-item">
              <span class="label">{{ t('nav.breed') }}：</span>
              <span class="value">{{ pet.breed }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ t('nav.age') }}：</span>
              <span class="value">{{ pet.age }} {{ t('message.yearsOld') }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ t('nav.gender') }}：</span>
              <span class="value">{{ pet.gender === 'male' ? t('message.male') : t('message.female') }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ t('nav.shelter') }}：</span>
              <span class="value">{{ pet.shelterName }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ t('nav.address') }}：</span>
              <span class="value">{{ pet.shelterAddress }}</span>
            </div>
          </div>
          
          <div class="pet-actions">
            <el-button 
              type="primary" 
              @click="adoptPet(pet)"
            >
              {{ t('nav.adoptNow') }}
            </el-button>
            <el-button @click="viewPetDetail(pet.pid)">
              {{ t('nav.viewDetails') }}
            </el-button>
          </div>
        </div>
        
        <div v-if="availablePets.length === 0" class="no-pets">
          <el-empty :description="t('nav.noPetsAvailable')">
            <template #description>
              <p>{{ t('nav.noPetsAvailable') }}</p>
              <p style="font-size: 14px; color: var(--el-text-color-secondary); margin-top: 10px;">
                {{ t('nav.allPetsAdopted') }}
              </p>
            </template>
          </el-empty>
        </div>
      </div>
      
      <Pagination
        v-if="total > 0"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[6, 12, 24, 48]"
        layout="sizes, prev, pager, next, ->, total"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
        @pageChange="handlePageChange"
        @sizeChange="handleSizeChange"
      />
    </main>
  </div>
</template>




<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAdd } from '@/api/adoptionspets'
import Pagination from '@/components/Pagination.vue'



// 使用组合式函数
const {
  // 响应式数据
  availablePets,
  currentPage,
  pageSize,
  total,
  themeStore,
  
  // 方法
  fetchAvailablePets,
  adoptPet,
  viewPetDetail,
  handlePageChange,
  handleSizeChange
} = useAdd()

// 使用i18n
const { t } = useI18n()

// 组件挂载时加载数据
onMounted(() => {
  fetchAvailablePets()
})
</script>


<style scoped>
@import '../styles/AdoptionsPets.css';
</style>


