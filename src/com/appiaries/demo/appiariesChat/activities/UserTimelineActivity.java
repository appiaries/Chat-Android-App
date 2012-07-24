package com.appiaries.demo.appiariesChat.activities;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.appiaries.demo.R;
import com.appiaries.demo.appiariesChat.models.ChatMessage;
import com.appiaries.demo.appiariesChat.models.Timeline;
import com.appiaries.demo.appiariesChat.models.User;
import com.appiaries.demo.appiariesChat.models.UserPreference;

/**
 * Timeline activity about a user/ユーザひとりのタイムラインを表示するアクティビティ
 * 
 * @author PC Phase
 *
 */
@SuppressLint("HandlerLeak")
public class UserTimelineActivity extends GlobalTimelineActivity {
	protected String ownerId;
	protected User user;

	protected boolean isMyFavorite() {
		try {
			return getMyPreference().isFavoriteUser(user);
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public int getMenuId() {
		return isMyTimeline() ? R.id.MyTimeline : 0;
	}

	@Override
	public void start() {
		showActionForm = false;
		ownerId = getIntent().getAction();

		super.start();

		if (isMyTimeline()) {
			showActionForm = true;
			actionForm.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.user_timeline, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isMyTimeline()) {
			menu.findItem(R.id.AddToFavorites).setVisible(false);
			menu.findItem(R.id.RemoveFromFavorites).setVisible(false);
		} else {
			menu.findItem(R.id.AddToFavorites).setVisible(!isMyFavorite());
			menu.findItem(R.id.RemoveFromFavorites).setVisible(isMyFavorite());
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.AddToFavorites) {
			changeFavoriteStatus(true);
			return true;
		} else if (id == R.id.RemoveFromFavorites) {
			changeFavoriteStatus(false);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Change user's favorite status/お気に入り登録状態を変更する
	 * 
	 * @param addOrRemove
	 *            True if add, false if remove/追加の場合は真、削除の場合は偽
	 */
	protected void changeFavoriteStatus(final boolean addOrRemove) {
		final Handler finishHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				refresh();
			}
		};

		runAsync(getString(R.string.indicator_saving), true, new Runnable() {
			@Override
			public void run() {
				try {
					getMyPreference().setFavoriteUser(user, addOrRemove);
					UserPreference.saveFavoriteUserState(
							UserTimelineActivity.this, user, addOrRemove);

					finishHandler.sendEmptyMessage(0);
				} catch (Exception ex) {
					handleAsyncException(ex);
				}
			}
		});
	}

	/**
	 * Check if my timeline/自分のタイムラインかを返す
	 * 
	 * @return My timeline or not/自分のタイムラインかどうか
	 */
	protected boolean isMyTimeline() {
		return ownerId.equals(getMe().getId());
	}

	@Override
	protected void onNewMessage(String text) throws Exception {
		if (isMyTimeline())
			super.onNewMessage(text);
	}

	@Override
	public void refresh() {
		// Get user of this timeline bofore refresh
		// 再読込の前にこのタイムラインのユーザを取得
		if (isMyTimeline()) {
			user = getMe();
			super.refresh();
		} else {
			// Get the user asynchronously
			// 非同期でユーザを取得

			final Handler finishHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					String title = "";
					if (isMyFavorite())
						title += "* ";
					title += user.getName();

					if (user.getAge().length() > 0)
						title += "(" + user.getAge() + ")";

					if (isMyFavorite())
						title += " " + getString(R.string.label_is_in_favorite);

					UserTimelineActivity.this.setTitle(title);
					UserTimelineActivity.super.refresh();
				}
			};

			runAsync(getString(R.string.indicator_loading), true,
					new Runnable() {
						@Override
						public void run() {
							try {
								user = User.lookupUser(
										UserTimelineActivity.this, ownerId);
								finishHandler.sendEmptyMessage(0);
							} catch (Exception ex) {
								handleAsyncException(ex);
							}
						}
					});
		}

	}

	@Override
	protected Timeline onRefreshTimeline() throws Exception {
		return ChatMessage.userTimeline(this, ownerId);
	}

	@Override
	protected void onListItemClicked(HashMap<String, String> hash)
			throws Exception {
		if (isMyTimeline()) {
			super.onListItemClicked(hash);
		}
	}
}
