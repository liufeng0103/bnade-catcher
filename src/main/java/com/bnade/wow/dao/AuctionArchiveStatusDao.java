package com.bnade.wow.dao;

import com.bnade.wow.v2.entity.AuctionArchiveStatus;
import com.bnade.wow.util.DBUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import java.sql.SQLException;

/**
 * Created by liufeng0103@163.com on 2018/1/2.
 */
public class AuctionArchiveStatusDao {

    private QueryRunner runner = new QueryRunner(DBUtils.getDataSource());

    /**
     * 查找AuctionArchiveStatus
     * @param status
     * @return
     * @throws SQLException
     */
    public AuctionArchiveStatus findOne(AuctionArchiveStatus status) throws SQLException {
        return runner.query("select id,realm_id as realmId,archive_date as archiveDate,status,message from auction_archive_status where realm_id=? and archive_date=? and status=?", new BeanHandler<AuctionArchiveStatus>(AuctionArchiveStatus.class), status.getRealmId(), status.getArchiveDate(), status.getStatus());
    }

    /**
     * 保存归档程序状态
     * @param status
     */
    public void save(AuctionArchiveStatus status) throws SQLException {
        runner.update("insert into auction_archive_status (realm_id,archive_date,status,message) values (?,?,?,?)", status.getRealmId(), status.getArchiveDate(), status.getStatus(), status.getMessage());
    }

}
