package com.appiaries.demo.appiariesChat.models;

import java.util.HashMap;

import org.json.JSONObject;

import com.appiaries.demo.api.DatastoreAPI;
import com.appiaries.demo.appiariesChat.activities.BaseActivity;

/**
 * User model ユーザーモデル
 * 
 * @author PC Phase
 * 
 */
public class User extends JSONModel {
	/**
	 * Datastore collection name/Datastoreコレクション名
	 */
	protected static final String collection = "users";

	/**
	 * Key name for user name/ユーザー名のキー名
	 */
	public static final String nameKey = "nickname";

	/**
	 * Getter for name/ユーザー名の設定関数
	 * 
	 * @return User name/ユーザー名
	 */
	public String getName() {
		String nick = getStringProperty(nameKey);
		nick = nick.substring(1, 1);
		return getStringProperty(nameKey);
	}

	/**
	 * Setter for user name/ユーザー名の設定関数
	 * 
	 * @param name
	 *            User name to set/設定するユーザー名
	 */
	public void setName(String name) {
		setStringProperty(nameKey, name);
	}

	/**
	 * Key name of age/年齢層のキー名
	 */
	public static final String ageKey = "age_group";

	/**
	 * Getter for age/年齢層の取得関数
	 * 
	 * @return Age/年齢層
	 */
	public String getAge() {
		return getStringProperty(ageKey);
	}

	@Override
	protected void buildHashMap(HashMap<String, String> hashMap)
			throws Exception {
		super.buildHashMap(hashMap);
		hashMap.put(nameKey, getName());
		hashMap.put(ageKey, getAge());
	}

	/**
	 * Register myself to datastore collection/Datastoreコレクションに自分自身を登録する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @throws Exception
	 */
	public static void registerMyself(BaseActivity activity) throws Exception {
		User me = activity.getMe();
		JSONObject json = me.getJson();
		UserPreference s = activity.getMyPreference();

		if (s.getHideAge()) {
			json = new JSONObject(json.toString());
			json.remove(User.ageKey);
		}

		if (json.has("_id")) {
			activity.getAPI().getDatastore(collection)
				.putObject("/" + json.getString("_id"), json);
		} else {
			activity.getAPI().getDatastore(collection)
				.postObject("/_new", json);
		}
	}

	/**
	 * Find users from Datastore collection by array of Id/
	 * Idの配列を元にDatastoreコレクションからユーザーを検索する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param ids
	 *            Array of Id/Id配列
	 * @return Users object/Usersオブジェクト
	 * @throws Exception
	 */
	public static Users findUsersByIds(BaseActivity activity, String[] ids)
			throws Exception {
		Users users = new Users();
		String condition = "";
		for (String id : ids) {
			if (condition.length() > 0)
				condition += ",";
			condition += id;
		}
		condition = "_id.in." + condition;

		JSONObject json = activity.getAPI().getDatastore(collection)
				.simpleSearchObjects("/", new String[] { condition }, null);
		users.setJson(json);

		return users;
	}

	/**
	 * Get a user from Datastore collection/Datastoreコレクションからユーザーを取得する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param userId
	 *            User Id/ユーザーId
	 * @return User object/Userオブジェクト
	 * @throws Exception
	 */
	public static User lookupUser(BaseActivity activity, String userId)
			throws Exception {
		User user = new User();
		JSONObject json = activity.getAPI().getDatastore(collection)
				.lookupObject("/" + userId);
		user.setJson(json);

		return user;
	}

	/**
	 * Search users matches to condition from Datastore collection/
	 * 条件に合致するユーザーをDatastoreコレクションから検索する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param objectPath
	 *            Object path on collection/コレクション上のオブジェクトパス
	 * @param condition
	 *            Search condition/検索条件
	 * @param q
	 *            Search keyword/検索キーワード
	 * @return Users object/Usersオブジェクト
	 * @throws Exception
	 */
	protected static Users searchUsers(BaseActivity activity,
			String objectPath, String condition, String q) throws Exception {
		if (condition == null || condition.length() < 1)
			condition = "";
		if (q != null && q.length() > 0)
			condition += "nickname.sw." + q;

		Users users = new Users();
		DatastoreAPI ds = activity.getAPI().getDatastore(collection);
		JSONObject json = ds.simpleSearchObjects("/",
				condition.length() > 0 ? new String[] { condition } : null,
				null);
		users.setJson(json);

		return users;
	}

	/**
	 * Search all users from Datastore collection/Datastoreコレクションから全ユーザーを検索する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param q
	 *            Search keyword/検索キーワード
	 * @return Users object/Usersオブジェクト
	 * @throws Exception
	 */
	public static Users allUsers(BaseActivity activity, String q)
			throws Exception {
		return searchUsers(activity, "/", null, q);
	}

	/**
	 * Search my favorite users from Datastore collection/
	 * データストアコレクションから自分のお気に入りユーザーを検索する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @return
	 * @throws Exception
	 */
	public static Users searchMyFavoriteUsers(BaseActivity activity)
			throws Exception {
		UserPreference pref = activity.getMyPreference();
		return findUsersByIds(activity, pref.favoriteUserIds());
	}
}
