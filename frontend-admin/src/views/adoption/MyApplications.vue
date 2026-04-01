<template>
  <div class="my-applications">
    <div class="page-card">
      <h2 class="page-title">我的申请</h2>
      
      <el-table :data="applicationList" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column label="宠物" min-width="180">
          <template #default="{ row }">
            <div class="pet-cell">
              <img v-if="row.petPhoto" :src="row.petPhoto" class="pet-thumb" />
              <span>{{ row.petName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="救助方审核" min-width="110">
          <template #default="{ row }">
            <el-tag :type="getReviewType(row.rescueReviewStatus)">
              {{ getReviewText(row.rescueReviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="管理员复核" min-width="110">
          <template #default="{ row }">
            <el-tag :type="getReviewType(row.adminReviewStatus)">
              {{ getReviewText(row.adminReviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="家访状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getHomeVisitType(row.homeVisitStatus)">
              {{ getHomeVisitText(row.homeVisitStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最终结果" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getFinalType(row.finalStatus)" size="large">
              {{ row.finalStatusDisplay }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="70" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
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
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="600px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="宠物名称">{{ currentDetail.petName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="自我介绍" :span="2">{{ currentDetail.selfIntroduction }}</el-descriptions-item>
        <el-descriptions-item label="救助方审核">
          <el-tag :type="getReviewType(currentDetail.rescueReviewStatus)">
            {{ getReviewText(currentDetail.rescueReviewStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见">{{ currentDetail.rescueReviewComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="管理员复核">
          <el-tag :type="getReviewType(currentDetail.adminReviewStatus)">
            {{ getReviewText(currentDetail.adminReviewStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="复核意见">{{ currentDetail.adminReviewComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家访状态">
          <el-tag :type="getHomeVisitType(currentDetail.homeVisitStatus)">
            {{ getHomeVisitText(currentDetail.homeVisitStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="家访备注">{{ currentDetail.homeVisitComment || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyApplications, getApplicationDetail } from '@/api/adoption'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const detailLoading = ref(false)
const applicationList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const currentDetail = ref(null)

const getReviewType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getReviewText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

const getHomeVisitType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const getHomeVisitText = (status) => {
  const map = { 0: '未安排', 1: '已安排', 2: '已完成', 3: '不通过' }
  return map[status] || '未知'
}

const getFinalType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyApplications({ pageNum: pageNum.value, pageSize: pageSize.value })
    applicationList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取申请列表失败')
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  detailLoading.value = true
  try {
    const res = await getApplicationDetail(row.id)
    currentDetail.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.pet-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .pet-thumb {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    object-fit: cover;
  }
}
</style>
