package com.appiaries.demo.appiariesChat.models;

/**
 * Message timeline model/メッセージタイムラインモデル
 * 
 * @author PC Phase
 * 
 */
public class Timeline extends JSONCollection<ChatMessage> {
	/**
	 * Constructor/コンストラクタ
	 */
	public Timeline() {
		super(ChatMessage.class);
	}

}
