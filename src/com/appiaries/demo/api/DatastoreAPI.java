package com.appiaries.demo.api;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Iterator;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.net.Uri;

/**
 * Accessor for Datastore API Datastore APIアクセスクラス
 * 
 * @author PC Phase
 * 
 */
public class DatastoreAPI {
	/**
	 * Datastore API base URL/DatastoreAPIのベースURL
	 */
	private static final String dataUrlBase = "https://api-datastore.appiaries.com/v1/dat/";

	/**
	 * Encoding(UTF-8)/文字コード(UTF-8)
	 */
	protected static final String defaultEncoding = "UTF-8";

	/**
	 * Token header name/トークン用ヘッダ名
	 */
	protected static final String tokenHeader = "X-Appiaries-Token";

	/**
	 * Profile API instance sharing credential/資格情報を共有するプロファイルAPIインスタンス
	 */
	private ProfileAPI profileAPI = null;

	/**
	 * Collection path name/コレクションパス名
	 */
	private String collection = null;

	/**
	 * Constructor/コンストラクタ
	 * 
	 * @param profileAPI
	 *            Profile API sharing credential/資格情報を共有するProfile API
	 * @param collection
	 *            Collection path/コレクションパス
	 */
	public DatastoreAPI(ProfileAPI profileAPI, String collection) {
		this.profileAPI = profileAPI;
		this.collection = collection;
	}

	/**
	 * Getter for profile API/プロファイルAPIの取得関数
	 * 
	 * @return Profile API instance/プロファイルAPIインスタンス
	 */
	public ProfileAPI getProfileAPI() {
		return profileAPI;
	}

	/**
	 * Bulder of object URL on datastore/datastore上のオブジェクトURLを構築する
	 * 
	 * @param collection
	 *            Collection name/コレクション名
	 * @param objectPath
	 *            Object path if null or empty, use
	 *            '_new'/オブジェクトパス。nullまたは空の場合は_newを代用する
	 * @return Object URL/オブジェクトURL
	 */
	public Uri buildObjectUrl(String objectPath) {
		if (objectPath == null || objectPath.length() < 1)
			objectPath = "/_new";

		return Uri.parse(dataUrlBase + ProfileAPI.contractPath + "/"
				+ ProfileAPI.appId + "/" + collection + objectPath);
	}

	/**
	 * Send http request and return Json/HTTPリクエストの送信しJsonオブジェクトを返す
	 * 
	 * Add store token instead of access token to header
	 * ヘッダにはアクセストークンの代わりにストアトークンを追加する
	 * 
	 * @param request
	 *            Request Request object/リクエストオブジェクト
	 * @return Json object/Jsonオブジェクト
	 * @throws Exception
	 */
	public JSONObject requestJson(HttpRequestBase request) throws Exception {
		// Add token to headder if not set
		// ヘッダにトークンが設定されていなければ追加
		if (request.getHeaders(tokenHeader).length < 1
				&& profileAPI.getStoreToken().length() > 0) {
			request.addHeader(tokenHeader, profileAPI.getStoreToken());
		}

		return profileAPI.requestJson(request);
	}

	/**
	 * Look up Json object in collection/Datastoreコレクション上のJsonオブジェクト1件を参照する
	 * 
	 * @param objectPath
	 *            Object path/オブジェクトパス
	 * @return Json object/Jsonオブジェクト
	 * @throws Exception
	 */
	public JSONObject lookupObject(String objectPath) throws Exception {
		HttpGet request = new HttpGet(buildObjectUrl(objectPath).toString());
		return requestJson(request);
	}

	/**
	 * Post Json object to datastore collection/DatastoreコレクションにJsonオブジェクトをPUTする
	 *
	 * @param objectPath
	 *            Object path/オブジェクトパス
	 * @param json
	 *            Json object to put/PUTするJsonオブジェクト
	 * @return Json object echoed back/エコーバックされたJsonオブジェクト
	 * @throws Exception
	 */
	public JSONObject postObject(String objectPath, JSONObject json)
			throws Exception {
		// Append get=true to echo json
		// エコーバックを受けるためget=trueパラメータを追加
		String url = buildObjectUrl(objectPath).buildUpon()
				.appendQueryParameter("get", "true").toString();
		HttpPost request = new HttpPost(url);
		request.setEntity(new StringEntity(json.toString(), defaultEncoding));

		return requestJson(request);
	}

