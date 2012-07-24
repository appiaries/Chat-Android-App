package com.appiaries.demo.appiariesChat.activities;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.appiaries.demo.R;
import com.appiaries.demo.api.TokenExpiresException;

/**
 * Base class of list style activity/リスト型アクティビティの基底クラス
 * 
 * @author PC Phase
 * 
 */
@SuppressLint("HandlerLeak")
public abstract class ListBaseActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener {
	/**
	 * Action form/アクションフォーム
	 */
	protected LinearLayout actionForm;

	/**
	 * If display action form on header/ヘッダのアクションフォームを表示するか
	 */
	protected boolean showActionForm = true;

	/**
	 * Button on action form/アクションフォーム上のボタン
	 */
	protected Button actionButton;

	/**
	 * Button label on action form/アクションフォーム上のボタンラベル
	 */
	protected String actionButtonLabel = "ACTION";

	/**
	 * Button hint on action form to display at indicator/
	 * 読み込み中ダイアログに表示するためのアクションフォームのボタンヒント
	 */
	protected String actionButtonHint = "In Action";

	/**
	 * Content of textbox after action/アクション実行後のアクションフォーム上のテキスト
	 */
	protected String textAfterAction = "";

	/**
	 * Initial content of textbox/アクションフォーム上のテキストボックスの初期値
	 */
	protected EditText actionText;

	/**
	 * Item list/アイテムリスト
	 */
	protected ListView itemList;

	/**
	 * List adapter/リストアダプタ
	 */
	protected BaseAdapter itemListAdapter;

	@Override
	public void start() {
		setContentView(R.layout.list);

		// Action form
		// アクションテキスト
		actionForm = (LinearLayout) findViewById(R.id.action_form);
		actionForm.setVisibility(showActionForm ? View.VISIBLE : View.GONE);

		// Action text box
		// アクションテキストボックス
		actionText = (EditText) findViewById(R.id.action_text);

		// Action button
		// アクションボタン
		actionButton = (Button) findViewById(R.id.action_button);
		actionButton.setText(actionButtonLabel);
		actionButton.setOnClickListener(this);
		actionButton.setHint(actionButtonHint);

		// Item list
		// 項目リスト
		itemList = (ListView) findViewById(R.id.item_list);
		itemList.setOnItemClickListener(this);
		
		refresh();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.Refresh) {
			refresh();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Reload list item with adapter/アダプタでアイテムリストを再読み込み
	 */
	protected void applyListAdapter() {
		if (itemListAdapter != null) {
			itemList.setAdapter(itemListAdapter);
			if (itemListAdapter.getCount() == 0) {
				Toast.makeText(this, getString(R.string.msg_no_results),
						commonToastDuration).show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == actionButton.getId()) {
			// Build inputed text
			// 入力文字列を構築
			SpannableStringBuilder sp = (SpannableStringBuilder) actionText
					.getText();
			final String text = sp.toString();

			// Required input
			// 入力が必要
			if (text.length() < 1) {
				Toast.makeText(this, getString(R.string.msg_input_words),
						commonToastDuration).show();
				return;
			}

			// Hide software keyboard
			// ソフトウェアキーボードを隠す
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(actionText.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);

			// Call onAction asynchronously
			// 非同期でonActionメソッドを呼び出し
			final Handler finishHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					actionText.setText(textAfterAction);
					Boolean toRefresh = (Boolean) msg.obj;
					if (toRefresh)
						refresh();
				}
			};

			runAsync(actionButtonHint, true, new Runnable() {
				@Override
				public void run() {
					try {
						textAfterAction = text;
						boolean toRefresh = onAction(text);

						Message msg = new Message();
						msg.obj = Boolean.valueOf(toRefresh);
						finishHandler.sendMessage(msg);
					} catch (Exception ex) {
						handleAsyncException(ex);
					}

				}
			});
		}
	}

	/**
	 * Override on each sub class to handle event action button clicked/
	 * アクションボタンがクリックされたイベントを処理するために各サブクラスで上書きする
	 * 
	 * @param text
	 *            Content in text box/テキストボックスに入力された内容
	 * @return To update item list/アイテムリストを更新するか
	 * @throws Exception
	 */
	protected abstract boolean onAction(String text) throws Exception;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Object selected = itemListAdapter.getItem(position);

		@SuppressWarnings("unchecked")
		HashMap<String, String> hash = (HashMap<String, String>) selected;

		try {
			onListItemClicked(hash);
		} catch (TokenExpiresException ex) {
			login(true);
		} catch (Exception ex) {
			handleException(ex);
		}
	}

	/**
	 * Override on each sub class to handle event item on list clicked/
	 * リスト上のアイテムがクリックされたイベントを処理するために各サブクラスで上書きする
	 * 
	 * @param hash
	 * @throws Exception
	 */
	protected abstract void onListItemClicked(HashMap<String, String> hash)
			throws Exception;

	/**
	 * Refresh item list/アイテムリストの再読込
	 * 
	 * Override as onRefresh actual process 実際の処理はonRefreshメソッドで上書きする
	 */
	public void refresh() {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				applyListAdapter();
			}
		};

		runAsync(getString(R.string.indicator_loading), false, new Runnable() {
			@Override
			public void run() {
				try {
					itemListAdapter = onRefresh();
					handler.sendEmptyMessage(0);
				} catch (Exception ex) {
					handleAsyncException(ex);
				}
			}
		});
	}

	/**
	 * Override on each sub class to handle event refresh item list/
	 * リストの再読込イベントを処理するために各サブクラスで上書きする
	 * 
	 * @return List adapter/リストアダプタ
	 * @throws Exception
	 */
	protected abstract BaseAdapter onRefresh() throws Exception;

}
