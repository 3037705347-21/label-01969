import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      // 宠物模块
      {
        path: 'pet/list',
        name: 'PetList',
        component: () => import('@/views/pet/PetList.vue'),
        meta: { title: '宠物列表' }
      },
      {
        path: 'pet/detail/:id',
        name: 'PetDetail',
        component: () => import('@/views/pet/PetDetail.vue'),
        meta: { title: '宠物详情', activeMenu: '/pet/list' }
      },
      {
        path: 'pet/my',
        name: 'MyPets',
        component: () => import('@/views/pet/MyPets.vue'),
        meta: { title: '我的宠物', roles: [2] }
      },
      {
        path: 'pet/manage',
        name: 'PetManage',
        component: () => import('@/views/pet/PetManage.vue'),
        meta: { title: '宠物管理', roles: [1] }
      },
      // 领养模块
      {
        path: 'adoption/my',
        name: 'MyApplications',
        component: () => import('@/views/adoption/MyApplications.vue'),
        meta: { title: '我的申请', roles: [3] }
      },
      {
        path: 'pet/adopted',
        name: 'MyAdoptedPets',
        component: () => import('@/views/pet/MyAdoptedPets.vue'),
        meta: { title: '我领养的宠物', roles: [3] }
      },
      {
        path: 'adoption/received',
        name: 'ReceivedApplications',
        component: () => import('@/views/adoption/ReceivedApplications.vue'),
        meta: { title: '收到的申请', roles: [2] }
      },
      {
        path: 'adoption/review',
        name: 'AdoptionReview',
        component: () => import('@/views/adoption/AdoptionReview.vue'),
        meta: { title: '领养审核', roles: [1] }
      },
      // 跟进模块
      {
        path: 'follow/my',
        name: 'MyFollowUp',
        component: () => import('@/views/follow/MyFollowUp.vue'),
        meta: { title: '我的跟进', roles: [3] }
      },
      {
        path: 'follow/manage',
        name: 'FollowManage',
        component: () => import('@/views/follow/FollowManage.vue'),
        meta: { title: '跟进管理', roles: [1, 2] }
      },
      // 用户模块
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', roles: [1] }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/UserProfile.vue'),
        meta: { title: '个人中心' }
      },
      // 系统模块
      {
        path: 'system/blacklist',
        name: 'Blacklist',
        component: () => import('@/views/system/Blacklist.vue'),
        meta: { title: '黑名单管理', roles: [1] }
      },
      // 知识库
      {
        path: 'knowledge/list',
        name: 'KnowledgeList',
        component: () => import('@/views/knowledge/KnowledgeList.vue'),
        meta: { title: '知识库' }
      },
      {
        path: 'knowledge/detail/:id',
        name: 'KnowledgeDetail',
        component: () => import('@/views/knowledge/KnowledgeDetail.vue'),
        meta: { title: '文章详情', activeMenu: '/knowledge/list' }
      },
      {
        path: 'knowledge/manage',
        name: 'KnowledgeManage',
        component: () => import('@/views/knowledge/KnowledgeManage.vue'),
        meta: { title: '知识库管理', roles: [1] }
      },
      // 消息中心
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/Message.vue'),
        meta: { title: '消息中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth === false) {
    next()
    return
  }
  
  if (!userStore.token) {
    next('/login')
    return
  }
  
  if (to.meta.roles && !to.meta.roles.includes(userStore.userInfo?.roleType)) {
    ElMessage.error('无权访问该页面')
    next(from.path || '/home')
    return
  }
  
  next()
})

export default router
