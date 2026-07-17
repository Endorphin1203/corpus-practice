<template>
  <div style="max-width: 600px; margin: 60px auto; text-align: center">
    <h2>练习完成！</h2>
    <div style="margin: 30px 0; font-size: 16px">
      <p>总题数：<strong>{{ session.totalQuestions }}</strong></p>
      <p>正确数：<strong>{{ session.correctCount }}</strong></p>
      <p>正确率：<strong style="font-size: 32px; color: #409eff">
        {{ session.totalQuestions ? Math.round(session.correctCount / session.totalQuestions * 100) + '%' : '-' }}
      </strong></p>
    </div>
    <div style="display: flex; gap: 12px; justify-content: center">
      <el-button type="primary" @click="$router.push('/exercise/setup')">再来一次</el-button>
      <el-button @click="$router.push('/')">返回仪表盘</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'

const route = useRoute()
const session = ref({})

onMounted(async () => {
  const res = await api.getSession(route.params.id)
  session.value = res.data
})
</script>
