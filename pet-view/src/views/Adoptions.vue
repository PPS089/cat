<template>
  <div class="adoptions-container" :class="{ dark: themeStore.preferences.theme === 'dark' }">
    <div class="page-header">
      <h1>{{ t('user.adoptionRecords') }}</h1>
      <p>{{ t('user.viewAdoptionHistory') }}</p>
    </div>

    <div class="adoptions-content">
      <div v-if="adoptions.length === 0" class="empty-state">
        <el-icon><Star /></el-icon>
        <h3>{{ t('user.noAdoptionRecords') }}</h3>
        <p>{{ t('user.noAdoptionYet') }}</p>
        <el-button type="primary" @click="browsePets">
          {{ t('user.browseAdoptablePets') }}
        </el-button>
      </div>

      <div v-else class="adoptions-list">
        <div v-for="adoption in adoptions" :key="adoption.id" class="adoption-card">
          <div class="pet-image">
            <img :src="adoption.pet.image || '/src/assets/img/dog.jpg'" :alt="adoption.pet.name" />
          </div>
          <div class="adoption-details">
            <div class="pet-info">
              <h3>{{ adoption.pet.name }}</h3>
              <p class="pet-breed">{{ adoption.pet.breed }}</p>
              <p class="pet-age">{{ adoption.pet.age }}岁 · {{ getDisplayGender(adoption.pet.gender) }}</p>
            </div>
            <div class="adoption-info">
              <div class="info-item">
                <span class="label">{{ t('user.adoptionDate') }}：</span>
                <span class="value">{{ formatDate(adoption.adoptDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('user.shelter') }}：</span>
                <span class="value">{{ adoption.shelter.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('user.status') }}：</span>
                <el-tag 
                  :type="'warning'"
                >
                  {{ getStatusText(adoption.pet.status) }}
                </el-tag>

              </div>
            </div>
            <div class="adoption-actions">
              <el-button type="primary" size="small" @click="viewAdoptionTimeline(adoption.pet.id, adoption.pet.name)">
                {{ t('user.viewAdoptionTimeline') }}
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 分页组件 -->
        <Pagination
          v-if="total > 0"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @update:current-page="currentPage = $event"
          @update:page-size="pageSize = $event"
          @pageChange="handlePageChange"
          @sizeChange="handleSizeChange"
        />
      </div>
    </div>
  </div>
  
    <Dialog></Dialog>
  
</template>

<script setup lang="ts">
import { onMounted, provide } from 'vue'
import { Star } from '@element-plus/icons-vue'
import { useAdoptions, useAdoptionTimeline } from '@/api/adoptions'
import { useI18n } from 'vue-i18n'
import { useFormatDate } from '@/utils/index'
import { getDisplayGender } from '@/utils/genderDisplay'
import Dialog from '@/components/Dialog.vue'
import Pagination from '@/components/Pagination.vue'

// 使用领养记录组合式函数
const { t } = useI18n()

const formatDate = useFormatDate().formatDate

// 使用领养时间线组合式函数 - 共享整个实例
const adoptionTimeline = useAdoptionTimeline()
const { loadAdoptionTimeline } = adoptionTimeline

// 提供共享状态给子组件
provide('adoptionTimeline', adoptionTimeline)

 const {
   // 响应式数据
   adoptions,
   themeStore,
   
   // 分页相关数据
   currentPage,
   pageSize,
   total,
   
   // 方法
   browsePets,
   loadAdoptions,
   handlePageChange,
   handleSizeChange,

 } = useAdoptions()

/**
 * 查看领养时间线
 */
const viewAdoptionTimeline = (petId: number, petName: string) => {
  console.log(`点击查看领养时间线 - 宠物ID: ${petId}, 名称: ${petName}`)
  loadAdoptionTimeline(petId, petName)
}

/**
 * 获取状态文本 - 根据pets表status字段判断
 */
const getStatusText = (petStatus: any): string => {
  
  if (petStatus === 'FOSTERING') {
    return t('user.adoptionStatusFostering')
  } else {
    return t('user.adoptionStatusNormal')
  }
}

// 使用统一的性别显示函数（已移至专门的工具文件）

onMounted(() => {
  loadAdoptions()
})

</script>

<style scoped>
@import '@/styles/adoptions.css';
</style>


