package com.bnade.wow.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.bnade.wow.entity.Item;
import com.bnade.wow.util.DBUtils;

public class ItemDao {

	private QueryRunner runner = new QueryRunner(DBUtils.getDataSource());

	/**
	 * 获取名字包含name的所有物品
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public List<Item> getItemsByName(String name) throws SQLException {
		return runner.query("select id,name,icon,itemClass,itemSubClass,inventoryType,itemLevel,hot from t_item where name like ? order by hot desc limit 10",
						new BeanListHandler<Item>(Item.class), "%" + name + "%");
	}
	
	public Item getItemById(int id) throws SQLException {
		return runner.query("select * from t_item where id=?", new BeanHandler<Item>(Item.class), id);
	}

}
