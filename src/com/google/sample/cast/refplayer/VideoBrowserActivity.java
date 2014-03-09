/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cast.refplayer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.sample.cast.refplayer.settings.CastPreference;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class VideoBrowserActivity extends Activity {

	private static final String TAG = "VideoBrowserActivity";
	private static final String TAG_TENCENT = "VideoBrowserActivity_tencent";

	private VideoCastManager mCastManager;
	private IVideoCastConsumer mCastConsumer;
	private MiniController mMini;
	private MenuItem mediaRouteMenuItem;

	private WebView webView = null;
	public Handler handler = new Handler();

	String youkuTitle = "优酷视频";
//	String youkuThumbUrl = "file:///android_asset/youku.jpg";
	String youkuThumbUrl = "null";
	String youkuUrl = "null";
	String youkuVid = "null";
	public MyObject testobj;

	public class MyObject {
		private Handler handler = null;
		private WebView webView = null;

		public MyObject(VideoBrowserActivity htmlActivity, Handler handler) {
			this.webView = (WebView) htmlActivity.findViewById(R.id.webView1);
			this.handler = handler;
		}

		public void init() {
			// 通过handler来确保init方法的执行在主线程中
			handler.post(new Runnable() {
				public void run() {
					Log.d(TAG, "MyObject.init run()");
					if (VERSION.SDK_INT < 17)
						webView.loadUrl("javascript:showHtmlcallJava2('" + youkuVid
								+ "')");
				}
			});
		}

		public void Java2Html() {
			Log.d(TAG, "MyObject.Java2Html()");
			webView.loadUrl("javascript:showHtmlcallJava2('" + youkuVid + "')");
		}

		@JavascriptInterface
		public String HtmlcallJava2(final String param) {
			youkuUrl = param;
			getYoukuVideoInfo("http://v.youku.com/player/getPlayList/VideoIDS/"
					+ youkuVid);

			Intent intent1 = new Intent("XBMC.cast");
			intent1.setDataAndType(
					Uri.parse(youkuUrl + "[@]" + youkuTitle + "[@]"
							+ youkuThumbUrl), null);

			Log.d(TAG + "_js_cb", youkuUrl);

			startActivity(intent1);

			return "Html call Java : " + param;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// VideoCastManager.checkGooglePlaySevices(this);
		setContentView(R.layout.video_browser);

		castIntent(getIntent());

		// ActionBar actionBar = getSupportActionBar();

		// mCastManager = CastApplication.getCastManager(this);
		/*
		 * // -- Adding MiniController mMini = (MiniController)
		 * findViewById(R.id.miniController1);
		 * mCastManager.addMiniController(mMini);
		 * 
		 * mCastConsumer = new VideoCastConsumerImpl() {
		 * 
		 * @Override public void onFailed(int resourceId, int statusCode) {
		 * 
		 * }
		 * 
		 * @Override public void onConnectionSuspended(int cause) { Log.d(TAG,
		 * "onConnectionSuspended() was called with cause: " + cause);
		 * com.google.sample.cast.refplayer.utils.Utils.
		 * showToast(VideoBrowserActivity.this, R.string.connection_temp_lost);
		 * }
		 * 
		 * @Override public void onConnectivityRecovered() {
		 * com.google.sample.cast.refplayer.utils.Utils.
		 * showToast(VideoBrowserActivity.this, R.string.connection_recovered);
		 * }
		 * 
		 * @Override public void onCastDeviceDetected(final RouteInfo info) { if
		 * (!CastPreference.isFtuShown(VideoBrowserActivity.this)) {
		 * CastPreference.setFtuShown(VideoBrowserActivity.this);
		 * 
		 * Log.d(TAG, "Route is visible: " + info); new
		 * Handler().postDelayed(new Runnable() {
		 * 
		 * @Override public void run() { if (mediaRouteMenuItem.isVisible()) {
		 * Log.d(TAG, "Cast Icon is visible: " + info.getName()); showFtu(); } }
		 * }, 1000); } } };
		 * 
		 * setupActionBar(actionBar);
		 * mCastManager.reconnectSessionIfPossible(this, false);
		 */
	}

	private void castIntent(Intent intent) {
		String action = intent.getAction();
		Matcher matcher;

		if (Intent.ACTION_SEND.equals(action) == false) {
			Log.d(TAG, "not a ACTION_SEND");
			return;
		}

		Log.d(TAG, "intent=" + intent.toString());
		Log.d(TAG, "intent dump:");

		StringBuilder str = new StringBuilder();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			Set<String> keys = bundle.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				str.append(key);
				str.append(":");
				str.append(bundle.get(key));
				str.append("\n");
			}
			Log.d(TAG, str.toString());
		}

		String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (extraText == null || extraText.equals("")) {
			Log.d(TAG, "empty intent extra text");
			return;
		}

		matcher = Pattern.compile("http://.*v.youku.com(.+?)/id_(.+?).html")
				.matcher(extraText);
		if (matcher.find()) {
			youkuVid = matcher.group(2);
			Log.d(TAG, "youku url detected: " + matcher.group(0));
			Log.d(TAG, "youku vid: " + youkuVid);
			castYouku(youkuVid);
		}

		matcher = Pattern.compile("http://.*v.qq.com/(.+?).html").matcher(
				extraText);
		if (matcher.find()) {
			Log.d(TAG, "tencent url detected: " + matcher.group(0));
			Log.d(TAG, "tencent partial url: " + matcher.group(1));
			castTencent("http://v.qq.com/" + matcher.group(1) + ".html");
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void castYouku(String vid) {
		webView = (WebView) this.findViewById(R.id.webView1);
		webView.setVisibility(View.INVISIBLE);
		webView.setVisibility(View.GONE);

		webView.getSettings().setDefaultTextEncodingName("UTF-8");
		webView.getSettings().setJavaScriptEnabled(true);
		testobj = new MyObject(this, handler);
		webView.addJavascriptInterface(testobj, "myObject");

		Log.d(TAG, "webView load youkump4.html");
		webView.loadUrl("file:///android_asset/youkump4.html");
		Log.d(TAG, "webView load youkump4.html done!");
	}

	public void getYoukuVideoInfo(String path) {
		getYoukuThumbAndTitle(path);
		/*
		 * new Thread() {
		 * 
		 * @Override public void run() { try { getYoukuThumbAndTitle(fPath); }
		 * catch (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } } }.start();
		 */
	}

	public void getYoukuThumbAndTitle(String path) {
		InputStream stream = null;
		JsonReader reader = null;

		try {
			stream = getWebPageStream(path);
			byte[] data = readInputStream(stream);// 得到html的二进制数据
			String html = new String(data, "utf8");
			System.out.println(html);

			stream = getWebPageStream(path);
			reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));

			parseYoukuData(reader);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void parseYoukuData(JsonReader reader) throws Exception {
		String name;
		boolean thumbFound = false;
		boolean titleFound = false;

		reader.beginObject();
		while (reader.hasNext()) {
			name = reader.nextName();
			if (name.equals("data")) {
				reader.beginArray();
				reader.beginObject();
				while (reader.hasNext()) {
					name = reader.nextName();
					if (name.equals("logo")) {
						youkuThumbUrl = reader.nextString();
						Log.d("youku", "thumbnailURL=" + youkuThumbUrl);
						thumbFound = true;
					} else if (name.equals("title")) {
						youkuTitle = reader.nextString();
						Log.d("youku", "title=" + youkuTitle);
						titleFound = true;
					} else {
						Log.d("youku", "skip " + name);
						reader.skipValue();
					}
					if (thumbFound && titleFound)
						return;
				}
				reader.endObject();
				reader.endArray();
			} else {
				reader.skipValue();
			}
		}
	}

	public InputStream getWebPageStream(String path) throws Exception {
		// 类 URL 代表一个统一资源定位符，它是指向互联网“资源”的指针。
		URL url = new URL(path);
		Log.v("getPictureData:  ", path);
		// 每个 HttpURLConnection 实例都可用于生成单个请求，
		// 但是其他实例可以透明地共享连接到 HTTP 服务器的基础网络
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// 设置 URL 请求的方法
		conn.setRequestMethod("GET");
		// 设置一个指定的超时值（以毫秒为单位），
		// 该值将在打开到此 URLConnection 引用的资源的通信链接时使用。
		conn.setConnectTimeout(6 * 1000);
		// conn.getInputStream()返回从此打开的连接读取的输入流

		System.out.println(conn.getResponseCode());
		if (conn.getResponseCode() == 200) {
			InputStream inStream = conn.getInputStream();// 通过输入流获取html数据
			return inStream;
		}
		return null;
	}

	public String getPictureData(String path) throws Exception {
		// 类 URL 代表一个统一资源定位符，它是指向互联网“资源”的指针。
		URL url = new URL(path);
		Log.v("getPictureData:  ", path);
		// 每个 HttpURLConnection 实例都可用于生成单个请求，
		// 但是其他实例可以透明地共享连接到 HTTP 服务器的基础网络
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// 设置 URL 请求的方法
		conn.setRequestMethod("GET");
		// 设置一个指定的超时值（以毫秒为单位），
		// 该值将在打开到此 URLConnection 引用的资源的通信链接时使用。
		conn.setConnectTimeout(6 * 1000);
		// conn.getInputStream()返回从此打开的连接读取的输入流

		System.out.println(conn.getResponseCode());
		if (conn.getResponseCode() == 200) {
			InputStream inStream = conn.getInputStream();// 通过输入流获取html数据
			byte[] data = readInputStream(inStream);// 得到html的二进制数据
			String html = new String(data, "utf8");
			return html;
		}
		return null;
	}

	public byte[] readInputStream(InputStream inStream) throws Exception {
		// 此类实现了一个输出流，其中的数据被写入一个 byte 数组
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// 字节数组
		byte[] buffer = new byte[1024];
		int len = 0;
		// 从输入流中读取一定数量的字节，并将其存储在缓冲区数组buffer 中
		while ((len = inStream.read(buffer)) != -1) {
			// 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此输出流
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		// toByteArray()创建一个新分配的 byte 数组。
		return outStream.toByteArray();
	}

	private void castTencent(final String url) {
		new Thread() {
			@Override
			public void run() {
				try {
					// String test =
					// getPictureData("http://v.qq.com/cover/o/obr3rfx7xdatznl.html");
					// Log.v("test come out: ", test);
					// System.out.println(test);
					Matcher matcher;
					//String tencentThumbUrl = "file:///android_asset/tencent.png";
					String tencentThumbUrl = "null";
					String tencentTitle = "腾讯视频";
					String content;
					String tencentVid = "null";

					content = getPictureData(url);
					System.out.println(content);

					matcher = Pattern.compile(
							"meta http-equiv=\"Content-Type\"")
							.matcher(content);
					if (matcher.find()) {
						Log.d(TAG_TENCENT, "redirect url detected");
						matcher = Pattern.compile("url=(.+?)\"").matcher(
								content);
						if (matcher.find()) {
							String redirectUrl = matcher.group(1);
							Log.d(TAG_TENCENT, "redirect_url=" + redirectUrl);
							matcher = Pattern.compile("vid=(.+?)$").matcher(
									redirectUrl);
							if (matcher.find()) {
								tencentVid = matcher.group(1);
								Log.d("tencent", "vid found in redirect url:"
										+ tencentVid);
								String regStr = "src=\"http://(.+?)"
										+ tencentVid + "(.+?)\" alt=\"(.+?)\"";
								content = getPictureData(redirectUrl);
								Log.d("tencent", "searching thumb and title:"
										+ regStr);
								matcher = Pattern.compile(regStr).matcher(
										content);
								if (matcher.find()) {
									tencentThumbUrl = "http://"
											+ matcher.group(1) + tencentVid
											+ matcher.group(2);
									tencentTitle = matcher.group(3);
								}
							} else {
								Log.d("tencent",
										"vid not found from redirect url");
								return;
							}
						}
					} else {
						matcher = Pattern.compile("vid:\"(.+?)\"").matcher(
								content);
						if (matcher.find()) {
							tencentVid = matcher.group(1);
							Log.d(TAG_TENCENT, "tencentVid: " + tencentVid);
						} else {
							Log.e(TAG_TENCENT, "tencentVid not found");
							return;
						}
						matcher = Pattern.compile("pic :\"(.+?)\"").matcher(
								content);
						if (matcher.find()) {
							tencentThumbUrl = matcher.group(1);
							Log.d(TAG_TENCENT, "tencentThumbUrl: "
									+ tencentThumbUrl);
						} else {
							Log.d(TAG_TENCENT, "tencentThumbUrl not found, "
									+ tencentThumbUrl + " will be used");
						}

						matcher = Pattern.compile("title :\"(.+?)\"").matcher(
								content);
						if (matcher.find()) {
							tencentTitle = matcher.group(1);
							Log.d(TAG_TENCENT, "tencentTitle: " + tencentTitle);
						} else {
							Log.d(TAG_TENCENT, "tencentTitle not found, "
									+ tencentTitle + " will be used");
						}
					}

					content = getPictureData("http://vv.video.qq.com/geturl?vid="
							+ tencentVid
							+ "&otype=xml&platform=1&ran=0%2E9652906153351068");
					// System.out.println(test2);
					matcher = Pattern.compile("<url>(.+?)</url>").matcher(
							content);
					if (matcher.find()) {
						Log.d(TAG_TENCENT, "streamUrl: " + matcher.group(1));

						Intent intent1 = new Intent("XBMC.cast");
						intent1.setDataAndType(
								Uri.parse(matcher.group(1) + "[@]"
										+ tencentTitle + "[@]"
										+ tencentThumbUrl), null);
						startActivity(intent1);
					}
				} catch (Exception e) {
					Log.e(TAG_TENCENT, e.toString());
					System.exit(0);
				}
			}
		}.start();
	}

	private void setupActionBar(ActionBar actionBar) {
		Log.d(TAG, "setupActionBar() is called");
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		// getSupportActionBar().setIcon(R.drawable.actionbar_logo_castvideos);
		// getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu() is called. " + VERSION.SDK_INT);
		super.onCreateOptionsMenu(menu);

		if (VERSION.SDK_INT >= 17)
			testobj.Java2Html();
		// getMenuInflater().inflate(R.menu.main, menu);

		// mediaRouteMenuItem = mCastManager.
		// addMediaRouterButton(menu, R.id.media_route_menu_item);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected() is called");
		// switch (item.getItemId()) {
		// case R.id.action_settings:
		// Intent i = new Intent(VideoBrowserActivity.this,
		// CastPreference.class);
		// startActivity(i);
		// break;
		// }
		return true;
	}

	private void showFtu() {
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown() is called");
		// if (!mCastManager.isConnected()) {
		// return super.onKeyDown(keyCode, event);
		// } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
		// changeVolume(CastApplication.VOLUME_INCREMENT);
		// } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
		// changeVolume(-CastApplication.VOLUME_INCREMENT);
		// } else {
		// return super.onKeyDown(keyCode, event);
		// }
		return super.onKeyDown(keyCode, event);
		// return true;
	}

	private void changeVolume(double volumeIncrement) {
		Log.d(TAG, "changeVolume() is called");
		// if (mCastManager == null) {
		// return;
		// }
		// try {
		// mCastManager.incrementVolume(volumeIncrement);
		// } catch (Exception e) {
		// Log.e(TAG, "onVolumeChange() Failed to change volume", e);
		// com.google.sample.cast.refplayer.utils.Utils.handleException(this,
		// e);
		// }
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume() was called");
		// mCastManager = CastApplication.getCastManager(this);
		// if (null != mCastManager) {
		// mCastManager.addVideoCastConsumer(mCastConsumer);
		// mCastManager.incrementUiCounter();
		// }

		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause() is called");
		// mCastManager.decrementUiCounter();
		// mCastManager.removeVideoCastConsumer(mCastConsumer);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy() is called");
		// if (null != mCastManager) {
		// mMini.removeOnMiniControllerChangedListener(mCastManager);
		// }
		super.onDestroy();
	}

}
