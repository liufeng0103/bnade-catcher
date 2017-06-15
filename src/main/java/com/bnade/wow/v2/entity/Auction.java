package com.bnade.wow.v2.entity;

/**
 * 拍卖数据记录
 * Created by liufeng0103@163.com on 2017/6/11.
 */
public class Auction {
    private int auc;
    private int item;
    private String owner;
    private String ownerRealm;
    private long bid;
    private long buyout;
    private int quantity;
    private String timeLeft;
    private int petSpeciesId;
    private int petLevel;
    private int petBreedId;
    private int context;
    private String bonusList;
    private int realmId;

    public int getAuc() {
        return auc;
    }

    public void setAuc(int auc) {
        this.auc = auc;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerRealm() {
        return ownerRealm;
    }

    public void setOwnerRealm(String ownerRealm) {
        this.ownerRealm = ownerRealm;
    }

    public long getBid() {
        return bid;
    }

    public void setBid(long bid) {
        this.bid = bid;
    }

    public long getBuyout() {
        return buyout;
    }

    public void setBuyout(long buyout) {
        this.buyout = buyout;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(String timeLeft) {
        this.timeLeft = timeLeft;
    }

    public int getPetSpeciesId() {
        return petSpeciesId;
    }

    public void setPetSpeciesId(int petSpeciesId) {
        this.petSpeciesId = petSpeciesId;
    }

    public int getPetLevel() {
        return petLevel;
    }

    public void setPetLevel(int petLevel) {
        this.petLevel = petLevel;
    }

    public int getPetBreedId() {
        return petBreedId;
    }

    public void setPetBreedId(int petBreedId) {
        this.petBreedId = petBreedId;
    }

    public int getContext() {
        return context;
    }

    public void setContext(int context) {
        this.context = context;
    }

    public String getBonusList() {
        return bonusList;
    }

    public void setBonusList(String bonusList) {
        this.bonusList = bonusList;
    }

    public int getRealmId() {
        return realmId;
    }

    public void setRealmId(int realmId) {
        this.realmId = realmId;
    }

    @Override
    public String toString() {
        return "Auction{" +
                "auc=" + auc +
                ", item=" + item +
                ", owner='" + owner + '\'' +
                ", ownerRealm='" + ownerRealm + '\'' +
                ", bid=" + bid +
                ", buyout=" + buyout +
                ", quantity=" + quantity +
                ", timeLeft='" + timeLeft + '\'' +
                ", petSpeciesId=" + petSpeciesId +
                ", petLevel=" + petLevel +
                ", petBreedId=" + petBreedId +
                ", context=" + context +
                ", bonusList='" + bonusList + '\'' +
                ", realmId=" + realmId +
                '}';
    }
}
