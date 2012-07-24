package com.appiaries.demo.appiariesChat.activities;

import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.appiaries.demo.R;
import com.appiaries.demo.appiariesChat.models.User;
import com.appiaries.demo.appiariesChat.models.UserPreference;

/**
 * Activity of preference screen/設定画面アクティビティ
 * 
 * @author PC Phase
 *
 */
public class PreferenceActivity extends BaseActivity implements OnClickListener {
	/**
	 * Checkbox for hide age flag/年齢非表示フラグのチェックボックス
	 */
	CheckBox hideAge = null;

	@Override
	public void start() {
		setContentView(R.layout.preference);

		// User name and age
		// ユーザ名と年齢
		TextView userName = (TextView) findViewById(R.id.user_name);
		TextView age = (TextView) findViewById(R.id.age);
		
		userName.setText(getMe().getName());
		age.setText(getMe().getAge());

		// Flag to hide age
		// 年齢非表示フラグ
		hideAge = (CheckBox) findViewById(R.id.hide_age);
		try {
			hideAge.setChecked(getMyPreference().getHideAge());
		} catch (Exception ex) {
			handleException(ex);
		}
		hideAge.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Hide menu and allow only back
		// 戻るのみ許可するためメニューを隠す
		return false;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == hideAge.getId()) {
			runAsync(getString(R.string.indicator_saving), false,
					new Runnable() {
						@Override
						public void run() {
							try {
								// Save flag to hide age immediately
								// 年齢非表示フラグを即座に保存
								UserPreference pref = getMyPreference();
								pref.setHideAge(hideAge.isChecked());
								UserPreference.saveHideAge(
										PreferenceActivity.this, pref);

								// Register me to users collection again to
								// update
								// 自分自身をusersコレクションに再登録
								User.registerMyself(PreferenceActivity.this);
							} catch (Exception ex) {
								handleAsyncException(ex);
							}
						}
					});
		}
	}
}
