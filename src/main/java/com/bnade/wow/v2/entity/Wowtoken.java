package com.bnade.wow.v2.entity;

/**
 * Created by liufeng0103@163.com on 2018/1/6.
 */
public class Wowtoken {
    private Long updated;
    private Integer buy;

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public Integer getBuy() {
        return buy;
    }

    public void setBuy(Integer buy) {
        this.buy = buy;
    }

    @Override
    public String toString() {
        return "Wowtoken{" +
                "updated=" + updated +
                ", buy=" + buy +
                '}';
    }
}
