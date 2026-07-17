<template>
  <div style="max-width: 600px; margin: 0 auto; padding: 40px 20px">
    <h2 style="margin-bottom: 30px">导入 Excel 语料</h2>

    <el-upload
      ref="uploadRef"
      drag
      :auto-upload="false"
      :limit="1"
      accept=".xlsx"
      :on-change="handleFileChange"
    >
      <el-icon class="el-icon--upload" :size="60"><UploadFilled /></el-icon>
      <div style="margin-top: 16px">将 .xlsx 文件拖到此处或点击选择</div>
      <template #tip>
        <div style="margin-top: 12px; color: #909399">
          支持描写续写和议论文 Excel，Sheet 名称自动映射分类
        </div>
      </template>
    </el-upload>

    <div v-if="file" style="margin-top: 20px; text-align: center">
      <p>已选择: {{ file.name }}</p>
      <el-button type="primary" size="large" @click="doImport" :loading="importing">开始导入</el-button>
    </div>

    <el-alert v-if="result" :title="`导入完成：新增 ${result.newCount} 条，跳过 ${result.skipCount} 条重复`"
      :type="result.errors.length > 0 ? 'warning' : 'success'" style="margin-top: 20px" />

    <div v-if="result && result.errors.length > 0" style="margin-top: 16px">
      <h4>异常记录：</h4>
      <ul>
        <li v-for="(err, i) in result.errors" :key="i" style="color: #e6a23c">{{ err }}</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import api from '../api'

const file = ref(null)
const importing = ref(false)
const result = ref(null)

function handleFileChange(uploadFile) { file.value = uploadFile.raw }

async function doImport() {
  importing.value = true
  const formData = new FormData()
  formData.append('file', file.value)
  const res = await api.importCorpus(formData)
  result.value = res.data
  importing.value = false
}
</script>
