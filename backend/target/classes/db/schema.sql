CREATE TABLE IF NOT EXISTS corpus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chinese TEXT NOT NULL,
    english TEXT NOT NULL,
    notes VARCHAR(500),
    category VARCHAR(50) NOT NULL COMMENT '大类：描写续写/议论文',
    subcategory VARCHAR(50) NOT NULL COMMENT '小类',
    user_id BIGINT DEFAULT NULL COMMENT '预留多用户',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_subcategory (category, subcategory)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS practice_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mode VARCHAR(20) NOT NULL COMMENT 'daily_review/exam_cram',
    question_type VARCHAR(50) NOT NULL,
    categories_filter VARCHAR(500) COMMENT 'JSON数组',
    total_questions INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    user_id BIGINT DEFAULT NULL,
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    finished_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS practice_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    corpus_id BIGINT NOT NULL,
    question_type VARCHAR(20) NOT NULL COMMENT 'translation/choice/writing',
    question_prompt TEXT NOT NULL,
    user_answer TEXT,
    ai_feedback TEXT COMMENT 'JSON格式',
    is_correct TINYINT DEFAULT 0,
    score INT COMMENT '0-100',
    answer_duration_seconds INT COMMENT '答题耗时秒',
    answered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    INDEX idx_corpus (corpus_id),
    INDEX idx_answered_at (answered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_provider (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL COMMENT 'AES加密存储',
    model_name VARCHAR(100) NOT NULL,
    is_active TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exercise_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    corpus_ids VARCHAR(500) COMMENT 'JSON数组',
    question_type VARCHAR(20) NOT NULL,
    question_data TEXT NOT NULL COMMENT 'JSON格式',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
