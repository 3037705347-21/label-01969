<template>
  <div class="follow-manage">
    <div class="page-card">
      <h2 class="page-title">跟进管理</h2>

      <el-table :data="recordList" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column prop="petName" label="宠物" min-width="120" />
        <el-table-column prop="adopterName" label="领养人" min-width="100" />
        <el-table-column prop="content" label="跟进内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="照片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.photos?.length"
              :src="row.photos[0]"
              :preview-src-list="row.photos"
              class="image-preview"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusDisplay }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="warning" @click="handleMarkAbnormal(row)">标记异常</el-button>
            <el-button text type="danger" @click="handleReclaim(row)">回收宠物</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPendingList, reviewFollowUp, reclaimPet } from '@/api/follow'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const recordList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPendingList({ pageNum: pageNum.value, pageSize: pageSize.value })
    recordList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取跟进记录失败')
  } finally {
    loading.value = false
  }
}

const handleMarkAbnormal = async (row) => {
  try {
    const { value: comment } = await ElMessageBox.prompt('请输入异常说明', '标记异常', {
      inputPlaceholder: '异常原因'
    })

    await reviewFollowUp(row.id, 2, comment || '')
    ElMessage.success('已标记为异常')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleReclaim = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入回收原因', '回收宠物', {
      inputPlaceholder: '如：弃养、虐待等',
      inputValidator: (val) => !!val || '请输入回收原因'
    })

    await reclaimPet(row.petId, reason)
    ElMessage.success('宠物已回收')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '回收操作失败')
    }
  }
}

onMounted(() => {
  fetchList()
})
</script>
