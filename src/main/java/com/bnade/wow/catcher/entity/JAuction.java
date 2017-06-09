package com.bnade.wow.catcher.entity;

import java.util.Collections;
import java.util.List;

public class JAuction {
	private int auc;
	private int item;
	private String owner;
	private String ownerRealm;
	private long bid;
	private long buyout;
	private int quantity;
	private String timeLeft;
	private int rand;
	private long seed;
	private int context;
	private List<JModifier> modifiers;
	private int petSpeciesId;
	private int petBreedId;
	private int petLevel;
	private int petQualityId;
	private List<JBonusList> bonusLists;

	/**
	 * 把BonusLists转化成String，且只显示自己关心的bonus
	 * @param bonusLists
	 * @return
	 */
	public String convertBonusListsToString() {
		String result = "";
		if (bonusLists != null && bonusLists.size() > 0) {
			StringBuffer sb = new StringBuffer();
			Collections.sort(bonusLists);
			for (JBonusList b : bonusLists) {
				if (JBonusList.bonusIds.contains(b.getBonusListId())) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(b.getBonusListId());
				}
			}
			result = sb.toString();
		}	
		return result;
	}
	
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

	public int getRand() {
		return rand;
	}

	public void setRand(int rand) {
		this.rand = rand;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public int getContext() {
		return context;
	}

	public void setContext(int context) {
		this.context = context;
	}

	public int getPetSpeciesId() {
		return petSpeciesId;
	}

	public void setPetSpeciesId(int petSpeciesId) {
		this.petSpeciesId = petSpeciesId;
	}

	public int getPetBreedId() {
		return petBreedId;
	}

	public void setPetBreedId(int petBreedId) {
		this.petBreedId = petBreedId;
	}

	public int getPetLevel() {
		return petLevel;
	}

	public void setPetLevel(int petLevel) {
		this.petLevel = petLevel;
	}

	public int getPetQualityId() {
		return petQualityId;
	}

	public void setPetQualityId(int petQualityId) {
		this.petQualityId = petQualityId;
	}

	public List<JModifier> getModifiers() {
		return modifiers;
	}

	public void setModifiers(List<JModifier> modifiers) {
		this.modifiers = modifiers;
	}

	public List<JBonusList> getBonusLists() {
		return bonusLists;
	}

	public void setBonusLists(List<JBonusList> bonusLists) {
		this.bonusLists = bonusLists;
	}

	@Override
	public String toString() {
		return "JAuction [auc=" + auc + ", item=" + item + ", owner=" + owner
				+ ", ownerRealm=" + ownerRealm + ", bid=" + bid + ", buyout="
				+ buyout + ", quantity=" + quantity + ", timeLeft=" + timeLeft
				+ ", rand=" + rand + ", seed=" + seed + ", context=" + context
				+ ", modifiers=" + modifiers + ", petSpeciesId=" + petSpeciesId
				+ ", petBreedId=" + petBreedId + ", petLevel=" + petLevel
				+ ", petQualityId=" + petQualityId + ", bonusLists="
				+ bonusLists + "]";
	}
	
}
