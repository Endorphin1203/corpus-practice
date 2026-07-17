<template>
  <div>
    <div class="dashboard-header">
      <h2>学习仪表盘</h2>
      <el-button v-if="hasData" @click="exportCsv" :loading="exporting">
        <el-icon><Download /></el-icon> 导出 CSV
      </el-button>
    </div>

    <div v-if="!hasData" class="empty-state">
      <el-result icon="info" title="还没有练习记录" sub-title="开始你的第一次练习吧！">
        <template #extra>
          <el-button type="primary" @click="$router.push('/exercise/setup')">开始练习</el-button>
          <el-button @click="$router.push('/corpus/import')">导入语料</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <StatsOverview :data="stats.overview" style="margin-bottom: 20px" />
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :xs="24" :lg="14">
          <AccuracyTrend :data="stats.trend" />
        </el-col>
        <el-col :xs="24" :lg="10" style="margin-top: 20px">
          <CategoryAccuracy :data="stats.byCategory" />
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :xs="24" :lg="14">
          <AvgDuration :data="stats.avgDuration" />
        </el-col>
        <el-col :xs="24" :lg="10">
          <WeakCorpusTable :data="stats.weakCorpus" />
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
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
const hasData = computed(() => (stats.overview.totalQuestions || 0) > 0)

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

<style scoped>
.dashboard-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
}
.dashboard-header h2 { margin: 0; }
.empty-state { padding: 60px 0; }
@media (max-width: 768px) {
  .dashboard-header { flex-direction: column; gap: 12px; align-items: flex-start; }
}
</style>