	/**
	 * Put Json object to datastore collection/DatastoreコレクションにJsonオブジェクトをPUTする
	 * 
	 * @param objectPath
	 *            Object path/オブジェクトパス
	 * @param json
	 *            Json object to put/PUTするJsonオブジェクト
	 * @return Json object echoed back/エコーバックされたJsonオブジェクト
	 * @throws Exception
	 */
	public JSONObject putObject(String objectPath, JSONObject json)
			throws Exception {
		// Append get=true to echo json
		// エコーバックを受けるためget=trueパラメータを追加
		String url = buildObjectUrl(objectPath).buildUpon()
				.appendQueryParameter("get", "true").toString();

		HttpPut request = new HttpPut(url);
		request.setEntity(new StringEntity(json.toString(), defaultEncoding));

		return requestJson(request);
	}

	/**
	 * Patch Json to datastore object/Datastore上のオブジェクトを部分的に変更する
	 * 
	 * @param objectPath
	 *            Object path/オブジェクトパス
	 * @param json
	 *            Json data/Jsonデータ
	 * @return Result of patch/パッチ処理の結果
	 * @throws Exception
	 */
	public JSONObject patchObject(String objectPath, JSONObject json)
			throws Exception {
		// Append get=true to echo json
		// エコーバックを受けるためget=trueパラメータを追加
		String url = buildObjectUrl(objectPath).buildUpon()
				.appendQueryParameter("get", "true")
				.appendQueryParameter("proc", "patch").toString();

		// Use HttpPost with proc=patch instead of HttpPatch
		// HttpPatchがないためproc=patchを付加したPostで代用
		HttpPost request = new HttpPost(url);

		JSONObject patch = new JSONObject(json.toString());
		@SuppressWarnings("rawtypes")
		Iterator i = patch.keys();
		while (i.hasNext()) {
			String key = i.next().toString();
			if (key.substring(0, 1).equals("_")) {
				i.remove();
			}
		}
		request.setEntity(new StringEntity(patch.toString(), defaultEncoding));

		return requestJson(request);
	}

	/**
	 * Delete Json object on datastore collection/
	 * Datastoreコレクション上のJsonオブジェクトを削除する
	 * 
	 * @param objectPath
	 *            Object path/オブジェクトパス
	 * @return Response Json/レスポンスJson
	 * @throws Exception
	 */
	public JSONObject deleteObject(String objectPath) throws Exception {
		HttpDelete request = new HttpDelete();
		request.setURI(new URI(buildObjectUrl(objectPath).toString()));
		return requestJson(request);
	}

	/**
	 * Search Json objects on datastore collection/
	 * Datastoreコレクション上のJsonオブジェクトを検索する
	 * 
	 * @param objectPath
	 *            Object path/オブジェクトパス
	 * @param from
	 *            Object index start from/開始インデックス
	 * @param to
	 *            Object index end to/終了インデックス
	 * @param conditions
	 *            Condition expressions/条件式
	 * @return Response Json/レスポンスJson
	 * @throws Exception
	 */
	public JSONObject searchObjects(String objectPath, int from, int to,
			String[] conditions, String sortBy) throws Exception {
		if (objectPath.substring(objectPath.length() - 1, 1) != "/")
			objectPath += "/";

		if (from > 0)
			objectPath += String.valueOf(from);
		objectPath += "-";
		if (to > 0)
			objectPath += String.valueOf(to);

		if (conditions != null) {
			for (String c : conditions) {
				objectPath += ";" + URLEncoder.encode(c, defaultEncoding);
			}
		}

		Uri uri = buildObjectUrl(objectPath);
		if (sortBy != null && sortBy.length() > 0) {
			uri = uri.buildUpon().appendQueryParameter("order", sortBy).build();
		}

		HttpGet request = new HttpGet(uri.toString());
		return requestJson(request);
	}

	/**
	 * Search Json objects on datastore collection in short handed/
	 * Datastoreコレクション上のJsonオブジェクトを簡略形で検索する
	 * 
	 * @param objectPath
	 *            Object path/オブジェクトパス
	 * @param condition
	 *            Condition express/条件式
	 * @return Response Json/レスポンスJson
	 * @throws Exception
	 */
	public JSONObject simpleSearchObjects(String objectPath,
			String[] conditions, String sortBy) throws Exception {
		return searchObjects(objectPath, 0, 0, conditions, sortBy);
	}
}
