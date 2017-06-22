CREATE DATABASE bnade;

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
	owner VARCHAR(12) NOT NULL COMMENT '玩家',
	owner_realm VARCHAR(8) NOT NULL COMMENT '玩家所在服务器',
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
-- 对realm_id分区，每个服务器的数据相对独立，一个服务器一个分区, 由于程序自动检查和创建分区，这里不创建所有服务器分区
-- 新增分区 ALTER TABLE auction ADD PARTITION (PARTITION p2 VALUES IN (2));
PARTITION BY LIST(realm_id) (
    PARTITION p1 VALUES IN (1)
);
-- 为item_id添加索引用于查询服务器某种物品的所有拍卖
ALTER TABLE auction ADD INDEX(item_id);
-- 用于查询某个卖家的所有拍卖物品
ALTER TABLE auction ADD INDEX(owner);


