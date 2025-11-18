<template>
  <div class="fosters-container" :class="{ dark: isDark }">
    <div class="page-header">
      <h1>{{ t('user.fosterRecords') }}</h1>
      <p>{{ t('user.viewFosterHistory') }}</p>
      
    </div>

    <div class="fosters-content">
      <div v-if="fosters.length === 0" class="empty-state">
        <el-icon><OfficeBuilding /></el-icon>
        <h3>{{ t('user.noFosterRecords') }}</h3>
        <p>{{ t('user.noFosterYet') }}</p>
      </div>

      <div v-else class="fosters-list">
        <div v-for="foster in fosters" :key="foster.id" class="foster-card">
          <div class="pet-image">
            <img :src="foster.pet.image || '/src/assets/img/dog.jpg'" :alt="foster.pet.name" />
          </div>
          <div class="foster-details">
            <div class="pet-info">
              <h3>{{ foster.pet.name }}</h3>
              <p class="pet-breed">{{ foster.pet.breed }}</p>
              <p class="pet-age">{{ foster.pet.age }}{{ t('user.yearsOld') }} · {{ getDisplayGender(foster.pet.gender) }}</p>
            </div>
            <div class="foster-info">
              <div class="info-item">
                <span class="label">{{ t('user.fosterStart') }}：</span>
                <span class="value">{{ formatDate(foster.startDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('user.fosterEnd') }}：</span>
                <span class="value">{{ foster.endDate ? formatDate(foster.endDate) : t('user.ongoing') }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('user.shelter') }}：</span>
                <span class="value">{{ foster.shelter?.name || t('user.unknown') }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('user.status') }}：</span>
                <el-tag :type="foster.status === 'ongoing' ? 'warning' : 'success'">
                  {{ foster.status === 'ongoing' ? t('user.ongoing') : t('user.fosterEnded') }}
                </el-tag>
              </div>
            </div>
            <div class="foster-actions">
             
              <el-button 
                type="danger" 
                size="small" 
                @click="handleDeleteFoster(foster.id)"
                :disabled="!foster.id || foster.id === undefined || foster.id === null"
              >
                {{ t('user.deleteRecord') }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页组件 -->
      <Pagination
        v-if="total > 0"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
        @pageChange="handlePageChange"
        @sizeChange="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { OfficeBuilding } from '@element-plus/icons-vue'
import { useFosters } from '@/api/fosters'
import { useFormatDate } from '@/utils'
import { getDisplayGender } from '@/utils/genderDisplay'
import Pagination from '@/components/Pagination.vue'

// 使用i18n
const { t } = useI18n()

const formatDate = useFormatDate().formatDate

// 使用统一的性别显示函数（已移至专门的工具文件）

// 使用组合式函数
const {
  deleteFoster,
  fosters,
  isDark,
  loadFosters,
  currentPage,
  pageSize,
  total,
  handlePageChange,
  handleSizeChange,
  applyTheme,
} = useFosters()

// 包装删除函数，传递必要的参数
const handleDeleteFoster = async (fosterId: number) => {
  await deleteFoster(fosterId, fosters, total, currentPage, loadFosters)
}

onMounted(() => {
  loadFosters()
  // 应用主题背景
  applyTheme()
})
</script>

<style scoped>
@import '@/styles/fosters.css';
</style>

