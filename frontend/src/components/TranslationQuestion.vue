<template>
  <div class="question-card">
    <div class="question-header">
      <el-tag>{{ index + 1 }} / {{ total }}</el-tag>
      <el-tag type="info">翻译题</el-tag>
      <span class="timer">⏱ {{ elapsed }}s</span>
    </div>

    <div class="chinese-text">{{ question.prompt }}</div>

    <el-input
      v-model="answer"
      type="textarea"
      :rows="4"
      placeholder="请输入英文翻译..."
      @keydown.ctrl.enter="submit"
    />

    <div style="margin-top: 16px; text-align: right">
      <el-button type="primary" @click="submit" :loading="submitting" :disabled="!answer.trim()">
        提交（Ctrl+Enter）
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  question: Object,
  index: Number,
  total: Number,
  initialElapsed: { type: Number, default: 0 }
})

const emit = defineEmits(['submit', 'elapsed'])

const answer = ref('')
const submitting = ref(false)
const elapsed = ref(props.initialElapsed)
let timer = null

onMounted(() => {
  timer = setInterval(() => { elapsed.value++; emit('elapsed', elapsed.value) }, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})

async function submit() {
  if (!answer.value.trim()) return
  submitting.value = true
  clearInterval(timer)
  emit('submit', { answer: answer.value, duration: elapsed.value })
}
</script>

<style scoped>
.question-card {
  max-width: 700px;
  margin: 60px auto;
  padding: 30px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  word-break: break-word;
  overflow-wrap: break-word;
}
.question-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 24px;
}
.timer { margin-left: auto; color: #909399; font-size: 14px; }
.chinese-text {
  font-size: 20px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  word-break: break-word;
  overflow-wrap: break-word;
}
</style>
