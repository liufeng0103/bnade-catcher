package com.bnade.wow.v2.dao;


import com.bnade.wow.util.DBUtils;
import com.bnade.wow.v2.entity.Realm;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.List;

/**
 * 服务器相关数据库操作
 * Created by liufeng0103@163.com on 2017/6/16.
 */
public class RealmDao {

    private QueryRunner runner = new QueryRunner(DBUtils.getDataSource());

    /**
     * 查询所有启用(active=1)的服务器信息
     * @return 服务器列表
     * @throws SQLException 数据库异常
     */
    public List<Realm> findAll() throws SQLException {
        return runner.query(
                "select id,name,type,url,last_modified as lastModified,`interval`,auction_quantity as auctionQuantity,owner_quantity as ownerQuantity,item_quantity as itemQuantity from realm where active=1",
                new BeanListHandler<Realm>(Realm.class));
    }

    /**
     * 更新服务器信息
     * @param realm 服务器信息
     * @return 更新条数
     * @throws SQLException 数据库异常
     */
    public int save(Realm realm) throws SQLException {
        return runner.update("update realm set last_modified=?,`interval`=?,auction_quantity=?,owner_quantity=?,item_quantity=? where id=?",
                realm.getLastModified(), realm.getInterval(), realm.getAuctionQuantity(),
                realm.getOwnerQuantity(), realm.getItemQuantity(), realm.getId());
    }

    /**
     * 通过服务器名查询服务器信息
     * @param name
     * @return
     * @throws SQLException
     */
    public Realm findByName(String name) throws SQLException {
        return runner.query(
                "select id,name,type,url,last_modified as lastModified,`interval`,auction_quantity as auctionQuantity,owner_quantity as ownerQuantity,item_quantity as itemQuantity from realm where name=?",
                new BeanHandler<Realm>(Realm.class), name);
    }
}
