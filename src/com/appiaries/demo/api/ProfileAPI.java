package com.appiaries.demo.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * appiaries profile API accessor appiaries APIアクセスクラス
 * 
 * @author PC Phase
 * 
 */
public class ProfileAPI {
	/**
	 * Application Id/アプリID
	 */
	public static final String appId = "appiarieschat";

	/**
	 * Client Id/クライアントID
	 */
	public static final String clientId = "94bbb47997df41c";

	/**
	 * Contact path/契約パス
	 */
	public static final String contractPath = "_sandbox";

	/**
	 * OAuth request base URL/OAuthリクエストベースURL
	 */
	private static final String authRequestUrlBase = "https://api-oauth.appiaries.com/v1/auth?display=touch&response_type=token&state=";

	/**
	 * OAuth callback URL/OAuthコールバックURL
	 */
	private static final String authCallbackUrl = "http://callback/";

	/**
	 * Get OAuth request URL/OAuthリクエストURLの取得
	 * 
	 * @param scopes
	 *            Profile scope to request/リクエストするプロフィールスコープ
	 * @return Request URL/リクエストURL
	 */
	public static String getAuthUrl(String[] scopes) {
		String scope = "";
		for (String s : scopes) {
			if (scope.length() > 0)
				scope += " ";
			scope += s;
		}

		Uri url = Uri.parse(authRequestUrlBase).buildUpon()
				.appendQueryParameter("client_id", clientId)
				.appendQueryParameter("scope", scope).build();

		return url.toString();
	}

	/**
	 * Handler of OAuth callback/OAuthコールバックハンドラ
	 * 
	 * If logged in return new API object, but if not valid URL return null
	 * ログインに成功したら新しいAPIオブジェクトを返し、正しくないコールバックURLであればnullを返す
	 * 
	 * @param context
	 *            Current context/現在のコンテキスト
	 * @param callbackUrl
	 *            URL to check/チェックするURL
	 * @return A new API object or null/新しいAPIオブジェクトかnull
	 * @throws Exception
	 */
	public static ProfileAPI handleAuthCallback(Context context, String callbackUrl)
			throws Exception {
		String prefix = authCallbackUrl + "#";
		if (callbackUrl.length() < prefix.length()
				|| !callbackUrl.substring(0, prefix.length()).equals(prefix))
			return null;

		// Replace sharp to question mark to make easy to parse
		// 解析しやすいように#を?に置換
		Uri uri = Uri.parse(callbackUrl.replace('#', '?'));
		String accessToken = uri.getQueryParameter("access_token");
		String storeToken = uri.getQueryParameter("store_token");

		if (accessToken != null && accessToken.length() > 0
				&& storeToken != null && storeToken.length() > 0) {
			ProfileAPI profileAPI = new ProfileAPI(context, accessToken,
					storeToken);
			return profileAPI;
		} else {
			throw new Exception("Invalid callback params");
		}
	}

	/**
	 * Shared preferences storage key/共有設定ストレージキー
	 */
	private static final String storageKey = "com.appiaries.demo.api";

	/**
	 * Access token key name/アクセストークンキー名
	 */
	private static final String accessTokenKey = "accessToken";

	/**
	 * Store token key name/ストアトークンキー名
	 */
	private static final String storeTokenKey = "storeToken";

	/**
	 * Profile Json key name/プロフィールJsonキー名
	 */
	private static final String profileJsonKey = "profileJson";

	/**
	 * profile API base URL profile/APIベースURL
	 */
	protected static final String profileUrlBase = "https://api-profiles.appiaries.com/v1/";

	/**
	 * Current context object used to get shared preferences
	 * 共有設定ストレージを取得するための現在のコンテキストオブジェクト
	 */
	private Context context = null;

	/**
	 * Current access token/現在のアクセストークン
	 */
	private String accessToken = "";

	/**
	 * Current store token/現在のストアトークン
	 */
	private String storeToken = "";

	/**
	 * Current profile Json object/現在のプロフィールJsonオブジェクト
	 */
	private JSONObject myProfileJson = null;

	/**
	 * General constructor/通常のコンストラクタ
	 * 
	 * @param context
	 *            Context object/コンテキストオブジェクト
	 * @throws Exception
	 */
	public ProfileAPI(Context context) {
		this.context = context;
		loadCredential();
	}

	/**
	 * Constructor used after authentication/認証後に使用するコンストラクタ
	 * 
	 * @param context
	 *            Context/コンテキスト
	 * @param accessToken
	 *            Access token got via OAuth/OAuthで取得したアクセストークン
	 * @param storeToken
	 *            Store token got via OAuth/OAuthで取得したストアトークン
	 * @throws Exception
	 */
	public ProfileAPI(Context context, String accessToken, String storeToken)
			throws Exception {
		this.context = context;
		this.accessToken = accessToken;
		this.storeToken = storeToken;
		this.myProfileJson = requestMyProfile();
	}

