import { createRouter, createWebHashHistory } from "vue-router"
import { useUserStore } from '../stores/user'

const routes = [
    {
        path:"/",
        name:"welcome",
        component: () => import('../views/Welcome.vue')
    },
    {
        path:"/login",
        name:"login",
        component: () => import('../views/Login.vue')
    },
    {
        path:"/register",
        name:"register",
        component: () => import('../views/Register.vue')
    },
    {
        path:"/user",
        name:"user",
        component: () => import('../views/Main.vue'),
        children: [
            // 首页 - 文章列表
            {
                path: "",
                name: "user-articles",
                component: () => import('../views/Articles.vue'),
                meta: { roles: ['user'] }
            },
            {
                path: "articles-detail/:id",
                name: "user-articles-detail",
                component: () => import('../views/Articledetail.vue'),
                meta: { roles: ['user'] }
            },
            {
                path: "articles/:id",
                name: "user-article-detail",
                component: () => import('../views/Articledetail.vue'),
                meta: { roles: ['user'] }
            },
            // 宠物管理
            {
                path: "pets",
                name: "user-pets",
                component: () => import('../views/Mypets.vue'),
                meta: { roles: ['user'] }
            },
            {
                path:'adoption-pets',
                name:'user-adoption-pets',
                component: () => import('../views/AdoptionsPets.vue'),
                meta: { roles: ['user'] }
            },
            {
                path: "pets/edit/:id",
                name: "user-pets-edit",
                component: () => import('../views/Editpet.vue'),
                meta: { roles: ['user'] }
            },
            {
                path: "pet-detail/:id",
                name: "user-pet-detail",
                component: () => import('../views/PetDetail.vue'),
                meta: { roles: ['user'] }
            },
            // 领养和寄养记录
            {
                path: "adoptions",
                name: "user-adoptions",
                component: () => import('../views/Adoptions.vue'),
                meta: { roles: ['user'] }
            },
            {
                path: "fosters",
                name: "user-fosters",
                component: () => import('../views/Fosters.vue'),
                meta: { roles: ['user'] }
            },
            // 健康中心
            {
                path: "health-alerts",
                name: "user-health-alerts",
                component: () => import('../views/Health.vue'),
                meta: { roles: ['user'] }
            },
            {
                path: "events",
                name: "user-events",
                component: () => import('../views/Records.vue'),
                meta: { roles: ['user'] }
            },
            // 用户中心
            {
                path: "profile",
                name: "user-profile",
                component: () => import('../views/Profile.vue'),
                meta: { roles: ['user'] }
            },
            {
                path: "settings",
                name: "user-settings",
                component: () => import('../views/Setting.vue'),
                meta: { roles: ['user'] }
            }
        ]
    },
    {
        path:"/exception",
        name:"exception",
        component: () => import('../views/Exception.vue')
    },
]
 
const router = createRouter({
    history: createWebHashHistory(),
    routes
})

// 全局路由守卫
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  const userid:string | null = localStorage.getItem('userId')
  console.log('路由守卫检查 - 路径:', to.path, '本地用户ID:', userid, 'Store用户ID:', userStore.info.userId)

  // 定义需要登录的页面路径 - 使用更精确的匹配
  const requireAuthPaths = [
    '/user', '/user/', '/user/articles', '/user/adoption-pets', '/user/pets/edit/', '/user/health-alerts', 
    '/user/adoptions', '/user/fosters', '/user/profile', 
    '/user/settings', '/user/events'
  ]

  // 检查是否需要登录
  const needsAuth = requireAuthPaths.some(path => 
    to.path === path || 
    to.path.startsWith(path) || 
    to.path.startsWith('/user/pet-detail/') ||
    to.path.startsWith('/user/articles/')
  )

  if (needsAuth) {
    // 首先检查本地存储中是否有用户ID，这是最基本的认证状态
    if (!userid) {
      console.log('本地存储中没有用户ID，重定向到登录页，原始路径:', to.path);
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }
    
    // 如果store中的用户信息不完整（userId为0），尝试从API获取
    if (!userStore.info.userId || userStore.info.userId === 0) {
      try {
        console.log('Store中用户信息不完整，尝试从API获取用户资料...');
        await userStore.fetchProfile()
        
        // 获取资料后再次检查用户ID
        console.log('API获取资料后 - Store用户ID:', userStore.info.userId, '本地用户ID:', userid);
        
        if (!userStore.info.userId || userStore.info.userId === 0) {
          console.log('API获取用户资料后userId仍为0，认证失败，重定向到登录页');
          return next({ path: '/login', query: { redirect: to.fullPath } })
        }
        
        // 验证API返回的userId与本地存储的userId是否一致
        if (userStore.info.userId && userStore.info.userId !== 0) {
          if (String(userStore.info.userId) !== userid) {
            console.warn(`userId不一致警告: API返回 ${userStore.info.userId} vs 本地存储 ${userid}，使用API返回的userId`);
            // 更新本地存储的userId
            localStorage.setItem('userId', String(userStore.info.userId));
          } else {
            console.log(`userId一致性检查通过: API返回 ${userStore.info.userId} vs 本地存储 ${userid}`);
          }
        } else {
          console.log('API返回的userId无效:', userStore.info.userId);
        }
      } catch (error) {
        console.error('获取用户资料失败:', error);
        return next({ path: '/login', query: { redirect: to.fullPath } })
      }
      
      // 再次检查userId - 确保获取到了有效的用户信息
      if (!userStore.info.userId || userStore.info.userId === 0) {
        console.log('用户资料获取失败或userId无效，重定向到登录页');
        return next({ path: '/login', query: { redirect: to.fullPath } })
      }
      
      console.log('用户认证成功，允许访问:', to.path);
    }
  }
  
  next()
})

export default router
