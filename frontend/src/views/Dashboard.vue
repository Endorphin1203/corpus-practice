<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px">
      <h2 style="margin: 0">学习仪表盘</h2>
      <el-button @click="exportCsv" :loading="exporting">
        <el-icon><Download /></el-icon> 导出 CSV
      </el-button>
    </div>
    <StatsOverview :data="stats.overview" style="margin-bottom: 20px" />
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="14">
        <AccuracyTrend :data="stats.trend" />
      </el-col>
      <el-col :span="10">
        <CategoryAccuracy :data="stats.byCategory" />
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="14">
        <AvgDuration :data="stats.avgDuration" />
      </el-col>
      <el-col :span="10">
        <WeakCorpusTable :data="stats.weakCorpus" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStatsStore } from '../stores/stats'
import api from '../api'
import StatsOverview from '../components/StatsOverview.vue'
import AccuracyTrend from '../components/AccuracyTrend.vue'
import CategoryAccuracy from '../components/CategoryAccuracy.vue'
import AvgDuration from '../components/AvgDuration.vue'
import WeakCorpusTable from '../components/WeakCorpusTable.vue'
import { Download } from '@element-plus/icons-vue'

const stats = useStatsStore()
const exporting = ref(false)

onMounted(() => stats.fetchAll())

async function exportCsv() {
  exporting.value = true
  try {
    const res = await api.exportCsv()
    const url = URL.createObjectURL(new Blob([res.data], { type: 'text/csv;charset=utf-8' }))
    const link = document.createElement('a')
    link.href = url
    link.download = '练习记录.csv'
    link.click()
    URL.revokeObjectURL(url)
  } finally {
    exporting.value = false
  }
}
</script>
