<template>
  <div class="received-applications">
    <div class="page-card">
      <h2 class="page-title">收到的申请</h2>

      <div class="search-form">
        <el-select v-model="status" placeholder="审核状态" clearable style="width: 120px" @change="fetchList">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
      </div>

      <el-table :data="applicationList" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column label="宠物" min-width="180">
          <template #default="{ row }">
            <div class="pet-cell">
              <img v-if="row.petPhoto" :src="row.petPhoto" class="pet-thumb" />
              <span>{{ row.petName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" min-width="120" />
        <el-table-column prop="applicantPhone" label="联系电话" min-width="130" />
        <el-table-column label="审核状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getReviewType(row.rescueReviewStatus)">
              {{ getReviewText(row.rescueReviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="showDetail(row)">详情</el-button>
            <template v-if="row.rescueReviewStatus === 0">
              <el-button text type="success" @click="handleReview(row, 1)">通过</el-button>
              <el-button text type="danger" @click="handleReview(row, 2)">拒绝</el-button>
            </template>
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
        <el-descriptions-item label="申请人">{{ currentDetail.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentDetail.applicantPhone }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="自我介绍" :span="2">{{ currentDetail.selfIntroduction }}</el-descriptions-item>
        <el-descriptions-item label="居住证明" :span="2">
          <a v-if="currentDetail.residenceProof" :href="currentDetail.residenceProof" target="_blank">查看文件</a>
          <span v-else>未上传</span>
        </el-descriptions-item>
        <el-descriptions-item label="收入证明" :span="2">
          <a v-if="currentDetail.incomeProof" :href="currentDetail.incomeProof" target="_blank">查看文件</a>
          <span v-else>未上传</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReceivedApplications, getApplicationDetail, rescueReview } from '@/api/adoption'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const reviewLoading = ref(false)
const applicationList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const status = ref(null)

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

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getReceivedApplications({
      status: status.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    applicationList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取申请列表失败')
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  try {
    const res = await getApplicationDetail(row.id)
    currentDetail.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
  }
}

const handleReview = async (row, reviewStatus) => {
  const action = reviewStatus === 1 ? '通过' : '拒绝'
  try {
    const { value: comment } = await ElMessageBox.prompt(`请输入${action}原因（可选）`, '审核确认', {
      inputPlaceholder: '审核意见',
      confirmButtonText: action,
      cancelButtonText: '取消'
    })

    reviewLoading.value = true
    await rescueReview({
      applicationId: row.id,
      status: reviewStatus,
      comment: comment || ''
    })

    ElMessage.success('审核完成')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审核操作失败')
    }
  } finally {
    reviewLoading.value = false
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
