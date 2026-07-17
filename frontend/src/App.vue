<template>
  <el-container style="min-height: 100vh">
    <!-- 移动端汉堡菜单按钮 -->
    <div v-if="isMobile" class="mobile-header">
      <el-button @click="collapsed = !collapsed" :icon="MenuIcon" circle />
      <span class="mobile-title">语料练习系统</span>
    </div>

    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '0px' : '220px'" class="sidebar" :class="{ collapsed: collapsed }">
      <div class="sidebar-title">语料练习系统</div>
      <el-menu
        :default-active="route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        @select="onMenuSelect"
      >
        <el-menu-item index="/">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/exercise/setup">
          <el-icon><Edit /></el-icon>
          <span>开始练习</span>
        </el-menu-item>
        <el-menu-item index="/corpus">
          <el-icon><Collection /></el-icon>
          <span>语料管理</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon>
          <span>练习历史</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 移动端遮罩 -->
    <div v-if="isMobile && !collapsed" class="overlay" @click="collapsed = true" />

    <el-main>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Menu as MenuIcon } from '@element-plus/icons-vue'

const route = useRoute()
const isMobile = ref(false)
const collapsed = ref(true)

function checkMobile() {
  isMobile.value = window.innerWidth < 768
  collapsed.value = isMobile.value
}

function onMenuSelect() {
  if (isMobile.value) collapsed.value = true
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style>
/* 全局过渡动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 全局空状态 */
body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
</style>

<style scoped>
.sidebar { transition: width 0.2s; overflow: hidden; background: #304156; }
.sidebar.collapsed { width: 0 !important; min-width: 0; }
.sidebar-title { padding: 20px; color: #fff; font-size: 18px; font-weight: bold; text-align: center; white-space: nowrap; }
.mobile-header {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 16px; background: #304156; color: #fff;
}
.mobile-title { font-size: 16px; font-weight: bold; }
.overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 999;
}

@media (min-width: 768px) {
  .mobile-header { display: none; }
}
</style>
