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

-- 物品奖励表, 保存物品可能的所有bonus组合
-- 这里不添加context信息，因为会出现不同context相同bonus_list,不利于相同装备合并统计
DROP TABLE IF EXISTS item_bonus;
CREATE TABLE IF NOT EXISTS item_bonus (
	item_id INT NOT NULL COMMENT '物品ID',
	bonus_list VARCHAR(20) NOT NULL COMMENT '装备奖励',
	PRIMARY KEY(item_id,bonus_list)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '物品奖励表';

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
DROP TABLE IF EXISTS wow_token;
CREATE TABLE IF NOT EXISTS wow_token (
	updated BIGINT UNSIGNED NOT NULL COMMENT '更新时间',
	buy INT UNSIGNED NOT NULL COMMENT '价格，单位G',
	PRIMARY KEY(updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '时光徽章表';