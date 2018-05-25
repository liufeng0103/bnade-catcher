package com.bnade.wow.dao;

import com.bnade.wow.entity.Bonus;
import com.bnade.wow.entity.Item;
import com.bnade.wow.entity.ItemSearchStatistic;
import com.bnade.wow.entity.ItemStatistic;
import com.bnade.wow.util.DBUtils;
import com.bnade.wow.v2.entity.ItemBonus;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import java.sql.Date;
import java.sql.SQLException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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
     * 更新物品信息
     * 目前只更新level， 有需要的以后修改这个更新
     *
     * @param item 物品信息
     * @return 数据库更新的记录数
     * @throws SQLException 数据库异常
     */
    public int update(Item item) throws SQLException {
        return runner.update(
                "update item set level=? where id=?",
                item.getLevel(), item.getId());
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
     * 通过物品ID获取bonus list
     * @param itemId 物品id
     * @return item bonus列表
     * @throws SQLException 数据库异常
     */
    public List<ItemBonus> findItemBonusesByItemId(Integer itemId) throws SQLException {
        return runner.query(
                "select item_id as itemId,bonus_list as bonusList from item_bonus where item_id=?",
                new BeanListHandler<ItemBonus>(ItemBonus.class), itemId);
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

    /**
     * 查询物品统计信息
     *
     * @param itemStatistic
     * @return
     * @throws SQLException
     */
    public ItemStatistic findItemStatistic(ItemStatistic itemStatistic) throws SQLException {
        String sql = "select * from item_statistic where item_id=? and bonus_list=? and valid_time=?";
        return runner.query(sql,
                new BeanHandler<ItemStatistic>(ItemStatistic.class, new BasicRowProcessor(new MyBeanProcessor())),
                itemStatistic.getItemId(), itemStatistic.getBonusList(), itemStatistic.getValidTime());
    }

    /**
     * 插件需要的物品数据
     * 目前不需要宠物和有多版本的物品
     * @return
     */
    public List<ItemStatistic> findBnadeAddonItems() throws SQLException {
        return runner.query("select * from item_statistic where item_id != 82800 and bonus_list='' and valid_time='9999-12-31 00:00:00'",
                new BeanListHandler<ItemStatistic>(ItemStatistic.class, new BasicRowProcessor(new MyBeanProcessor())));
    }

    /**
     * 保存物品统计信息
     *
     * @param i ItemStatistic
     * @return
     * @throws SQLException
     */
    public int saveItemStatistic(ItemStatistic i) throws SQLException {
        return runner.update(
                "insert into item_statistic (item_id,bonus_list,pet_species_id,pet_breed_id, market_price,cheapest_price,quantity,realm_quantity,valid_realm_quantity,valid_time) values (?,?,?,?,?,?,?,?,?,?)",
                i.getItemId(), i.getBonusList(), 0, 0, i.getMarketPrice(), i.getCheapestPrice(), i.getQuantity(), i.getRealmQuantity(), i.getValidRealmQuantity(), i.getValidTime());
    }

    /**
     * 更新最新的统计信息
     *
     * @param i
     * @return
     * @throws SQLException
     */
    public int updateItemStatistic(ItemStatistic i) throws SQLException {
        return runner.update(
                "update item_statistic set market_price=?,cheapest_price=?,quantity=?,realm_quantity=?,valid_realm_quantity=? where id=? and valid_time=?",
                i.getMarketPrice(), i.getCheapestPrice(), i.getQuantity(), i.getRealmQuantity(), i.getValidRealmQuantity(), i.getId(), i.getValidTime());
    }

    /**
     * 保存物品搜索统计
     *
     * @param itemSearchStatistic 参数
     * @return 数据库更新的记录数
     * @throws SQLException 数据库异常
     */
    public int saveItemSeachStatistic(ItemSearchStatistic itemSearchStatistic) throws SQLException {
        return runner.update(
                "insert into item_search_statistic (item_id, search_count, search_date) values (?,?,?)",
                itemSearchStatistic.getItemId(), itemSearchStatistic.getSearchCount(), itemSearchStatistic.getSearchDate());
    }

    /**
     * 更新物品搜索的次数
     *
     * @param itemSearchStatistic 物品搜索统计
     * @return  数据库更新的记录数
     * @throws SQLException 数据库异常
     */
    public int updateItemSeachStatisticCount(ItemSearchStatistic itemSearchStatistic) throws SQLException {
        return runner.update(
                "update item_search_statistic set search_count=? where item_id=? and search_date=?",
                itemSearchStatistic.getSearchCount(), itemSearchStatistic.getItemId(), itemSearchStatistic.getSearchDate());
    }

    /**
     * 通过物品id查询物品搜索统计
     *
     * @param itemId 物品id
     * @return 物品搜索统计
     * @throws SQLException 数据库异常
     */
    public ItemSearchStatistic findItemSearchStatisticByItemIdAndSearchDate(Integer itemId, LocalDate search_date) throws SQLException {
        return runner.query(
                "select item_id as itemId, search_count as searchCount, search_date as searchDate from item_search_statistic where item_id=? and search_date=?",
                new BeanHandler<ItemSearchStatistic>(ItemSearchStatistic.class), itemId, search_date);
    }

    /**
     * 获取所有的bonus
     * @return bonus列表
     * @throws SQLException 数据库异常
     */
    public List<Bonus> findAllBonuses() throws SQLException {
        return runner.query(
                "select id, name, comment from bonus",
                new BeanListHandler<Bonus>(Bonus.class));
    }

    /**
     * 查询大于某个id的所有物品id
     * @param id 物品id
     * @return id列表
     * @throws SQLException 数据库异常
     */
    public List<Integer> findIdsGreaterThan(Integer id) throws SQLException {
        return runner.query(
                "select id from item where id >= ?",
                new ColumnListHandler<Integer>(), id);
    }
}
