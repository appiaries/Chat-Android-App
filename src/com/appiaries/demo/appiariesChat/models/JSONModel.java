package com.appiaries.demo.appiariesChat.models;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.json.JSONObject;

import android.util.Log;

/**
 * Base model class wrapping Json Jsonデータをラッピングする規定モデルクラス
 * 
 * @author PC Phase
 * 
 */
public class JSONModel {
	/**
	 * Json key for id idを示すJsonキー
	 */
	public static final String idKey = "_id";

	/**
	 * Json key for owner id オーナーIDを示すJsonキー
	 */
	public static final String ownerIdKey = "_owner";

	/**
	 * Json key for created time stamp data 作成日タイムスタンプを示すJsonキー
	 */
	public static final String createdAtKey = "_cts";

	/**
	 * Date time format to display 表示用の時間フォーマット
	 */
	protected static SimpleDateFormat displayDateFormat = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	/**
	 * Raw json to wrap with this object オブジェクトでラッピングする生Jsonデータ
	 */
	protected JSONObject json = new JSONObject();

	/**
	 * Getter for raw Json 生Jsonデータの取得関数
	 * 
	 * @return The raw Json of this object/オブジェクトの生Json
	 */
	public JSONObject getJson() {
		return this.json;
	}

	/**
	 * Setter for raw Json 生Jsonデータの設定関数
	 * 
	 * @param json
	 *            A New raw Json of this object/このオブジェクトの新しい生Json
	 */
	public void setJson(JSONObject json) {
		this.json = json;
	}

	/**
	 * Getter for string value in json Jsonデータの文字列値の取得関数
	 * 
	 * @param name
	 *            Key name of data/データのキー名
	 * @return String value for the key, but empty if not
	 *         exists/キー名に対応する文字列値。存在しない場合は空文字列を返す
	 */
	protected String getStringProperty(String name) {
		return getStringProperty(name, "");
	}

	/**
	 * Getter for string value in Json Jsonデータの文字列値の取得関数
	 * 
	 * @param name
	 *            Key name of data/データのキー名
	 * @param ifEmpty
	 *            String value to return if not exists/存在しない場合に返す文字列値
	 * @return String value for the key/キー名に対応する文字列値
	 */
	protected String getStringProperty(String name, String ifEmpty) {
		if (json.has(name)) {
			try {
				String value = json.getString(name);
				return value;
			} catch (Exception ex) {
				Log.d("JSONModel", ex.getMessage());
			}
		}

		return ifEmpty;
	}

	/**
	 * Setter for string value in Json Jsonデータの文字列値の設定関数
	 * 
	 * @param name
	 *            Key name of data/データのキー名
	 * @param value
	 *            String value to set, if null remove the key and
	 *            value/設定する文字列値。nullの場合キーと値を削除する
	 */
	protected void setStringProperty(String name, String value) {
		if (value == null) {
			json.remove(name);
		} else {
			try {
				json.put(name, value);
			} catch (Exception ex) {
				Log.d("JSONModel", ex.getMessage());
			}
		}
	}

	/**
	 * Getter for Id Idの取得関数
	 * 
	 * @return Id of this object/このオブジェクトのId
	 */
	public String getId() {
		return getStringProperty(idKey);
	}

	/**
	 * Setter for Id Idの設定関数
	 * 
	 * @return Id of this object/このオブジェクトのId
	 */
	public void setId(String id) {
		setStringProperty(idKey, id);
	}

	/**
	 * Getter for owner Id オーナーIdの取得関数
	 * 
	 * @return Id of this object/このオブジェクトのId
	 */
	public String getOwnerId() {
		return getStringProperty(ownerIdKey);
	}

	/**
	 * Getter for time stamp that this object created on server サーバー上での作成日時の取得関数
	 * 
	 * @return Formatted date time/フォーマットされた日時
	 */
	public String getCreatedAt() {
		String createdAt = getStringProperty(createdAtKey);
		double d = Double.parseDouble(createdAt);
		Timestamp ts = new Timestamp((long) d);
		return displayDateFormat.format(ts);
	}

	/**
	 * Converter to string hash map to use listing 一覧表示で利用する文字列ハッシュマップへの変換関数
	 * 
	 * Override buildHashmap method in each class to customize mapping
	 * クラスごとにマッピングをカスタマイズするためにはbuildHashMapメソッドをオーバーライドする
	 * 
	 * @return String hash map of this object/文字列ハッシュマップ化されたオブジェクトデータ
	 */
	public HashMap<String, String> toHashMap() throws Exception {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		buildHashMap(hashMap);
		return hashMap;
	}

	/**
	 * Hash map builder must be overridden in each model class
	 * モデルクラスごとにオーバーライドするハッシュマップ構築関数
	 * 
	 * @param hashMap
	 *            Hash map to build/構築の対象となるハッシュマップ
	 * @throws Exception
	 */
	protected void buildHashMap(HashMap<String, String> hashMap)
			throws Exception {
		hashMap.put(idKey, getId());
		hashMap.put(createdAtKey, getCreatedAt());
		hashMap.put(ownerIdKey, getOwnerId());
	}

	/**
	 * Utility method to catenate two keys 2つのキーを連結するユーティリティ関数
	 * 
	 * @param key1
	 *            Key1/キー1
	 * @param key2
	 *            Key2/キー2
	 * @return Catenated key/連結されたキー
	 */
	public static String catKeys(String key1, String key2) {
		return key1 + "_" + key2;
	}
}
