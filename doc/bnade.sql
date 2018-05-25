USE bnade;

-- 服务器信息表
DROP TABLE IF EXISTS `realm`;
CREATE TABLE IF NOT EXISTS `realm` (
	`id` INT NOT NULL COMMENT '服务器ID',
	`name` VARCHAR(20) NOT NULL COMMENT '服务器名（包括合服）',
	`type` VARCHAR(3) NOT NULL COMMENT '服务器类型PVP，PVE',
	`url` VARCHAR(128) NOT NULL COMMENT '拍卖行文件地址',
	`last_modified` BIGINT NOT NULL DEFAULT 0 COMMENT '文件更新时间',
	`interval` BIGINT NOT NULL DEFAULT 0 COMMENT '数据更新间隔',
	`auction_quantity` INT NOT NULL DEFAULT 0 COMMENT '拍卖数量',
	`owner_quantity` INT NOT NULL DEFAULT 0 COMMENT '卖家数量',
	`item_quantity` INT NOT NULL DEFAULT 0 COMMENT '物品种类数量',
	`active` INT NOT NULL DEFAULT 1 COMMENT '是否启用，1-启用，其它不启用',
	PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '服务器信息表';

-- 拍卖数据表，保存所有服务器的所有拍卖
DROP TABLE IF EXISTS auction;
CREATE TABLE IF NOT EXISTS auction (
	auc INT NOT NULL COMMENT '拍卖ID',
	item_id INT NOT NULL COMMENT '物品ID',
	owner VARCHAR(12) NOT NULL COMMENT '卖家',
	owner_realm VARCHAR(8) NOT NULL COMMENT '卖家所在服务器',
	bid BIGINT NOT NULL COMMENT '竞价',
	buyout BIGINT NOT NULL COMMENT '一口价',
	quantity INT NOT NULL COMMENT '数量',
	time_left VARCHAR(12) NOT NULL COMMENT '剩余时间',
	pet_species_id INT NOT NULL COMMENT '宠物ID',
	pet_level INT NOT NULL COMMENT '宠物等级',
	pet_breed_id INT NOT NULL COMMENT '宠物类型',
	context INT NOT NULL COMMENT '物品出处',
	bonus_list VARCHAR(20) NOT NULL COMMENT '物品奖励',
	realm_id INT NOT NULL COMMENT '服务器ID',
	PRIMARY KEY(auc,realm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '拍卖数据表'
-- 对realm_id分区，每个服务器的数据相对独立，一个服务器一个分区, 由于程序自动检查和创建分区，这里不创建所有服务器分区，主要用来标识这是一个分区表
-- 新增分区 ALTER TABLE auction ADD PARTITION (PARTITION p2 VALUES IN (2));
PARTITION BY LIST(realm_id) (
    PARTITION p1 VALUES IN (1)
);
-- 分区表的索引会为每个分区创建自己的索引
-- 为item_id添加索引用于查询服务器某种物品的所有拍卖
ALTER TABLE auction ADD INDEX(item_id);
-- 用于查询某个卖家的所有拍卖物品
ALTER TABLE auction ADD INDEX(owner);

-- 最低一口价拍卖数据表
DROP TABLE IF EXISTS cheapest_auction;
CREATE TABLE IF NOT EXISTS cheapest_auction (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增ID，便于插入数据',
    auc INT NOT NULL COMMENT '拍卖ID',
    item_id INT NOT NULL COMMENT '物品ID',
    owner VARCHAR(12) NOT NULL COMMENT '卖家',zzz
    owner_realm VARCHAR(8) NOT NULL COMMENT '卖家所在服务器',
    bid BIGINT NOT NULL COMMENT '竞价',
    buyout BIGINT NOT NULL COMMENT '一口价',
    quantity INT NOT NULL COMMENT '数量',
    total_quantity INT NOT NULL COMMENT '总数量',
    time_left VARCHAR(12) NOT NULL COMMENT '剩余时间',
    pet_species_id INT NOT NULL COMMENT '宠物ID',
    pet_level INT NOT NULL COMMENT '宠物等级',
    pet_breed_id INT NOT NULL COMMENT '宠物类型',
    context INT NOT NULL COMMENT '物品出处',
    bonus_list VARCHAR(20) NOT NULL COMMENT '物品奖励',
    realm_id INT NOT NULL COMMENT '服务器ID',
    PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '最低一口价拍卖数据表';
-- 查询物品在所有服务器的最低一口价
ALTER TABLE cheapest_auction ADD INDEX(item_id, pet_species_id);
-- 删除某个服务器所有数据
ALTER TABLE cheapest_auction ADD INDEX(realm_id);

-- 拍卖数据归档job状态表
DROP TABLE IF EXISTS auction_archive_status;
CREATE TABLE IF NOT EXISTS auction_archive_status (
	id INT NOT NULL AUTO_INCREMENT COMMENT 'id',
    realm_id INT NOT NULL COMMENT '服务器ID',
    archive_date VARCHAR(10) NOT NULL COMMENT '归档日期',
    status INT NOT NULL COMMENT '状态 1成功，0失败',
    message VARCHAR(256) NOT NULL COMMENT '状态信息',
	PRIMARY KEY(id),
	KEY (realm_id, archive_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '拍卖数据归档job状态表';

-- 物品信息表
DROP TABLE IF EXISTS item;
CREATE TABLE IF NOT EXISTS item (
	id	INT NOT NULL COMMENT '物品ID',
	name VARCHAR(80) NOT NULL COMMENT '物品名',
	icon VARCHAR(64) NOT NULL COMMENT '图标名',
	item_class INT NOT NULL COMMENT '类型',
	item_sub_class INT NOT NULL COMMENT '子类型',
	inventory_type INT NOT NULL COMMENT '装备位置',
	level INT NOT NULL COMMENT '物品等级',
	hot INT NOT NULL DEFAULT 0 COMMENT '物品热度',
	PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '物品信息表';
-- 物品查询时需要对物品名成做模糊查询
-- MySQL5.7开始支持中文的全文检索
-- 使用ngram插件为name列添加全文索引
-- 分词大小请在MySQL的配置文件中设置，默认为2，ngram_token_size=1
-- 搜索有集中模式，这是使用布尔全文搜索模式， SELECT * FROM item WHERE MATCH (name) AGAINST ('玫瑰' IN BOOLEAN MODE);
ALTER TABLE item ADD FULLTEXT INDEX ngram_idx(name) WITH PARSER ngram;
-- 通过物品名查询物品
ALTER TABLE item ADD INDEX(name);

-- 奖励表
DROP TABLE IF EXISTS bonus;
CREATE TABLE IF NOT EXISTS bonus (
	id INT NOT NULL COMMENT 'id',
	name VARCHAR(8) NOT NULL COMMENT '奖励名',
	comment VARCHAR(80) NOT NULL DEFAULT '' COMMENT '说明',
	PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '奖励表';


-- 物品奖励表, 保存物品可能的所有bonus组合
-- 这里不添加context信息，因为会出现不同context相同bonus_list,不利于相同装备合并统计
DROP TABLE IF EXISTS item_bonus;
CREATE TABLE IF NOT EXISTS item_bonus (
	item_id INT NOT NULL COMMENT '物品ID',
	bonus_list VARCHAR(20) NOT NULL COMMENT '装备奖励',
	PRIMARY KEY(item_id,bonus_list)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '物品奖励表';

-- 物品相关统计表
DROP TABLE IF EXISTS item_statistic;
CREATE TABLE IF NOT EXISTS item_statistic (
    id INT NOT NULL AUTO_INCREMENT COMMENT '自增ID，便于插入数据',
	item_id INT NOT NULL COMMENT '物品ID',
    bonus_list VARCHAR(20) NOT NULL COMMENT '物品奖励',
    pet_species_id INT NOT NULL COMMENT '宠物ID',
    pet_breed_id INT NOT NULL COMMENT '宠物类型',
	market_price BIGINT NOT NULL COMMENT '市场价',
	cheapest_price BIGINT NOT NULL COMMENT '最低价',
	quantity INT NOT NULL COMMENT '物品数量',
	realm_quantity INT NOT NULL COMMENT '服务器数量',
	valid_realm_quantity INT NOT NULL COMMENT '有效的服务器数量',
	valid_time DATETIME NOT NULL COMMENT '有效时间，9999-12-31的记录保存最全最新的信息',
	PRIMARY KEY(id),
	KEY(item_id,pet_species_id,valid_time), -- 获取物品9999-12-31的记录
	KEY(valid_time) -- 对9999-12-31物品的查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '物品相关统计表';
-- ALTER TABLE bnade.item_statistic ADD cheapest_price BIGINT DEFAULT 0 NOT NULL COMMENT '最低价';

-- 物品搜索统计表，保存物品每天被搜索的次数
DROP TABLE IF EXISTS item_search_statistic;
CREATE TABLE IF NOT EXISTS item_search_statistic (
    id INT NOT NULL AUTO_INCREMENT COMMENT '自增ID，便于插入数据',
	item_id INT NOT NULL COMMENT '物品ID',
	search_count INT NOT NULL COMMENT '搜索次数',
	search_date DATE NOT NULL COMMENT '搜索日期',
	PRIMARY KEY(id),
    KEY(search_date, item_id),
    UNIQUE KEY(search_date, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '物品搜索统计表';

-- 宠物信息表
DROP TABLE IF EXISTS pet;
CREATE TABLE IF NOT EXISTS pet (
	id INT NOT NULL COMMENT '宠物ID',
	name VARCHAR(16) NOT NULL COMMENT '宠物名',
	icon VARCHAR(64) NOT NULL COMMENT '宠物图标',
	hot INT NOT NULL DEFAULT 0 COMMENT '宠物热度',
	PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '宠物信息表';

-- 宠物类型以及属性值
DROP TABLE IF EXISTS pet_stats;
CREATE TABLE IF NOT EXISTS pet_stats (
	species_id INT NOT NULL COMMENT '宠物id',
	breed_id INT NOT NULL COMMENT '成长类型',
	pet_quality_id INT NOT NULL DEFAULT 3 COMMENT '品质,默认蓝色品质',
	level INT NOT NULL DEFAULT 25 COMMENT '等级,默认25级',
	health INT NOT NULL COMMENT '生命值',
	power INT NOT NULL COMMENT '攻击力',
	speed INT NOT NULL COMMENT '速度',
	PRIMARY KEY(species_id,breed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '宠物类型以及属性值表';

-- 时光徽章表
DROP TABLE IF EXISTS wowtoken;
CREATE TABLE IF NOT EXISTS wowtoken (
	updated BIGINT UNSIGNED NOT NULL COMMENT '更新时间',
	buy INT UNSIGNED NOT NULL COMMENT '价格，单位G',
	PRIMARY KEY(updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '时光徽章表';

-- 留言表
DROP TABLE IF EXISTS message_board;
CREATE TABLE IF NOT EXISTS message_board (
    id INT NOT NULL AUTO_INCREMENT COMMENT '自增ID，便于插入数据',
	nickname VARCHAR(16) NOT NULL COMMENT '昵称',
	message VARCHAR(128) NOT NULL COMMENT '留言',
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '留言表';

-- ----------------------------用户管理
CREATE TABLE IF NOT EXISTS t_user (
	id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	openId VARCHAR(32) NOT NULL,
	email VARCHAR(20) NOT NULL default '',
	validated INT UNSIGNED default 0,
	nickname VARCHAR(20) NOT NULL,
	token VARCHAR(32) NOT NULL default '',
	expire BIGINT NOT NULL default 0,
	createTime BIGINT UNSIGNED NOT NULL,
	updateTime timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE t_user ADD INDEX(openId);
ALTER TABLE t_user ADD INDEX(token);
-- ALTER TABLE t_user ADD validated INT UNSIGNED default 0;
-- ALTER TABLE t_user ADD token VARCHAR(32) NOT NULL default '';
-- ALTER TABLE t_user ADD expire BIGINT NOT NULL default 0;

-- 封杀的ip
CREATE TABLE IF NOT EXISTS t_user_block_ip (
	ip VARCHAR(16) NOT NULL,
	message VARCHAR(128) NOT NULL default '',
    PRIMARY KEY(ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- ALTER TABLE t_user_block_ip ADD message VARCHAR(128) NOT NULL default '';

CREATE TABLE IF NOT EXISTS t_user_realm (
	userId INT UNSIGNED NOT NULL,
	realmId INT UNSIGNED NOT NULL,
    PRIMARY KEY(userId,realmId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS t_user_character (
	id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	userId INT UNSIGNED NOT NULL,
	realmId INT UNSIGNED NOT NULL,
	name VARCHAR(12) NOT NULL,
    PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE t_user_character ADD INDEX(userId);

CREATE TABLE IF NOT EXISTS t_user_item_notification (
	id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	userId INT UNSIGNED NOT NULL,
	realmId INT UNSIGNED NOT NULL,
	itemId INT UNSIGNED NOT NULL,
	petSpeciesId INT UNSIGNED NOT NULL default 0,
	petBreedId INT UNSIGNED NOT NULL default 0,
	bonusList VARCHAR(20) NOT NULL default '',
	isInverted INT UNSIGNED NOT NULL,	-- 0-低于 其它-高于
	price BIGINT UNSIGNED NOT NULL,
	emailNotification INT UNSIGNED NOT NULL default 1,
    PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- ALTER TABLE t_user_item_notification ADD bonusList VARCHAR(20) NOT NULL default '';
ALTER TABLE t_user_item_notification ADD INDEX(userId,realmId);
ALTER TABLE t_user_item_notification ADD INDEX(realmId);
ALTER TABLE t_user_item_notification ADD UNIQUE INDEX(userId,realmId,itemId,petSpeciesId,petBreedId,bonusList,isInverted);

-- ALTER TABLE t_user_item_notification ADD petSpeciesId INT UNSIGNED NOT NULL default 0;
-- ALTER TABLE t_user_item_notification ADD petBreedId INT UNSIGNED NOT NULL default 0;

CREATE TABLE IF NOT EXISTS t_user_mail_validation (
	userId INT UNSIGNED NOT NULL,
	acode VARCHAR(128) NOT NULL,
	email VARCHAR(20) NOT NULL default '',
	expired BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY(userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- ALTER TABLE t_user_mail_validation ADD email VARCHAR(20) NOT NULL default '';

-- 激活码
CREATE TABLE IF NOT EXISTS t_user_activation (
	activationCode VARCHAR(16) NOT NULL,
    PRIMARY KEY(activationCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- 激活历史
CREATE TABLE IF NOT EXISTS t_user_activation_history (
	activationCode VARCHAR(16) NOT NULL,
	userId INT UNSIGNED NOT NULL,
	dateTime timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(activationCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -----------------------物价信息表，用于tsm app数据的更新
CREATE TABLE IF NOT EXISTS t_tsm_app_data (
	id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	realmId INT UNSIGNED NOT NULL,			-- 服务器id
	itemId INT UNSIGNED NOT NULL,			-- 物品ID
	minBuyout BIGINT UNSIGNED NOT NULL,		-- 最低一口价(近期)
    historical BIGINT UNSIGNED NOT NULL,	-- 历史价格(近期)
    PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE t_tsm_app_data ADD INDEX(realmId);

-- View, 包含tsm需要的所有数据
CREATE VIEW v_tsm_app_data as
select a.realmId,a.itemId,buy,minBuyout,historical,quantity
from t_tsm_app_data a
join t_ah_min_buyout_data b on
a.realmId=b.realmId and a.itemId=b.item
join t_item_market c on
c.itemId=a.itemId

-- 服务器数据的版本信息
CREATE TABLE IF NOT EXISTS t_tsm_realm_data_version (
	realmId INT UNSIGNED NOT NULL,			-- 服务器id
    version VARCHAR(20) NOT NULL,			-- 插件版本
    PRIMARY KEY(realmId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;