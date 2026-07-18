<template>
  <div v-if="store.loading" style="text-align: center; padding: 100px">
    <el-icon class="is-loading" :size="40"><Loading /></el-icon>
    <p style="margin-top: 20px; color: #909399">正在生成题目...</p>
  </div>

  <div v-else-if="showFeedback">
    <FeedbackPanel :feedback="feedback" :question="current" @next="nextQuestion" />
  </div>

  <div v-else-if="current">
    <TranslationQuestion
      v-if="current.questionType === 'translation'"
      :question="current"
      :index="store.currentIndex"
      :total="store.questions.length"
      @submit="handleSubmit"
    />
    <ChoiceQuestion
      v-else-if="current.questionType === 'choice'"
      :question="current"
      :index="store.currentIndex"
      :total="store.questions.length"
      @submit="handleSubmit"
    />
    <WritingQuestion
      v-else-if="current.questionType === 'writing'"
      :question="current"
      :index="store.currentIndex"
      :total="store.questions.length"
      @submit="handleSubmit"
    />
    <div v-else style="text-align: center; padding: 80px; color: #909399">
      未知题型
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
import ChoiceQuestion from '../components/ChoiceQuestion.vue'
import WritingQuestion from '../components/WritingQuestion.vue'
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
  // 构建实际题目内容：选择题为题干，翻译题为中文原文，写作为场景描述
  let questionPrompt = q.prompt || ''
  if (q.questionType === 'writing') questionPrompt = q.sceneDescription || ''
  if (q.questionType === 'choice' && q.options) {
    questionPrompt = q.prompt + '\n' + q.options.map((o, i) => String.fromCharCode(65 + i) + '. ' + o).join('\n')
  }

  const res = await api.submitAnswer(store.sessionId, {
    corpusId: q.corpusId,
    questionType: q.questionType,
    questionPrompt,
    userAnswer: answer,
    providerId: null
  })
  feedback.value = res.data
  showFeedback.value = true
}

async function nextQuestion() {
  showFeedback.value = false
  feedback.value = null
  store.next()
  if (store.isFinished()) {
    await api.finishSession(store.sessionId)
    router.push(`/exercise/result/${store.sessionId}`)
  }
}
</script>
