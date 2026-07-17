<template>
  <el-row :gutter="20">
    <el-col :span="6" v-for="card in cards" :key="card.label">
      <el-card shadow="hover">
        <div style="text-align: center">
          <div style="color: #909399; font-size: 14px">{{ card.label }}</div>
          <div style="font-size: 28px; font-weight: bold; margin-top: 8px; color: #303133">
            {{ card.value }}
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ data: Object })

const cards = computed(() => [
  { label: '总练习次数', value: props.data.totalSessions || 0 },
  { label: '总答题数', value: props.data.totalQuestions || 0 },
  { label: '正确率', value: (props.data.accuracyRate || 0) + '%' },
  { label: '学习天数', value: props.data.studyDays || 0 },
])
</script>
