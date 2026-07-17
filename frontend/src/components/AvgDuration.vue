<template>
  <el-card>
    <template #header>⏱️ 各分类平均耗时（秒）</template>
    <div ref="chartRef" style="height: 350px"></div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ data: Array })
const chartRef = ref(null)
let chart = null

function render() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const categories = props.data.map(d => d.subcategory)
  const values = props.data.map(d => Math.round(d.avg_duration * 10) / 10)

  chart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}: {c} 秒' },
    xAxis: { type: 'category', data: categories, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}s' } },
    series: [{
      data: values, type: 'bar',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#e6a23c' },
          { offset: 1, color: '#f5dab1' }
        ])
      }
    }]
  })
}

onMounted(render)
watch(() => props.data, render, { deep: true })
</script>
