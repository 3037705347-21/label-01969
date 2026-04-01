<template>
  <div class="knowledge-manage">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">知识库管理</h2>
        <el-button type="primary" @click="showEditDialog(null)">
          <el-icon><Plus /></el-icon>新建文章
        </el-button>
      </div>

      <el-table :data="articleList" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="300" />
        <el-table-column label="分类" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)" size="small">
              {{ getCategoryName(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读量" min-width="100" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="editForm.id ? '编辑文章' : '新建文章'" width="800px" @close="handleEditDialogClose">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="editForm.category" placeholder="请选择分类">
            <el-option label="养宠常识" :value="1" />
            <el-option label="领养须知" :value="2" />
            <el-option label="疾病护理" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" :rows="12" placeholder="请输入文章内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getKnowledgeList, createKnowledge, updateKnowledge, deleteKnowledge } from '@/api/knowledge'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const articleList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const editDialogVisible = ref(false)
const submitLoading = ref(false)
const editFormRef = ref()

const editForm = reactive({
  id: null,
  title: '',
  category: null,
  content: '',
  status: 0
})

const editRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const getCategoryType = (cat) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[cat] || 'info'
}

const getCategoryName = (cat) => {
  const map = { 1: '养宠常识', 2: '领养须知', 3: '疾病护理' }
  return map[cat] || '其他'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeList({
      status: null,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    articleList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const showEditDialog = (row) => {
  // 先重置校验状态
  editFormRef.value?.resetFields()
  if (row) {
    Object.assign(editForm, row)
  } else {
    Object.assign(editForm, { id: null, title: '', category: null, content: '', status: 0 })
  }
  editDialogVisible.value = true
}

const handleEditDialogClose = () => {
  editFormRef.value?.resetFields()
}

const handleSubmit = async () => {
  await editFormRef.value.validate()
  submitLoading.value = true
  try {
    if (editForm.id) {
      await updateKnowledge(editForm)
      ElMessage.success('更新成功')
    } else {
      await createKnowledge(editForm)
      ElMessage.success('创建成功')
    }
    editDialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error(error.message || '保存失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该文章吗？', '提示', { type: 'warning' })
    await deleteKnowledge(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
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
</style>
