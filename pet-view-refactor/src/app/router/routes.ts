import type { RouteRecordRaw } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'welcome',
    component: () => import('@/modules/misc/views/WelcomeView.vue'),
    meta: { public: true },
  },
  {
    path: '/login',
    component: AuthLayout,
    meta: { public: true },
    children: [
      {
        path: '',
        name: 'login',
        component: () => import('@/modules/auth/views/LoginView.vue'),
      },
    ],
  },
  {
    path: '/admin/login',
    component: AuthLayout,
    meta: { public: true },
    children: [
      {
        path: '',
        name: 'admin-login',
        component: () => import('@/modules/auth/views/AdminLoginView.vue'),
      },
    ],
  },
  {
    path: '/register',
    component: AuthLayout,
    meta: { public: true },
    children: [
      {
        path: '',
        name: 'register',
        component: () => import('@/modules/auth/views/RegisterView.vue'),
      },
    ],
  },
  {
    path: '/forgot-password',
    component: AuthLayout,
    meta: { public: true },
    children: [
      {
        path: '',
        name: 'forgot-password',
        component: () => import('@/modules/auth/views/ForgotPasswordView.vue'),
      },
    ],
  },
  {
    path: '/unauthorized',
    component: AuthLayout,
    meta: { public: true },
    children: [
      {
        path: '',
        name: 'unauthorized',
        component: () => import('@/modules/auth/views/UnauthorizedView.vue'),
      },
    ],
  },
  {
    path: '/forbidden',
    component: AuthLayout,
    meta: { public: true },
    children: [
      {
        path: '',
        name: 'forbidden',
        component: () => import('@/modules/auth/views/ForbiddenView.vue'),
      },
    ],
  },
  {
    path: '/user',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'user-articles',
        component: () => import('@/modules/articles/views/ArticlesView.vue'),
      },
      {
        path: 'articles-detail/:id',
        name: 'user-articles-detail',
        component: () => import('@/modules/articles/views/ArticleDetailView.vue'),
      },
      {
        path: 'articles/:id',
        name: 'user-article-detail',
        component: () => import('@/modules/articles/views/ArticleDetailView.vue'),
      },
      {
        path: 'pets',
        name: 'user-pets',
        component: () => import('@/modules/pets/views/MyPetsView.vue'),
      },
      {
        path: 'adoption-pets',
        name: 'user-adoption-pets',
        component: () => import('@/modules/pets/views/AdoptionPetsView.vue'),
      },
      {
        path: 'pets/edit/:id',
        name: 'user-pets-edit',
        component: () => import('@/modules/pets/views/EditPetView.vue'),
      },
      {
        path: 'pet-detail/:id',
        name: 'user-pet-detail',
        component: () => import('@/modules/pets/views/PetDetailView.vue'),
      },
      {
        path: 'adoptions',
        name: 'user-adoptions',
        component: () => import('@/modules/pets/views/AdoptionsView.vue'),
      },
      {
        path: 'fosters',
        name: 'user-fosters',
        component: () => import('@/modules/pets/views/FostersView.vue'),
      },
      {
        path: 'health-alerts',
        name: 'user-health-alerts',
        component: () => import('@/modules/health/views/HealthAlertsView.vue'),
      },
      {
        path: 'events',
        name: 'user-events',
        component: () => import('@/modules/events/views/PetEventsView.vue'),
      },
      {
        path: 'profile',
        name: 'user-profile',
        component: () => import('@/modules/profile/views/ProfileView.vue'),
      },
      {
        path: 'settings',
        name: 'user-settings',
        component: () => import('@/modules/settings/views/SettingsView.vue'),
      },
    ],
  },
  {
    path: '/admin',
    component: AppLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'admin-home',
        component: () => import('@/modules/admin/views/AdminDashboardView.vue'),
      },
      {
        path: 'adoptions',
        name: 'admin-adoptions',
        component: () => import('@/modules/admin/views/AdminAdoptionsView.vue'),
      },
      {
        path: 'fosters',
        name: 'admin-fosters',
        component: () => import('@/modules/admin/views/AdminFostersView.vue'),
      },
      {
        path: 'pets',
        name: 'admin-pets',
        component: () => import('@/modules/admin/views/AdminPetsView.vue'),
      },
      {
        path: 'species',
        name: 'admin-species',
        component: () => import('@/modules/admin/views/AdminSpeciesView.vue'),
      },
      {
        path: 'articles',
        name: 'admin-articles',
        component: () => import('@/modules/admin/views/AdminArticlesView.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    component: AuthLayout,
    meta: { public: true },
    children: [
      {
        path: '',
        name: 'not-found',
        component: () => import('@/modules/misc/views/NotFoundView.vue'),
      },
    ],
  },
]
