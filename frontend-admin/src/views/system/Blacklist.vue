<template>
  <div class="blacklist">
    <div class="page-card">
      <h2 class="page-title">黑名单管理</h2>

      <div class="search-form">
        <el-select v-model="query.type" placeholder="类型" clearable style="width: 120px" @change="fetchList">
          <el-option label="领养人" :value="1" />
          <el-option label="救助方" :value="2" />
        </el-select>
        <el-button type="danger" @click="addDialogVisible = true">添加黑名单</el-button>
      </div>

      <el-table :data="blacklist" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column prop="userId" label="用户ID" min-width="100" />
        <el-table-column label="类型" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'warning' : 'danger'">
              {{ row.type === 1 ? '领养人' : '救助方' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="拉黑原因" min-width="300" />
        <el-table-column prop="createTime" label="拉黑时间" min-width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleRemove(row)">移除</el-button>
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

    <!-- 添加黑名单对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加黑名单" width="480px" @close="resetAddForm">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="80px">
        <el-form-item label="用户" prop="userId">
          <el-select
            v-model="addForm.userId"
            filterable
            remote
            reserve-keyword
            placeholder="输入用户名搜索"
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            style="width: 100%"
            @change="handleUserChange"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.username}（${user.realName || '未实名'}，${user.roleName}）`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="addForm.type" placeholder="请选择类型" style="width: 100%" disabled>
            <el-option label="领养人" :value="1" />
            <el-option label="救助方" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="拉黑原因" prop="reason">
          <el-input v-model="addForm.reason" type="textarea" :rows="3" placeholder="请输入拉黑原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="addLoading" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBlacklistPage, removeFromBlacklist, addToBlacklist } from '@/api/blacklist'
import { getUserList } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const blacklist = ref([])
const total = ref(0)

const query = reactive({
  type: null,
  pageNum: 1,
  pageSize: 10
})

const addDialogVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref()
const userSearchLoading = ref(false)
const userOptions = ref([])

const addForm = reactive({
  userId: null,
  type: null,
  reason: ''
})

const addRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  reason: [{ required: true, message: '请输入拉黑原因', trigger: 'blur' }]
}

const searchUsers = async (keyword) => {
  if (!keyword) {
    userOptions.value = []
    return
  }
  userSearchLoading.value = true
  try {
    const res = await getUserList({ pageNum: 1, pageSize: 50 })
    userOptions.value = (res.data.records || []).filter(u =>
      u.username?.includes(keyword) || u.realName?.includes(keyword)
    )
  } catch {
    userOptions.value = []
  } finally {
    userSearchLoading.value = false
  }
}

const handleUserChange = (userId) => {
  const user = userOptions.value.find(u => u.id === userId)
  if (user) {
    // roleType: 2救助方 3领养人 -> blacklist type: 2救助方 1领养人
    addForm.type = user.roleType === 2 ? 2 : 1
  }
}

const resetAddForm = () => {
  addForm.userId = null
  addForm.type = null
  addForm.reason = ''
  userOptions.value = []
  addFormRef.value?.resetFields()
}

const handleAdd = async () => {
  await addFormRef.value.validate()
  addLoading.value = true
  try {
    await addToBlacklist(addForm.userId, addForm.type, addForm.reason)
    ElMessage.success('添加黑名单成功')
    addDialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error(error.message || '添加黑名单失败')
  } finally {
    addLoading.value = false
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getBlacklistPage(query)
    blacklist.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取黑名单列表失败')
  } finally {
    loading.value = false
  }
}

const handleRemove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要将该用户移出黑名单吗？', '提示', { type: 'warning' })
    await removeFromBlacklist(row.id)
    ElMessage.success('已移出黑名单')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '移除操作失败')
    }
  }
}

onMounted(() => {
  fetchList()
})
</script>
