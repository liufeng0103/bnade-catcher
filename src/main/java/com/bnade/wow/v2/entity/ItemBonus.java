package com.bnade.wow.v2.entity;

/**
 * 物品奖励
 * Created by liufeng0103@163.com on 2017/6/30.
 */
public class ItemBonus {
    private Integer itemId;
    private String bonusList;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemBonus itemBonus = (ItemBonus) o;

        if (itemId != null ? !itemId.equals(itemBonus.itemId) : itemBonus.itemId != null) return false;
        return bonusList != null ? bonusList.equals(itemBonus.bonusList) : itemBonus.bonusList == null;
    }

    @Override
    public int hashCode() {
        int result = itemId != null ? itemId.hashCode() : 0;
        result = 31 * result + (bonusList != null ? bonusList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ItemBonus{" +
                "itemId=" + itemId +
                ", bonusList='" + bonusList + '\'' +
                '}';
    }
}
