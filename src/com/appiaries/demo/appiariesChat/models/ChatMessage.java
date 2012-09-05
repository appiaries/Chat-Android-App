package com.appiaries.demo.appiariesChat.models;

import java.util.HashMap;

import org.json.JSONObject;

import com.appiaries.demo.appiariesChat.activities.BaseActivity;

/**
 * Chat message on timeline/タイムライン上のメッセージ
 * 
 * @author PC Phase
 *
 */
public class ChatMessage extends JSONModel {
	/**
	 * Datastore collection name/Datastoreコレクション名
	 */
	protected static final String collection = "timeline";

	/**
	 * Poster/投稿ユーザー
	 */
	private User user = null;

	/**
	 * Getter for poster/投稿ユーザーの取得関数
	 * 
	 * @return User object/ユーザーオブジェクト
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Setter for poster/投稿ユーザーの設定関数
	 * 
	 * @param user
	 *            User object/ユーザーオブジェクト
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Key name for message text/メッセージテキストのキー名
	 */
	public static final String textKey = "text";

	/**
	 * Getter for message text/メッセージテキストの取得関数
	 * 
	 * @return Message text/メッセージテキスト
	 */
	public String getText() {
		return getStringProperty(textKey);
	}

	/**
	 * Setter for message text/メッセージテキストの設定関数
	 * 
	 * @param text
	 *            Message text to set/設定するメッセージテキスト
	 */
	public void setText(String text) {
		setStringProperty(textKey, text);
	}

	@Override
	protected void buildHashMap(HashMap<String, String> hashMap)
			throws Exception {
		super.buildHashMap(hashMap);
		User user = getUser();
		if (user != null) {
			hashMap.put(User.nameKey, user.getName());
		}

		hashMap.put(textKey, getText());
	}

	/**
	 * Load and set users to messages on timeline/ユーザーを取得しタイムラインの各メッセージに設定する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param timeline
	 *            Timeline object/Timelineオブジェクト
	 * @return Timeline object/Timelineオブジェクト
	 * @throws Exception
	 */
	protected static Timeline setTimelineUsers(BaseActivity activity,
			Timeline timeline) throws Exception {
		String[] ids = timeline.uniqueOwnerIds();
		Users users = User.findUsersByIds(activity, ids);
		HashMap<String, User> dictionary = users.toDictionary();

		for (ChatMessage m : timeline.getObjects()) {
			if (dictionary.containsKey(m.getOwnerId())) {
				User user = dictionary.get(m.getOwnerId());
				if (user != null)
					m.setUser(user);
			}
		}

		return timeline;
	}

	/**
	 * Load recent timeline from datastore collection/
	 * 最新のタイムラインをDatastoreコレクションから読み込む
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @return Timeline object/Timelineオブジェクト
	 * @throws Exception
	 */
	public static Timeline recentTimeline(BaseActivity activity)
			throws Exception {
		Timeline timeline = new Timeline();
		JSONObject json = activity.getAPI().getDatastore(collection)
				.simpleSearchObjects("/", null, "-" + createdAtKey);
		timeline.setJson(json);

		return setTimelineUsers(activity, timeline);
	}

	/**
	 * Load user timeline from datastore collection/
	 * Datastoreコレクションからユーザータイムラインを読み込む
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param userId
	 *            User Id/ユーザーId
	 * @return Timeline object/Timelineオブジェクト
	 * @throws Exception
	 */
	public static Timeline userTimeline(BaseActivity activity, String userId)
			throws Exception {
		Timeline timeline = new Timeline();
		JSONObject json = activity
				.getAPI()
				.getDatastore(collection)
				.simpleSearchObjects("/",
						new String[] { "_owner.eq." + userId },
						"-" + createdAtKey);
		timeline.setJson(json);

		return setTimelineUsers(activity, timeline);
	}

	/**
	 * Post new message to Datastore collection/Datastoreコレクションにメッセージを投稿する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param chatMessage
	 *            ChatMessage object to post/投稿するChatMessageオブジェクト
	 * @throws Exception
	 */
	public static void postNewMessage(BaseActivity activity,
			ChatMessage chatMessage) throws Exception {
		JSONObject json = chatMessage.getJson();
		if (json.has("_id")) {
			activity.getAPI().getDatastore(collection)
				.putObject("/" + json.getString("_id"), json);
		} else {
			activity.getAPI().getDatastore(collection)
				.postObject("/_new", json);
		}
	}

	/**
	 * Delete message from Datastore collection/Datastoreコレクションからメッセージを削除する
	 * 
	 * @param activity
	 *            BaseActivity instance/BaseActivityインスタンス
	 * @param id
	 *            Message Id to delete/削除するメッセージId
	 * @throws Exception
	 */
	public static void deleteMessage(BaseActivity activity, String id)
			throws Exception {
		activity.getAPI().getDatastore(collection).deleteObject("/" + id);
	}
}
