<template>
  <div v-if="store.loading" style="text-align: center; padding: 100px">
    <el-icon class="is-loading" :size="40"><Loading /></el-icon>
    <p style="margin-top: 20px; color: #909399">正在生成题目...</p>
  </div>

  <div v-else-if="showFeedback">
    <FeedbackPanel :feedback="feedback" @next="nextQuestion" />
  </div>

  <div v-else-if="current">
    <TranslationQuestion
      v-if="current.questionType === 'translation'"
      :question="current"
      :index="store.currentIndex"
      :total="store.questions.length"
      @submit="handleSubmit"
    />
    <div v-else style="text-align: center; padding: 80px; color: #909399">
      此题型将在后续版本支持
      <br /><br />
      <el-button @click="nextQuestion">跳过</el-button>
    </div>
  </div>

  <div v-else style="text-align: center; padding: 100px">
    <p>没有题目</p>
    <el-button @click="$router.push('/exercise/setup')">返回配置</el-button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useExerciseStore } from '../stores/exercise'
import api from '../api'
import TranslationQuestion from '../components/TranslationQuestion.vue'
import FeedbackPanel from '../components/FeedbackPanel.vue'
import { Loading } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const store = useExerciseStore()
const feedback = ref(null)
const showFeedback = ref(false)

const current = computed(() => store.currentQuestion())

async function handleSubmit({ answer, duration }) {
  const q = current.value
  const res = await api.submitAnswer(store.sessionId, {
    corpusId: q.corpusId,
    questionType: q.questionType,
    userAnswer: answer,
    providerId: null
  })
  feedback.value = res.data
  showFeedback.value = true
}

function nextQuestion() {
  showFeedback.value = false
  feedback.value = null
  store.next()
  if (store.isFinished()) {
    api.finishSession(store.sessionId)
    router.push(`/exercise/result/${store.sessionId}`)
  }
}
</script>
