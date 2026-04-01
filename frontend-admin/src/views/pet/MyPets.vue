<template>
  <div class="my-pets">
    <div class="page-card">
      <div class="page-header">
        <h2 class="page-title">我的宠物</h2>
        <el-button type="primary" @click="showEditDialog(null)">
          <el-icon><Plus /></el-icon>发布宠物
        </el-button>
      </div>

      <el-table :data="petList" v-loading="loading" stripe empty-text="暂无数据" style="width: 100%">
        <el-table-column label="照片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.photos && row.photos.length > 0"
              :src="row.photos[0]"
              :preview-src-list="row.photos"
              class="image-preview"
              fit="cover"
            />
            <span v-else>暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="100" />
        <el-table-column prop="species" label="物种" min-width="80" />
        <el-table-column prop="breed" label="品种" min-width="100" />
        <el-table-column prop="ageDisplay" label="年龄" min-width="90" />
        <el-table-column prop="genderDisplay" label="性别" min-width="60" />
        <el-table-column prop="location" label="地区" min-width="120" />
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusDisplay }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button text type="success" @click="showMatchDialog(row)" v-if="row.status === 1">推荐</el-button>
            <el-button text type="primary" @click="handleStatusChange(row)">状态</el-button>
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
          @change="fetchPetList"
        />
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="editForm.id ? '编辑宠物' : '发布宠物'" width="700px" @close="handleEditDialogClose">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="editForm.name" placeholder="请输入宠物名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物种" prop="species">
              <el-select v-model="editForm.species" placeholder="请选择">
                <el-option label="猫" value="猫" />
                <el-option label="狗" value="狗" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品种">
              <el-input v-model="editForm.breed" placeholder="如：金毛、英短" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄(月)" prop="ageMonths">
              <el-input-number v-model="editForm.ageMonths" :min="1" :max="300" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="editForm.gender">
                <el-radio :value="1">公</el-radio>
                <el-radio :value="2">母</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在地区" prop="location">
              <el-input v-model="editForm.location" placeholder="如：北京市朝阳区" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="健康状况">
          <el-input v-model="editForm.healthStatus" placeholder="如：已绝育、已驱虫" />
        </el-form-item>
        <el-form-item label="性格标签">
          <el-select v-model="editForm.personalityTags" multiple placeholder="选择性格标签">
            <el-option label="粘人" value="粘人" />
            <el-option label="独立" value="独立" />
            <el-option label="活泼" value="活泼" />
            <el-option label="安静" value="安静" />
            <el-option label="亲人" value="亲人" />
            <el-option label="胆小" value="胆小" />
          </el-select>
        </el-form-item>
        <el-form-item label="照片">
          <el-upload
            ref="uploadRef"
            action="/api/file/upload"
            :headers="{ Authorization: `Bearer ${userStore.token}` }"
            :on-success="handlePhotoSuccess"
            :on-remove="handlePhotoRemove"
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="允许独居">
              <el-switch v-model="editForm.allowSingleLiving" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需要经验">
              <el-switch v-model="editForm.requireExperience" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="领养要求">
          <el-input v-model="editForm.adoptionRequirements" type="textarea" :rows="3" placeholder="其他领养要求说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 更新状态对话框 -->
    <el-dialog v-model="statusDialogVisible" title="更新状态" width="420px">
      <el-form label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="statusForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option :value="1" label="待领养" />
            <el-option :value="2" label="审核中" />
            <el-option :value="3" label="已领养" />
            <el-option :value="4" label="暂不领养" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusLoading" @click="confirmStatusChange">确定</el-button>
      </template>
    </el-dialog>

    <!-- 推荐领养人对话框 -->
    <el-dialog v-model="matchDialogVisible" title="推荐领养人" width="800px">
      <div v-if="currentMatchPet" class="match-pet-info">
        <span>宠物：<strong>{{ currentMatchPet.name }}</strong></span>
        <span>要求：{{ currentMatchPet.requireExperience ? '需要养宠经验' : '无需经验' }} | {{ currentMatchPet.allowSingleLiving ? '允许独居' : '不允许独居' }}</span>
      </div>
      <el-table :data="matchedAdopters" v-loading="matchLoading" stripe empty-text="暂无匹配的领养人">
        <el-table-column label="匹配度" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.matchScore" :color="getScoreColor(row.matchScore)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="100">
          <template #default="{ row }">
            {{ row.realName || row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
        <el-table-column label="认证" width="80">
          <template #default="{ row }">
            <el-tag :type="row.verified ? 'success' : 'info'" size="small">
              {{ row.verified ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matchReason" label="匹配说明" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getMyPetList, createPet, updatePet, updatePetStatus, deletePet, getMatchedAdopters } from '@/api/pet'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const loading = ref(false)
const petList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const editDialogVisible = ref(false)
const submitLoading = ref(false)
const editFormRef = ref()
const uploadRef = ref()
const photoFileList = ref([])

// 更新状态相关
const statusDialogVisible = ref(false)
const statusLoading = ref(false)
const statusForm = reactive({ id: null, status: null })

// 推荐领养人相关
const matchDialogVisible = ref(false)
const matchLoading = ref(false)
const currentMatchPet = ref(null)
const matchedAdopters = ref([])

const editForm = reactive({
  id: null,
  name: '',
  species: '',
  breed: '',
  ageMonths: 12,
  gender: 1,
  healthStatus: '',
  personalityTags: [],
  photos: [],
  location: '',
  allowSingleLiving: 1,
  requireExperience: 0,
  adoptionRequirements: ''
})

const editRules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  species: [{ required: true, message: '请选择物种', trigger: 'change' }],
  ageMonths: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  location: [{ required: true, message: '请输入所在地区', trigger: 'blur' }]
}

const getStatusType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

const fetchPetList = async () => {
  loading.value = true
  try {
    const res = await getMyPetList({ pageNum: pageNum.value, pageSize: pageSize.value })
    petList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const showEditDialog = (row) => {
  // 先重置校验状态
  editFormRef.value?.resetFields()
  if (row) {
    Object.assign(editForm, {
      ...row,
      photos: row.photos ? [...row.photos] : [],
      personalityTags: row.personalityTags ? [...row.personalityTags] : []
    })
    photoFileList.value = (row.photos || []).map((url, i) => ({
      name: `photo${i}.jpg`,
      url: url,
      status: 'success',
      response: { code: 200, data: url }
    }))
  } else {
    Object.assign(editForm, {
      id: null, name: '', species: '', breed: '', ageMonths: 12, gender: 1,
      healthStatus: '', personalityTags: [], photos: [], location: '',
      allowSingleLiving: 1, requireExperience: 0, adoptionRequirements: ''
    })
    photoFileList.value = []
  }
  editDialogVisible.value = true
}

const handleEditDialogClose = () => {
  editFormRef.value?.resetFields()
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

const handlePhotoSuccess = (response, uploadFile, uploadFiles) => {
  if (response.code === 200 && response.data) {
    const photoUrl = response.data
    // 保存服务器返回的 URL 到 response 中，用于后续提交
    uploadFile.response = response
    // 使用本地 blob URL 作为预览，避免 404 问题
    // uploadFile.url 已经在上传时自动设置为 blob URL
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

const handlePhotoRemove = (uploadFile, uploadFiles) => {
  // 使用回调参数 uploadFiles（已移除后的最新列表），而非 photoFileList.value
  editForm.photos = uploadFiles
    .filter(file => file.status === 'success' && file.response?.data)
    .map(file => file.response.data)
}

const syncPhotosFromFileList = () => {
  editForm.photos = photoFileList.value
    .filter(file => file.status === 'success' && file.response?.data)
    .map(file => file.response.data)
}

const handleSubmit = async () => {
  await editFormRef.value.validate()
  submitLoading.value = true
  try {
    const submitData = {
      ...editForm,
      photos: [...editForm.photos]
    }
    if (editForm.id) {
      await updatePet(submitData)
      ElMessage.success('更新成功')
    } else {
      await createPet(submitData)
      ElMessage.success('发布成功')
    }
    editDialogVisible.value = false
    fetchPetList()
  } finally {
    submitLoading.value = false
  }
}

const handleStatusChange = (row) => {
  statusForm.id = row.id
  statusForm.status = row.status
  statusDialogVisible.value = true
}

const confirmStatusChange = async () => {
  statusLoading.value = true
  try {
    await updatePetStatus(statusForm.id, statusForm.status)
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    fetchPetList()
  } finally {
    statusLoading.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该宠物信息吗？', '提示', { type: 'warning' })
  await deletePet(row.id)
  ElMessage.success('删除成功')
  fetchPetList()
}

// 推荐领养人功能
const showMatchDialog = async (row) => {
  currentMatchPet.value = row
  matchDialogVisible.value = true
  matchLoading.value = true
  try {
    const res = await getMatchedAdopters(row.id, 10)
    matchedAdopters.value = res.data || []
  } catch (error) {
    ElMessage.error(error.message || '获取推荐领养人失败')
    matchedAdopters.value = []
  } finally {
    matchLoading.value = false
  }
}

const getScoreColor = (score) => {
  if (score >= 70) return '#52C41A'
  if (score >= 50) return '#FF6B35'
  return '#909399'
}

onMounted(() => {
  fetchPetList()
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

.image-preview {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.match-pet-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
  
  strong {
    color: #303133;
  }
}
</style>
