<template>
  <el-card>
    <template #header>📊 各分类正确率</template>
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
  const values = props.data.map(d => d.accuracy)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: categories, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      data: values, type: 'bar',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#79bbff' }
        ])
      }
    }]
  })
}

onMounted(render)
watch(() => props.data, render, { deep: true })
</script>