	/**
	 * Getter for context/コンテキスト取得関数
	 * 
	 * @return Current context object/現在のコンテキストオブジェクト
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Getter for access token/アクセストークンの取得関数
	 * 
	 * @return Access token/アクセストークン
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * Getter for store token/ストアトークンの取得関数
	 * 
	 * @return Store token/ストアトークン
	 */
	public String getStoreToken() {
		return storeToken;
	}

	/**
	 * Getter for my profile json/自分自身のプロファイルJsonの取得関数
	 * 
	 * @return My profile json/自分のプロファイルJson
	 */
	public JSONObject getMyProfileJson() {
		return myProfileJson;
	}

	/**
	 * Request my profile json via API/自分のプロファイルJsonをAPIにリクエスト
	 * 
	 * @param force
	 *            Force to get/強制取得
	 * @return Json object/Jsonオブジェクト
	 * @throws Exception
	 */
	public JSONObject requestMyProfile() throws Exception {
		String url = profileUrlBase + contractPath + "/" + appId;
		HttpGet request = new HttpGet(url);
		request.addHeader("Authorization", "Bearer " + accessToken);
		return requestJson(request);
	}

	/**
	 * Check if logged in/ログイン判定
	 * 
	 * @return True if access token and store token is not
	 *         empty/アクセストークンとストアトークンが空でなければ真
	 */
	public boolean isLoggedIn() {
		return accessToken.length() > 0 && storeToken.length() > 0
				&& myProfileJson != null;
	}

	/**
	 * Getter for shared preferences/共有設定ストレージの取得関数
	 * 
	 * @return Shared preferences object/共有設定ストレージオブジェクト
	 */
	public SharedPreferences getPreferences() {
		return context.getSharedPreferences(storageKey, Context.MODE_PRIVATE);
	}

	/**
	 * Load credential from shared preferences/共有設定ストレージからの認証情報読み込み
	 */
	public void loadCredential() {
		SharedPreferences pref = getPreferences();

		accessToken = pref.getString(accessTokenKey, "");
		storeToken = pref.getString(storeTokenKey, "");

		String profile = pref.getString(profileJsonKey, "");
		if (profile != null && profile.length() > 0) {
			try {
				myProfileJson = new JSONObject(profile);
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Save credential to shared preferences/共有設定ストレージへの認証情報の保存
	 */
	public void saveCredential() {
		SharedPreferences.Editor editor = getPreferences().edit();

		editor.putString(accessTokenKey, accessToken);
		editor.putString(storeTokenKey, storeToken);

		if (myProfileJson == null) {
			editor.remove(profileJsonKey);
		} else {
			editor.putString(profileJsonKey, myProfileJson.toString());
		}

		editor.commit();
	}

	/**
	 * Logout/ログアウト
	 */
	public void logout() {
		// Remove all cookies assigned to this application/
		// このアプリのCookieをすべて削除
		CookieSyncManager.createInstance(getContext());
		CookieManager manager = CookieManager.getInstance();
		manager.removeAllCookie();
		CookieSyncManager.getInstance().sync();

		// Save empty tokens/トークンを空にして保存
		accessToken = "";
		storeToken = "";
		myProfileJson = null;
		saveCredential();
	}

	/**
	 * Send http request and return Json/HTTPリクエストの送信しJsonオブジェクトを返す
	 * 
	 * @param request
	 *            Request Request object/リクエストオブジェクト
	 * @return Json object/Jsonオブジェクト
	 * @throws Exception
	 */
	public JSONObject requestJson(HttpRequestBase request) throws Exception,
			TokenExpiresException {
		// Send request
		// リクエストの送信
		request.addHeader("Content-Type", "application/json");
		HttpClient client = new DefaultHttpClient();
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);

		HttpResponse response = client.execute(request);
		int code = response.getStatusLine().getStatusCode();
		if (code < 300) {
			// If status is success, convert response to Json
			// 正常ステータスであればレスポンスをJsonに変換
			if (response.getEntity() == null) {
				return null;
			}

			InputStream stream = response.getEntity().getContent();
			InputStreamReader streamReader = new InputStreamReader(stream);
			BufferedReader bufReader = new BufferedReader(streamReader);

			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = bufReader.readLine()) != null) {
				builder.append(line);
			}

			JSONObject json = new JSONObject(builder.toString());
			return json;
		} else if (401 == code || 403 == code) {
			// Logout if token expired, and fires TokenExpiresException
			// トークンの期限切れなら強制ログアウト、TokenExpiresExceptionを発火
			logout();
			throw new TokenExpiresException();
		} else {
			// Bad response
			// 不正なレスポンス
			throw new Exception("Bad http response: " + String.valueOf(code));
		}
	}

	/**
	 * Get datastore access object/データストアアクセスオブジェクトを取得
	 * 
	 * @return Datastore access object/データストアアクセスオブジェクト
	 */
	public DatastoreAPI getDatastore(String collection) {
		return new DatastoreAPI(this, collection);
	}
}
