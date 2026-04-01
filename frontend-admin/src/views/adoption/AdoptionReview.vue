<template>
  <div class="adoption-review">
    <div class="page-card">
      <h2 class="page-title">领养审核（管理员复核）</h2>

      <el-table :data="applicationList" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column label="宠物" min-width="180">
          <template #default="{ row }">
            <div class="pet-cell">
              <img v-if="row.petPhoto" :src="row.petPhoto" class="pet-thumb" />
              <span>{{ row.petName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" min-width="100" />
        <el-table-column prop="applicantPhone" label="联系电话" min-width="130" />
        <el-table-column label="救助方审核" min-width="100">
          <template #default="{ row }">
            <el-tag type="success">已通过</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="管理员复核" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getReviewType(row.adminReviewStatus)">
              {{ getReviewText(row.adminReviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="showDetail(row)">详情</el-button>
            <template v-if="row.adminReviewStatus === 0">
              <el-button text type="success" @click="handleAdminReview(row, 1)">通过</el-button>
              <el-button text type="danger" @click="handleAdminReview(row, 2)">拒绝</el-button>
            </template>
            <template v-if="row.adminReviewStatus === 1">
              <el-button text type="warning" @click="handleHomeVisit(row)">家访</el-button>
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
    <el-dialog v-model="detailVisible" title="申请详情" width="700px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="宠物名称">{{ currentDetail.petName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentDetail.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentDetail.applicantPhone }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="自我介绍" :span="2">{{ currentDetail.selfIntroduction }}</el-descriptions-item>
        <el-descriptions-item label="居住证明">
          <a v-if="currentDetail.residenceProof" :href="currentDetail.residenceProof" target="_blank">查看</a>
          <span v-else>未上传</span>
        </el-descriptions-item>
        <el-descriptions-item label="收入证明">
          <a v-if="currentDetail.incomeProof" :href="currentDetail.incomeProof" target="_blank">查看</a>
          <span v-else>未上传</span>
        </el-descriptions-item>
        <el-descriptions-item label="救助方审核">
          <el-tag type="success">已通过</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="救助方意见">{{ currentDetail.rescueReviewComment || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 家访状态更新对话框 -->
    <el-dialog v-model="homeVisitDialogVisible" title="更新家访状态" width="500px">
      <el-form :model="homeVisitForm" label-width="100px">
        <el-form-item label="家访结果" required>
          <el-select v-model="homeVisitForm.status" placeholder="请选择家访结果" style="width: 100%">
            <el-option label="已安排家访" :value="1" />
            <el-option label="家访通过" :value="2" />
            <el-option label="家访不通过" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="homeVisitForm.comment" type="textarea" :rows="3" placeholder="请输入家访情况说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="homeVisitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="homeVisitLoading" @click="submitHomeVisit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPendingReviewList, getApplicationDetail, adminReview, updateHomeVisit } from '@/api/adoption'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const applicationList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const currentDetail = ref(null)

const homeVisitDialogVisible = ref(false)
const homeVisitLoading = ref(false)
const homeVisitForm = reactive({
  applicationId: null,
  status: null,
  comment: ''
})

const getReviewType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getReviewText = (status) => {
  const map = { 0: '待复核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPendingReviewList({ pageNum: pageNum.value, pageSize: pageSize.value })
    applicationList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取审核列表失败')
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

const handleAdminReview = async (row, reviewStatus) => {
  const action = reviewStatus === 1 ? '通过' : '拒绝'
  try {
    const { value: comment } = await ElMessageBox.prompt(`请输入${action}原因（可选）`, '复核确认', {
      inputPlaceholder: '复核意见',
      confirmButtonText: action
    })

    await adminReview({
      applicationId: row.id,
      status: reviewStatus,
      comment: comment || ''
    })

    ElMessage.success('复核完成')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '复核操作失败')
    }
  }
}

const handleHomeVisit = (row) => {
  homeVisitForm.applicationId = row.id
  homeVisitForm.status = null
  homeVisitForm.comment = ''
  homeVisitDialogVisible.value = true
}

const submitHomeVisit = async () => {
  if (!homeVisitForm.status) {
    ElMessage.warning('请选择家访结果')
    return
  }

  homeVisitLoading.value = true
  try {
    await updateHomeVisit({
      applicationId: homeVisitForm.applicationId,
      status: homeVisitForm.status,
      comment: homeVisitForm.comment || ''
    })

    ElMessage.success('家访状态更新成功')
    homeVisitDialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error(error.message || '家访状态更新失败')
  } finally {
    homeVisitLoading.value = false
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
