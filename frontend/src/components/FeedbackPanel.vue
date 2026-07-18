<template>
  <div class="feedback-panel" v-if="feedback">
    <!-- 原题回顾 -->
    <div v-if="question" class="section question-review">
      <h4>📋 原题</h4>
      <div class="question-text">
        <template v-if="question.questionType === 'translation'">
          请将以下中文翻译为英文：<br/>
          <strong>{{ question.prompt }}</strong>
        </template>
        <template v-else-if="question.questionType === 'choice'">
          {{ question.prompt }}
        </template>
        <template v-else-if="question.questionType === 'writing'">
          {{ question.sceneDescription }}
        </template>
      </div>
    </div>

    <!-- 整体判断 -->
    <div :class="['verdict-bar', feedback.verdict]">
      <span v-if="feedback.verdict === 'correct'">✅ 回答正确</span>
      <span v-else-if="feedback.verdict === 'partial'">⚠️ 大意正确，有小问题</span>
      <span v-else>❌ 回答有误</span>
    </div>

    <!-- 语法错误 -->
    <div v-if="feedback.grammarErrors && feedback.grammarErrors.length > 0" class="section">
      <h4>📝 语法错误</h4>
      <div v-for="(err, i) in feedback.grammarErrors" :key="i" class="error-item">
        <span class="location">{{ err.location }}</span>
        <p>{{ err.error }}</p>
        <p class="suggestion">💡 {{ err.suggestion }}</p>
      </div>
    </div>

    <!-- 用词不当 -->
    <div v-if="feedback.wordChoiceIssues && feedback.wordChoiceIssues.length > 0" class="section">
      <h4>📖 用词建议</h4>
      <div v-for="(issue, i) in feedback.wordChoiceIssues" :key="i" class="error-item">
        <div class="word-compare">
          <span class="word-badge wrong">{{ issue.original }}</span>
          <span class="arrow">→</span>
          <span class="word-badge correct">{{ issue.alternative }}</span>
        </div>
        <p>{{ issue.issue }}</p>
      </div>
    </div>

    <!-- 改进版本 -->
    <div v-if="feedback.improvedVersion" class="section">
      <el-collapse>
        <el-collapse-item title="✨ 参考译文（点击展开）">
          <div class="improved">{{ feedback.improvedVersion }}</div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <div style="text-align: center; margin-top: 20px">
      <el-button type="primary" @click="$emit('next')">下一题</el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({ feedback: Object, question: Object })
defineEmits(['next'])
</script>

<style scoped>
.feedback-panel { max-width: 700px; margin: 30px auto; word-break: break-word; overflow-wrap: break-word; }
.verdict-bar {
  padding: 16px 20px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
}
.verdict-bar.correct { background: #f0f9eb; color: #67c23a; border: 1px solid #c2e7b0; }
.verdict-bar.partial { background: #fdf6ec; color: #e6a23c; border: 1px solid #f5dab1; }
.verdict-bar.incorrect { background: #fef0f0; color: #f56c6c; border: 1px solid #fbc4c4; }
.section { margin-bottom: 20px; overflow: hidden; }
.section h4 { margin-bottom: 12px; color: #303133; }
.error-item {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 8px;
  word-break: break-word;
  overflow-wrap: break-word;
}
.location { color: #409eff; font-weight: bold; }
.suggestion { color: #67c23a; margin-top: 4px; }
.improved {
  background: #f0f9eb;
  padding: 16px;
  border-radius: 6px;
  line-height: 1.8;
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}
.question-review {
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
  overflow: hidden;
}
.question-text {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}
.word-compare {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.word-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
}
.word-badge.wrong {
  background: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #f5dab1;
}
.word-badge.correct {
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}
.arrow {
  flex-shrink: 0;
  color: #909399;
  font-weight: bold;
  line-height: 1.6;
}
</style>
