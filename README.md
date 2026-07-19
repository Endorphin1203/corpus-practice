# 语料练习系统 / Corpus Practice System

AI 驱动的英语写作语料练习工具。导入中英语料，自动生成练习题，AI 精细纠错反馈。

An AI-powered English writing corpus practice tool. Import your Chinese-English corpus, auto-generate exercises, and get detailed AI feedback.

---

## 中文

### 功能

- **语料管理** — Excel 批量导入（自动识别 Sheet 分类）+ 界面增删改查
- **三种题型** — 翻译题（中译英）、语境选择题（AI 生成干扰项）、场景写作题
- **AI 精细纠错** — 语法检查 + 用词分析 + 润色参考译文
- **学习追踪** — 正确率趋势图、分类正确率对比、薄弱语料标记
- **练习模式** — 日常积累（随机抽取）/ 考前突击（优先薄弱项）
- **多 AI 后端** — 支持 DeepSeek、通义千问、智谱等国产大模型，可随时切换
- **中断恢复** — 练习中途离开，再次进入可恢复上次进度

### 技术栈

Spring Boot 3 · Vue 3 · Element Plus · ECharts · MySQL · MyBatis-Plus

### 快速开始

```bash
# 1. 准备 MySQL，创建数据库
mysql -u root -e "CREATE DATABASE IF NOT EXISTS corpus_practice"

# 2. 设置数据库密码
export DB_PASSWORD=你的密码

# 3. 导入语料
cd scripts && python3 import_excel.py

# 4. 构建前端
cd frontend && npm install && npx vite build

# 5. 构建后端
cd backend && mvn clean package -DskipTests -q

# 6. 启动
java -jar backend/target/corpus-practice-0.1.0.jar
# 浏览器打开 http://localhost:8080
```

### 初始配置

1. 进入「系统设置」→ 添加 AI 后端（如 DeepSeek）
2. 进入「语料管理」→ 导入 Excel 或直接使用导入脚本
3. 进入「开始练习」→ 选择分类和题型 → 开始答题

---

## English

### Features

- **Corpus Management** — Batch import from Excel (auto-detect sheet categories) + web CRUD
- **3 Question Types** — Translation (CN→EN), Context Choice (AI-generated distractors), Scene Writing
- **AI Detailed Feedback** — Grammar check + word choice analysis + polished reference version
- **Learning Dashboard** — Accuracy trend chart, category breakdown, weak spot tracking
- **2 Practice Modes** — Daily review (random) / Exam cram (prioritize weak areas)
- **Multi AI Provider** — Supports OpenAI-compatible APIs (DeepSeek, Qwen, Zhipu, etc.), switch anytime
- **Session Resume** — Interrupted during practice? Resume from where you left off

### Tech Stack

Spring Boot 3 · Vue 3 · Element Plus · ECharts · MySQL · MyBatis-Plus

### Quick Start

```bash
# 1. Prepare MySQL
mysql -u root -e "CREATE DATABASE IF NOT EXISTS corpus_practice"

# 2. Set DB password
export DB_PASSWORD=your_password

# 3. Import corpus data
cd scripts && python3 import_excel.py

# 4. Build frontend
cd frontend && npm install && npx vite build

# 5. Build backend
cd backend && mvn clean package -DskipTests -q

# 6. Run
java -jar backend/target/corpus-practice-0.1.0.jar
# Open http://localhost:8080
```

### Initial Setup

1. Go to "Settings" → Add an AI provider (e.g., DeepSeek)
2. Go to "Corpus Management" → Import Excel
3. Go to "Start Practice" → Select categories and question types → Begin

---

## License

MIT
