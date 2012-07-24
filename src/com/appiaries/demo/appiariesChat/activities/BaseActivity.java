package com.appiaries.demo.appiariesChat.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.appiaries.demo.R;
import com.appiaries.demo.api.ProfileAPI;
import com.appiaries.demo.api.TokenExpiresException;
import com.appiaries.demo.appiariesChat.models.User;
import com.appiaries.demo.appiariesChat.models.UserPreference;

/**
 * Base class for all activities require authentication
 * 認証を必要とするすべてのアクティビティの基底クラス
 * 
 * @author PC Phase
 * 
 */
@SuppressLint("HandlerLeak")
public abstract class BaseActivity extends Activity {
	/**
	 * Common length to display toast 共通のトースト表示時間
	 */
	public static final int commonToastDuration = 5000;

	/**
	 * Returns menu Id to hide menu to current activity
	 * 現在のアクティビティへのメニューを隠すためメニューのIDを返す
	 * 
	 * Override on each sub class if required 必要があれば各サブクラスで上書きする
	 * 
	 * @return Menu Id/メニューId
	 */
	public int getMenuId() {
		return 0;
	}

	/**
	 * Instance of profile API accessor profile APIインスタンス
	 */
	private ProfileAPI profileAPI = null;

	/**
	 * Getter for profile API accessor profile APIインスタンスの取得関数
	 * 
	 * @return profile API instance/profile APIインスタンス
	 */
	public ProfileAPI getAPI() {
		if (profileAPI == null) {
			profileAPI = new ProfileAPI(this);
		}

		return profileAPI;
	}

	/**
	 * User object of mine 自分自身を示すユーザオブジェクト
	 */
	protected User me = null;

	/**
	 * Preference of mine 自分自身のプレファレンス
	 */
	protected UserPreference myPreference = null;

	/**
	 * Getter for user object of mine 自分自身を示すユーザオブジェクトの取得関数
	 * 
	 * @return User object/ユーザオブジェクト
	 * @throws Exception
	 */
	public User getMe() {
		ProfileAPI api = getAPI();
		try {
			if (me == null && api.isLoggedIn()) {
				me = new User();
				me.setJson(api.getMyProfileJson());
			}
		} catch (Exception ex) {
			handleException(ex);
		}

		return me;
	}

	/**
	 * Getter for my preference 自分のプレファレンスの取得関数
	 * 
	 * @return Preference/プレファレンス
	 * @throws Exception
	 */
	public UserPreference getMyPreference() throws Exception {
		if (myPreference == null)
			myPreference = UserPreference.loadMyPreference(this);

		return myPreference;
	}

	/**
	 * Threads shared dialog to indicate processing or loading
	 * 処理中・読み込み中を示すスレッド間共通ダイアログ
	 */
	private ProgressDialog loadingIndicator = null;

	public ProgressDialog getLoadingIndicator() {
		if (loadingIndicator == null) {
			loadingIndicator = new ProgressDialog(this);
			loadingIndicator.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			loadingIndicator.setMessage(getString(R.string.indicator_loading));
			loadingIndicator.setCancelable(true);
		}

		return loadingIndicator;
	}

	/**
	 * Handler to receive exception from another thread
	 * 他スレッドからの例外をメインスレッドで受け取るためのハンドラ
	 */
	private Handler asyncExceptionHandler = null;

