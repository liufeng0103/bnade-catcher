package com.bnade.wow.v2.dao;

import com.bnade.wow.util.DBUtils;
import com.bnade.wow.util.TimeUtils;
import com.bnade.wow.v2.entity.Auction;
import com.bnade.wow.v2.entity.ItemBonus;
import com.bnade.wow.v2.entity.LowestAuction;
import com.bnade.wow.v2.entity.Realm;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 拍卖数据的数据库操作
 * Created by liufeng0103@163.com on 2017/6/11.
 */
public class AuctionDao {

    private static final Logger logger = LoggerFactory.getLogger(AuctionDao.class);

    private static AuctionDao auctionDao;

    private QueryRunner runner = DBUtils.getQueryRunner();

    public static AuctionDao getInstance() {
        return auctionDao == null ? auctionDao = new AuctionDao() : auctionDao;
    }

    /**
     * 保存拍卖数据
     * @param aucs 拍卖数据
     * @throws SQLException 数据库异常
     */
    public void save(List<Auction> aucs) throws SQLException {
        Connection con = DBUtils.getDataSource().getConnection();
        try {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            Object[][] params = new Object[aucs.size()][14];
            for (int i = 0; i < aucs.size(); i++) {
                Auction auc = aucs.get(i);
                params[i][0] = auc.getAuc();
                params[i][1] = auc.getItemId();
                params[i][2] = auc.getOwner();
                params[i][3] = auc.getOwnerRealm();
                params[i][4] = auc.getBid();
                params[i][5] = auc.getBuyout();
                params[i][6] = auc.getQuantity();
                params[i][7] = auc.getTimeLeft();
                params[i][8] = auc.getPetSpeciesId();
                params[i][9] = auc.getPetLevel();
                params[i][10] = auc.getPetBreedId();
                params[i][11] = auc.getContext();
                params[i][12] = auc.getBonusList();
                params[i][13] = auc.getRealmId();
            }
            runner.batch(con, "insert into auction (auc,item_id,owner,owner_realm,bid,buyout,quantity,time_left,pet_species_id,pet_level,pet_breed_id,context,bonus_list,realm_id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", params);
            con.commit();
            con.setAutoCommit(autoCommit);
        } finally {
            DbUtils.closeQuietly(con);
        }
    }

    /**
     * 删除某个服务器的所有拍卖数据
     * 由于auction使用的分区表，通过删除分区来快速删除所有数据
     * @param realmId 服务器id
     * @throws SQLException 数据库操作异常
     */
    public void deleteByRealmId(int realmId) throws SQLException {
        // 判断分区是否存在
        Object obj = runner.query("SELECT * FROM INFORMATION_SCHEMA.partitions WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME='auction' and partition_name=?"
                , new BeanHandler<>(Object.class), "p" + realmId);
        if (obj == null) {
            logger.info("p{}分区不存在,创建该分区", realmId);
            runner.update(" ALTER TABLE auction ADD PARTITION (PARTITION p" + realmId + " VALUES IN (" + realmId + "))");
        } else {
            logger.info("删除并重新创建分区p{}", realmId);
            runner.update("ALTER TABLE auction DROP PARTITION p" + realmId);
            runner.update(" ALTER TABLE auction ADD PARTITION (PARTITION p" + realmId + " VALUES IN (" + realmId + "))");
        }
    }

    /**
     * 通过realm id删除这个服务器的所有最低一口价拍卖数据
     * @param realmId 服务器id
     * @throws SQLException 数据库异常
     */
    public void deleteLowestByRealmId(int realmId) throws SQLException {
        runner.update("DELETE FROM cheapest_auction where realm_id=?", realmId);
    }

