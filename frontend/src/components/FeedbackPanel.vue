<template>
  <div class="feedback-panel" v-if="feedback">
    <!-- ========== 选择题目专区 ========== -->
    <template v-if="isChoice">
      <!-- 原题回顾 -->
      <div class="section question-review">
        <h4>📋 题目</h4>
        <div class="question-text">
          <p class="stem">{{ question.prompt }}</p>
          <div v-if="question.stemTranslation" class="translation">📖 {{ question.stemTranslation }}</div>
          <div class="options-review">
            <p v-for="(opt, oi) in question.options" :key="oi"
               :class="['option-review-line', {
                 correct: opt === question.referenceAnswer,
                 wrong: isWrongOption(opt)
               }]">
              {{ ['A','B','C','D'][oi] }}. {{ opt }}
              <el-tag v-if="opt === question.referenceAnswer" type="success" size="small" style="margin-left: 6px">✓ 正确</el-tag>
              <el-tag v-if="isWrongOption(opt)" type="danger" size="small" style="margin-left: 6px">✗ 你的选择</el-tag>
            </p>
          </div>
        </div>
      </div>

      <!-- 语料来源 -->
      <div class="section corpus-source">
        <h4>📚 题目来源语料</h4>
        <div class="corpus-line" v-if="question.corpusCategory || question.corpusSubcategory">
          <el-tag size="small">{{ question.corpusCategory || '' }}</el-tag>
          <el-tag size="small" type="info" style="margin-left: 6px">{{ question.corpusSubcategory || '' }}</el-tag>
        </div>
        <div class="corpus-line" style="margin-top: 8px">
          <span class="label">中文：</span>{{ question.corpusChinese }}
        </div>
        <div class="corpus-line">
          <span class="label">英文：</span>{{ question.corpusEnglish }}
        </div>
      </div>

      <!-- 错题解析（仅答错时显示） -->
      <div v-if="feedback.improvedVersion" class="section analysis-box">
        <h4>💡 错题解析</h4>
        <p>{{ feedback.improvedVersion }}</p>
      </div>
    </template>

    <!-- ========== 翻译题/写作题目专区 ========== -->
    <template v-else>
      <!-- 原题回顾 -->
      <div v-if="question" class="section question-review">
        <h4>📋 原题</h4>
        <div class="question-text">
          <template v-if="question.questionType === 'translation'">
            请将以下中文翻译为英文：<br/>
            <strong>{{ question.prompt }}</strong>
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
    </template>

    <div style="text-align: center; margin-top: 20px">
      <el-button type="primary" @click="$emit('next')">下一题</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ feedback: Object, question: Object })
defineEmits(['next'])

const isChoice = computed(() => props.question?.questionType === 'choice')

// 找出用户选择的选项（从 feedback 的用词建议中取 original，即用户选的文本）
const userChoice = computed(() => {
  if (!isChoice.value) return null
  const issues = props.feedback?.wordChoiceIssues
  return issues?.length > 0 ? issues[0].original : null
})

function isWrongOption(opt) {
  return isChoice.value && opt === userChoice.value && opt !== props.question?.referenceAnswer
}
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
.question-text .stem { font-weight: 600; margin-bottom: 12px; }
.question-text .translation {
  margin: 10px 0; padding: 8px 12px; background: #ecf5ff;
  border-radius: 6px; font-size: 14px; color: #409eff;
}
.options-review { margin-top: 14px; display: flex; flex-direction: column; gap: 6px; }
.option-review-line {
  padding: 8px 12px; border-radius: 6px; margin: 0;
  line-height: 1.7; word-break: break-word; overflow-wrap: break-word;
}
.option-review-line.correct { background: #f0f9eb; border: 1px solid #c2e7b0; }
.option-review-line.wrong { background: #fef0f0; border: 1px solid #fbc4c4; }
.corpus-source {
  background: #fafafa; padding: 16px; border-radius: 8px;
  border: 1px dashed #dcdfe6;
}
.corpus-line { margin: 4px 0; line-height: 1.6; word-break: break-word; overflow-wrap: break-word; }
.corpus-line .label { color: #909399; }
.analysis-box {
  background: #fef0f0; padding: 16px; border-radius: 8px;
  border: 1px solid #fbc4c4;
}
.analysis-box h4 { color: #f56c6c; }
.analysis-box p { line-height: 1.8; margin: 0; color: #303133; }
</style>
