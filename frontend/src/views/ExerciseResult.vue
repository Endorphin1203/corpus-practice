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
          <!-- 选择题：拆分为题干 + 逐行选项 -->
          <template v-if="a.question_type === 'choice'">
            <p class="field"><strong>题干：</strong>{{ choiceStem(a.question_prompt) }}</p>
            <div class="options-list">
              <p v-for="(opt, oi) in choiceOptions(a.question_prompt)" :key="oi"
                 :class="['option-line', { correct: opt.trim() === (a.english || '').trim() }]">
                {{ String.fromCharCode(65 + oi) }}. {{ opt }}
              </p>
            </div>
          </template>
          <!-- 翻译/写作题：直接显示 -->
          <p v-else class="field"><strong>题目：</strong>{{ a.question_prompt }}</p>
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

// 选择题题干（选项前的部分）
function choiceStem(prompt) {
  if (!prompt) return ''
  const lines = prompt.split('\n')
  // 找到第一个以 A. 或 a. 开头的行，之前的是题干
  const idx = lines.findIndex(l => /^[A-Da-d]\.\s/.test(l.trim()))
  if (idx > 0) return lines.slice(0, idx).join('\n')
  if (idx === 0) return ''
  return prompt // 没有找到选项标记，全部当题干
}

// 选择题选项列表
function choiceOptions(prompt) {
  if (!prompt) return []
  const lines = prompt.split('\n')
  return lines
    .filter(l => /^[A-Da-d]\.\s/.test(l.trim()))
    .map(l => l.replace(/^[A-Da-d]\.\s?/, ''))
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
.options-list {
  margin: 8px 0 8px 12px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
}
.option-line {
  margin: 4px 0;
  line-height: 1.6;
}
.option-line.correct {
  color: #67c23a;
  font-weight: bold;
}
</style>
