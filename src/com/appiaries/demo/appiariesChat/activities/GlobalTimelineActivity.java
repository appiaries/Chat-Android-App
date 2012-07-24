package com.appiaries.demo.appiariesChat.activities;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;

import com.appiaries.demo.R;
import com.appiaries.demo.appiariesChat.models.ChatMessage;
import com.appiaries.demo.appiariesChat.models.JSONModel;
import com.appiaries.demo.appiariesChat.models.Timeline;

/**
 * Activity displays global timeline/全体のタイムラインを表示するアクティビティ
 * 
 * @author PC Phase
 * 
 */
public class GlobalTimelineActivity extends TimelineBaseActivity implements
		DialogInterface.OnClickListener {
	protected AlertDialog alertToDelete = null;
	protected String messageIdToDelete;

	@Override
	public int getMenuId() {
		return R.id.GlobalTimeline;
	}

	@Override
	public void start() {
		super.start();
	}

	@Override
	protected void onNewMessage(String text) throws Exception {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setText(text);
		chatMessage.setUser(getMe());

		ChatMessage.postNewMessage(this, chatMessage);
	}

	@Override
	protected Timeline onRefreshTimeline() throws Exception {
		return ChatMessage.recentTimeline(this);
	}

	@Override
	protected void onListItemClicked(HashMap<String, String> hash)
			throws Exception {
		String ownerId = hash.get(JSONModel.ownerIdKey);
		if (ownerId == null) {
			throw new Exception("Owner Id Unknown");
		}

		String messageId = hash.get(JSONModel.idKey);

		if (ownerId.equals(getMe().getId())) {
			// If message selected is mine, confirm to delete it
			// もし選択したメッセージが自分のメッセージであれば削除の確認をする
			if (alertToDelete == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.title_this_is_your_message);
				builder.setMessage(R.string.msg_confirm_delete);
				builder.setPositiveButton("OK", this);
				builder.setNegativeButton("NO", null);
				alertToDelete = builder.create();
			}

			messageIdToDelete = messageId;
			alertToDelete.show();

		} else {
			// If message selected is not mine, move to the user timeline
			// もし選択したメッセージが自分のメッセージでなければユーザータイムラインに遷移する
			startUserTimeline(ownerId);
		}
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (dialog == alertToDelete) {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(android.os.Message msg) {
					refresh();
				}
			};

			runAsync(getString(R.string.indicator_deleting), true,
					new Runnable() {
						@Override
						public void run() {
							try {
								ChatMessage.deleteMessage(
										GlobalTimelineActivity.this,
										messageIdToDelete);
								handler.sendEmptyMessage(0);
							} catch (Exception ex) {
								handleAsyncException(ex);
							}
						}
					});
		}
	}
}
