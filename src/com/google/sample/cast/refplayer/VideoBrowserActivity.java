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
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class VideoBrowserActivity extends Activity {

	private static final String TAG = "VideoBrowserActivity";
	private static final String TAG_TENCENT = "VideoBrowserActivity_tencent";
	private static final String CAST_INTENT_NAME = "CloudCast";

	private VideoCastManager mCastManager;
	private IVideoCastConsumer mCastConsumer;
	private MiniController mMini;
	private MenuItem mediaRouteMenuItem;

	private WebView webView = null;
	public Handler handler = new Handler();
	private Button buttonSD;
	private Button buttonHD;
	private Button buttonFHD;
	private String MyDefinitionType;
	private String LeTVurl;

	String youkuTitle = "优酷视频";
	String youkuThumbUrl = "http://static.youku.com/index/img/header/yklogo.png?qq-pf-to=pcqq.c2c";
	String youkuUrl = "http://www.youku.com";
	String youkuVid = "null";

	String sohuTitle = "搜狐视频";
	String sohuThumbUrl = "http://i1.letvimg.com/img/201206/29/iphonelogo.png";
	String sohuUrl = "http://www.sohu.com";
	String[] sohuUrls = null;
	String[] sohuExtraUrls = null;
	String sohuVid = "null";

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
		
		
		castIntent(getIntent());


	}
	
	   class submitOnClieckListener1 implements OnClickListener{  
	        @Override  
	        public void onClick(View v) {  
	//本地机器部署为服务器，从本地下载a.txt文件内容在textView上显示           

	            new Thread(){  
	                public void run(){
	                	MyDefinitionType = "normal";
	                	Log.v("submitOnClieckListener1","normal");
		            	RealcastLeTV(LeTVurl,MyDefinitionType); 
	                	Log.v("submitOnClieckListener1","normal");
	                    }                     
	            }.start();                        
	        }  
	          
	    }   
	  	
	   class submitOnClieckListener2 implements OnClickListener{  
	        @Override  
	        public void onClick(View v) {  
	//本地机器部署为服务器，从本地下载a.txt文件内容在textView上显示           

	            new Thread(){  
	                public void run(){
	                	MyDefinitionType = "high";
		            	RealcastLeTV(LeTVurl,MyDefinitionType);  
	                	Log.v("submitOnClieckListener1","high");
	                    }                     
	            }.start();                        
	        }  
	          
	    } 	
	   
	   class submitOnClieckListener3 implements OnClickListener{  
	        @Override  
	        public void onClick(View v) {  
	//本地机器部署为服务器，从本地下载a.txt文件内容在textView上显示           

	            new Thread(){  
	                public void run(){
	                	MyDefinitionType = "super";
		            	RealcastLeTV(LeTVurl,MyDefinitionType);  
	                	Log.v("submitOnClieckListener1","super");
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

		matcher = Pattern.compile("http://.*v.youku.com(.+?)/id_(.+?).html")
				.matcher(extraText);
		if (matcher.find()) {
			youkuVid = matcher.group(2);
			Log.d(TAG, "youku url detected: " + matcher.group(0));
			Log.d(TAG, "youku vid: " + youkuVid);
			buttonSD.setVisibility(View.INVISIBLE);
			buttonHD.setVisibility(View.INVISIBLE);
			buttonFHD.setVisibility(View.INVISIBLE);
			castYouku(youkuVid);
			return;
		}

		matcher = Pattern.compile("http://.*v.qq.com/(.+?).html").matcher(
				extraText);
		if (matcher.find()) {
			Log.d(TAG, "tencent url detected: " + matcher.group(0));
			Log.d(TAG, "tencent partial url: " + matcher.group(1));
			buttonSD.setVisibility(View.INVISIBLE);
			buttonHD.setVisibility(View.INVISIBLE);
			buttonFHD.setVisibility(View.INVISIBLE);
			castTencent("http://v.qq.com/" + matcher.group(1) + ".html");
			return;
		}

		// http://m.letv.com/vplay_20020870.html
		// http://www.letv.com/ptv/vplay/20020870.html
		matcher = Pattern.compile("http://.*letv.com/vplay_(.+?).html")
				.matcher(extraText);
		if (matcher.find()) {
			Log.d(TAG, "letv url detected: " + matcher.group(0));
			Log.d(TAG, "letv vplay id: " + matcher.group(1));
			castLeTV("http://www.letv.com/ptv/vplay/" + matcher.group(1)
					+ ".html");
			return;
		}

		// http://m.tv.sohu.com/v1647325.shtml?channeled=1210010500
		// http://tv.sohu.com/20140306/n396178078.shtml
		matcher = Pattern.compile("http://m.tv.sohu.com/v(.+?).shtml(.+?)")
				.matcher(extraText);
		if (matcher.find()) {
			buttonSD.setVisibility(View.INVISIBLE);
			buttonHD.setVisibility(View.INVISIBLE);
			buttonFHD.setVisibility(View.INVISIBLE);
			Log.d(TAG, "sohu url detected: " + matcher.group(0));
			Log.d(TAG, "sohu vid: " + matcher.group(1));
			castSohu(matcher.group(0));
			return;
		}

		Log.d(TAG, "no existing cast for url: " + extraText);
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
					String tencentThumbUrl = "http://imgcache.gtimg.cn/tencentvideo_v1/vstyle/web/v3/style/images/logo.png";
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
					System.exit(0);
				}
			}
		}.start();
	}

	private void castLeTV(final String url) {
		new Thread() {
			@Override
			public void run() {
				try {	
						Matcher matcher;
						String LeTVThumbUrl = "http://i1.letvimg.com/img/201206/29/iphonelogo.png";
						String LeTVTitle = "乐视视频";
						String content;
						String LeTVVid = "null";
						
						LeTVurl = url;

						content = getPictureData("http://www.flvcd.com/parse.php?kw="
								+ url + "&format=" + "normal");
						//System.out.println(content);

						matcher = Pattern.compile("format=high(.+?)").matcher(
								content);
						if (matcher.find()) {
							buttonHD.setVisibility(View.VISIBLE);
							Log.d("LeTV", "buttonHD= VISIBLE");
						}else{
							buttonHD.setVisibility(View.INVISIBLE);
							Log.d("LeTV", "buttonHD= INVISIBLE");
						}
						
						matcher = Pattern.compile("format=super(.+?)").matcher(
								content);
						if (matcher.find()) {
							buttonFHD.setVisibility(View.VISIBLE);
							Log.d("LeTV", "buttonFHD= VISIBLE");							
						}else{
							buttonFHD.setVisibility(View.INVISIBLE);
							Log.d("LeTV", "buttonFHD= INVISIBLE");		
						}
						
						buttonSD.setVisibility(View.VISIBLE);
						Log.d("LeTV", "buttonSD= VISIBLE");

					}
				catch (Exception e) {
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

					matcher = Pattern.compile("img:	\"(.+?)\"").matcher(
							content);
					if (matcher.find()) {
						LeTVThumbUrl = matcher.group(1);
						Log.d("LeTVreal", "LeTVThumbUrl=" + LeTVThumbUrl);
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
	 * for mobile url:
	 * http://m.tv.sohu.com/v1647325.shtml?channeled=1210010500
	 */
	private void castSohu(final String url) {
		new Thread() {
			@Override
			public void run() {
				try {
					Matcher matcher;
					String content;

					matcher = Pattern.compile("m.tv.sohu.com/v(.+?).shtml").matcher(url);
					if (matcher.find()) {
						sohuVid = matcher.group(1);
						Log.d("sohu", "vid=" + sohuVid);
					} else {
						Log.d("sohu", "vid not found in: " + url);
						System.exit(0);
					}

					// http://hot.vrs.sohu.com/vrs_flash.action?vid=1475503
					content = getPictureData("http://hot.vrs.sohu.com/vrs_flash.action?vid="
							+ sohuVid);
					InputStream stream = null;
					JsonReader reader = null;

					stream = getWebPageStream("http://hot.vrs.sohu.com/vrs_flash.action?vid="
							+ sohuVid);
					reader = new JsonReader(new InputStreamReader(stream,
							"UTF-8"));

					parseSohuData(reader);
					if (sohuUrls.length != sohuExtraUrls.length)
						return;

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
							return;
						strUrl = part1 + sohuExtraUrls[i] + "?key=" + key;
						sohuUrl = sohuUrl + strUrl;
						if (i < sohuUrls.length - 1)
							sohuUrl = sohuUrl + " , ";
					}
					Log.d("sohu", "sohuUrl=" + sohuUrl);

					Intent intent1 = new Intent(CAST_INTENT_NAME);
					intent1.setDataAndType(
							Uri.parse(sohuUrl + "[@]" + sohuTitle
									+ "[@]" + sohuThumbUrl), null);
					startActivity(intent1);
					System.exit(0);
				} catch (Exception e) {
					Log.e("sohu", e.toString());
					System.exit(0);
				}
			}
		}.start();
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
