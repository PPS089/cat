<template>
  <!-- 领养时间线对话框 -->
  <el-dialog
    v-model="timelineDialogVisible"
    :title="`${t('user.adoptionTimeline')} - ${selectedPetName}`"
    width="60%"
    :close-on-click-modal="false"
    @close="closeTimelineDialog">
    
    <div v-loading="timelineLoading" class="timeline-container">
      <div v-if="timeline.length === 0" class="empty-state">
        <el-empty :description="t('user.noTimelineRecords')" />
      </div>
      
      <div v-else class="timeline">
        <el-timeline>
          <el-timeline-item
            v-for="item in timeline"
            :key="item.id"
            :timestamp="formatDate(item.actionDate)"
            :type="getTimelineItemType(item.action)"
            placement="top">
            <el-card>
              <div class="timeline-item-content">
                <div class="item-header">
                  <h4>{{ getActionTypeText(item.action) }}</h4>
                  <el-tag :type="getTagType(item.action)" size="small">
                    {{ item.status }}
                  </el-tag>
                </div>
                <p class="item-description">{{ item.description }}</p>
                <div v-if="item.shelterName" class="item-shelter">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>{{ item.shelterName }}</span>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeTimelineDialog">{{ t('common.close') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { OfficeBuilding } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useFormatDate } from '@/utils/index'
import { watch, inject } from 'vue'
import { useAdoptionTimeline } from '@/api/adoptions'

const { t } = useI18n()
const formatDate = useFormatDate().formatDate

// 使用inject获取共享的timeline实例
const adoptionTimeline = inject('adoptionTimeline') || useAdoptionTimeline()

// 从共享实例中解构需要的属性和方法
const {
  timeline,
  loading: timelineLoading,
  dialogVisible: timelineDialogVisible,
  selectedPetName,
  closeDialog: closeTimelineDialog,
  getActionTypeText
} = adoptionTimeline as ReturnType<typeof useAdoptionTimeline>

// 监听弹窗状态变化
watch(timelineDialogVisible, (newVal) => {
  console.log('弹窗状态变化:', newVal)
})

/**
 * 获取时间线项类型
 */
const getTimelineItemType = (action: string): string => {
  const typeMap: Record<string, string> = {
    'adopted': 'primary',
    'fostered': 'warning',
    'foster_ended': 'info',
    'returned': 'danger'
  }
  return typeMap[action] || 'info'
}

/**
 * 获取标签类型
 */
const getTagType = (action: string): string => {
  const typeMap: Record<string, string> = {
    'adopted': 'success',
    'fostered': 'warning',
    'foster_ended': 'info',
    'returned': 'danger'
  }
  return typeMap[action] || 'info'
}
</script>

<style scoped>
.timeline-container {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.timeline {
  padding: 20px 0;
}

.timeline-item-content {
  padding: 10px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.item-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.item-description {
  margin: 10px 0;
  color: #606266;
  line-height: 1.5;
}

.item-shelter {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>