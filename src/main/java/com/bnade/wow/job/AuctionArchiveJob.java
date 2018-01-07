package com.bnade.wow.job;


import com.bnade.wow.dao.AuctionArchiveStatusDao;
import com.bnade.wow.util.TimeUtils;
import com.bnade.wow.v2.dao.AuctionDao;
import com.bnade.wow.v2.dao.RealmDao;
import com.bnade.wow.v2.entity.AuctionArchiveStatus;
import com.bnade.wow.v2.entity.CheapestAuctionDaily;
import com.bnade.wow.v2.entity.CheapestAuctionMonthly;
import com.bnade.wow.v2.entity.Realm;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * 归档昨天，清除前天
 * 1. 从文件或数据库读取所有服务器信息
 * 2. 获取前天的日期，从数据库查询是否已处理过该服务器该天的归档
 * 3. 如果没有就获取该服务器该天的所有记录
 * 4. 按每天4个时段计算平均价格和平均数量
 * 5. 保存数据到年历史纪录表
 * 6. 保存该天到数据库表示已经处理过该天记录了
 * 7. drop掉该天的集合
 * 
 * @author liufeng0103
 *
 */
public class AuctionArchiveJob implements Job {

	private static Logger logger = LoggerFactory.getLogger(AuctionArchiveJob.class);

	private String logHeader;
	private AuctionArchiveStatusDao auctionArchiveStatusDao;
	private RealmDao realmDao;
	private AuctionDao auctionDao = new AuctionDao();
	private AuctionArchiveHelper auctionArchiveHelper;

	public AuctionArchiveJob() {
		realmDao = new RealmDao();
		auctionArchiveStatusDao = new AuctionArchiveStatusDao();
		auctionArchiveHelper = new AuctionArchiveHelper();
	}

	public void process(Realm realm, String handleDate) throws Exception {
		long start = System.currentTimeMillis();
		logHeader = "服务器[" + realm.getName() + "-" + realm.getId() + "]";
		addInfo("开始归档{}的数据", handleDate);

		AuctionArchiveStatus auctionArchiveStatus = new AuctionArchiveStatus(realm.getId(), handleDate, AuctionArchiveStatus.STATUS_SUCCESS, "");
		if(auctionArchiveStatusDao.findOne(auctionArchiveStatus) == null) {
			addInfo("开始获取{}的数据", handleDate);
			List<CheapestAuctionDaily> aucs = auctionDao.findCheapestAuctionDailyByRealmIdAndDate(realm.getId(), handleDate);
			if (aucs.size() > 0) {
				addInfo("获取数据完毕, 共{}条", aucs.size());
				try {
					List<CheapestAuctionMonthly> result = auctionArchiveHelper.process(aucs, handleDate);
					addInfo("数据分析完毕共{}条", result.size());
					String month = TimeUtils.getYearMonth(TimeUtils.parse(handleDate).getTime());
					addInfo("把数据归档到{}年的集合", month);
					auctionDao.saveCheapestAuctionMonthlies(result, realm.getId(), month);
					auctionArchiveStatusDao.save(auctionArchiveStatus);
					addInfo("数据添加为已归档", handleDate);
				} catch (Exception e) {
					String msg = e.getMessage();
					addError("出错：{}", e.getMessage());
					e.printStackTrace();

					auctionArchiveStatus.setStatus(AuctionArchiveStatus.STATUS_FAILED);
					auctionArchiveStatus.setMessage(msg.length() > 256 ? msg.substring(0, 256) : msg); // 防止超过db定义长度
					auctionArchiveStatusDao.save(auctionArchiveStatus);
				}
			} else {
				addInfo("获取{}的数据0条，不处理", realm.getName());
			}
		} else {
			addInfo("已处理过");
		}
		addInfo("完毕,用时{}", TimeUtils.format(System.currentTimeMillis() - start));
	}

	public void clean(Realm realm, String handleDate) throws SQLException {
		logHeader = "服务器[" + realm.getName() + "-" + realm.getId() + "]";
		AuctionArchiveStatus auctionArchiveStatus = new AuctionArchiveStatus(realm.getId(), handleDate, AuctionArchiveStatus.STATUS_SUCCESS, "");
		if(auctionArchiveStatusDao.findOne(auctionArchiveStatus) != null) {
			addInfo("开始删除{}的集合", handleDate);
			auctionDao.dropCheapestAuctionDaily(realm.getId(), handleDate);
			addInfo("删除{}的集合完毕", handleDate);
		} else {
			addInfo("未归档过{}的数据或数据不存在", handleDate);
		}
	}

	private void addInfo(String msg, Object... arguments) {
		logger.info(logHeader + msg, arguments);
	}

	private void addError(String msg, Object... arguments) {
		logger.error(logHeader + msg, arguments);
	}

	public static void main(String[] args) throws Exception {
//		args = new String[2];
//		args[0] = "古尔丹";
//		args[1] = "20160507";
//		logger.info("启动");
//		long start = System.currentTimeMillis();
//		if (args != null && args.length == 1) {
//			AuctionArchiveJob task = new AuctionArchiveJob();
//			List<String> realmNames = FileUtil.fileLineToList("realmlist.txt");
//			String handleDate = args[0];
//			String cleanDate = TimeUtil.getDate(TimeUtil.parse(handleDate), -1);
//			for (String realmName : realmNames) {
//				task.process(realmName, handleDate);
//				task.clean(realmName, cleanDate);
//				if (task.isShutdown()) {
//					logger.info("准备退出，当前运行服务器[{}]", realmName);
//					break;
//				}
//			}
//			task.finished();
//		} else if (args != null && args.length > 1) {
//			String realmName = args[0];
//			String handleDate = args[1];
//			AuctionArchiveJob task = new AuctionArchiveJob();
//			task.process(realmName, handleDate);
//			task.finished();
//		} else {
//			AuctionArchiveJob task = new AuctionArchiveJob();
//			List<String> realmNames = FileUtil.fileLineToList("realmlist.txt");
//			String handleDate = TimeUtil.getDate(-1);
//			String cleanDate = TimeUtil.getDate(-2);
//			for (String realmName : realmNames) {
//				task.process(realmName, handleDate);
//				task.clean(realmName, cleanDate);
//				if (task.isShutdown()) {
//					logger.info("准备退出，当前运行服务器[{}]", realmName);
//					break;
//				}
//			}
//			task.finished();
//		}
//		logger.info("运行结束，用时{}", TimeUtil.format(System.currentTimeMillis() - start));
		AuctionArchiveJob auctionArchiveJob = new AuctionArchiveJob();
//		Realm realm = new RealmDao().findByName("万色星辰-奥蕾莉亚-世界之树-布莱恩");
//		auctionArchiveJob.process(realm, "20180102");
//		auctionArchiveJob.clean(realm, "20180102");
		auctionArchiveJob.execute(null);

	}

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		long start = System.currentTimeMillis();
		try {
			String handleDate = TimeUtils.getDate(-1); // 归档昨天
			String cleanDate = TimeUtils.getDate(-2); // 删除前天
			List<Realm> realms = realmDao.findAll();
			for (Realm realm : realms) {
				process(realm, handleDate);
				clean(realm, cleanDate);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.info("归档完毕用时{}", TimeUtils.format(System.currentTimeMillis() - start));
		}

	}
}
