<template>
  <div class="home" v-loading="loading">
    <div class="welcome-card">
      <div class="welcome-content">
        <h1>欢迎来到宠物领养系统 🐾</h1>
        <p>给流浪的毛孩子一个温暖的家</p>
        <el-button type="primary" size="large" @click="router.push('/pet/list')">
          浏览待领养宠物
        </el-button>
      </div>
      <div class="welcome-image">🐕🐈</div>
    </div>
    
    <el-row :gutter="24">
      <el-col :xs="24" :sm="8" :md="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(255, 107, 53, 0.1); color: #FF6B35;">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.petCount }}</div>
            <div class="stat-label">待领养宠物</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8" :md="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(82, 196, 26, 0.1); color: #52C41A;">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.adoptedCount }}</div>
            <div class="stat-label">成功领养</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8" :md="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(24, 144, 255, 0.1); color: #1890FF;">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">注册用户</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <div class="section">
      <div class="section-header">
        <h2>最新待领养宠物</h2>
        <el-button text type="primary" @click="router.push('/pet/list')">查看更多 →</el-button>
      </div>
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="pet in latestPets" :key="pet.id">
          <div class="pet-card" @click="handlePetClick(pet)">
            <div class="pet-image">
              <el-image :src="pet.photos?.[0]" :alt="pet.name" fit="cover">
                <template #error>
                  <div class="image-error"><el-icon :size="40"><Picture /></el-icon></div>
                </template>
              </el-image>
              <el-tag class="pet-status" :type="pet.status === 1 ? 'success' : 'info'" size="small">
                {{ pet.statusDisplay }}
              </el-tag>
            </div>
            <div class="pet-info">
              <h3>{{ pet.name }}</h3>
              <p>{{ pet.breed }} · {{ pet.ageDisplay }} · {{ pet.genderDisplay }}</p>
              <p class="location"><el-icon><Location /></el-icon>{{ pet.location }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-if="latestPets.length === 0 && !loading" description="暂无待领养宠物" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPetList, getPetStats } from '@/api/pet'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const stats = ref({
  petCount: 0,
  adoptedCount: 0,
  userCount: 0
})

const latestPets = ref([])

const handlePetClick = (pet) => {
  router.push(`/pet/detail/${pet.id}`)
}

const fetchStats = async () => {
  try {
    const res = await getPetStats()
    stats.value.petCount = res.data.petCount || 0
    stats.value.adoptedCount = res.data.adoptedCount || 0
    stats.value.userCount = res.data.userCount || 0
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

const fetchLatestPets = async () => {
  loading.value = true
  try {
    const res = await getPetList({ pageNum: 1, pageSize: 4, status: 1 })
    latestPets.value = res.data.records || []
  } catch (e) {
    ElMessage.error(e.message || '获取宠物数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchLatestPets()
})
</script>

<style lang="scss" scoped>
.home {
  width: 100%;
}

.welcome-card {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  border-radius: 16px;
  padding: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  color: #fff;
}

.welcome-content {
  h1 {
    font-size: 28px;
    margin: 0 0 12px;
  }
  
  p {
    font-size: 16px;
    opacity: 0.9;
    margin: 0 0 24px;
  }
  
  .el-button {
    background: #fff;
    color: #FF6B35;
    border: none;
    
    &:hover {
      background: rgba(255, 255, 255, 0.9);
    }
  }
}

.welcome-image {
  font-size: 80px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  cursor: default;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-info {
  .stat-value {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
  }
  
  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }
}

.section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  h2 {
    font-size: 18px;
    color: #303133;
    margin: 0;
  }
}

.pet-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  &:active {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.pet-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: #f5f7fa;
  
  :deep(.el-image) {
    width: 100%;
    height: 100%;
  }
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .image-error {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #c0c4cc;
  }
  
  .pet-status {
    position: absolute;
    top: 12px;
    right: 12px;
  }
}

.pet-info {
  padding: 16px;
  
  h3 {
    font-size: 16px;
    color: #303133;
    margin: 0 0 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  p {
    font-size: 13px;
    color: #909399;
    margin: 0 0 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .location {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #606266;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .welcome-card {
    padding: 24px;
    flex-direction: column;
    text-align: center;
  }

  .welcome-content {
    h1 {
      font-size: 20px;
    }
    p {
      font-size: 14px;
      margin-bottom: 16px;
    }
  }

  .welcome-image {
    font-size: 48px;
    margin-top: 12px;
  }

  .section {
    padding: 16px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .welcome-content h1 {
    font-size: 18px;
  }

  .stat-icon {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }

  .stat-info .stat-value {
    font-size: 22px;
  }
}
</style>
