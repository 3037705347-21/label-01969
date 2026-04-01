<template>
  <div class="user-list">
    <div class="page-card">
      <h2 class="page-title">用户管理</h2>

      <div class="search-form">
        <el-select v-model="query.roleType" placeholder="角色类型" clearable style="width: 120px">
          <el-option label="管理员" :value="1" />
          <el-option label="救助方" :value="2" />
          <el-option label="领养人" :value="3" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 100px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
          <el-option label="黑名单" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="userList" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="roleName" label="角色" min-width="100" />
        <el-table-column label="认证状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.verifyStatus === 1 ? 'success' : 'info'">
              {{ row.verifyStatus === 1 ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="160" />
        <el-table-column label="200" width="130" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" text type="danger" @click="handleStatus(row, 0)">禁用</el-button>
            <el-button v-if="row.status === 0" text type="success" @click="handleStatus(row, 1)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="fetchList"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="600px">
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentUser.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ currentUser.roleName }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">
          {{ currentUser.verifyStatus === 1 ? '已认证' : '未认证' }}
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentUser.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职业">{{ currentUser.occupation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="养宠经验">{{ currentUser.petExperience || '-' }}</el-descriptions-item>
        <el-descriptions-item label="机构名称" v-if="currentUser.roleType === 2">
          {{ currentUser.orgName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="机构资质" v-if="currentUser.roleType === 2">
          <a v-if="currentUser.orgLicense" :href="currentUser.orgLicense" target="_blank">查看</a>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, updateUserStatus } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const total = ref(0)

const query = reactive({
  roleType: null,
  status: null,
  pageNum: 1,
  pageSize: 10
})

const detailVisible = ref(false)
const currentUser = ref(null)

const getStatusType = (status) => {
  const map = { 0: 'danger', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '禁用', 1: '正常', 2: '黑名单' }
  return map[status] || '未知'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getUserList(query)
    userList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const showDetail = (row) => {
  currentUser.value = row
  detailVisible.value = true
}

const handleStatus = async (row, status) => {
  const action = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', { type: 'warning' })
    await updateUserStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || `${action}操作失败`)
    }
  }
}

onMounted(() => {
  fetchList()
})
</script>
