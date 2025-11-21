<template>
  <div class="adopt-pet-container" :class="{ 'dark': themeStore.preferences.theme === 'dark' }">
    <!-- 组件加载确认 -->

    <header class="adopt-pet-header">
      <h1>{{ t('nav.adoptCenter') }}</h1>
    </header>
    
    <main class="adopt-pet-main">
      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-card class="filter-card">
          <template #header>
            <div class="card-header">
              <span>{{ t('nav.filterOptions') }}</span>
              <el-button type="primary" size="small" @click="resetFilters">
                {{ t('nav.resetFilters') }}
              </el-button>
            </div>
          </template>
          
          <div class="filter-form">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-form-item :label="t('nav.breed')">
                  <el-input 
                    v-model="breed" 
                    :placeholder="t('nav.breedPlaceholder')"
                    clearable
                    @keyup.enter="applyFilters"
                  />
                </el-form-item>
              </el-col>
              
              <el-col :span="6">
                <el-form-item :label="t('nav.gender')">
                  <el-select 
                    v-model="gender" 
                    :placeholder="t('nav.genderPlaceholder')"
                    clearable
                  >
                    <el-option 
                      v-for="item in genderOptions" 
                      :key="item.value" 
                      :label="item.label" 
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :span="6">
                <el-form-item :label="t('nav.ageRange')">
                  <el-input 
                    v-model.number="minAge" 
                    type="number"
                    :min="0" 
                    :max="30" 
                    :placeholder="t('nav.minAge')"
                    style="width: 45%"
                  />
                  <span style="margin: 0 5px">-</span>
                  <el-input 
                    v-model.number="maxAge" 
                    type="number"
                    :min="0" 
                    :max="30" 
                    :placeholder="t('nav.maxAge')"
                    style="width: 45%"
                  />
                </el-form-item>
              </el-col>
              
              <el-col :span="6">
                <el-form-item>
                  <el-button type="primary" @click="applyFilters">
                    {{ t('nav.applyFilters') }}
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>

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
import { onMounted, computed } from 'vue'
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
  breed,
  gender,
  minAge,
  maxAge,
  themeStore,
  
  // 方法
  fetchAvailablePets,
  resetFilters,
  applyFilters,
  adoptPet,
  viewPetDetail,
  handlePageChange,
  handleSizeChange
} = useAdd()

// 使用i18n
const { t } = useI18n()

// 性别选项
const genderOptions = computed(() => [
  { value: 'male', label: t('message.male') },
  { value: 'female', label: t('message.female') }
])

// 组件挂载时加载数据
onMounted(() => {
  fetchAvailablePets()
})
</script>


<style scoped>
@import '../styles/AdoptionsPets.css';
</style>