	/**
	 * Getter for inter-threads exception handler スレッド間例外ハンドラの取得関数
	 * 
	 * @return Handler object/ハンドラオブジェクト
	 */
	public Handler getAsyncExceptionHandler() {
		if (asyncExceptionHandler == null) {
			asyncExceptionHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// Dismiss loading indicator
					// 読み込み中ダイアログを閉じる
					getLoadingIndicator().dismiss();

					// Display login screen if token expried
					// トークン期限切れであればログイン画面を表示する
					Exception ex = (Exception) msg.obj;
					if (ex instanceof TokenExpiresException)
						login(true);
					else
						handleException(ex);
				}
			};

		}

		return asyncExceptionHandler;
	}

	/**
	 * Proxy to handle exception from another thread via handler
	 * ハンドラ経由で他のスレッドでの例外を処理するプロキシ
	 * 
	 * @param ex
	 *            Exception object/例外オブジェクト
	 */
	public void handleAsyncException(Exception ex) {
		Message msg = new Message();
		msg.obj = ex;
		getAsyncExceptionHandler().sendMessage(msg);
	}

	/**
	 * Shared exception handler 共通例外処理関数
	 * 
	 * @param ex
	 *            Exception object/例外オブジェクト
	 */
	public void handleException(Exception ex) {
		Toast.makeText(this, ex.getMessage(), commonToastDuration).show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// If logged in start main process, but display login screen
		// ログイン済みであればメイン処理を開始し、未ログインであればログイン画面を表示
		if (getAPI().isLoggedIn())
			start();
		else
			login(false);
	}

	/**
	 * Implement main process instead of onCreate on each sub class
	 * 各サブクラスでonCreateに相当するメイン処理を記述する
	 */
	public abstract void start();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.base, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Hide current menu
		// 現在のメニュー項目を非表示にする
		MenuItem item = menu.findItem(this.getMenuId());
		if (item != null)
			item.setVisible(false);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;

		// Main menu: dispatch to activities
		// メインメニュー: 各アクティビティに分岐
		switch (item.getItemId()) {
		case R.id.GlobalTimeline:
			intent = new Intent(this, GlobalTimelineActivity.class);
			break;
		case R.id.AllUsers:
			intent = new Intent(this, AllUsersActivity.class);
			break;
		case R.id.FavoriteUsers:
			intent = new Intent(this, FavoriteUsersActivity.class);
			break;
		case R.id.Preference:
			intent = new Intent(this, PreferenceActivity.class);
			break;
		case R.id.Logout:
			logout();
			break;
		case R.id.MyTimeline:
			startUserTimeline(me.getId());
			return true;
		}

		if (intent != null) {
			startActivity(intent);
			return true;
		}

		return false;
	}

	/**
	 * Display login screen ログイン画面の表示
	 * 
	 * @param becouseExpired
	 *            Reason is token expired/理由がトークン期限切れによるものか
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void login(boolean becouseExpired) {
		if (becouseExpired) {
			Toast.makeText(this, R.string.msg_token_expired,
					commonToastDuration).show();
		}

		final WebView wv = new WebView(this);
		wv.getSettings().setJavaScriptEnabled(true);
		setContentView(wv);

		// Get OAuth URL from Profile API class
		// Profile APIクラスからOAuth認証URLを取得
		String authUrl = ProfileAPI
				.getAuthUrl(new String[] { "nickname", "age" });

		wv.loadUrl(authUrl);
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				BaseActivity activity = BaseActivity.this;
				try {
					// Check for each URL to display if a correct callback URL
					// 表示されようとするURLが正しいコールバックURLかを確認
					ProfileAPI auth = ProfileAPI.authCallback(activity, url);
					if (auth != null) {
						Toast.makeText(activity,
								getString(R.string.msg_logged_in),
								commonToastDuration).show();

						profileAPI = auth;
						User.registerMyself(activity);

						// All login process done, save credential
						// すべてのログイン処理が正常に完了したので資格情報を保存
						auth.saveCredential();
						activity.start();
						wv.destroy();
					}
				} catch (Exception ex) {
					activity.handleException(ex);
				}
			}
		});
	}

	/**
	 * Logout ログアウト
	 * 
	 * Remove cookies and shared preference, and display login screen
	 * Cookieや共通設定ストレージを削除してログイン画面を再表示する
	 */
	public void logout() {
		CookieSyncManager.createInstance(this);
		CookieManager manager = CookieManager.getInstance();
		manager.removeAllCookie();
		CookieSyncManager.getInstance().sync();

		getAPI().logout();
		Toast.makeText(this, getString(R.string.msg_logged_out),
				commonToastDuration).show();
		login(false);
	}

	/**
	 * Async process runner 非同期処理実行関数
	 * 
	 * Display loading indicator and run process passed
	 * 読み込み中ダイアログを表示し、指定の処理を実行する
	 * 
	 * @param message
	 *            Message to display on loading indicator/読み込み中ダイアログに表示するメッセージ
	 * @param stayLoading
	 *            If stay loading indicator on
	 *            finished/終了時に読み込み中ダイアログを表示したままにするか
	 * @param proc
	 *            Process to run/実行する処理
	 */
	public void runAsync(String message, final boolean stayLoading,
			final Runnable proc) {
		final ProgressDialog indicator = getLoadingIndicator();
		final boolean stay = message == null || message.length() < 1? false: stayLoading;

		final Handler stopHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (!stay) {
					indicator.dismiss();
				}
			}
		};

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				proc.run();
				stopHandler.sendEmptyMessage(0);
			}
		});

		if ( message != null && message.length() > 0 ) {
			indicator.setMessage(message);
			indicator.show();
		}

		t.start();
	}

	/**
	 * Starter user timeline activity ユーザタイムラインアクティビティの開始
	 * 
	 * @param userId
	 *            Target user Id/対象のユーザId
	 */
	public void startUserTimeline(String userId) {
		Intent intent = new Intent(this, UserTimelineActivity.class);
		intent.setAction(userId);

		startActivity(intent);
	}
}
