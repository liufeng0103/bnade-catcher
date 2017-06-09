package com.bnade.wow.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bnade.wow.entity.Auction;
import com.bnade.wow.entity.Realm;
import com.bnade.wow.util.DBUtils;
import com.bnade.wow.util.TimeUtils;

public class AuctionDao {

	private static Logger logger = LoggerFactory.getLogger(AuctionDao.class);
	
	private QueryRunner runner = new QueryRunner(DBUtils.getDataSource());
	
	// ---------------------------- t_ah_data_x ----------------------------
	private static final String AUCTION_TABLE_PREFIX = "t_ah_data_";
	/**
	 * 删除服务器t_ah_data_x表的所有数据
	 * @param realmId
	 * @throws SQLException 
	 */
	public void deleteAll(int realmId) throws SQLException {
		runner.update("truncate " + AUCTION_TABLE_PREFIX + realmId);
	}
	
	public void insert(int realmId, List<Auction> aucs) throws SQLException {
		Connection con = DBUtils.getDataSource().getConnection();
		try {
			long current = System.currentTimeMillis();
			boolean autoCommit = con.getAutoCommit();
			con.setAutoCommit(false);
			String tableName = AUCTION_TABLE_PREFIX + realmId;		
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
				params[i][12] = auc.getBonusLists();
				params[i][13] = current;
			}
			runner.batch(con, "insert into "
							+ tableName
							+ " (auc,item,owner,ownerRealm,bid,buyout,quantity,timeLeft,petSpeciesId,petLevel,petBreedId,context,bonusLists,lastModifed) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", params);
			con.commit();
			con.setAutoCommit(autoCommit);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private static final String MINBUYOUT_AUCTION_TABLE = "t_ah_min_buyout_data";
	/**
	 * 删除服务器t_ah_min_buyout_data表的所有数据
	 * @param realmId
	 * @throws SQLException 
	 */
	public void deleteAllMinBuyout(int realmId) throws SQLException {
		runner.update("delete from " + MINBUYOUT_AUCTION_TABLE + " where realmId=?", realmId);
	}
	
	public void insertMinBuyout(int realmId, List<Auction> aucs) throws SQLException {
		Connection con = DBUtils.getDataSource().getConnection();
		try {
			long current = System.currentTimeMillis();
			boolean autoCommit = con.getAutoCommit();
			con.setAutoCommit(false);			
			Object[][] params = new Object[aucs.size()][15];
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
				params[i][12] = auc.getBonusLists();
				params[i][13] = realmId;
				params[i][14] = current;
			}
			runner.batch(
					con,
					"insert into "
							+ MINBUYOUT_AUCTION_TABLE
							+ " (auc,item,owner,ownerRealm,bid,buyout,quantity,timeLeft,petSpeciesId,petLevel,petBreedId,context,bonusLists,realmId,lastModifed) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					params);
			con.commit();
			con.setAutoCommit(autoCommit);
		} finally {
			DbUtils.closeQuietly(con);
		}		
	}
	
	/**
	 * 查询物品在所有服务器的最低一口价数据
	 * @param auc
	 * @return
	 * @throws SQLException
	 */
	public List<Auction> getMinBuyout(Auction auc) throws SQLException  {
		return runner.query("select * from "+ MINBUYOUT_AUCTION_TABLE + " where item=? and bonusLists=?", new BeanListHandler<Auction>(Auction.class), auc.getItem(), auc.getBonusLists());
	}
	
	// ---------------------------- t_ah_min_buyout_data_yyyyMMdd_x ----------------------------
	private static final String DAILY_MINBUYOUT_AUCTION_TABLE_PREFIX = "t_ah_min_buyout_data_";
	
	/**
	 * 
	 * @param realmId
	 * @param aucs
	 * @throws SQLException
	 */
	public void copyMinBuyoutToDaily(Realm realm) throws SQLException {
		String tableName = DAILY_MINBUYOUT_AUCTION_TABLE_PREFIX
				+ TimeUtils.getDate(realm.getLastModified()) + "_"
				+ realm.getId();
		checkAndCreateMinBuyoutDailyTable(tableName);
		runner.update(
				"insert into "
						+ tableName
						+ " (item,owner,ownerRealm,bid,buyout,quantity,petSpeciesId,petBreedId,bonusLists,lastModifed) select item,owner,ownerRealm,bid,buyout,quantity,petSpeciesId,petBreedId,bonusLists,"
						+ System.currentTimeMillis()
						+ " from t_ah_min_buyout_data where realmId=? and item != 82800",
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
	
}
