<template>
  <div class="message-center" v-loading="loading">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">消息中心</h2>
        <el-button text type="primary" :loading="readAllLoading" @click="handleReadAll">全部已读</el-button>
      </div>
      
      <div class="message-tabs">
        <el-radio-group v-model="messageType" @change="fetchList">
          <el-radio-button :value="''">全部</el-radio-button>
          <el-radio-button :value="1">系统通知</el-radio-button>
          <el-radio-button :value="2">审核结果</el-radio-button>
          <el-radio-button :value="3">跟进提醒</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="message-list">
        <div
          v-for="msg in messageList"
          :key="msg.id"
          class="message-item"
          :class="{ unread: msg.isRead === 0 }"
          @click="handleRead(msg)"
        >
          <div class="message-icon">
            <el-icon v-if="msg.type === 1"><Bell /></el-icon>
            <el-icon v-else-if="msg.type === 2"><Document /></el-icon>
            <el-icon v-else><Calendar /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="title">{{ msg.title }}</span>
              <span class="time">{{ msg.createTime }}</span>
            </div>
            <p class="content">{{ msg.content }}</p>
          </div>
          <div class="unread-dot" v-if="msg.isRead === 0"></div>
        </div>
      </div>
      
      <el-empty v-if="messageList.length === 0 && !loading" description="暂无消息" />
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="fetchList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMessageList, markAsRead, markAllAsRead } from '@/api/message'
import { ElMessage } from 'element-plus'
import { eventBus } from '@/utils/eventBus'

const loading = ref(false)
const readAllLoading = ref(false)
const messageList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const messageType = ref('')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMessageList({
      type: messageType.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    messageList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取消息列表失败')
  } finally {
    loading.value = false
  }
}

const handleRead = async (msg) => {
  if (msg.isRead === 0) {
    try {
      await markAsRead(msg.id)
      msg.isRead = 1
      eventBus.emit('message-read')
    } catch (error) {
      ElMessage.error(error.message || '标记已读失败')
    }
  }
}

const handleReadAll = async () => {
  readAllLoading.value = true
  try {
    await markAllAsRead()
    messageList.value.forEach(msg => msg.isRead = 1)
    eventBus.emit('message-read')
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    readAllLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .page-title {
    margin: 0;
    border: none;
    padding: 0;
  }
}

.message-tabs {
  margin-bottom: 24px;
}

.message-list {
  .message-item {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;
    border-radius: 8px;
    border: 1px solid #f0f0f0;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
    position: relative;
    
    &:hover {
      background: #f5f7fa;
      transform: translateX(4px);
      border-color: #e0e0e0;
    }
    
    &:active {
      background: #ebeef5;
      transform: translateX(2px);
    }
    
    &.unread {
      background: rgba(#FF6B35, 0.05);
      border-color: rgba(#FF6B35, 0.2);
      
      &:hover {
        background: rgba(#FF6B35, 0.08);
      }
    }
  }
  
  .message-icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    background: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    color: #FF6B35;
  }
  
  .message-content {
    flex: 1;
    
    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      .title {
        font-size: 15px;
        font-weight: 500;
        color: #303133;
      }
      
      .time {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .content {
      font-size: 14px;
      color: #606266;
      margin: 0;
      line-height: 1.5;
    }
  }
  
  .unread-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #FF6B35;
    position: absolute;
    top: 12px;
    right: 12px;
  }
}
</style>
