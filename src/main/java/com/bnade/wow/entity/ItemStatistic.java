package com.bnade.wow.entity;

import java.sql.Timestamp;

/**
 * 物品统计
 * Created by liufeng0103@163.com on 2017/8/7.
 */
public class ItemStatistic {

    private Integer id;
    private Integer itemId;
    private String bonusList;
    private Integer petSpeciesId;
    private Integer petBreedId;
    private Long marketPrice;
    private Integer quantity;
    private Integer realmQuantity;
    private Integer validRealmQuantity;
    private Timestamp validTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getBonusList() {
        return bonusList;
    }

    public void setBonusList(String bonusList) {
        this.bonusList = bonusList;
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

    public Long getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(Long marketPrice) {
        this.marketPrice = marketPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getRealmQuantity() {
        return realmQuantity;
    }

    public void setRealmQuantity(Integer realmQuantity) {
        this.realmQuantity = realmQuantity;
    }

    public Integer getValidRealmQuantity() {
        return validRealmQuantity;
    }

    public void setValidRealmQuantity(Integer validRealmQuantity) {
        this.validRealmQuantity = validRealmQuantity;
    }

    public Timestamp getValidTime() {
        return validTime;
    }

    public void setValidTime(Timestamp validTime) {
        this.validTime = validTime;
    }

    @Override
    public String toString() {
        return "ItemStatistic{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", bonusList='" + bonusList + '\'' +
                ", petSpeciesId=" + petSpeciesId +
                ", petBreedId=" + petBreedId +
                ", marketPrice=" + marketPrice +
                ", quantity=" + quantity +
                ", realmQuantity=" + realmQuantity +
                ", validRealmQuantity=" + validRealmQuantity +
                ", validTime=" + validTime +
                '}';
    }
}
