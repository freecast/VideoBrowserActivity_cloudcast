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
    private VideoCastManager mCastManager;
    private IVideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;

    private WebView webView = null;  
    public Handler handler = new Handler();  
    String httpurl = "XNjc4MjgwNTEy";
    String loadurl = ""; 
    String title = "";
    String sharedText = "";
    String youkuTitle = "null";
    String youkuThumbUrl = "null";
    boolean youkuDone = false;
    public MyObject testobj;

    class JsObject {
        @JavascriptInterface
        public String toString() { return "injectedObject"; }
     }

	public class MyObject {
	    private Handler handler = null;  
	    private WebView webView = null;  
	  
	    public MyObject(VideoBrowserActivity htmlActivity, Handler handler) {  
	        this.webView = (WebView)htmlActivity.findViewById(R.id.webView1);  
	        this.handler = handler;  
	    }  
	      
	    public void init(){  
	        //通过handler来确保init方法的执行在主线程中  
	        handler.post(new Runnable() {  
	              
	            public void run() {  
	            	Log.d("@@@@ VideoBrowserActivity", "run()"); 
	            	if (VERSION.SDK_INT < 17)
	                   webView.loadUrl("javascript:showHtmlcallJava2('" + httpurl + "')");  
	                
	            }  
	        });  
	    } 
	    
	    public void Java2Html() {
        	Log.d("@@@@ VideoBrowserActivity", "Java2Html()"); 
            webView.loadUrl("javascript:showHtmlcallJava2('" + httpurl + "')");  
             	    }
	    
	    @JavascriptInterface
	    public String HtmlcallJava2(final String param) {
	    	loadurl = param;	    	
	     	 try {
	     		getYoukuVideoInfo("http://v.youku.com/player/getPlayList/VideoIDS/"+httpurl);
	     	} catch (Exception e) {
	     		// TODO Auto-generated catch block
	     		e.printStackTrace();
	     	}

	     	 Intent intent1 = new Intent("XBMC.cast");
    		intent1.setDataAndType(Uri.parse(loadurl+"[@]" + youkuTitle + "[@]" + youkuThumbUrl), null);

    		Log.d("%%%%% startActivity(intent1)", loadurl); 
    		
    		startActivity(intent1);
	    		    		
	    	return "Html call Java : " + param;  
	    }     
		
	}	

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @SuppressLint("JavascriptInterface")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  //      VideoCastManager.checkGooglePlaySevices(this);
        Log.d(TAG, "onCreate was called");
        setContentView(R.layout.video_browser);


		 Intent intent = getIntent();
		 String action = intent.getAction();
		 String type = intent.getType();
		 
		 Log.d(TAG, "action=" + action);
		 Log.d(TAG, "type=" + type);
		 Log.d(TAG, "intent=" + intent.toString());
		 Log.d(TAG, "intent.extra=" + intent.getExtras().toString());
		 Log.d(TAG, "intent.StringExtra" + intent.getStringExtra(Intent.EXTRA_TEXT));

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
				str.append("\n\r");
			}
			Log.d(TAG, str.toString());
		}
		    
		 if (Intent.ACTION_SEND.equals(action) && type != null) {
			 if ("text/plain".equals(type)) {
				 sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
				 Log.v("%%%% get intent string = ", sharedText);
			 }
		 }
		 
         String target1 = "v.youku.com";
         
         int a = sharedText.indexOf(target1);
         if(a > 0)
         {
        	 CallYouku();
         }

         String target2 = "v.qq.com";
         
         int b = sharedText.indexOf(target2);
         if(b > 0)
         {
        	 CallTencent();
         }	
         
         
 //       ActionBar actionBar = getSupportActionBar();

 //       mCastManager = CastApplication.getCastManager(this);
