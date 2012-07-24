package com.appiaries.demo.appiariesChat.models;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;

import com.appiaries.demo.api.TokenExpiresException;
import com.appiaries.demo.appiariesChat.activities.BaseActivity;

/**
 * Preference of user ユーザの個別設定
 * 
 * @author PC Phase
 * 
 */
public class UserPreference extends JSONModel {
	/**
	 * Collection name/コレクション名
	 */
	private static final String collection = "preferences";

	/**
	 * Constructor/コンストラクタ
	 */
	public UserPreference() throws Exception {
		super();
		JSONObject favoriteUsers = new JSONObject();
		json.put(favoriteUsersKey, favoriteUsers);
	}

	/**
	 * Key name of flag to hide age/年齢を非公開フラグのキー名
	 */
	private static final String hideAgeKey = "hideAge";

	/**
	 * Getter for flag to hide age/年齢を非公開フラグの取得関数
	 * 
	 * @return Current value of flag/現在のフラグ値
	 */
	public boolean getHideAge() {
		String bool = getStringProperty(hideAgeKey, null);
		return bool == null ? false : Boolean.valueOf(bool);
	}

	/**
	 * Setter for flag to hide age/年齢非公開フラグの設定関数
	 * 
	 * @param value
	 *            Value of flag to set/設定するフラグ値
	 */
	public void setHideAge(boolean value) {
		String str = String.valueOf(value);
		setStringProperty(hideAgeKey, str);
	}

	/**
	 * Key name of favorite users/お気に入りユーザのキー名
	 */
	private static final String favoriteUsersKey = "favoriteUsers";

	/**
	 * Get array of Id of favorite users/お気に入りユーザのId配列を取得する
	 * 
	 * @return Array of Id/Id配列
	 * @throws Exception
	 */
	public String[] favoriteUserIds() throws Exception {
		try {
			JSONObject favorites = json.getJSONObject(favoriteUsersKey);

			@SuppressWarnings("rawtypes")
			Iterator it = favorites.keys();
			ArrayList<String> ids = new ArrayList<String>();
			while (it.hasNext()) {
				String key = it.next().toString();
				if (favorites.getBoolean(key))
					ids.add(key);
			}
			return ids.toArray(new String[] {});
		} catch (Exception ex) {
			return new String[0];
		}
	}

	/**
	 * Check if a user if my favorite/指定ユーザが自分のお気に入りユーザかを判定する
	 * 
	 * @param user
	 *            User object to check/確認するUserオブジェクト
	 * @return True if my favorite/お気に入りだったら真
	 */
	public boolean isFavoriteUser(User user) {
		try {
			JSONObject favorites = json.getJSONObject(favoriteUsersKey);
			return favorites.getBoolean(user.getId());
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Set user to favorite or not/ユーザをお気に入りに登録/解除する
	 * 
	 * @param user
	 *            Target User object/対象となるUserオブジェクト
	 * @param addOrRemove
	 *            Add to or remove from favorites/登録か削除か
	 * @throws Exception
	 */
	public void setFavoriteUser(User user, boolean addOrRemove)
			throws Exception {
		JSONObject favorites = json.getJSONObject(favoriteUsersKey);
		favorites.put(user.getId(), addOrRemove);
	}

	/**
	 * Build my preference object path in Datastore collection/
	 * Datastoreコレクション上の自分の設定パスを構成する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param subPath
	 *            Sub path to inside object/オブジェクト内のサブパス
	 * @return Path string/パス文字列
	 * @throws Exception
	 */
	protected static String buildMyPreferencePath(BaseActivity activity,
			String subPath) throws Exception {
		String path = "/" + activity.getMe().getId();
		if (subPath != null && subPath.length() > 0)
			path += "/" + subPath;

		return path;
	}

	/**
	 * Load my preference from Datastore collection/
	 * Datastoreコレクションから自分の個別設定を取得する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @return UserPreference object/UserPreferenceオブジェクト
	 * @throws Exception
	 */
	public static UserPreference loadMyPreference(BaseActivity activity)
			throws Exception {
		UserPreference pref = new UserPreference();

		try {
			JSONObject json = activity.getAPI().getDatastore(collection)
					.lookupObject(buildMyPreferencePath(activity, null));
			pref.setJson(json);
		} catch (TokenExpiresException ex) {
			throw ex;
		} catch (Exception ex) {
			// Try to put new preference
			// 新しいプレファレンスを保存
			activity.getAPI()
					.getDatastore(collection)
					.putObject(buildMyPreferencePath(activity, null),
							pref.getJson());
		}

		return pref;
	}

	/**
	 * Patch my preference on Datastore　collection/
	 * Datastoreコレクション上の自分の個別設定を部分的に書き換える
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param subPath
	 *            Sub path in object/オブジェクト内のサブパス
	 * @param key
	 *            Key name to patch/書き換えるキー名
	 * @param value
	 *            Value to patch/置き換える値
	 * @throws Exception
	 */
	protected static void patchMyPreference(BaseActivity activity,
			String subPath, String key, String value) throws Exception {
		JSONObject json = new JSONObject();
		json.put(key, value);
		activity.getAPI().getDatastore(collection)
				.patchObject(buildMyPreferencePath(activity, subPath), json);
	}

	/**
	 * Save flag to hide age on Datastore collection/
	 * 年齢非公開フラグをDatastoreコレクション上に保存する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param pref
	 *            UserPreference object to save/保存するUserPreferenceオブジェクト
	 * @throws Exception
	 */
	public static void saveHideAge(BaseActivity activity, UserPreference pref)
			throws Exception {
		patchMyPreference(activity, null, hideAgeKey,
				String.valueOf(pref.getHideAge()));
	}

	/**
	 * Save state of favorite about a user to Datastore collection/
	 * ユーザのお気に入り登録状況をDatastoreコレクション上に保存する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param user
	 *            Target User object/対象となるUserオブジェクト
	 * @param addOrRemove
	 *            Add or remove/登録か削除か
	 * @throws Exception
	 */
	public static void saveFavoriteUserState(BaseActivity activity, User user,
			boolean addOrRemove) throws Exception {
		patchMyPreference(activity, favoriteUsersKey, user.getId(),
				String.valueOf(addOrRemove));
	}
}
