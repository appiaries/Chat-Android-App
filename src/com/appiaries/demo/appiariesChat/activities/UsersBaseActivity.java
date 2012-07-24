package com.appiaries.demo.appiariesChat.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;

import com.appiaries.demo.R;
import com.appiaries.demo.appiariesChat.models.JSONModel;
import com.appiaries.demo.appiariesChat.models.User;
import com.appiaries.demo.appiariesChat.models.Users;

/**
 * Activity base class to list users/ユーザ一覧を表示するための基底アクティビティクラス
 * 
 * @author PC Phase
 *
 */
public abstract class UsersBaseActivity extends ListBaseActivity {
	/**
	 * Current users object to display item list/アイテムリスト上に表示する現在のUsersオブジェクト
	 */
	protected Users users = null;

	/**
	 * Search query keyword/検索キーワード
	 */
	protected String query = "";

	@Override
	public void start() {
		actionButtonLabel = getString(R.string.btn_search);
		actionButtonHint = getString(R.string.indicator_searching);
		super.start();
	}

	@Override
	protected BaseAdapter onRefresh() throws Exception {
		users = onQuery(query);
		ArrayList<HashMap<String, String>> hashArray = users.toHashArray();
		SimpleAdapter sa = new SimpleAdapter(this, hashArray,
				R.layout.user_row, new String[] { User.nameKey, User.ageKey },
				new int[] { R.id.user_name, R.id.age });

		return sa;
	}

	/**
	 * Override on each sub class to handle event query users/
	 * ユーザー検索イベントを処理するため各サブクラスで上書きする
	 * 
	 * @param q
	 *            Query keyword/検索キーワード
	 * @return Users object/Usersオブジェクト
	 * @throws Exception
	 */
	protected abstract Users onQuery(String q) throws Exception;

	@Override
	protected boolean onAction(String text) throws Exception {
		// Set query keyword and refresh
		// 検索キーワードを設定して再読込
		query = text;
		return true;
	}

	@Override
	protected void onListItemClicked(HashMap<String, String> hash)
			throws Exception {
		String userId = hash.get(JSONModel.idKey);
		if (userId == null) {
			throw new Exception("User Id Unknown");
		}

		// Start the user's timeline activity
		// そのユーザのタイムラインアクティビティを開始
		startUserTimeline(userId);
	}

}
