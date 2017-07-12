package com.bnade.wow.entity;


import java.time.LocalDate;
import java.util.Date;

/**
 * 物品查询统计
 * Created by liufeng0103@163.com on 2017/7/12.
 */
public class ItemSearchStatistic {
    private Integer itemId;
    private Integer searchCount;
    private Date searchDate;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }

    public Date getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(Date searchDate) {
        this.searchDate = searchDate;
    }

    @Override
    public String toString() {
        return "ItemSearchStatistic{" +
                "itemId=" + itemId +
                ", searchCount=" + searchCount +
                ", searchDate=" + searchDate +
                '}';
    }
}
