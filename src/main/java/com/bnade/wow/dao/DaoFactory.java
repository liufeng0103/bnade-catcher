package com.bnade.wow.dao;

public class DaoFactory {
	
	private static AuctionDao auctionDao;
	private static ItemDao itemDao;

	public static AuctionDao getAuctionDao() {
		if (auctionDao == null) {
			auctionDao = new AuctionDao();
		}
		return auctionDao;
	}
	
	public static ItemDao getItemDao() {
		if (itemDao == null) {
			itemDao = new ItemDao();
		}
		return itemDao;
	}
	
	public static RealmDao getRealmDao() {
		return new RealmDao();
	}
	
	public static UserDao getUserDao() {
		return new UserDao();
	}
	
}
