package com.bnade.wow.v2.entity;

/**
 * 每月的历史数据
 * Created by liufeng0103@163.com on 2018/1/2.
 */
public class CheapestAuctionMonthly {
    private Integer itemId;
    private Integer petSpeciesId;
    private Integer petBreedId;
    private String bonusList;
    // 平均价格
    private Long buyout;
    // 平均数量
    private Integer quantity;
    // 时段
    private Long lastModified;
    // 时段该类物品数量，用于archive中计算平均
    private Integer count;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getPetSpeciesId() {
        return petSpeciesId;
    }

    public void setPetSpeciesId(Integer petSpeciesId) {
        this.petSpeciesId = petSpeciesId;
    }

    public Integer getPetBreedId() {
        return petBreedId;
    }

    public void setPetBreedId(Integer petBreedId) {
        this.petBreedId = petBreedId;
    }

    public String getBonusList() {
        return bonusList;
    }

    public void setBonusList(String bonusList) {
        this.bonusList = bonusList;
    }

    public Long getBuyout() {
        return buyout;
    }

    public void setBuyout(Long buyout) {
        this.buyout = buyout;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CheapestAuctionMonthly{" +
                "itemId=" + itemId +
                ", petSpeciesId=" + petSpeciesId +
                ", petBreedId=" + petBreedId +
                ", bonusList='" + bonusList + '\'' +
                ", buyout=" + buyout +
                ", quantity=" + quantity +
                ", lastModified=" + lastModified +
                ", count=" + count +
                '}';
    }
}
