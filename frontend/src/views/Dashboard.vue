<template>
  <div>
    <h2 style="margin-bottom: 24px">学习仪表盘</h2>
    <StatsOverview :data="stats.overview" style="margin-bottom: 20px" />
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="14">
        <AccuracyTrend :data="stats.trend" />
      </el-col>
      <el-col :span="10">
        <CategoryAccuracy :data="stats.byCategory" />
      </el-col>
    </el-row>
    <WeakCorpusTable :data="stats.weakCorpus" />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useStatsStore } from '../stores/stats'
import StatsOverview from '../components/StatsOverview.vue'
import AccuracyTrend from '../components/AccuracyTrend.vue'
import CategoryAccuracy from '../components/CategoryAccuracy.vue'
import WeakCorpusTable from '../components/WeakCorpusTable.vue'

const stats = useStatsStore()
onMounted(() => stats.fetchAll())
</script>
