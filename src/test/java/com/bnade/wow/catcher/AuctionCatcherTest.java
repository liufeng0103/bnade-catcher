package com.bnade.wow.catcher;

import com.bnade.wow.entity.Realm;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by liufeng0103@163.com on 2017/8/20.
 */
public class AuctionCatcherTest {
    @Test
    public void process() throws Exception {
        Realm realm = new Realm();
        realm.setId(88);
        realm.setName("影之哀伤");
        realm.setUrl("http://auction-api-cn.worldofwarcraft.com/auction-data/009e169a7dee9b2f44417b8ea90e6152/auctions.json");
//        new AuctionCatcher().processAuctions(realm);
    }

}