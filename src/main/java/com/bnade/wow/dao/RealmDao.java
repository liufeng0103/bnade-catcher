package com.bnade.wow.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.bnade.wow.entity.Realm;
import com.bnade.wow.util.DBUtils;

public class RealmDao {
	
	private QueryRunner runner = new QueryRunner(DBUtils.getDataSource());

	public int update(Realm realm) throws SQLException {
		return runner.update("update t_realm set lastModified=?,maxAucId=?,auctionQuantity=?,playerQuantity=?,itemQuantity=?,lastUpdateTime=? where id=?",
				realm.getLastModified(), realm.getMaxAucId(), realm.getAuctionQuantity(),
				realm.getPlayerQuantity(), realm.getItemQuantity(), System.currentTimeMillis(), realm.getId());
	}
	
	public List<Realm> getAll() throws SQLException {
		return runner
				.query("select id,name,type,url,maxAucId,auctionQuantity,playerQuantity,itemQuantity,lastModified,lastUpdateTime from t_realm",
						new BeanListHandler<Realm>(Realm.class));
	}
	
}
