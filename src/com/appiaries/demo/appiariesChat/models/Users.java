package com.appiaries.demo.appiariesChat.models;

/**
 * User collection model/ユーザコレクションモデル
 * 
 * @author PC Phase
 * 
 */

public class Users extends JSONCollection<User> {
	/**
	 * Constructor/コンストラクタ
	 */
	public Users() {
		super(User.class);
	}
}
