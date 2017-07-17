package com.bnade.wow.v2.dao;

import com.bnade.wow.v2.entity.ItemBonus;
import org.junit.Test;

import java.util.List;

/**
 * Created by liufeng0103@163.com on 2017/7/1.
 */
public class AuctionDaoTest {

    private AuctionDao auctionDao = AuctionDao.getInstance();

    @Test
    public void getItemIds() throws Exception {
        List<Integer> itemIds = auctionDao.getItemIds();
        System.out.println(itemIds);
    }

    @Test
    public void findAllItemBonusesTest() throws Exception {
        List<ItemBonus> itemBonuses = auctionDao.findAllItemBonuses();
        for (ItemBonus itemBonus : itemBonuses) {
            System.out.println(itemBonus);
        }
        System.out.println(itemBonuses.size());
    }

    @Test
    public void findByItemIdTest() throws Exception {
        auctionDao.findByRealmIdAndItemId(1, 124442).forEach((auction) -> {
            System.out.println(auction);
        });
    }

}