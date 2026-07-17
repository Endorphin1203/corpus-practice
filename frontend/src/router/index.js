import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
  { path: '/exercise/setup', name: 'ExerciseSetup', component: () => import('../views/ExerciseSetup.vue') },
  { path: '/exercise/session/:id', name: 'ExerciseSession', component: () => import('../views/ExerciseSession.vue') },
  { path: '/exercise/result/:id', name: 'ExerciseResult', component: () => import('../views/ExerciseResult.vue') },
  { path: '/corpus', name: 'CorpusManage', component: () => import('../views/CorpusManage.vue') },
  { path: '/corpus/import', name: 'CorpusImport', component: () => import('../views/CorpusImport.vue') },
  { path: '/history', name: 'History', component: () => import('../views/History.vue') },
  { path: '/settings', name: 'Settings', component: () => import('../views/Settings.vue') },
]

export default createRouter({
  history: createWebHistory(),
  routes
})
