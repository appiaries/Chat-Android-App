package com.appiaries.demo.appiariesChat.activities;

import com.appiaries.demo.R;
import com.appiaries.demo.appiariesChat.models.User;
import com.appiaries.demo.appiariesChat.models.Users;

/**
 * Activity displays favorite users お気に入りユーザを表示するアクティビティ
 * 
 * @author PC Phase
 * 
 */
public class FavoriteUsersActivity extends UsersBaseActivity {
	@Override
	public int getMenuId() {
		return R.id.FavoriteUsers;
	}

	@Override
	public void start() {
		showActionForm = false;
		super.start();
	}

	@Override
	protected Users onQuery(String q) throws Exception {
		return User.searchMyFavoriteUsers(this);
	}
}
