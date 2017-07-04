package com.bnade.wow.dao;

import com.bnade.wow.entity.Item;
import com.bnade.wow.util.DBUtils;
import com.bnade.wow.v2.entity.ItemBonus;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import java.sql.SQLException;
import java.util.List;

public class ItemDao {

    private static ItemDao itemDao;

    public static ItemDao getInstance() {
        return itemDao == null ? itemDao = new ItemDao() : itemDao;
    }

    private QueryRunner runner = DBUtils.getQueryRunner();

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
        return runner.query("select id,name,icon,item_class as itemClass,item_sub_class as itemSubClass,inventory_type as inventoryType,level,hot from item where id=?", new BeanHandler<Item>(Item.class), id);
    }

    /**
     * 获取所有物品id
     *
     * @return 物品id列表
     * @throws SQLException 数据库异常
     */
    public List<Integer> getIds() throws SQLException {
        return runner.query(
                "select distinct id from item",
                new ColumnListHandler<Integer>());
    }

    /**
     * 添加新的item到物品表
     * @param item 物品
     * @return 数据库更新的记录数
     * @throws SQLException 数据库异常
     */
    public int save(Item item) throws SQLException {
        return runner.update(
                "insert into item (id,name,icon,item_class,item_sub_class,inventory_type,level) values(?,?,?,?,?,?,?)",
                item.getId(), item.getName(), item.getIcon(), item.getItemClass(), item.getItemSubClass(), item.getInventoryType(), item.getLevel());
    }

    /**
     * 通过ID删除某个物品
     * @param itemId 物品id
     * @return 更新数据库条数
     * @throws SQLException 数据库异常
     */
    public int deleteById(Integer itemId) throws SQLException {
        return runner.update("delete from item where id=?", itemId);
    }

    /**
     * 获取所有的item bonus信息
     * @return item bonus列表
     * @throws SQLException 数据库异常
     */
    public List<ItemBonus> findAllItemBonuses() throws SQLException {
        return runner.query(
                "select item_id as itemId,bonus_list as bonusList from item_bonus",
                new BeanListHandler<ItemBonus>(ItemBonus.class));
    }

    /**
     * 添加新的item bonus
     * @param itemBonus 物品奖励信息
     * @return 数据库更新的记录数
     * @throws SQLException 数据库异常
     */
    public int saveItemBonus(ItemBonus itemBonus) throws SQLException {
        return runner.update(
                "insert into item_bonus (item_id,bonus_list) values(?,?)",
                itemBonus.getItemId(), itemBonus.getBonusList());
    }

}
