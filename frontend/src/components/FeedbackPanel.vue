<template>
  <div class="feedback-panel" v-if="feedback">
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
        <p><el-tag type="warning">{{ issue.original }}</el-tag> → <el-tag type="success">{{ issue.alternative }}</el-tag></p>
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
defineProps({ feedback: Object })
defineEmits(['next'])
</script>

<style scoped>
.feedback-panel { max-width: 700px; margin: 30px auto; }
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
.section { margin-bottom: 20px; }
.section h4 { margin-bottom: 12px; color: #303133; }
.error-item {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 8px;
}
.location { color: #409eff; font-weight: bold; }
.suggestion { color: #67c23a; margin-top: 4px; }
.improved {
  background: #f0f9eb;
  padding: 16px;
  border-radius: 6px;
  line-height: 1.8;
}
</style>
