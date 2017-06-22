package com.bnade.wow.v2.entity;

/**
 * 服务器相关信息
 * Created by liufeng0103@163.com on 2017/6/16.
 */
public class Realm {
    private Integer id;
    private String name;
    private String type;
    private String url;
    private Long lastModified;
    private Long interval;
    private Integer auctionQuantity;
    private Integer ownerQuantity;
    private Integer itemQuantity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public Integer getAuctionQuantity() {
        return auctionQuantity;
    }

    public void setAuctionQuantity(Integer auctionQuantity) {
        this.auctionQuantity = auctionQuantity;
    }

    public Integer getOwnerQuantity() {
        return ownerQuantity;
    }

    public void setOwnerQuantity(Integer ownerQuantity) {
        this.ownerQuantity = ownerQuantity;
    }

    public Integer getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(Integer itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    @Override
    public String toString() {
        return "Realm{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", lastModified=" + lastModified +
                ", interval=" + interval +
                ", auctionQuantity=" + auctionQuantity +
                ", ownerQuantity=" + ownerQuantity +
                ", itemQuantity=" + itemQuantity +
                '}';
    }
}
