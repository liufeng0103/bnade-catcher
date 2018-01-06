package com.bnade.wow.v2.dao;

import com.bnade.wow.util.DBUtils;
import com.bnade.wow.v2.entity.Wowtoken;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import java.sql.SQLException;

/**
 * Created by liufeng0103@163.com on 2018/1/6.
 */
public class WowtokenDao {

    private QueryRunner runner = DBUtils.getQueryRunner();

    /**
     * 保存单个时光徽章数据
     * @param wowtoken
     * @throws SQLException
     */
    public void save(Wowtoken wowtoken) throws SQLException {
        runner.update("insert into wowtoken (updated,buy) values(?,?)", wowtoken.getUpdated(), wowtoken.getBuy());
    }

    /**
     * 清空wowtoken表
     * @throws SQLException
     */
    public void deleteAll() throws SQLException {
        runner.update("truncate wowtoken");
    }

    /**
     * 通过更新时间查询徽章信息
     * @param updated
     * @return
     * @throws SQLException
     */
    public Wowtoken findByUpdated(long updated) throws SQLException {
        return runner.query("select updated,buy from wowtoken where updated=?", new BeanHandler<Wowtoken>(Wowtoken.class), updated);
    }
}
