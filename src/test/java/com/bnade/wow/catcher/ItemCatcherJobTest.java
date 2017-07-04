package com.bnade.wow.catcher;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by liufeng0103@163.com on 2017/6/30.
 */
public class ItemCatcherJobTest {

    @Test
    public void executeTest() throws Exception {
        new ItemCatcherJob().execute(null);
    }

}