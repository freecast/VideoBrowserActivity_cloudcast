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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import android.widget.AdapterView.OnItemClickListener;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class VideoBrowserActivity extends Activity implements OnItemClickListener {

	private static final String TAG = "VideoBrowserActivity";
	private static final String TAG_TENCENT = "VideoBrowserActivity_tencent";
	private static final String CAST_INTENT_NAME = "CloudCast";

	private VideoCastManager mCastManager;
	private IVideoCastConsumer mCastConsumer;
	private MiniController mMini;
	private MenuItem mediaRouteMenuItem;
	
	private ListView listView;

	private WebView webView = null;
	public Handler handler = new Handler();
	private Button buttonSD;
	private Button buttonHD;
	private Button buttonFHD;
	private String MyDefinitionType;
	private String LeTVurl;
	
	private String webSite;

	String youkuTitle = "优酷视频";
	String youkuThumbUrl = "http://static.youku.com/index/img/header/yklogo.png?qq-pf-to=pcqq.c2c";
	String youkuUrl = "http://www.youku.com";
	String youkuVid = "null";

	String sohuTitle = "搜狐视频";
	String sohuThumbUrl = "http://i1.letvimg.com/img/201206/29/iphonelogo.png";
	String sohuUrl = "http://www.sohu.com";
	String[] sohuUrls = null;
	String[] sohuExtraUrls = null;
	String[] sohuFinalUrls = null;
	String sohuVid = "null";

	String tencentThumbUrl = "http://imgcache.gtimg.cn/tencentvideo_v1/vstyle/web/v3/style/images/logo.png";
	String tencentTitle = "腾讯视频";
	String content = "null";
	String tencentVid = "null";

	List<String> listData;
	ArrayAdapter<String> listAdapter;
	ListView lv;

	public MyObject testobj = null;

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

			Intent intent1 = new Intent(CAST_INTENT_NAME);
			intent1.setDataAndType(
					Uri.parse(youkuUrl + "[@]" + youkuTitle + "[@]"
							+ youkuThumbUrl), null);

			Log.d(TAG + "_js_cb", youkuUrl);

			startActivity(intent1);
			System.exit(0);

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

		buttonSD = (Button) findViewById(R.id.button1);
		buttonHD = (Button) findViewById(R.id.button2);
		buttonFHD = (Button) findViewById(R.id.button3);

		buttonSD.setOnClickListener(new submitOnClieckListener1());
		buttonHD.setOnClickListener(new submitOnClieckListener2()); 
		buttonFHD.setOnClickListener(new submitOnClieckListener3()); 		

		lv = (ListView) findViewById(R.id.list);

		listData = new ArrayList<String>();
		listData.add("loading......");
        listAdapter = new ArrayAdapter<String>(this, R.layout.video_browser,
                R.id.text1, listData);
        lv.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        final Intent intent = getIntent();
		new Thread() {
			public void run() {
				castIntent(intent);
			}
		}.start();
	}

	class submitOnClieckListener1 implements OnClickListener {
		@Override
		public void onClick(View v) {
			// 本地机器部署为服务器，从本地下载a.txt文件内容在textView上显示

			new Thread() {
				public void run() {
					MyDefinitionType = "normal";
					Log.v("submitOnClieckListener1", "normal");
					RealcastLeTV(LeTVurl, MyDefinitionType);
					Log.v("submitOnClieckListener1", "normal");
				}
			}.start();
		}
	}

	class submitOnClieckListener2 implements OnClickListener {
		@Override
		public void onClick(View v) {
			// 本地机器部署为服务器，从本地下载a.txt文件内容在textView上显示

			new Thread() {
				public void run() {
					MyDefinitionType = "high";
					RealcastLeTV(LeTVurl, MyDefinitionType);
					Log.v("submitOnClieckListener1", "high");
				}
			}.start();
		}
	}

	class submitOnClieckListener3 implements OnClickListener {
		@Override
		public void onClick(View v) {
			// 本地机器部署为服务器，从本地下载a.txt文件内容在textView上显示

			new Thread() {
				public void run() {
					MyDefinitionType = "super";
					RealcastLeTV(LeTVurl, MyDefinitionType);
					Log.v("submitOnClieckListener1", "super");
				}
			}.start();
		}
	}

	private void castIntent(Intent intent) {
		String action = intent.getAction();
		Matcher matcher;
		testobj = null;

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

		if (castYouku(extraText) == true)
			return;

		if (castTencent(extraText) == true)
			return;

		if (castSohu(extraText) == true)
			return;

		// http://m.letv.com/vplay_20020870.html
		// http://www.letv.com/ptv/vplay/20020870.html
		matcher = Pattern.compile("http://.*letv.com/vplay_(.+?).html")
				.matcher(extraText);
		if (matcher.find()) {
			webSite = "letv";
			Log.d(TAG, "letv url detected: " + matcher.group(0));
			Log.d(TAG, "letv vplay id: " + matcher.group(1));
			castLeTV("http://www.letv.com/ptv/vplay/" + matcher.group(1)
					+ ".html");
			return;
		}



		Log.d(TAG, "no existing cast for url: " + extraText);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private boolean castYouku(String url) {
		Matcher matcher;
		matcher = Pattern.compile("http://.*v.youku.com(.+?)/id_(.+?).html")
				.matcher(url);
		if (matcher.find()) {
			webSite = "youku";
			youkuVid = matcher.group(2);
			Log.d(TAG, "youku url detected: " + matcher.group(0));
			Log.d(TAG, "youku vid: " + youkuVid);

			castYouku(youkuVid);
		} else {
			return false;
		}

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

		return true;
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

	private boolean castTencent(String url) {
		Matcher matcher;
		matcher = Pattern.compile("http://.*v.qq.com/(.+?).html(.*)").matcher(url);
		if (matcher.find()) {
			webSite = "qq";
			Log.d(TAG_TENCENT, "tencent url detected: " + matcher.group(0));
			Log.d(TAG_TENCENT, "tencent partial url: " + matcher.group(1));
		} else {
			return false;
		}
		url = "http://v.qq.com/" + matcher.group(1) + ".html" + matcher.group(2);

		final String qqUrl;
		matcher = Pattern.compile("cid=(.+?)&vid=(.+?)$").matcher(url);
		if (matcher.find()) {
			//String cid = matcher.group(1);
			String vid = matcher.group(2);
			tencentBoke(vid);
			return true;
		} else {
			qqUrl = url;
		}
		Log.d(TAG_TENCENT, "qqUrl = " + qqUrl);
		//qqUrl = url;
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

					content = getPictureData(qqUrl);
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
						tencentDirectURL(qqUrl, content);
					}

					content = getPictureData("http://vv.video.qq.com/geturl?vid="
							+ tencentVid
							+ "&otype=xml&platform=1&ran=0%2E9652906153351068");
					// System.out.println(test2);
					matcher = Pattern.compile("<url>(.+?)</url>").matcher(
							content);
					if (matcher.find()) {
						Log.d(TAG_TENCENT, "streamUrl: " + matcher.group(1));

						Intent intent1 = new Intent(CAST_INTENT_NAME);
						intent1.setDataAndType(
								Uri.parse(matcher.group(1) + "[@]"
										+ tencentTitle + "[@]"
										+ tencentThumbUrl), null);
						startActivity(intent1);
						System.exit(0);
					}
				} catch (Exception e) {
					Log.e(TAG_TENCENT, e.toString());
					e.printStackTrace();
					//System.exit(0);
				}
			}
		}.start();
		return true;
	}
	//<!-- 兼容v.qq.com老的播放页面跳转到boke 放到v.qq.com/play.html -->
	//<title>腾讯播客-实拍中国家属被马航带离 大声哭喊：救救我</title>
	private boolean tencentBoke(String vid) {
		Matcher matcher;
		try {
			content = getPictureData("http://play.v.qq.com/play?vid="+vid);
			matcher = Pattern.compile("<title>(.*)</title>").matcher(content);
			if (matcher.find())
				tencentTitle = matcher.group(1);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		try {
			content = getPictureData("http://vv.video.qq.com/geturl?vid="
					+ vid
					+ "&otype=xml&platform=1&ran=0%2E9652906153351068");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		// System.out.println(test2);
		matcher = Pattern.compile("<url>(.+?)</url>").matcher(
				content);
		if (matcher.find()) {
			Log.d(TAG_TENCENT, "streamUrl: " + matcher.group(1));

			Intent intent1 = new Intent(CAST_INTENT_NAME);
			intent1.setDataAndType(
					Uri.parse(matcher.group(1) + "[@]"
							+ tencentTitle + "[@]"
							+ tencentThumbUrl), null);
			startActivity(intent1);
			System.exit(0);
		}

		return true;
	}
	private void tencentDirectURL(String url, String content) {
		Matcher matcher;
		matcher = Pattern.compile("\\?vid=(.+?)$").matcher(url);
		if (matcher.find()) {
			tencentVid = matcher.group(1);
			Log.d(TAG_TENCENT+"directURL", "vid found in url: " + tencentVid);
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

		// id="s0014qqdkhn"  title="一仆二主 第01集"
		matcher = Pattern.compile("id=\"" + tencentVid + "\".*title=\"(.+?)\"").matcher(
				content);
		if (matcher.find()) {
			tencentTitle = matcher.group(1);
			Log.d(TAG_TENCENT, "tencentTitle: " + tencentTitle);
		} else {
			Log.d(TAG_TENCENT, "tencentTitle not found, "
					+ tencentTitle + " will be used");
		}
	}


	public class LeTVlistener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String definition = "normal";
			Log.d(TAG, "position:" + position + "  item:"
					+ parent.getItemAtPosition(position).toString());

			if (position == 1)
				definition = "high";
			else if (position == 2)
				definition = "super";

			Log.v("LeTVlistItemListener", definition);
			RealcastLeTV(LeTVurl, definition);
			Log.v("LeTVlistItemListener", definition + " cast finished");
		}
	}

	private void castLeTV(final String url) {
        listAdapter = new ArrayAdapter<String>(this, R.layout.video_browser,
                R.id.text1, listData);
        lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new LeTVlistener());

		new Thread() {
			@Override
			public void run() {
				try {
					Matcher matcher;
					String LeTVThumbUrl = "http://i1.letvimg.com/img/201206/29/iphonelogo.png";
					String LeTVTitle = "乐视视频";
					String content;
					String LeTVVid = "null";

					boolean superFound = false;
					boolean hdFound = false;
					LeTVurl = url;

					content = getPictureData("http://www.flvcd.com/parse.php?kw="
							+ url + "&format=" + "normal");
					// System.out.println(content);

					Log.d("LeTV", "buttonSD= VISIBLE");

					matcher = Pattern.compile("format=high(.+?)").matcher(
							content);
					if (matcher.find()) {
						hdFound = true;
						Log.d("LeTV", "buttonHD= VISIBLE");
					} else {
						Log.d("LeTV", "buttonHD= INVISIBLE");
					}

					matcher = Pattern.compile("format=super(.+?)").matcher(
							content);
					if (matcher.find()) {
						superFound = true;
						Log.d("LeTV", "buttonFHD= VISIBLE");
					} else {
						Log.d("LeTV", "buttonFHD= INVISIBLE");
					}

					final boolean HDvisible = hdFound;
					final boolean superVisible = superFound;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							listData.clear();
							listData.add("标清");
							if (HDvisible)
								listData.add("高清");
							if (superVisible)
								listData.add("超清");
					        listAdapter.notifyDataSetChanged();
/*
							buttonSD.setVisibility(View.VISIBLE);
							if (HDvisible)
								buttonHD.setVisibility(View.VISIBLE);
							if (superVisible)
								buttonFHD.setVisibility(View.VISIBLE);
*/
					        }
					});
				} catch (Exception e) {
					Log.e("LeTV", e.toString());
					System.exit(0);
				}
			}

		}.start();
	}

	private void RealcastLeTV(final String url, final String strDefinitionType) 
	{
		new Thread() {
			@Override
			public void run() {
				try {
					
					Matcher matcher;
					String LeTVThumbUrl = "http://i1.letvimg.com/img/201206/29/iphonelogo.png";
					String LeTVTitle = "乐视视频";
					String content;
					String LeTVVid = "null";

					content = getPictureData(url);
					//System.out.println(content);

					matcher = Pattern.compile("img:\\s*\"(.+?)\"").matcher(
							content);
					if (matcher.find()) {
						LeTVThumbUrl = matcher.group(1);
						Log.d("LeTVreal", "LeTVThumbUrl=" + LeTVThumbUrl);
					} else {
						Log.d("LeTVreal", "LeTVThumbUrl not found");
					}

					matcher = Pattern.compile("irTitle\" content=\"(.+?)\"")
							.matcher(content);
					if (matcher.find()) {
						LeTVTitle = matcher.group(1);
						Log.d("LeTVreal", "LeTVTitle=" + LeTVTitle);
					}

					// super, high, normal
					
					content = getPictureData("http://www.flvcd.com/parse.php?kw="
							+ url + "&format=" + strDefinitionType);
					// System.out.println(content);

					matcher = Pattern.compile("var clipurl = \"(.+?)\"")
							.matcher(content);
					if (matcher.find()) {
						String redirectUrl = matcher.group(1);
						Log.d("LeTVreal", "redirect_url=" + redirectUrl);

						Intent intent1 = new Intent(CAST_INTENT_NAME);
						Log.d("LeTVreal", "new intent was done");
						intent1.setDataAndType(
								Uri.parse(redirectUrl + "[@]" + LeTVTitle
										+ "[@]" + LeTVThumbUrl), null);
						Log.d("LeTVreal", "intent set data and type was done");
						startActivity(intent1);
						Log.d("LeTVreal", "startActivity was done");
						System.exit(0);
					}
				} catch (Exception e) {
					Log.e("LeTV", e.toString());
					System.exit(0);
				}
			}
		}.start();
	}

    /* for PC url 
	 * http://tv.sohu.com/20140306/n396178078.shtml
	 */
	private void getSohuVid(final String url) {
		Matcher matcher;
		String content;

		// http://tv.sohu.com/20140306/n396178078.shtml
		try {
			content = getPictureData(url);
			// var vid="1643301";
			matcher = Pattern.compile("var vid=\"(.+?)\";").matcher(content);
			if (matcher.find()) {
				sohuVid = matcher.group(1);
				Log.d("sohu", "vid=" + sohuVid);
			} else {
				System.out.println(content);
				System.exit(0);
				return;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

    /*
	 * http://m.tv.sohu.com/v1647325.shtml?channeled=1210010500
	 * http://m.tv.sohu.com/20140306/n396178078.shtml
	 */
	private boolean castSohu(String url) {
		Matcher matcher;

		matcher = Pattern.compile("http://.*tv\\.sohu\\.com")
				.matcher(url);
		if (matcher.find() == false)
			return false;

		Log.d(TAG, "sohu url detected: " + url);
		listVideoSegs(url);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, "position:" + position + "  item:"
				+ parent.getItemAtPosition(position).toString());

		Intent intent1 = new Intent(CAST_INTENT_NAME);
		int section = position + 1;
		String title = sohuTitle + " 片段" + section;
		intent1.setDataAndType(
				Uri.parse(sohuFinalUrls[position] + "[@]" + title + "[@]"
						+ sohuThumbUrl), null);
		startActivity(intent1);
	}

	public String geturl(String url) {
		try {
			Matcher matcher;
			String content;
			ArrayList<String> list = new ArrayList<String>();

			matcher = Pattern.compile("m.tv.sohu.com/v(.+?).shtml").matcher(url);
			if (matcher.find()) {
				sohuVid = matcher.group(1);
			} else {
				content = getPictureData(url);
				matcher = Pattern.compile("vid : \"(.+?)\"").matcher(content);
				if (matcher.find()) {
					sohuVid = matcher.group(1);
				} else {
					Log.d("sohu", "vid not found in: " + url);
					return null;
				}
			}
			Log.d("sohu", "vid=" + sohuVid);

			// http://hot.vrs.sohu.com/vrs_flash.action?vid=1475503
			InputStream stream = null;
			JsonReader reader = null;

			stream = getWebPageStream("http://hot.vrs.sohu.com/vrs_flash.action?vid="
					+ sohuVid);
			reader = new JsonReader(new InputStreamReader(stream,
					"UTF-8"));

			parseSohuData(reader);
			if (sohuUrls.length != sohuExtraUrls.length)
				return null;

			/*
			 * "http://data.vod.itc.cn/?prot=2&file="        +
			 * paths[i].replace('http://data.vod.itc.cn','') +
			 * '&new='+newpaths[i]
			 */
			int i;
			sohuUrl = "";
			for (i = 0; i < sohuUrls.length; i++) {
				Matcher match;
				match = Pattern.compile("http://data.vod.itc.cn(.+?)$").matcher(sohuUrls[i]);
				if (match.find() == false)
					System.exit(0);
				String part1 = match.group(1);
				Log.d("sohu", "part1=" + part1);

				String strUrl = "http://data.vod.itc.cn/?prot=2&file=" + part1 + "&new=" + sohuExtraUrls[i];
				content = getPictureData(strUrl);
				System.out.println(content);

				/*
				 * http://183.57.146.23/sohu/4/|425|113.90.234.243|3kYEI9wHApB9acO8Ooz1cOjZJjMze5PEnx-qfA..|1|0|1|1803
				 * url = link.split('|')[0].rstrip("/")+newpaths[i]+'?key='+key
				 */
				match = Pattern.compile("^(.+?)/\\|(.+?)\\|(.+?)\\|(.+?)\\|").matcher(content);
				if (match.find() == false)
					System.exit(0);

				String key = match.group(4);
				Log.d("sohu", "key=" + key);

				part1 = match.group(1);
				if (part1 == null)
					return null;
				strUrl = part1 + sohuExtraUrls[i] + "?key=" + key;
				list.add(strUrl);
				sohuUrl = sohuUrl + strUrl;
				if (i < sohuUrls.length - 1)
					sohuUrl = sohuUrl + " , ";
			}
			sohuFinalUrls = (String[]) list.toArray(new String[list.size()]);
			Log.d("sohu", "sohuUrl=" + sohuUrl);

			if (sohuFinalUrls.length == 1) {
				Intent intent1 = new Intent(CAST_INTENT_NAME);
				intent1.setDataAndType(
						Uri.parse(sohuUrl + "[@]" + sohuTitle + "[@]"
								+ sohuThumbUrl), null);
				startActivity(intent1);
				System.exit(0);
			}

			return sohuUrl;
		} catch (Exception e) {
			Log.e("sohu", e.toString());
			e.printStackTrace();
			//System.exit(0);
		}
		return null;
	}

    private void appendData(String url) {
        if (listData == null)
            return;
        geturl(url);
        for (int i = 1; i <= sohuFinalUrls.length; i++)
            listData.add(sohuTitle + " 片段" + i);
    }
    class DataLoadThread extends Thread {
    	private ArrayAdapter<String> adapter;
    	String url;
    	DataLoadThread(ArrayAdapter<String> adapter, String url) {
    		this.adapter = adapter;
    		this.url = url;
    	}
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                appendData(url);
                // 因为Android控件只能通过主线程（ui线程）更新，所以用此方法
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 当数据改变时调用此方法通知view更新
                        adapter.notifyDataSetChanged();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void listVideoSegs(String url) {
        listData = new ArrayList<String>();

		if (lv == null)
            return;

        listAdapter = new ArrayAdapter<String>(this, R.layout.video_browser,
                R.id.text1, listData);
        lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(this);

		DataLoadThread currentThread = new DataLoadThread(listAdapter, url);
		currentThread.start();
    }

	public boolean parseSohuData(JsonReader reader) throws Exception {
		String name;
		boolean thumbFound = false;
		boolean titleFound = false;
		boolean urlFound = false;

		reader.beginObject();
		while (reader.hasNext()) {
			name = reader.nextName();
			if (name.equals("data")) {
				reader.beginObject();
				while (reader.hasNext()) {
					name = reader.nextName();
					if (name.equals("tvName")) {
						titleFound = true;
						sohuTitle = reader.nextString();
						Log.d("sohu", "title=" + sohuTitle);
					} else if (name.equals("coverImg")) {
						thumbFound = true;
						sohuThumbUrl = reader.nextString();
						Log.d("sohu", "thumbUrl="+sohuThumbUrl);
					} else if (name.equals("clipsURL")) {
						Log.d("sohu", "parse clipsURL");
						sohuUrls = parseSohuUrls(reader);
					} else if (name.equals("su")) {
						Log.d("sohu", "parse su");
						sohuExtraUrls = parseSohuUrls(reader);
					} else {
						if (titleFound && thumbFound && urlFound)
							return true;
						reader.skipValue();
					}
				}
				reader.endObject();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return false;
	}
	private String[] parseSohuUrls(JsonReader reader) throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		String[] retStr = null;
		Log.d("sohu", "url list starts:");
		reader.beginArray();
		while (reader.hasNext()) {
			String s = reader.nextString();
			list.add(s);
			Log.d("sohu", s);
		}
		Log.d("sohu", "url list ends");
		reader.endArray();
		retStr = (String[]) list.toArray(new String[list.size()]);
		return retStr;
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

		if (VERSION.SDK_INT >= 17 && testobj != null)
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
