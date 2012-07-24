package com.appiaries.demo.appiariesChat.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;

import com.appiaries.demo.R;
import com.appiaries.demo.appiariesChat.models.ChatMessage;
import com.appiaries.demo.appiariesChat.models.JSONModel;
import com.appiaries.demo.appiariesChat.models.Timeline;
import com.appiaries.demo.appiariesChat.models.User;

public abstract class TimelineBaseActivity extends ListBaseActivity {
	/**
	 * Timeline object to display on item list アイテムリスト上で表示を行うTimelineオブジェクト
	 */
	protected Timeline timeline = null;

	@Override
	public void start() {
		actionButtonLabel = getString(R.string.btn_post);
		actionButtonHint = getString(R.string.indicator_posting);

		super.start();
	}

	@Override
	protected BaseAdapter onRefresh() throws Exception {
		timeline = onRefreshTimeline();
		ArrayList<HashMap<String, String>> hashArray = timeline.toHashArray();
		SimpleAdapter sa = new SimpleAdapter(this, hashArray,
				R.layout.timeline_row, new String[] { ChatMessage.textKey,
						User.nameKey, JSONModel.createdAtKey }, new int[] {
						R.id.text, R.id.user_name, R.id.created_at });
		return sa;
	}

	/**
	 * Override on each sub class to handle event refresh timeline
	 * タイムラインの再読込イベントを処理するために各サブクラスで上書きする
	 * 
	 * @return Timeline object/Timelineオブジェクト
	 * @throws Exception
	 */
	protected abstract Timeline onRefreshTimeline() throws Exception;

	@Override
	protected boolean onAction(String text) throws Exception {
		onNewMessage(text);
		textAfterAction = "";
		return true;
	}

	/**
	 * Override on each sub class to handle event of new message
	 * 新しいメッセージの投稿イベントを処理するために各サブクラスで上書きする
	 * 
	 * @param text
	 * @throws Exception
	 */
	protected abstract void onNewMessage(String text) throws Exception;
}
