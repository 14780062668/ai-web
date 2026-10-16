-- wk_item 建表与索引存档（ddl-auto=validate，表结构由本脚本维护）
CREATE TABLE IF NOT EXISTS wk_item (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  title      VARCHAR(100)  NOT NULL,
  content    VARCHAR(2000) NOT NULL,
  category   VARCHAR(32)   NOT NULL,
  -- Hibernate 6+ 对 LocalDateTime 默认映射 datetime(6)，validate 模式下精度必须一致
  created_at DATETIME(6)   NOT NULL,
  updated_at DATETIME(6)   NOT NULL,
  -- 列表查询 = category 等值 + updated_at 倒序，联合索引避免全表扫描与回表排序
  KEY idx_category_updated (category, updated_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 已有表补索引：
-- ALTER TABLE wk_item ADD INDEX idx_category_updated (category, updated_at);
