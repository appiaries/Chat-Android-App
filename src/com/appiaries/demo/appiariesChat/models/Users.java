package com.appiaries.demo.appiariesChat.models;

/**
 * User collection model/ユーザーコレクションモデル
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
