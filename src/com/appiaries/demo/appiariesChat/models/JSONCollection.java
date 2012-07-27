package com.appiaries.demo.appiariesChat.models;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

/**
 * Collection model wrapping Json/Jsonをラップするコレクションモデル
 * 
 * @author PC Phase
 * 
 * @param <T>
 *            Class of collection elements/コレクション要素のクラス
 */
public class JSONCollection<T extends JSONModel> extends JSONModel {

	/**
	 * Class of collection elements: Used to create Java object
	 * コレクション要素のクラス(Javaオブジェクト作成に使用)
	 */
	protected Class<T> modelClass;

	/**
	 * Key name of objects array/オブジェクト配列のキー名
	 */
	protected String objectsKey = "_objs";

	/**
	 * コンストラクタ/Constructor
	 * 
	 * @param c
	 *            Java class of <T>/<T>に指定するJavaクラス
	 */
	public JSONCollection(Class<T> c) {
		modelClass = c;
	}

	/**
	 * Count of all/全データ件数
	 */
	private int count = 0;

	/**
	 * Getter for count of all/全データ件数の取得関数
	 * 
	 * @return Count of all/全データ件数
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 * Setter for count of all/全データ件数の設定関数
	 * 
	 * @param count
	 *            Count of all/全データ件数
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Array list of model Java objects cache/モデルJavaオブジェクトのキャッシュ配列リスト
	 */
	protected ArrayList<T> objects = null;

	/**
	 * Override setJson to refresh Java objects cache
	 * JavaオブジェクトキャッシュをリフレッシュするためにsetJsonを上書き
	 * 
	 * @param json
	 *            Json data/Jsonデータ
	 */
	@Override
	public void setJson(JSONObject json) {
		objects = null;
		super.setJson(json);
	}

	/**
	 * Converter to Java object's array list/Javaオブジェクト配列リストへの変換関数
	 * 
	 * @return
	 */
	public ArrayList<T> getObjects() {
		// Return if stored
		// 保存済みであればそれを返す
		if (objects != null)
			return objects;

		// Build Java object's array list from Json array
		// Json配列からJavaオブジェクトの配列リストを構築
		objects = new ArrayList<T>();
		if (json.has(this.objectsKey)) {
			try {
				JSONArray array = json.getJSONArray(this.objectsKey);
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					if (object != null) {
						T model = modelClass.newInstance();
						model.setJson(object);
						objects.add(model);
					}
				}
			} catch (Exception ex) {
				Log.d("JSONCollection", ex.getMessage());
			}
		}

		return objects;
	}

	/**
	 * Converter to array of string hash map for listing/
	 * 一覧表示に利用する文字列ハッシュマップ配列への変換関数
	 * 
	 * @return Array of string hash map/文字列ハッシュマップ配列
	 */
	public ArrayList<HashMap<String, String>> toHashArray() throws Exception {
		ArrayList<HashMap<String, String>> hashArray = new ArrayList<HashMap<String, String>>();
		for (T object : getObjects()) {
			hashArray.add(object.toHashMap());
		}
		return hashArray;
	}

	/**
	 * Convert to hash map dictionary keyed by Id/Idをキーとしたハッシュマップ辞書への変換
	 * 
	 * @return　Hash map dictionary/ハッシュマップ辞書
	 * @throws Exception
	 */
	public HashMap<String, T> toDictionary() throws Exception {
		HashMap<String, T> dictionary = new HashMap<String, T>();
		for (T object : getObjects()) {
			dictionary.put(object.getId(), object);
		}
		return dictionary;
	}

	/**
	 * Get unique owner Id array/オーナーIDのユニーク配列を取得
	 * 
	 * @return Unique owner id array/ユニークなオーナーIDの配列
	 */
	public String[] uniqueOwnerIds() {
		ArrayList<String> ids = new ArrayList<String>();
		for (T object : getObjects()) {
			String ownerId = object.getOwnerId();
			if (ownerId.length() > 0 && !ids.contains(ownerId))
				ids.add(ownerId);
		}

		return ids.toArray(new String[] {});
	}
}
