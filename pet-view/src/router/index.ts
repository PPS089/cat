import { createRouter, createWebHashHistory } from "vue-router"

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
router.beforeEach((to, _from, next) => {
  const userid = localStorage.getItem('userId')
  
  // 检查路由是否需要认证
  // const needsAuth = to.path.startsWith('/user')
  const auth=["/","/login","/register","/exception"]

  
  if (!auth.includes(to.path) && !userid) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  
  next()
})

export default router
