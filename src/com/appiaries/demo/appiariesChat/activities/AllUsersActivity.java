package com.appiaries.demo.appiariesChat.activities;

import com.appiaries.demo.R;
import com.appiaries.demo.appiariesChat.models.User;
import com.appiaries.demo.appiariesChat.models.Users;

/**
 * Activity to display all users/全ユーザーを表示するアクティビティ
 * 
 * @author PC Phase
 * 
 */
public class AllUsersActivity extends UsersBaseActivity {
	@Override
	public int getMenuId() {
		return R.id.AllUsers;
	}

	@Override
	protected Users onQuery(String q) throws Exception {
		return User.allUsers(this, q);
	}
}
