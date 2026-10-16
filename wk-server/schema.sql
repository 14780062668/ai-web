-- wk_item 建表与索引存档（ddl-auto=validate，表结构由本脚本维护）
CREATE TABLE IF NOT EXISTS wk_item (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  title      VARCHAR(100)  NOT NULL,
  content    VARCHAR(2000) NOT NULL,
  category   VARCHAR(32)   NOT NULL,
  -- 扩展字段：优先级/状态/标签(逗号分隔)/负责人/邮箱/进度/备注，均可空
  priority   VARCHAR(8)    NULL,
  status     VARCHAR(8)    NULL,
  tags       VARCHAR(255)  NULL,
  owner      VARCHAR(50)   NULL,
  email      VARCHAR(100)  NULL,
  progress   INT           NULL,
  remark     VARCHAR(500)  NULL,
  -- Hibernate 6+ 对 LocalDateTime 默认映射 datetime(6)，validate 模式下精度必须一致
  created_at DATETIME(6)   NOT NULL,
  updated_at DATETIME(6)   NOT NULL,
  -- 列表查询 = category 等值 + updated_at 倒序，联合索引避免全表扫描与回表排序
  KEY idx_category_updated (category, updated_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 已有表补索引：
-- ALTER TABLE wk_item ADD INDEX idx_category_updated (category, updated_at);

-- 已有表补扩展字段：
-- ALTER TABLE wk_item
--   ADD COLUMN priority VARCHAR(8) NULL,
--   ADD COLUMN status VARCHAR(8) NULL,
--   ADD COLUMN tags VARCHAR(255) NULL,
--   ADD COLUMN owner VARCHAR(50) NULL,
--   ADD COLUMN email VARCHAR(100) NULL,
--   ADD COLUMN progress INT NULL,
--   ADD COLUMN remark VARCHAR(500) NULL;