/*
        // -- Adding MiniController
        mMini = (MiniController) findViewById(R.id.miniController1);
        mCastManager.addMiniController(mMini);

        mCastConsumer = new VideoCastConsumerImpl() {

            @Override
            public void onFailed(int resourceId, int statusCode) {

            }

            @Override
            public void onConnectionSuspended(int cause) {
                Log.d(TAG, "onConnectionSuspended() was called with cause: " + cause);
                com.google.sample.cast.refplayer.utils.Utils.
                        showToast(VideoBrowserActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                com.google.sample.cast.refplayer.utils.Utils.
                        showToast(VideoBrowserActivity.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final RouteInfo info) {
                if (!CastPreference.isFtuShown(VideoBrowserActivity.this)) {
                    CastPreference.setFtuShown(VideoBrowserActivity.this);

                    Log.d(TAG, "Route is visible: " + info);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mediaRouteMenuItem.isVisible()) {
                                Log.d(TAG, "Cast Icon is visible: " + info.getName());
                                showFtu();
                            }
                        }
                    }, 1000);
                }
            }
        };

        setupActionBar(actionBar);
        mCastManager.reconnectSessionIfPossible(this, false);
        */
    }

    private void CallYouku(){
    
  	  StringTokenizer commaToker = new StringTokenizer(sharedText,"_"); 
  	  String[] result = new String[commaToker.countTokens()]; //
  	  int i=0; 
  	  while(commaToker.hasMoreTokens()){ 
  	 
  	   result[i] = commaToker.nextToken(); 
  	   Log.d("%%%% result = ", result[i]);
  	   i++; 
  	  } 	
  		 
  	  StringTokenizer commaToker2 = new StringTokenizer(result[i-1],"."); 
  	  String[] result2 = new String[commaToker2.countTokens()]; //
  	  int m=0; 
  	  while(commaToker2.hasMoreTokens()){ 
  	 
  	   result2[m] = commaToker2.nextToken(); 
  	   Log.d("!!!!	result2 = ", result2[m]);
  	   m++; 
  	  } 
  	  
  	  StringTokenizer commaToker3 = new StringTokenizer(result[0],"：】"); 
  	  String[] result3 = new String[commaToker3.countTokens()]; //
  	  int n=0; 
  	  while(commaToker3.hasMoreTokens()){ 
  	 
  	   result3[n] = commaToker3.nextToken(); 
  	   Log.d("!!!!	result3 = ", result3[n]);
  	   n++; 
  	  } 	  
  	  
  	 title = result3[1]; 
  	  
  	 httpurl = result2[0];
  	 

  	 
  		 webView = (WebView)this.findViewById(R.id.webView1);
//  		 webView.setVisibility(View.INVISIBLE); 
//  		 webView.setVisibility(View.GONE); 

  	     /*webView.addJavascriptInterface(new JsObject(), "injectedObject");
  	     webView.loadData("", "text/html", null);
  	     webView.loadUrl("javascript:alert(injectedObject.toString())");
  	     
  	     Log.d("VideoBrowserActivity", "javascript:alert(url)");*/

  		 //设置字符集编码  
 		 webView.getSettings().setDefaultTextEncodingName("UTF-8");  
  		 //开启JavaScript支持  
  		 webView.getSettings().setJavaScriptEnabled(true);
  		 testobj = new MyObject(this,handler);
  		 webView.addJavascriptInterface(testobj, "myObject");  
  		 //加载assets目录下的文件  
  		 String url = "file:///android_asset/youkump4.html";  
  		 Log.d("@@@@ VideoBrowserActivity", "webView load youkump4.html");
  		 webView.loadUrl(url);	
  		 //webView.loadUrl("javascript:showHtmlcallJava2('" + httpurl + "')");
  		 
  		 Log.d("@@@@ VideoBrowserActivity", "webView.loadUrl(url)");
    	
    }

	public void getYoukuVideoInfo(String path) {
		final String fPath = path;
		try {
			getYoukuThumbAndTitle(fPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		new Thread() {
			@Override
			public void run() {
				try {
					getYoukuThumbAndTitle(fPath);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		*/
	}
	
	public void getYoukuThumbAndTitle(String path) throws Exception {
    	InputStream stream = null;
    	JsonReader reader = null;

    	try {
			stream = getWebPageStream(path);
	        byte[] data = readInputStream(stream);// 得到html的二进制数据  
	        String html = new String(data,"utf8");
	        System.out.println(html);

			stream = getWebPageStream(path);
			reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));

			parseYoukuData(reader);
		} finally {
			reader.close();
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

	public InputStream getWebPageStream(String path) throws Exception{  
        // 类 URL 代表一个统一资源定位符，它是指向互联网“资源”的指针。  
        URL url = new URL(path);  
        Log.v("getPictureData:  ", path);
        // 每个 HttpURLConnection 实例都可用于生成单个请求，  
        //但是其他实例可以透明地共享连接到 HTTP 服务器的基础网络  
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
        //设置 URL 请求的方法  
        conn.setRequestMethod("GET");  
        //设置一个指定的超时值（以毫秒为单位），  
        //该值将在打开到此 URLConnection 引用的资源的通信链接时使用。  
        conn.setConnectTimeout(6 * 1000);  
        // conn.getInputStream()返回从此打开的连接读取的输入流  
        
        System.out.println(conn.getResponseCode());
        if(conn.getResponseCode()==200)
        {
            InputStream inStream = conn.getInputStream();// 通过输入流获取html数据
            return inStream; 
        }
        return null;  
    } 
    
    public String getPictureData(String path) throws Exception{  
        // 类 URL 代表一个统一资源定位符，它是指向互联网“资源”的指针。  
        URL url = new URL(path);  
        Log.v("getPictureData:  ", path);
        // 每个 HttpURLConnection 实例都可用于生成单个请求，  
        //但是其他实例可以透明地共享连接到 HTTP 服务器的基础网络  
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
        //设置 URL 请求的方法  
        conn.setRequestMethod("GET");  
        //设置一个指定的超时值（以毫秒为单位），  
        //该值将在打开到此 URLConnection 引用的资源的通信链接时使用。  
        conn.setConnectTimeout(6 * 1000);  
        // conn.getInputStream()返回从此打开的连接读取的输入流  
        
        System.out.println(conn.getResponseCode());
        if(conn.getResponseCode()==200)
        {
        InputStream inStream = conn.getInputStream();// 通过输入流获取html数据  
        byte[] data = readInputStream(inStream);// 得到html的二进制数据  
        String html = new String(data,"utf8");  
        return html; 
        }
        return null;  
    }  	


public byte[] readInputStream(InputStream inStream) throws Exception{  
    //此类实现了一个输出流，其中的数据被写入一个 byte 数组  
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
    // 字节数组  
    byte[] buffer = new byte[1024];  
    int len = 0;  
    //从输入流中读取一定数量的字节，并将其存储在缓冲区数组buffer 中  
    while ((len = inStream.read(buffer)) != -1) {  
        // 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此输出流  
        outStream.write(buffer, 0, len);  
    }  
    inStream.close();  
    //toByteArray()创建一个新分配的 byte 数组。  
    return outStream.toByteArray();  
}  
    
    
    private void CallTencent(){
    
    	 new Thread(){  
      	   @Override  
      	   public void run()  
      	   {  
      	   //把网络访问的代码放在这里     
      		   
     	        try {  
  	            //String test = getPictureData("http://v.qq.com/cover/o/obr3rfx7xdatznl.html");  
  	            //Log.v("test come out: ", test);
  	            //System.out.println(test);
     	        	String pattStr;
     	        	Pattern pattern;
     	        	Matcher matcher;
     	        	String tencentThumbUrl = "null";
     	        	String tencentTitle = "null";
     	        	
       	        	String target0 = "m.";
       	        	int d = sharedText.indexOf(target0);
       	        	String test;
       	        	
       	        	if(d > 0)
       	        	{
       	        	int f = sharedText.length();
       	        	char g[]=new char[f-d-2];
       	        	sharedText.getChars(d+2,f,g,0);
    	            String h=new String(g);
    	            Log.v("zhengshi", sharedText);
    	            Log.v("zhengshi", h);
       	        	
    	            test = getPictureData("http://"+h); 
       	        	}
       	        	else
       	        	{
       	        		test = getPictureData(sharedText); 
       	        	}
       	        	System.out.println(test);
       	        	pattStr = new String("meta http-equiv=\"Content-Type\"");
       	        	pattern = Pattern.compile(pattStr);
       	        	matcher = pattern.matcher(test);
       	        	if (matcher.find()) {
       	        		matcher = Pattern.compile("url=(.+?)\"").matcher(test);
       	        		if (matcher.find()) {
       	        			String redirectUrl = matcher.group(1);
       	        			Log.d("tencent", "redirect_url=" + redirectUrl);
       	        			matcher = Pattern.compile("vid=(.+?)$").matcher(redirectUrl);
       	        			if (matcher.find()) {
       	        				String vid = matcher.group(1);
       	        				Log.d("tencent", "vid found in redirect url:" + vid);
           	        			String regStr = "src=\"http://(.+?)"+vid+"(.+?)\" alt=\"(.+?)\"";
           	        			test = getPictureData(redirectUrl);
           	        			Log.d("tencent", "searching thumb and title:" + regStr);
           	        			matcher = Pattern.compile(regStr).matcher(test);
           	        			if (matcher.find()) {
           	        				tencentThumbUrl = "http://"+matcher.group(1)+vid+matcher.group(2);
           	        				tencentTitle = matcher.group(3);
           	        			}       	        				
       	        			} else {
       	        				Log.d("tencent", "vid not found from redirect url");
       	        			}
       	        		}
       	        	} else {
           	        	//System.out.println(test);
           	        	pattStr = new String("pic :\"(.+?)\"");
           	        	System.out.println("zhengshi start pattern: " + pattStr);
           	        	pattern = Pattern.compile(pattStr);
           	        	matcher = pattern.matcher(test);
           	        	if(matcher.find()) {
           	        	  tencentThumbUrl = matcher.group(1);
           	        	  System.out.println(tencentThumbUrl);
           	        	} else {
           	        	  System.out.println("tencentThumbUrl not found");
           	        	}

           	        	pattStr = new String("title :\"(.+?)\"");
           	        	System.out.println("zhengshi start pattern: " + pattStr);
           	        	pattern = Pattern.compile(pattStr);
           	        	matcher = pattern.matcher(test);
           	        	if(matcher.find()) {
             	        	  tencentTitle = matcher.group(1);
             	        	  System.out.println(tencentTitle);
             	        	} else {
             	        	  System.out.println("title not found");
             	        	}
           	        	System.out.println("zhengshi end pattern");	
       	        	}

  	            String target = "vid:";
  	            
  	            int a = test.indexOf(target);
  	            
  	            char c[]=new char[11];
  	            test.getChars(a+5,a+16,c,0);
  	            String s=new String(c);
  	            Log.v("test", s);
  	            
  	            String test2 = getPictureData("http://vv.video.qq.com/geturl?vid="+s+"&otype=xml&platform=1&ran=0%2E9652906153351068"); 
  	            //System.out.println(test2);
  	            
  	            String target1 = "http";
  	            int b = test2.indexOf(target1);
  	            String target2 = "</url>";
  	            int m = test2.indexOf(target2);
  	            Log.v("test", b+"  "+m);
  	            
  	            char n[]=new char[m-b];
  	            test2.getChars(b,m,n,0);
  	            String k=new String(n);
  	            Log.v("test", k);
  	            
  		    	loadurl = k;	    	
  	    		Intent intent1 = new Intent("XBMC.cast");
  	    		intent1.setDataAndType(Uri.parse(loadurl+"[@]"+tencentTitle+"[@]"+tencentThumbUrl), null);

  	    		Log.d("%%%%% start Tencent ", loadurl); 
  	    		
  	    		startActivity(intent1);  	            
  	            
  	            
  	        } catch (Exception e) {  
  	            Log.e("GetHtmlCodeActivity", e.toString());  
  	          System.exit(0);
  	        }  
      	  }  
      	}.start();    	
    	
    	
    }
    
    
    
    private void setupActionBar(ActionBar actionBar) {
		Log.d(TAG, "setupActionBar() is called");
   //     actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
   //     getSupportActionBar().setIcon(R.drawable.actionbar_logo_castvideos);
  //      getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    Log.d(TAG, "onCreateOptionsMenu() is called. " + VERSION.SDK_INT);
        super.onCreateOptionsMenu(menu);
        
        if (VERSION.SDK_INT >= 17)
           testobj.Java2Html();
      //  getMenuInflater().inflate(R.menu.main, menu);

      //  mediaRouteMenuItem = mCastManager.
      //          addMediaRouterButton(menu, R.id.media_route_menu_item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    Log.d(TAG, "onOptionsItemSelected() is called");
   //     switch (item.getItemId()) {
   //         case R.id.action_settings:
   //             Intent i = new Intent(VideoBrowserActivity.this, CastPreference.class);
   //             startActivity(i);
  //              break;
  //      }
        return true;
    }

    private void showFtu() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    Log.d(TAG, "onKeyDown() is called");
     //   if (!mCastManager.isConnected()) {
    //        return super.onKeyDown(keyCode, event);
    //    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
         //   changeVolume(CastApplication.VOLUME_INCREMENT);
    //    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
        //    changeVolume(-CastApplication.VOLUME_INCREMENT);
   //     } else {
   //         return super.onKeyDown(keyCode, event);
   //     }
    return super.onKeyDown(keyCode, event);
       // return true;
    }

    private void changeVolume(double volumeIncrement) {
		Log.d(TAG, "changeVolume() is called");
      //  if (mCastManager == null) {
      //      return;
     //   }
     //   try {
     //       mCastManager.incrementVolume(volumeIncrement);
    //    } catch (Exception e) {
     //       Log.e(TAG, "onVolumeChange() Failed to change volume", e);
    //        com.google.sample.cast.refplayer.utils.Utils.handleException(this, e);
    //    }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
      //  mCastManager = CastApplication.getCastManager(this);
      //  if (null != mCastManager) {
      //      mCastManager.addVideoCastConsumer(mCastConsumer);
     //       mCastManager.incrementUiCounter();
     //   }

        super.onResume();
    }

    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause() is called");
      //  mCastManager.decrementUiCounter();
      //  mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy() is called");
        //if (null != mCastManager) {
        //    mMini.removeOnMiniControllerChangedListener(mCastManager);
        //}
        super.onDestroy();
    }

}
