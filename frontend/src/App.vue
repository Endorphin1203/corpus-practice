<template>
  <el-container style="min-height: 100vh">
    <el-aside width="220px" style="background: #304156">
      <div style="padding: 20px; color: #fff; font-size: 18px; font-weight: bold; text-align: center">
        语料练习系统
      </div>
      <el-menu
        :default-active="route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
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
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
