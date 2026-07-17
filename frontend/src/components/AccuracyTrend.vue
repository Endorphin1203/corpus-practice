<template>
  <el-card>
    <template #header>📈 正确率趋势（近30天）</template>
    <div ref="chartRef" style="height: 300px"></div>
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

  const dates = props.data.map(d => d.date.substring(5))
  const values = props.data.map(d => d.accuracy)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      data: values, type: 'line', smooth: true,
      lineStyle: { color: '#409EFF' },
      areaStyle: { color: 'rgba(64,158,255,0.1)' }
    }]
  })
}

onMounted(render)
watch(() => props.data, render, { deep: true })
</script>
