package com.bnade.wow.catcher;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by liufeng0103@163.com on 2017/6/30.
 */
public class ItemCatcherJobTest {

    private ItemCatcherJob itemCatcherJob = new ItemCatcherJob();

    @Test
    public void executeTest() throws Exception {
        new ItemCatcherJob().execute(null);
    }

    @Test
    public void addNewItems() throws Exception {
        itemCatcherJob.addNewItems();
    }

    @Test
    public void addNewItemBonuses() throws Exception {
        itemCatcherJob.addNewItemBonuses();
    }

}