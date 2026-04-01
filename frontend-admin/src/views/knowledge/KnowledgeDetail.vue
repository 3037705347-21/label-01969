<template>
  <div class="knowledge-detail" v-loading="loading">
    <div class="page-card" v-if="article">
      <div class="article-header">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <el-tag :type="getCategoryType(article.category)">
          {{ getCategoryName(article.category) }}
        </el-tag>
      </div>
      
      <h1 class="article-title">{{ article.title }}</h1>
      
      <div class="article-meta">
        <span><el-icon><View /></el-icon>{{ article.viewCount }} 次阅读</span>
        <span><el-icon><Clock /></el-icon>{{ article.createTime }}</span>
      </div>
      
      <div class="article-content" v-html="article.content"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getKnowledgeDetail } from '@/api/knowledge'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const article = ref(null)

const getCategoryType = (cat) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[cat] || 'info'
}

const getCategoryName = (cat) => {
  const map = { 1: '养宠常识', 2: '领养须知', 3: '疾病护理' }
  return map[cat] || '其他'
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeDetail(route.params.id)
    article.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style lang="scss" scoped>
.knowledge-detail {
  width: 100%;
}

.article-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.article-title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 16px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
  
  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.article-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  
  :deep(p) {
    margin-bottom: 16px;
  }
  
  :deep(h2), :deep(h3) {
    margin: 24px 0 12px;
  }
  
  :deep(ul), :deep(ol) {
    margin: 12px 0;
    padding-left: 24px;
  }
}
</style>
