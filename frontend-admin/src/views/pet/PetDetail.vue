<template>
  <div class="pet-detail" v-loading="loading">
    <div class="page-card detail-card" v-if="pet">
      <!-- 返回按钮 -->
      <div class="detail-back">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>返回列表
        </el-button>
      </div>

      <el-row :gutter="32">
        <el-col :span="10">
          <div class="image-gallery">
            <div class="main-image">
              <el-image 
                :src="currentImage" 
                :alt="pet.name"
                fit="cover"
                :preview-src-list="pet.photos || []"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon :size="60"><Picture /></el-icon>
                    <span>暂无图片</span>
                  </div>
                </template>
              </el-image>
              <!-- 状态角标 -->
              <div class="status-badge" :class="'status-' + pet.status">
                {{ pet.statusDisplay }}
              </div>
            </div>
            <div class="thumbnail-list" v-if="pet.photos?.length > 1">
              <div
                v-for="(photo, index) in pet.photos"
                :key="index"
                class="thumbnail"
                :class="{ active: currentImage === photo }"
                @click="currentImage = photo"
              >
                <img :src="photo" />
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="14">
          <div class="pet-info">
            <div class="info-header">
              <h1>{{ pet.name }}</h1>
              <el-tag :type="getStatusType(pet.status)" size="large" effect="dark" round>{{ pet.statusDisplay }}</el-tag>
            </div>
            
            <p class="pet-brief" v-if="pet.adoptionRequirements">{{ pet.adoptionRequirements }}</p>
            
            <div class="info-grid">
              <div class="info-item">
                <div class="info-icon">🐾</div>
                <div>
                  <span class="label">物种</span>
                  <span class="value">{{ pet.species }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">🏷️</div>
                <div>
                  <span class="label">品种</span>
                  <span class="value">{{ pet.breed || '未知' }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">📅</div>
                <div>
                  <span class="label">年龄</span>
                  <span class="value">{{ pet.ageDisplay }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">⚧</div>
                <div>
                  <span class="label">性别</span>
                  <span class="value">{{ pet.genderDisplay }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">💊</div>
                <div>
                  <span class="label">健康状况</span>
                  <span class="value">{{ pet.healthStatus || '良好' }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">📍</div>
                <div>
                  <span class="label">所在地区</span>
                  <span class="value">{{ pet.location }}</span>
                </div>
              </div>
            </div>
            
            <div class="info-section" v-if="pet.personalityTags?.length">
              <h3>🎭 性格标签</h3>
              <div class="tags">
                <el-tag v-for="tag in pet.personalityTags" :key="tag" type="info" effect="plain" round>{{ tag }}</el-tag>
              </div>
            </div>
            
            <div class="info-section">
              <h3>📋 领养要求</h3>
              <div class="requirements">
                <div class="requirement-item" :class="{ positive: pet.allowSingleLiving }">
                  <el-icon v-if="pet.allowSingleLiving"><CircleCheck /></el-icon>
                  <el-icon v-else><CircleClose /></el-icon>
                  {{ pet.allowSingleLiving ? '允许独居' : '不允许独居' }}
                </div>
                <div class="requirement-item" :class="{ positive: !pet.requireExperience }">
                  <el-icon v-if="!pet.requireExperience"><CircleCheck /></el-icon>
                  <el-icon v-else><Warning /></el-icon>
                  {{ pet.requireExperience ? '需要养宠经验' : '无需养宠经验' }}
                </div>
              </div>
            </div>
            
            <div class="info-section publisher-section">
              <h3>👤 发布者</h3>
              <div class="publisher-info">
                <el-avatar :size="36">{{ pet.publisherName?.charAt(0) }}</el-avatar>
                <span>{{ pet.publisherName }}</span>
              </div>
            </div>
            
            <div class="action-buttons" v-if="userStore.isAdopter && pet.status === 1">
              <el-button type="primary" size="large" round @click="showApplyDialog = true">
                <el-icon><Edit /></el-icon>申请领养
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 申请领养对话框 -->
    <el-dialog v-model="showApplyDialog" title="申请领养" width="600px" @close="handleApplyDialogClose">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="自我介绍" prop="selfIntroduction">
          <el-input
            v-model="applyForm.selfIntroduction"
            type="textarea"
            :rows="4"
            placeholder="请介绍您的情况，包括家庭环境、养宠经验等"
          />
        </el-form-item>
        <el-form-item label="居住证明">
          <el-upload
            action="/api/file/upload"
            :headers="{ Authorization: `Bearer ${userStore.token}` }"
            :on-success="handleResidenceSuccess"
            :on-remove="handleResidenceRemove"
            :before-upload="beforeProofUpload"
            v-model:file-list="residenceFileList"
            :limit="1"
            :on-exceed="() => ElMessage.warning('只能上传一个文件')"
            accept=".jpg,.jpeg,.png,.gif,.pdf"
          >
            <el-button type="primary" plain><el-icon><Upload /></el-icon>上传文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 jpg、png、gif、pdf 格式，不超过 10MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="收入证明">
          <el-upload
            action="/api/file/upload"
            :headers="{ Authorization: `Bearer ${userStore.token}` }"
            :on-success="handleIncomeSuccess"
            :on-remove="handleIncomeRemove"
            :before-upload="beforeProofUpload"
            v-model:file-list="incomeFileList"
            :limit="1"
            :on-exceed="() => ElMessage.warning('只能上传一个文件')"
            accept=".jpg,.jpeg,.png,.gif,.pdf"
          >
            <el-button type="primary" plain><el-icon><Upload /></el-icon>上传文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 jpg、png、gif、pdf 格式，不超过 10MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="handleApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Picture, Upload, ArrowLeft, Edit, CircleCheck, CircleClose, Warning } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getPetDetail } from '@/api/pet'
import { applyAdoption } from '@/api/adoption'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const pet = ref(null)
const currentImage = ref('')

const showApplyDialog = ref(false)
const applyLoading = ref(false)
const applyFormRef = ref()
const residenceFileList = ref([])
const incomeFileList = ref([])

const applyForm = reactive({
  petId: null,
  selfIntroduction: '',
  residenceProof: '',
  incomeProof: ''
})

const applyRules = {
  selfIntroduction: [{ required: true, message: '请填写自我介绍', trigger: 'blur' }]
}

const getStatusType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

const handleResidenceSuccess = (response, uploadFile) => {
  if (response.code === 200 && response.data) {
    applyForm.residenceProof = response.data
    uploadFile.url = response.data
    ElMessage.success('居住证明上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
    // 上传失败移除文件
    residenceFileList.value = []
  }
}

const handleResidenceRemove = () => {
  applyForm.residenceProof = ''
}

const handleIncomeSuccess = (response, uploadFile) => {
  if (response.code === 200 && response.data) {
    applyForm.incomeProof = response.data
    uploadFile.url = response.data
    ElMessage.success('收入证明上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
    incomeFileList.value = []
  }
}

const handleIncomeRemove = () => {
  applyForm.incomeProof = ''
}

const allowedProofTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf']

const beforeProofUpload = (file) => {
  if (!allowedProofTypes.includes(file.type)) {
    ElMessage.error('仅支持 jpg、png、gif、pdf 格式的文件')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

const handleApplyDialogClose = () => {
  applyFormRef.value?.resetFields()
  applyForm.residenceProof = ''
  applyForm.incomeProof = ''
  residenceFileList.value = []
  incomeFileList.value = []
}

const fetchPetDetail = async () => {
  loading.value = true
  try {
    const res = await getPetDetail(route.params.id)
    pet.value = res.data
    currentImage.value = res.data.photos?.[0] || ''
    applyForm.petId = res.data.id
  } catch (error) {
    ElMessage.error(error.message || '获取宠物详情失败')
  } finally {
    loading.value = false
  }
}

const handleApply = async () => {
  await applyFormRef.value.validate()
  applyLoading.value = true
  try {
    await applyAdoption(applyForm)
    ElMessage.success('申请提交成功，请等待审核')
    showApplyDialog.value = false
    fetchPetDetail()
  } catch (error) {
    ElMessage.error(error.message || '申请提交失败，请重试')
  } finally {
    applyLoading.value = false
  }
}

onMounted(() => {
  fetchPetDetail()
})
</script>

<style lang="scss" scoped>
.pet-detail {
  max-width: 100%;
  margin: 0 auto;
}

.detail-card {
  padding: 28px 32px !important;
}

.detail-back {
  margin-bottom: 16px;

  .el-button {
    font-size: 14px;
    color: #909399;
    padding: 0;

    &:hover {
      color: #FF6B35;
    }
  }
}

.image-gallery {
  position: sticky;
  top: 24px;

  .main-image {
    position: relative;
    width: 100%;
    aspect-ratio: 4 / 3;
    border-radius: 16px;
    overflow: hidden;
    margin-bottom: 12px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

    :deep(.el-image) {
      width: 100%;
      height: 100%;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #c0c4cc;

      span {
        margin-top: 12px;
        font-size: 14px;
      }
    }
  }

  .status-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    padding: 5px 14px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;
    backdrop-filter: blur(8px);

    &.status-1 { background: rgba(82, 196, 26, 0.9); }
    &.status-2 { background: rgba(250, 173, 20, 0.9); }
    &.status-3 { background: rgba(144, 147, 153, 0.9); }
    &.status-4 { background: rgba(245, 34, 45, 0.9); }
  }

  .thumbnail-list {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .thumbnail {
    width: 64px;
    height: 64px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.3s;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);

    &.active {
      border-color: #FF6B35;
      box-shadow: 0 2px 10px rgba(255, 107, 53, 0.3);
    }

    &:hover {
      border-color: #FF8C5A;
      transform: translateY(-2px);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.pet-info {
  .info-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 10px;

    h1 {
      font-size: 26px;
      color: #303133;
      margin: 0;
      font-weight: 700;
    }
  }

  .pet-brief {
    color: #606266;
    font-size: 13px;
    line-height: 1.7;
    margin: 0 0 20px;
    padding: 10px 14px;
    background: #fffaf5;
    border-left: 3px solid #FF6B35;
    border-radius: 0 8px 8px 0;
  }

  .info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 20px;
    padding: 16px;
    background: linear-gradient(135deg, #f8f9fc 0%, #f0f2f8 100%);
    border-radius: 12px;
  }

  .info-item {
    display: flex;
    align-items: center;
    gap: 10px;

    .info-icon {
      font-size: 18px;
      width: 34px;
      height: 34px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
      flex-shrink: 0;
    }

    .label {
      display: block;
      font-size: 11px;
      color: #909399;
      margin-bottom: 1px;
    }

    .value {
      font-size: 14px;
      color: #303133;
      font-weight: 600;
    }
  }

  .info-section {
    margin-bottom: 18px;

    h3 {
      font-size: 15px;
      color: #303133;
      margin: 0 0 10px;
      font-weight: 600;
    }

    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .el-tag {
        padding: 4px 12px;
        font-size: 13px;
      }
    }

    .requirements {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
    }

    .requirement-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 14px;
      border-radius: 8px;
      font-size: 13px;
      color: #F5222D;
      background: rgba(245, 34, 45, 0.06);

      .el-icon {
        font-size: 16px;
      }

      &.positive {
        color: #52C41A;
        background: rgba(82, 196, 26, 0.06);
      }
    }
  }

  .publisher-section {
    .publisher-info {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 14px;
      background: #f8f9fc;
      border-radius: 10px;

      span {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
      }
    }
  }

  .action-buttons {
    margin-top: 24px;

    .el-button {
      width: 200px;
      height: 46px;
      font-size: 15px;
      font-weight: 600;
      letter-spacing: 1px;
      transition: all 0.3s ease;

      .el-icon {
        margin-right: 6px;
      }

      &:hover:not(:disabled) {
        transform: translateY(-3px);
        box-shadow: 0 8px 24px rgba(255, 107, 53, 0.35);
      }

      &:active:not(:disabled) {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(255, 107, 53, 0.2);
      }
    }
  }
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

// 响应式
@media (max-width: 992px) {
  .detail-card {
    padding: 16px !important;
  }

  :deep(.el-row) {
    flex-direction: column;

    .el-col {
      max-width: 100%;
      flex: 0 0 100%;
    }
  }

  .image-gallery {
    position: static;
  }

  .pet-info {
    margin-top: 20px;

    .info-header h1 {
      font-size: 22px;
    }
  }
}
</style>
