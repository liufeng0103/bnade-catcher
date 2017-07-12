package com.bnade.wow.dao;

import com.bnade.wow.entity.Item;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * ItemDao测试
 * Created by liufeng0103@163.com on 2017/6/30.
 */
public class ItemDaoTest {

    private ItemDao itemDao = new ItemDao();

    @Test
    public void getIdsTest() throws Exception {
        List<Integer> itemIds = itemDao.getIds();
        System.out.println(itemIds);
    }

    @Test
    public void saveTest() throws Exception {
        int itemId = 99999999;
        itemDao.deleteById(itemId);
        Item item = new Item();
        item.setId(itemId);
        item.setName("物品名");
        item.setIcon("icon test");
        item.setItemClass(1);
        item.setItemSubClass(2);
        item.setInventoryType(3);
        item.setLevel(4);
        item.setHot(0);
        Assert.assertEquals(1, itemDao.save(item));
        Item dbItem = itemDao.getItemById(itemId);
        System.out.println(dbItem);
        Assert.assertEquals(true, item.toString().equals(dbItem.toString()));
        Assert.assertEquals(1, itemDao.deleteById(itemId));
    }

    @Test
    public void findAllItemBonusesTest() throws Exception {
        System.out.println(itemDao.findAllItemBonuses());
    }

    @Test
    public void findItemSearchStatisticByItemIdAndSearchDateTest() throws Exception {
        System.out.println(new java.sql.Date(new Date().getTime()));
//        System.out.println(itemDao.findItemSearchStatisticByItemIdAndSearchDate(123, new Date()));
    }
}