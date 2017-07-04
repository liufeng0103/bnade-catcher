package com.bnade.wow.v2.dao;

import com.bnade.wow.v2.entity.Realm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by liufeng0103@163.com on 2017/6/16.
 */
public class RealmDaoTest {

    private RealmDao realmDao;

    @Before
    public void setUp() throws Exception {
        realmDao = new RealmDao();
    }

    @Test
    public void findAll() throws Exception {
        System.out.println(realmDao.findAll());
    }

    @Test
    public void save() throws Exception {
        Realm realm = new Realm();
        realm.setId(1);
        realm.setLastModified(1234567l);
        realm.setInterval(123l);
        realm.setAuctionQuantity(456);
        realm.setOwnerQuantity(789);
        realm.setItemQuantity(321);
        Assert.assertEquals(1, realmDao.save(realm));
    }

}