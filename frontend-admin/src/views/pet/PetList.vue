<template>
  <div class="pet-list" v-loading="loading">
    <div class="page-card">
      <h2 class="page-title">浏览宠物</h2>
      
      <div class="search-form">
        <el-select v-model="query.species" placeholder="物种" clearable style="width: 120px">
          <el-option label="猫" value="猫" />
          <el-option label="狗" value="狗" />
          <el-option label="其他" value="其他" />
        </el-select>
        <el-input v-model="query.breed" placeholder="品种" clearable style="width: 150px" />
        <el-select v-model="query.gender" placeholder="性别" clearable style="width: 100px">
          <el-option label="公" :value="1" />
          <el-option label="母" :value="2" />
        </el-select>
        <el-input v-model="query.location" placeholder="地区" clearable style="width: 150px" />
        <el-button type="primary" :loading="loading" @click="fetchPetList">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
      
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="pet in petList" :key="pet.id">
          <div class="pet-card" @click="handleCardClick(pet)">
            <div class="pet-image">
              <el-image :src="pet.photos?.[0]" :alt="pet.name" fit="cover">
                <template #error>
                  <div class="image-error"><el-icon :size="40"><Picture /></el-icon></div>
                </template>
              </el-image>
              <el-tag class="pet-status" :type="getStatusType(pet.status)" size="small">
                {{ pet.statusDisplay }}
              </el-tag>
            </div>
            <div class="pet-info">
              <h3>{{ pet.name }}</h3>
              <p>{{ pet.breed }} · {{ pet.ageDisplay }} · {{ pet.genderDisplay }}</p>
              <div class="tags">
                <el-tag v-for="tag in (pet.personalityTags || []).slice(0, 2)" :key="tag" size="small" type="info">
                  {{ tag }}
                </el-tag>
              </div>
              <p class="location"><el-icon><Location /></el-icon>{{ pet.location }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
      
      <el-empty v-if="petList.length === 0 && !loading" description="暂无符合条件的宠物" />
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[8, 12, 16, 24]"
          layout="total, sizes, prev, pager, next"
          @change="fetchPetList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPetList } from '@/api/pet'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const petList = ref([])
const total = ref(0)

const query = reactive({
  species: '',
  breed: '',
  gender: null,
  location: '',
  status: 1,
  pageNum: 1,
  pageSize: 8
})

const getStatusType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

const handleCardClick = (pet) => {
  router.push(`/pet/detail/${pet.id}`)
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

const resetQuery = () => {
  query.species = ''
  query.breed = ''
  query.gender = null
  query.location = ''
  query.pageNum = 1
  fetchPetList()
}

onMounted(() => {
  fetchPetList()
})
</script>

<style lang="scss" scoped>
.pet-list {
  width: 100%;
}

.pet-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
  margin-bottom: 24px;
  
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
  height: 200px;
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
    margin: 0 0 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .tags {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
    min-height: 24px;
  }
  
  .location {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #606266;
    margin: 0;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: stretch;

    .el-select,
    .el-input {
      width: 100% !important;
    }
  }

  .page-title {
    font-size: 18px;
  }

  .pagination-wrapper {
    :deep(.el-pagination) {
      flex-wrap: wrap;
      justify-content: center;
    }
  }
}
</style>
