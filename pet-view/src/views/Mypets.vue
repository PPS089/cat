<template>
  <div class="pets-container" :class="{ 'dark': themeStore.preferences.theme === 'dark' }">
    <header class="pets-header">
      <h1>我的领养宠物</h1>
    </header>
    
    <main class="pets-main">
      <div class="pets-list">
        <!-- <div v-if="loading" class="loading-state">
          <p>{{ t('message.loading') }}</p>
        </div> -->
        
        <div v-if="pets.length === 0" class="empty-state">
          <p>暂无领养的宠物</p>
          <button @click="addPet" class="add-first-pet-btn">去领养宠物</button>
        </div>
        
        <div v-else class="pets-grid">
          <div v-for="pet in pets" :key="pet.pid" class="pet-card">
            <div class="pet-image">
              <img :src="pet.image || '/src/assets/img/dog.jpg'" :alt="pet.name" />
              <div class="pet-type-badge adoption">
                领养
              </div>
            </div>
            <div class="pet-info">
              <h3>{{ pet.name }}
                <span v-if="pet.petStatus === 'FOSTERING' || pet.isFostering" class="fostering-indicator">(寄养中)</span>
              </h3>
              <p class="pet-breed">{{ pet.breed }}</p>
              <p class="pet-age">{{ pet.age }}岁</p>
              <p v-if="pet.adoptionDate" class="pet-date">
                {{ t('message.adoptDate') }}: {{ pet.adoptionDate }}
              </p>
              <p v-if="pet.sname" class="pet-shelter">
                {{ t('message.shelter') }}: {{ pet.sname }}
              </p>

              <div class="pet-actions">
                <!-- 寄养相关操作 - 这些是通过/user/adoptions获取的领养宠物，总是显示寄养按钮 -->
                <template v-if="pet.petStatus === 'FOSTERING' || pet.isFostering">
                  <!-- 如果正在寄养中，显示结束寄养按钮 -->
                  <button @click="endFoster(pet)" class="end-foster-btn">{{ t('message.endFoster') }}</button>
                  <div class="foster-status">🏠 {{ t('message.fostering') }}</div>
                </template>
                <template v-else>
                  <!-- 如果未寄养，显示开始寄养按钮 -->
                  <button @click="startFoster(pet)" class="start-foster-btn">{{ t('message.startFoster') }}</button>
                </template>
                <button @click="editPet(pet.pid)" class="edit-btn">{{ t('message.edit') }}</button>
              <!-- <button @click="deletePet(pet.id)" class="delete-btn">{{ translations.delete }}</button> -->
            </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>

  <!-- 分页组件 -->
  <Pagination
    v-if="total > 0"
    :current-page="currentPage"
    :page-size="pageSize"
    :total="total"
    :page-sizes="[5, 10, 20, 50]"
    @update:current-page="currentPage = $event"
    @update:page-size="pageSize = $event"
    @pageChange="handlePageChange"
    @sizeChange="handlePageSizeChange"
  />

  <!-- 寄养对话框 -->
  <el-dialog
    v-model="showFosterDialog"
    :title="t('message.startFoster')"
    width="500px"
    :before-close="() => showFosterDialog = false"
  >
    <el-form :model="{ selectedShelter }" label-width="120px">
      <el-form-item :label="t('message.selectShelter')">
        <el-select 
          v-model="selectedShelter" 
          :placeholder="'请选择收容所'"
          style="width: 100%"
        >
          <el-option label="请选择收容所" :value="null" />
          <el-option 
            v-for="shelter in shelters" 
            :key="shelter.sid" 
            :label="`${shelter.shelterName} - ${shelter.shelterAddress}`"
            :value="shelter.sid"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showFosterDialog = false">{{ t('message.cancel') }}</el-button>
        <el-button type="primary" @click="confirmStartFoster">{{ t('message.confirm') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { usePets } from '@/api/mypets'
import { useThemeStore } from '@/stores/theme'
import { onMounted, watch } from 'vue'
import Pagination from '@/components/Pagination.vue'

// 初始化存储
const themeStore = useThemeStore()

// 解构使用usePets组合式函数
const {
  // 响应式状态
    pets,
    shelters,
    showFosterDialog,
    selectedShelter,
  
  // 分页状态
  currentPage,
  pageSize,
  total,
  
  // i18n 函数
  t,
  
  // 方法
  addPet,
  editPet,
  startFoster,
  confirmStartFoster,
  endFoster,
  fetchPets,
  handlePageChange,
  handlePageSizeChange
} = usePets()





onMounted(async () => {
  fetchPets()
})

// 监听分页状态变化（用于调试）
watch([currentPage, pageSize, total], ([newPage, newSize, newTotal], [oldPage, oldSize, oldTotal]) => {
  console.log('分页状态变化:', {
    currentPage: { old: oldPage, new: newPage },
    pageSize: { old: oldSize, new: newSize },
    total: { old: oldTotal, new: newTotal }
  })
})

</script>

<style scoped >
@import '@/styles/mypets.css';
</style>
