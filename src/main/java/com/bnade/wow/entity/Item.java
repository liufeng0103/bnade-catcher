package com.bnade.wow.entity;

public class Item {

	// 宠物笼id
	public static final int PET_CAGE_ID = 82800;

	private int id;
	private String name;
	private String icon;
	private int itemClass;
	private int itemSubClass;
	private int inventoryType;
	private int level;
	private int hot;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getItemClass() {
		return itemClass;
	}

	public void setItemClass(int itemClass) {
		this.itemClass = itemClass;
	}

	public int getItemSubClass() {
		return itemSubClass;
	}

	public void setItemSubClass(int itemSubClass) {
		this.itemSubClass = itemSubClass;
	}

	public int getInventoryType() {
		return inventoryType;
	}

	public void setInventoryType(int inventoryType) {
		this.inventoryType = inventoryType;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getHot() {
		return hot;
	}

	public void setHot(int hot) {
		this.hot = hot;
	}

	@Override
	public String toString() {
		return "Item{" +
				"id=" + id +
				", name='" + name + '\'' +
				", icon='" + icon + '\'' +
				", itemClass=" + itemClass +
				", itemSubClass=" + itemSubClass +
				", inventoryType=" + inventoryType +
				", level=" + level +
				", hot=" + hot +
				'}';
	}
}
