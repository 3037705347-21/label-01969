<template>
  <div class="pet-manage">
    <div class="page-card">
      <h2 class="page-title">宠物管理</h2>

      <div class="search-form">
        <el-select v-model="query.species" placeholder="物种" clearable style="width: 120px">
          <el-option label="猫" value="猫" />
          <el-option label="狗" value="狗" />
          <el-option label="其他" value="其他" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="待领养" :value="1" />
          <el-option label="审核中" :value="2" />
          <el-option label="已领养" :value="3" />
          <el-option label="暂不领养" :value="4" />
        </el-select>
        <el-button type="primary" @click="fetchPetList">搜索</el-button>
      </div>

      <el-table :data="petList" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column label="照片" width="100">
          <template #default="{ row }">
            <img v-if="row.photos?.[0]" :src="row.photos[0]" class="image-preview" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="100" />
        <el-table-column prop="species" label="物种" min-width="80" />
        <el-table-column prop="breed" label="品种" min-width="100" />
        <el-table-column prop="publisherName" label="发布者" min-width="120" />
        <el-table-column prop="location" label="地区" min-width="120" />
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusDisplay }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="router.push(`/pet/detail/${row.id}`)">查看</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="fetchPetList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPetList, deletePet } from '@/api/pet'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const petList = ref([])
const total = ref(0)

const query = reactive({
  species: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

const getStatusType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

const fetchPetList = async () => {
  loading.value = true
  try {
    const res = await getPetList(query)
    petList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取宠物列表失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该宠物信息吗？', '提示', { type: 'warning' })
    await deletePet(row.id)
    ElMessage.success('删除成功')
    fetchPetList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchPetList()
})
</script>
