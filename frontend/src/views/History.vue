<template>
  <div>
    <h2 style="margin-bottom: 20px">练习历史</h2>
    <el-table :data="sessions" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="模式" width="100">
        <template #default="{ row }">
          <el-tag :type="row.mode === 'daily_review' ? '' : 'warning'">
            {{ row.mode === 'daily_review' ? '日常' : '考前' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="questionType" label="题型" />
      <el-table-column prop="totalQuestions" label="总题数" width="80" />
      <el-table-column prop="correctCount" label="正确" width="80" />
      <el-table-column label="正确率" width="100">
        <template #default="{ row }">
          {{ row.totalQuestions ? Math.round(row.correctCount / row.totalQuestions * 100) + '%' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="startedAt" label="开始时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/exercise/result/${row.id}`)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const sessions = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  const res = await api.getSessions()
  sessions.value = res.data
  loading.value = false
})
</script>