    /**
     * 保存最低一口价拍卖数据
     * @param aucs 拍卖数据
     * @throws SQLException 数据库异常
     */
    public void saveLowest(List<LowestAuction> aucs) throws SQLException {
        Connection con = DBUtils.getDataSource().getConnection();
        try {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            Object[][] params = new Object[aucs.size()][15];
            for (int i = 0; i < aucs.size(); i++) {
                LowestAuction auc = aucs.get(i);
                params[i][0] = auc.getAuc();
                params[i][1] = auc.getItemId();
                params[i][2] = auc.getOwner();
                params[i][3] = auc.getOwnerRealm();
                params[i][4] = auc.getBid();
                params[i][5] = auc.getBuyout();
                params[i][6] = auc.getQuantity();
                params[i][7] = auc.getTotalQuantity();
                params[i][8] = auc.getTimeLeft();
                params[i][9] = auc.getPetSpeciesId();
                params[i][10] = auc.getPetLevel();
                params[i][11] = auc.getPetBreedId();
                params[i][12] = auc.getContext();
                params[i][13] = auc.getBonusList();
                params[i][14] = auc.getRealmId();
            }
            runner.batch(con,
                    "insert into cheapest_auction (auc,item_id,owner,owner_realm,bid,buyout,quantity,total_quantity,time_left,pet_species_id,pet_level,pet_breed_id,context,bonus_list,realm_id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    params);
            con.commit();
            con.setAutoCommit(autoCommit);
        } finally {
            DbUtils.closeQuietly(con);
        }
    }

    /**
     * 拷贝服务器当前最低一口价数据到历史表
     * 不拷贝装笼宠物
     * @param realm 服务器信息
     * @throws SQLException 数据库异常
     */
    public void copyMinBuyoutToDaily(Realm realm) throws SQLException {
        String tableName = "t_ah_min_buyout_data_"
                + TimeUtils.getDate(realm.getLastModified()) + "_"
                + realm.getId();
        checkAndCreateMinBuyoutDailyTable(tableName);
        runner.update(
                "insert into "
                        + tableName
                        + " (item,owner,ownerRealm,bid,buyout,quantity,petSpeciesId,petBreedId,bonusLists,lastModifed) select item_id,owner,owner_realm,bid,buyout,quantity,pet_species_id,pet_breed_id,bonus_list,"
                        + System.currentTimeMillis()
                        + " from cheapest_auction where realm_id=? and item_id != 82800",
                realm.getId());
    }

    private void checkAndCreateMinBuyoutDailyTable(String tableName) throws SQLException {
        if (!DBUtils.isTableExist(tableName)) {
            StringBuffer sb = new StringBuffer();
            sb.append("CREATE TABLE IF NOT EXISTS " + tableName + " (");
            sb.append("id INT UNSIGNED NOT NULL AUTO_INCREMENT,");
            sb.append("item INT UNSIGNED NOT NULL,");
            sb.append("owner VARCHAR(12) NOT NULL,");
            sb.append("ownerRealm VARCHAR(8) NOT NULL,");
            sb.append("bid BIGINT UNSIGNED NOT NULL,");
            sb.append("buyout BIGINT UNSIGNED NOT NULL,");
            sb.append("quantity INT UNSIGNED NOT NULL,");
            sb.append("petSpeciesId INT UNSIGNED NOT NULL,");
            sb.append("petBreedId INT UNSIGNED NOT NULL,");
            sb.append("bonusLists VARCHAR(20) NOT NULL,");
            sb.append("lastModifed BIGINT UNSIGNED NOT NULL,");
            sb.append("PRIMARY KEY(id)");
            sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            runner.update(sb.toString());
            runner.update("ALTER TABLE " + tableName + " ADD INDEX(item)");
            logger.info("表{}未创建， 创建表和索引", tableName);
        }
    }

    /**
     * 获取当前所有服务器拍卖行的物品id
     * @return 物品id，唯一
     * @throws SQLException 数据库异常
     */
    public List<Integer> getItemIds() throws SQLException {
        return runner.query(
                "select distinct item_id from cheapest_auction",
                new ColumnListHandler<Integer>());
    }

    /**
     * 获取拍卖行中所有的item bonus,用来添加新的bonus
     * 1. 宠物(item_id = 82800)不需要
     * 2. 只更新一下类型(class)
     * 武器(2)
     * 护甲(4)
     * 宝石(3) 主要是圣物
     * @return item bonus列表
     */
    public List<ItemBonus> findAllItemBonuses() throws SQLException {
        return runner.query(
                "select a.item_id as itemId,a.bonus_list as bonusList,i.item_class as itemClass,i.level from cheapest_auction a join item i on a.item_id=i.id where i.item_class in (2,3,4) and i.level >= 800 group by item_id,bonus_list",
                new BeanListHandler<ItemBonus>(ItemBonus.class));
    }
}
