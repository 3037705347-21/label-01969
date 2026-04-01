<template>
  <div class="my-follow-up">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">我的跟进</h2>
        <el-button type="primary" @click="showSubmitDialog">
          <el-icon><Plus /></el-icon>提交跟进
        </el-button>
      </div>
      
      <el-table :data="recordList" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column prop="petName" label="宠物" min-width="120" />
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
        <el-table-column prop="adminComment" label="管理员评论" min-width="150" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" min-width="160" />
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
    
    <!-- 提交跟进对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交跟进记录" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择宠物" prop="petId">
          <el-select v-model="form.petId" placeholder="请选择已领养的宠物" @change="handlePetChange">
            <el-option
              v-for="app in adoptedApplications"
              :key="app.petId"
              :label="app.petName"
              :value="app.petId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请描述宠物近期状况"
          />
        </el-form-item>
        <el-form-item label="上传照片">
          <el-upload
            ref="uploadRef"
            action="/api/file/upload"
            :headers="{ Authorization: `Bearer ${userStore.token}` }"
            :on-success="handlePhotoSuccess"
            :on-error="handlePhotoError"
            :before-upload="beforeUpload"
            v-model:file-list="photoFileList"
            list-type="picture-card"
            accept="image/*"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持 jpg、png、gif 格式，单张不超过 10MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getMyRecords, submitFollowUp } from '@/api/follow'
import { getMyApplications } from '@/api/adoption'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const loading = ref(false)
const recordList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const submitDialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const uploadRef = ref()
const photoFileList = ref([])
const adoptedApplications = ref([])

const form = reactive({
  petId: null,
  applicationId: null,
  content: '',
  photos: []
})

const rules = {
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  content: [{ required: true, message: '请填写跟进内容', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyRecords({ pageNum: pageNum.value, pageSize: pageSize.value })
    recordList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取跟进记录失败')
  } finally {
    loading.value = false
  }
}

const fetchAdoptedApplications = async () => {
  try {
    const res = await getMyApplications({ pageNum: 1, pageSize: 100 })
    adoptedApplications.value = (res.data.records || []).filter(app => app.finalStatus === 1)
  } catch (e) {
    ElMessage.error(e.message || '获取已领养宠物列表失败')
  }
}

const showSubmitDialog = () => {
  formRef.value?.resetFields()
  form.petId = null
  form.applicationId = null
  form.content = ''
  form.photos = []
  photoFileList.value = []
  submitDialogVisible.value = true
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

const handlePetChange = (petId) => {
  const app = adoptedApplications.value.find(a => a.petId === petId)
  if (app) {
    form.applicationId = app.id
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

const handlePhotoSuccess = (response, uploadFile) => {
  if (response.code === 200 && response.data) {
    uploadFile.response = response
    syncPhotosFromFileList()
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
    const index = photoFileList.value.findIndex(f => f.uid === uploadFile.uid)
    if (index > -1) {
      photoFileList.value.splice(index, 1)
    }
  }
}

const handlePhotoError = (error, uploadFile) => {
  ElMessage.error('图片上传失败，请重试')
  const index = photoFileList.value.findIndex(f => f.uid === uploadFile.uid)
  if (index > -1) {
    photoFileList.value.splice(index, 1)
  }
}

const syncPhotosFromFileList = () => {
  form.photos = photoFileList.value
    .filter(file => file.status === 'success' && file.response?.data)
    .map(file => file.response.data)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await submitFollowUp(form)
    ElMessage.success('跟进记录提交成功')
    submitDialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error(error.message || '提交失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchList()
  fetchAdoptedApplications()
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

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
