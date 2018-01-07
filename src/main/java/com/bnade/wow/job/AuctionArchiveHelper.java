package com.bnade.wow.job;

import com.bnade.wow.entity.Pet;
import com.bnade.wow.util.TimeUtils;
import com.bnade.wow.v2.entity.CheapestAuctionDaily;
import com.bnade.wow.v2.entity.CheapestAuctionMonthly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AuctionArchiveHelper {
	
	// 每天分成几个时段
	private static final int PERIOD = 4;

	public List<CheapestAuctionMonthly> process(List<CheapestAuctionDaily> aucs, String date) throws Exception {
		long startTime = TimeUtils.parse(date).getTime();
		long period = TimeUtils.DAY / PERIOD;
		// map用来保存每个时段物品信息
		Map<Long, Map<String, CheapestAuctionMonthly>> periodMap = new HashMap<>();
		// 初始化map
		for (int i = 1; i <= PERIOD; i++) {
			Map<String, CheapestAuctionMonthly> result = new HashMap<>();
			if (i == PERIOD) {
				periodMap.put(startTime + TimeUtils.DAY, result);
			} else {
				periodMap.put(startTime + period * i, result);
			}
		}
		for (CheapestAuctionDaily auc : aucs) {
			// 不归档宠物价格
			if (auc.getItemId() == Pet.PET_ITEM_ID) {
				continue;
			}
			String key = "" + auc.getItemId() + auc.getPetSpeciesId() + auc.getPetBreedId() + auc.getBonusList();
			long lastModified = auc.getLastModified();
			// 计算数据属于哪个时段
			long aucPeriod = 0;
			for (int i = 1; i <= PERIOD; i++) {
				long tmpTime = startTime + period * i;
				if (i == 1) {
					aucPeriod = tmpTime;
				} else if (i == PERIOD) {
					if (lastModified >= (tmpTime - period)) {
						aucPeriod = startTime + TimeUtils.DAY;
					}
				} else {
					if (lastModified >= (tmpTime - period)) {
						aucPeriod = tmpTime;
					}
				}
			}
			// 保存各时段每种物品的总价格，总数量和总物品数量
			Map<String, CheapestAuctionMonthly> aucMap = periodMap.get(aucPeriod);
			if (aucMap != null) {
				CheapestAuctionMonthly tmpAuc = aucMap.get(key);
				if (tmpAuc == null) {
					CheapestAuctionMonthly historyAuction = new CheapestAuctionMonthly();
					historyAuction.setItemId(auc.getItemId());
					historyAuction.setPetSpeciesId(auc.getPetSpeciesId());
					historyAuction.setPetBreedId(auc.getPetBreedId());
					historyAuction.setBonusList(auc.getBonusList());
					historyAuction.setBuyout(auc.getBuyout()); // 计算单价
					historyAuction.setQuantity(auc.getQuantity());
					historyAuction.setCount(1);
					historyAuction.setLastModified(aucPeriod);
					aucMap.put(key, historyAuction);
				} else {
					tmpAuc.setBuyout(tmpAuc.getBuyout() + auc.getBuyout()); // 计算单价
					tmpAuc.setQuantity(tmpAuc.getQuantity() + auc.getQuantity());
					tmpAuc.setCount(tmpAuc.getCount() + 1);
				}
			} else {
				throw new Exception("获取不到时段" + aucPeriod + "的map");
			}
		}
		// 计算平均价格，平均数量
		List<CheapestAuctionMonthly> result = new ArrayList<>();
		for (Map<String, CheapestAuctionMonthly> historyAucs : periodMap.values()) {
			for (CheapestAuctionMonthly auc : historyAucs.values()) {
				auc.setBuyout(auc.getBuyout() / auc.getCount());
				auc.setQuantity(auc.getQuantity() / auc.getCount());
				result.add(auc);
			}
		}
		return result;
	}
}
