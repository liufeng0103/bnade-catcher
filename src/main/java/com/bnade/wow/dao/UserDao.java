package com.bnade.wow.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.bnade.wow.entity.UserItemNotification;
import com.bnade.wow.util.DBUtils;

public class UserDao {

	private QueryRunner run = new QueryRunner(DBUtils.getDataSource());

	public List<UserItemNotification> getItemNotificationsByRealmId(int realmId)
			throws SQLException {
		return run
				.query("select userId,realmId,itemId,petSpeciesId,petBreedId,bonusList,i.name as itemName,i.level as itemLevel,email,isInverted,price from t_user_item_notification n join t_user u on n.userId=u.id join item i on i.id=n.itemId where (n.realmId=? or n.realmId=0) and n.itemId!=82800 and n.emailNotification=1 and u.validated=1"
						+ " union all select userId,realmId,82800,petSpeciesId,petBreedId,bonusList,p.name as itemName,0 as itemLevel,email,isInverted,price from t_user_item_notification n join t_user u on n.userId=u.id join t_pet p on p.id=n.petSpeciesId where (n.realmId=? or n.realmId=0) and n.itemId=82800 and n.emailNotification=1 and u.validated=1",
						new BeanListHandler<UserItemNotification>(
								UserItemNotification.class), realmId, realmId);
	}
//	
//	public void save(User user) throws SQLException {
//		run.update(
//				"insert into t_user (openId,nickname,createTime) values (?,?,?)",
//				user.getOpenID(), user.getNickname(),
//				System.currentTimeMillis());
//	}
//
//	public void update(User user) throws SQLException {
//		run.update(
//				"update t_user set email=?, validated=?, nickname=? where id=?",
//				user.getEmail(), user.getValidated(), user.getNickname(),
//				user.getId());
//	}
//
//	public User getUserByOpenID(String openID) throws SQLException {
//		return run
//				.query("select id,openId,email,nickname,validated,expire from t_user where openId=?",
//						new BeanHandler<User>(User.class), openID);
//	}
//
//	public User getUserByID(int id) throws SQLException {
//		return run
//				.query("select id,openId,email,nickname,validated,expire from t_user where id=?",
//						new BeanHandler<User>(User.class), id);
//	}
//
//	public User getUserByToken(String token) throws SQLException {
//		return run
//				.query("select id,openId,email,nickname,validated,expire from t_user where token=?",
//						new BeanHandler<User>(User.class), token);
//	}
//
//	public User getUserByMail(String mail) throws SQLException {
//		return run
//				.query("select id,openId,email,nickname,validated,expire from t_user where email=?",
//						new BeanHandler<User>(User.class), mail);
//	}
//
//	public void addRealm(UserRealm realm) throws SQLException {
//		run.update("insert into t_user_realm (userId,realmId) values (?,?)",
//				realm.getUserId(), realm.getRealmId());
//	}
//
//	public void deleteRealm(UserRealm realm) throws SQLException {
//		run.update("delete from t_user_realm where userId=? and realmId=?",
//				realm.getUserId(), realm.getRealmId());
//	}
//
//	public List<UserRealm> getRealms(int userId) throws SQLException {
//		return run
//				.query("select userId,realmId,r.lastModified from t_user_realm ur join t_realm r on ur.realmId = r.id where userId=?",
//						new BeanListHandler<UserRealm>(UserRealm.class), userId);
//	}
//
//	public void addItemNotification(UserItemNotification item)
//			throws SQLException {
//		run.update(
//				"insert into t_user_item_notification (userId,realmId,itemId,petSpeciesId,petBreedId,bonusList,isInverted,price) values (?,?,?,?,?,?,?,?)",
//				item.getUserId(), item.getRealmId(), item.getItemId(),
//				item.getPetSpeciesId(), item.getPetBreedId(),
//				item.getBonusList(), item.getIsInverted(), item.getPrice());
//	}
//
//	public List<UserItemNotification> getItemNotifications(int userId)
//			throws SQLException {
//		List<UserItemNotification> items = run
//				.query("select userId,realmId,itemId,i.name as itemName,petSpeciesId,petBreedId,bonusList,i.itemLevel, isInverted,price,emailNotification from t_user_item_notification n join t_item i on n.itemId = i.id where n.userId=? and n.itemId != 82800 "
//						+ " union all select userId,realmId,82800,p.name as itemName,petSpeciesId,petBreedId,bonusList,0 as itemLevel, isInverted,price,emailNotification from t_user_item_notification n join t_pet p on n.petSpeciesId=p.id where n.userId=? and n.itemId = 82800 ",
//						new BeanListHandler<UserItemNotification>(
//								UserItemNotification.class), userId, userId);
//		for (UserItemNotification item : items) {
//			if (item.getItemId() == Pet.PET_ITEM_ID) {
//				item.setPetStats(run
//						.query("select speciesId,breedId,petQualityId,level,health,power,speed from t_pet_stats where speciesId=? and breedId=?",
//								new BeanHandler<PetStats>(PetStats.class),
//								item.getPetSpeciesId(), item.getPetBreedId()));
//			}
//		}
//		return items;
//	}
//
//
//
//	public void deleteItemNotifications(List<UserItemNotification> itemNs)
//			throws SQLException {
//		Object[][] params = new Object[itemNs.size()][7];
//		for (int i = 0; i < itemNs.size(); i++) {
//			UserItemNotification itemN = itemNs.get(i);
//			params[i][0] = itemN.getUserId();
//			params[i][1] = itemN.getRealmId();
//			params[i][2] = itemN.getItemId();
//			params[i][3] = itemN.getIsInverted();
//			params[i][4] = itemN.getPetSpeciesId();
//			params[i][5] = itemN.getPetBreedId();
//			params[i][6] = itemN.getBonusList();
//		}
//		run.batch(
//				"delete from t_user_item_notification where userId=? and realmId=? and itemId=? and isInverted=? and petSpeciesId=? and petBreedId=? and bonusList=?",
//				params);
//	}
//
//	public void updateItemNotification(UserItemNotification itemN)
//			throws SQLException {
//		run.update(
//				"update t_user_item_notification set price=? where userId=? and realmId=? and itemId=? and isInverted=? and petSpeciesId=? and petBreedId=? and bonusList=?",
//				itemN.getPrice(), itemN.getUserId(), itemN.getRealmId(),
//				itemN.getItemId(), itemN.getIsInverted(),
//				itemN.getPetSpeciesId(), itemN.getPetBreedId(),
//				itemN.getBonusList());
//	}
//
//	public void updateEmailNotifications(List<UserItemNotification> itemNs)
//			throws SQLException {
//		Object[][] params = new Object[itemNs.size()][8];
//		for (int i = 0; i < itemNs.size(); i++) {
//			UserItemNotification itemN = itemNs.get(i);
//			params[i][0] = itemN.getEmailNotification();
//			params[i][1] = itemN.getUserId();
//			params[i][2] = itemN.getRealmId();
//			params[i][3] = itemN.getItemId();
//			params[i][4] = itemN.getIsInverted();
//			params[i][5] = itemN.getPetSpeciesId();
//			params[i][6] = itemN.getPetBreedId();
//			params[i][7] = itemN.getBonusList();
//		}
//		run.batch(
//				"update t_user_item_notification set emailNotification=? where userId=? and realmId=? and itemId=? and isInverted=? and petSpeciesId=? and petBreedId=? and bonusList=?",
//				params);
//	}
//
//	public void addMailValidation(UserMailValidation userM) throws SQLException {
//		run.update(
//				"insert into t_user_mail_validation (userId,email,acode,expired) values (?,?,?,?)",
//				userM.getUserId(), userM.getEmail(), userM.getAcode(),
//				userM.getExpired());
//	}
//
//	public UserMailValidation getMailValidationById(int id) throws SQLException {
//		return run
//				.query("select userId,email,acode,expired from t_user_mail_validation where userId=?",
//						new BeanHandler<UserMailValidation>(
//								UserMailValidation.class), id);
//	}
//
//	public void deleteMailValidationById(int id) throws SQLException {
//		run.update("delete from t_user_mail_validation where userId=?", id);
//	}
//
//	public void updateMailValidationById(UserMailValidation userM)
//			throws SQLException {
//		run.update(
//				"update t_user_mail_validation set email=?,acode=?,expired=? where userId=?",
//				userM.getEmail(), userM.getAcode(), userM.getExpired(),
//				userM.getUserId());
//	}
//
//	public void updateUserToken(int id, String token) throws SQLException {
//		run.update("update t_user set token=? where id=?", token, id);
//	}
//
//	public void addCharacter(UserCharacter character) throws SQLException {
//		run.update(
//				"insert into t_user_character (userId,realmId,name) values (?,?,?)",
//				character.getUserId(), character.getRealmId(),
//				character.getName());
//	}
//
//	public void deleteCharacter(UserCharacter character) throws SQLException {
//		run.update(
//				"delete from t_user_character where userId=? and realmId=? and name=?",
//				character.getUserId(), character.getRealmId(),
//				character.getName());
//	}
//
//	public List<UserCharacter> getCharacters(int userId) throws SQLException {
//		return run
//				.query("select userId,realmId,name from t_user_character where userId=?",
//						new BeanListHandler<UserCharacter>(UserCharacter.class),
//						userId);
//	}
}
