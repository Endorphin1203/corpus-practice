<template>
  <div style="max-width: 800px; margin: 40px auto; padding: 0 20px">
    <h2 style="text-align: center">练习完成！</h2>

    <div class="summary">
      <div class="stat-item">
        <div class="stat-value">{{ session.totalQuestions || 0 }}</div>
        <div class="stat-label">总题数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ session.correctCount || 0 }}</div>
        <div class="stat-label">正确数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value highlight">
          {{ session.totalQuestions ? Math.round(session.correctCount / session.totalQuestions * 100) + '%' : '-' }}
        </div>
        <div class="stat-label">正确率</div>
      </div>
    </div>

    <div v-if="answers.length > 0" style="margin-top: 30px">
      <h3>答题详情</h3>
      <div v-for="(a, i) in answers" :key="a.id" class="answer-card">
        <div class="answer-header">
          <el-tag :type="a.question_type === 'translation' ? '' : a.question_type === 'choice' ? 'warning' : 'danger'" size="small">
            第{{ i + 1 }}题 · {{ typeLabel(a.question_type) }}
          </el-tag>
          <el-tag type="info" size="small">{{ a.category }} / {{ a.subcategory }}</el-tag>
          <el-tag :type="a.is_correct ? 'success' : 'danger'" size="small">
            {{ a.is_correct ? '✅ 正确' : '❌ 错误' }}
          </el-tag>
        </div>

        <div class="answer-body">
          <p class="field"><strong>题目：</strong>{{ a.question_prompt }}</p>
          <p class="field"><strong>你的答案：</strong>{{ a.user_answer || '(未作答)' }}</p>
          <p v-if="a.english" class="field"><strong>参考答案：</strong>{{ a.english }}</p>
        </div>
      </div>
    </div>

    <div style="display: flex; gap: 12px; justify-content: center; margin-top: 30px">
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
const answers = ref([])

function typeLabel(type) {
  return { translation: '翻译题', choice: '选择题', writing: '写作题' }[type] || type
}

onMounted(async () => {
  const [sessionRes, answersRes] = await Promise.all([
    api.getSession(route.params.id),
    api.getSessionAnswers(route.params.id)
  ])
  session.value = sessionRes.data
  answers.value = answersRes.data
})
</script>

<style scoped>
.summary {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin: 30px 0;
}
.stat-item { text-align: center; }
.stat-value { font-size: 32px; font-weight: bold; color: #303133; }
.stat-value.highlight { color: #409eff; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.answer-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.answer-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}
.answer-body .field {
  margin: 6px 0;
  line-height: 1.6;
  color: #303133;
}
</style>
