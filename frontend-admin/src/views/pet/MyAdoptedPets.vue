<template>
  <div class="my-adopted-pets">
    <div class="page-card">
      <h2 class="page-title">我领养的宠物</h2>

      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="pet in petList" :key="pet.id">
          <div class="pet-card" @click="router.push(`/pet/detail/${pet.id}`)">
            <div class="pet-image">
              <el-image :src="pet.photos?.[0]" :alt="pet.name" fit="cover">
                <template #error>
                  <div class="image-error"><el-icon :size="40"><Picture /></el-icon></div>
                </template>
              </el-image>
            </div>
            <div class="pet-info">
              <h3>{{ pet.name }}</h3>
              <p>{{ pet.breed }} · {{ pet.ageDisplay }} · {{ pet.genderDisplay }}</p>
              <p class="location"><el-icon><Location /></el-icon>{{ pet.location }}</p>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-if="petList.length === 0 && !loading" description="暂无已领养的宠物" />

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
import { useRouter } from 'vue-router'
import { getMyAdoptedPets } from '@/api/pet'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const petList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyAdoptedPets({ pageNum: pageNum.value, pageSize: pageSize.value })
    petList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取宠物列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
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
}

.pet-info {
  padding: 16px;

  h3 {
    font-size: 16px;
    color: #303133;
    margin: 0 0 8px;
  }

  p {
    font-size: 13px;
    color: #909399;
    margin: 0 0 4px;
  }

  .location {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #606266;
  }
}
</style>
