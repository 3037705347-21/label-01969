<template>
  <div class="layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
        <div class="logo" @click="router.push('/home')">
          <span class="logo-icon">🐾</span>
          <span v-show="!isCollapse" class="logo-text">宠物领养</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-menu-item index="/pet/list">
            <el-icon><Search /></el-icon>
            <span>浏览宠物</span>
          </el-menu-item>

          <!-- 领养人菜单 -->
          <template v-if="userStore.isAdopter">
            <el-menu-item index="/adoption/my">
              <el-icon><Document /></el-icon>
              <span>我的申请</span>
            </el-menu-item>
            <el-menu-item index="/pet/adopted">
              <el-icon><List /></el-icon>
              <span>我领养的宠物</span>
            </el-menu-item>
            <el-menu-item index="/follow/my">
              <el-icon><Calendar /></el-icon>
              <span>我的跟进</span>
            </el-menu-item>
          </template>

          <!-- 救助方菜单 -->
          <template v-if="userStore.isRescuer">
            <el-menu-item index="/pet/my">
              <el-icon><List /></el-icon>
              <span>我的宠物</span>
            </el-menu-item>
            <el-menu-item index="/adoption/received">
              <el-icon><Tickets /></el-icon>
              <span>收到的申请</span>
            </el-menu-item>
            <el-menu-item index="/follow/manage">
              <el-icon><View /></el-icon>
              <span>跟进管理</span>
            </el-menu-item>
          </template>

          <!-- 管理员菜单 -->
          <template v-if="userStore.isAdmin">
            <el-sub-menu index="admin">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/user/list">用户管理</el-menu-item>
              <el-menu-item index="/pet/manage">宠物管理</el-menu-item>
              <el-menu-item index="/adoption/review">领养审核</el-menu-item>
              <el-menu-item index="/follow/manage">跟进管理</el-menu-item>
              <el-menu-item index="/system/blacklist">黑名单</el-menu-item>
              <el-menu-item index="/knowledge/manage">知识库管理</el-menu-item>
            </el-sub-menu>
          </template>

          <el-menu-item index="/knowledge/list">
            <el-icon><Reading /></el-icon>
            <span>知识库</span>
          </el-menu-item>

          <el-menu-item index="/message">
            <el-icon><Bell /></el-icon>
            <span>消息中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="header-right">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
              <el-icon class="header-icon" @click="router.push('/message')"><Bell /></el-icon>
            </el-badge>

            <el-dropdown @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getUnreadCount } from '@/api/message'
import { eventBus } from '@/utils/eventBus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(window.innerWidth < 768)
const unreadCount = ref(0)

const handleResize = () => {
  isCollapse.value = window.innerWidth < 768
}

const activeMenu = computed(() => {
  return route.meta.activeMenu || route.path
})

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) {}
}

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'logout') {
    userStore.logout()
  }
}

onMounted(() => {
  fetchUnreadCount()
  setInterval(fetchUnreadCount, 60000)
  eventBus.on('message-read', fetchUnreadCount)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  eventBus.off('message-read', fetchUnreadCount)
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.layout {
  height: 100vh;
}

.sidebar {
  background: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  transition: width 0.3s;
  overflow: hidden;
  height: 100vh;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(#FF6B35, 0.05);
  }

  &:active {
    background: rgba(#FF6B35, 0.1);
  }

  .logo-icon {
    font-size: 28px;
  }

  .logo-text {
    font-size: 18px;
    font-weight: 600;
    color: #FF6B35;
    margin-left: 8px;
  }
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 61px);

  :deep(.el-menu-item) {
    transition: all 0.2s ease;

    &:hover {
      background-color: rgba(#FF6B35, 0.05);
    }

    &.is-active {
      background-color: rgba(#FF6B35, 0.1);
      color: #FF6B35;
    }
  }

  :deep(.el-sub-menu__title) {
    transition: all 0.2s ease;

    &:hover {
      background-color: rgba(#FF6B35, 0.05);
    }
  }

  :deep(.el-sub-menu .el-menu-item) {
    padding-left: 50px !important;
  }
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;

  .collapse-btn {
    font-size: 20px;
    cursor: pointer;
    color: #606266;
    padding: 8px;
    border-radius: 6px;
    transition: all 0.2s ease;

    &:hover {
      color: #FF6B35;
      background: rgba(#FF6B35, 0.1);
    }

    &:active {
      transform: scale(0.95);
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;

  .message-badge {
    display: inline-flex;
    align-items: center;
    line-height: normal;

    :deep(.el-badge__content) {
      z-index: 1;
    }
  }

  .header-icon {
    font-size: 20px;
    width: 36px;
    height: 36px;
    cursor: pointer;
    color: #606266;
    border-radius: 6px;
    transition: all 0.2s ease;
    display: inline-flex !important;
    align-items: center;
    justify-content: center;

    &:hover {
      color: #FF6B35;
      background: rgba(#FF6B35, 0.1);
    }

    &:active {
      transform: scale(0.95);
    }
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(#FF6B35, 0.05);
  }

  .username {
    color: #303133;
    font-size: 14px;
  }
}

.main {
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}

// 响应式适配
@media (max-width: 768px) {
  .header {
    padding: 0 12px;
  }

  .header-right {
    gap: 12px;

    .username {
      display: none;
    }
  }

  .main {
    padding: 12px;
  }
}
</style>
