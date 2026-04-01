<template>
  <div class="knowledge-list" v-loading="loading">
    <div class="page-card">
      <h2 class="page-title">知识库</h2>
      
      <div class="category-tabs">
        <el-radio-group v-model="category" @change="fetchList">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button :value="1">养宠常识</el-radio-button>
          <el-radio-button :value="2">领养须知</el-radio-button>
          <el-radio-button :value="3">疾病护理</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="article-list">
        <div class="article-card" v-for="article in articleList" :key="article.id" @click="handleArticleClick(article)">
          <div class="article-header">
            <el-tag size="small" :type="getCategoryType(article.category)">
              {{ getCategoryName(article.category) }}
            </el-tag>
            <span class="view-count"><el-icon><View /></el-icon>{{ article.viewCount }}</span>
          </div>
          <h3>{{ article.title }}</h3>
          <p class="content-preview">{{ getContentPreview(article.content) }}</p>
          <div class="article-footer">
            <span>{{ article.createTime }}</span>
          </div>
        </div>
      </div>
      
      <el-empty v-if="articleList.length === 0 && !loading" description="暂无文章" />
      
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
import { getKnowledgeList } from '@/api/knowledge'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const articleList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const category = ref('')

const getCategoryType = (cat) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[cat] || 'info'
}

const getCategoryName = (cat) => {
  const map = { 1: '养宠常识', 2: '领养须知', 3: '疾病护理' }
  return map[cat] || '其他'
}

const getContentPreview = (content) => {
  if (!content) return ''
  const text = content.replace(/<[^>]+>/g, '')
  return text.length > 100 ? text.substring(0, 100) + '...' : text
}

const handleArticleClick = (article) => {
  router.push(`/knowledge/detail/${article.id}`)
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeList({
      category: category.value || null,
      status: 1,
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

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.category-tabs {
  margin-bottom: 24px;

  :deep(.el-radio-group) {
    flex-wrap: wrap;
    gap: 0;
  }

  :deep(.el-radio-button__inner) {
    border-radius: 0;
    padding: 10px 24px;
    font-size: 14px;
    transition: all 0.3s ease;
  }

  :deep(.el-radio-button:first-child .el-radio-button__inner) {
    border-radius: 8px 0 0 8px;
  }

  :deep(.el-radio-button:last-child .el-radio-button__inner) {
    border-radius: 0 8px 8px 0;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background-color: #FF6B35;
    border-color: #FF6B35;
    box-shadow: -1px 0 0 0 #FF6B35;
    color: #fff;
    font-weight: 600;
  }

  :deep(.el-radio-button__inner:hover) {
    color: #FF6B35;
  }
}

.article-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.article-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
  
  &:active {
    transform: translateY(-1px);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  }
  
  .article-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .view-count {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #909399;
    }
  }
  
  h3 {
    font-size: 16px;
    color: #303133;
    margin: 0 0 8px;
    line-height: 1.4;
  }
  
  .content-preview {
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
    margin: 0 0 12px;
  }
  
  .article-footer {
    font-size: 12px;
    color: #909399;
  }
}
</style>
