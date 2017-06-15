package com.bnade.wow.v2.dao;

import com.bnade.wow.util.DBUtils;
import com.bnade.wow.v2.entity.Auction;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
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

    private QueryRunner runner = new QueryRunner(DBUtils.getDataSource());

    public void save(int realmId, List<Auction> aucs) throws SQLException {
        Connection con = DBUtils.getDataSource().getConnection();
        try {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            Object[][] params = new Object[aucs.size()][14];
            for (int i = 0; i < aucs.size(); i++) {
                Auction auc = aucs.get(i);
                params[i][0] = auc.getAuc();
                params[i][1] = auc.getItem();
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
                params[i][13] = realmId;
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
}
