package com.MyMobile;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.webkit.URLUtil;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.vidyo.vidyosample.activity.VidyoSampleActivity;
import com.vidyo.vidyosample.app.ApplicationJni;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends Activity implements ChatxEventListener  {

	private static final String TAG = "VidyoSampleActivity";

	private final String VIDYO_MY_ACCOUNT_RESPONDER_TAG = "MyMobile_VidyoMyAccountResponder";
	private final String VIDYO_JOIN_CONFERENCE_RESPONDER_TAG = "MyMobile_VidyoJoinConferenceResponder";

	// Offsets to place the video window below the top buttons.
	private final int landscapeOffset = 70;
	private final int portraitOffset = 70;

	// Time between engagement status requests
	private final long statusUpdateInterval = 2000;

	private static final int FILECHOOSER_RESULTCODE = 2888;
    	private ValueCallback<Uri> mUploadMessage;
    	private Uri mCapturedImageURI = null;
		private Intent mServiceIntent;
	private ValueCallback<Uri[]> mFilePathCallback;
	private String mCameraPhotoPath;
	public static final int INPUT_FILE_REQUEST_CODE = 1;

	private Chatx chatx;

	Handler message_handler;
	public static final int SHOW_MSG = 10;



	@Override
	protected void onDestroy() {
		Log.v(TAG,"MainActivity.onDestroy start...");
		super.onDestroy();
		if(chatx != null){
			if(chatx.getIsConnected() == true){
				chatx.Disconnect();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG,"MainActivity.onCreate start...");

		super.onCreate(savedInstanceState);

		if(chatx == null){
			Log.v(TAG,"MainActivity.onCreate start...chatx->null...");
		}
		else{
			Log.v(TAG,"MainActivity.onCreate start...chatx is not null...");
		}

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (savedInstanceState == null) {
			Object object = (Object)getApplication();
			ApplicationJni app;
			if (object instanceof ApplicationJni) {
				app = (ApplicationJni)object;


				// THIS MUST BE CALLED TO INITIALIZE THE VIDYOCLIENT JNI LIBRARY
				Log.d(TAG, "Initializing JNI");
				app.LmiAndroidJniInitialize();
			}
		}

			setContentView(R.layout.activity_main);



			
			WebView webview = (WebView)findViewById(R.id.webView);
			webview.setWebChromeClient(new WebChromeClient() {
	             
	            // openFileChooser for Android 3.0+
	            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){  
	                
	                // Update message
	                mUploadMessage = uploadMsg;
	                 
	                try{    
	                 
	                    // Create AndroidExampleFolder at sdcard
	                     
	                    File imageStorageDir = new File(
	                                           Environment.getExternalStoragePublicDirectory(
	                                           Environment.DIRECTORY_PICTURES)
	                                           , "AndroidExampleFolder");
	                                            
	                    if (!imageStorageDir.exists()) {
	                        // Create AndroidExampleFolder at sdcard
	                        imageStorageDir.mkdirs();
	                    }
	                     
	                    // Create camera captured image file path and name 
	                    File file = new File(
	                                    imageStorageDir + File.separator + "IMG_"
	                                    + String.valueOf(System.currentTimeMillis()) 
	                                    + ".jpg");
	                                     
	                    mCapturedImageURI = Uri.fromFile(file); 
	                     
	                    // Camera capture image intent
	                    final Intent captureIntent = new Intent(
	                                                  android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	                                                   
	                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
	                    
	                    Intent i = new Intent(Intent.ACTION_GET_CONTENT); 
	                    i.addCategory(Intent.CATEGORY_OPENABLE);
	                    i.setType("image/*");
	                     
	                    // Create file chooser intent
	                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
	                     
	                    // Set camera intent to file chooser 
	                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
	                                           , new Parcelable[] { captureIntent });
	                     
	                    // On select image call onActivityResult method of activity
	                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
	                     
	                  }
	                 catch(Exception e){
	                     Toast.makeText(getBaseContext(), "Exception:"+e, 
	                                Toast.LENGTH_LONG).show();
	                 }
	                 
	            }
	             
	            // openFileChooser for Android < 3.0
	            public void openFileChooser(ValueCallback<Uri> uploadMsg){
	                openFileChooser(uploadMsg, "");
	            }
	             
	            //openFileChooser for other Android versions
	            public void openFileChooser(ValueCallback<Uri> uploadMsg, 
	                                       String acceptType, 
	                                       String capture) {
	                                        
	                openFileChooser(uploadMsg, acceptType);
	            }
	            //For API 21 (Lollipop)
	            public boolean onShowFileChooser(
	                    WebView webView, ValueCallback<Uri[]> filePathCallback,
	                    WebChromeClient.FileChooserParams fileChooserParams) {

	                // Double check that we don't have any existing callbacks
	                if(mFilePathCallback != null) {
	                    mFilePathCallback.onReceiveValue(null);
	                }
	                mFilePathCallback = filePathCallback;

	                // Set up the take picture intent
	                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	                    // Create the File where the photo should go
	                    File photoFile = null;
	                    try {
	                        photoFile = createImageFile();
	                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
	                    } catch (IOException ex) {
	                        // Error occurred while creating the File
	                        ex.printStackTrace();
	                    }

	                    // Continue only if the File was successfully created
	                    if (photoFile != null) {
	                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
	                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
	                                Uri.fromFile(photoFile));
	                    } else {
	                        takePictureIntent = null;
	                    }
	                }

	                // Set up the intent to get an existing image
	                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
	                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
	                contentSelectionIntent.setType("image/*");

	                // Set up the intents for the Intent chooser
	                Intent[] intentArray;
	                if(takePictureIntent != null) {
	                    intentArray = new Intent[]{takePictureIntent};
	                } else {
	                    intentArray = new Intent[0];
	                }

	                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
	                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
	                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
	                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

	                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

	                return true;
	            }
	 
	        });  
			
			if (savedInstanceState != null )
			{
				
				/* Set Webview properties */		
			    webview.getSettings().setJavaScriptEnabled(true);
				webview.addJavascriptInterface(new  JavaScriptHandler(this), "mobileHost");
			    webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
			    webview.getSettings().setDomStorageEnabled(true);
			    webview.getSettings().setLoadWithOverviewMode(true);
			    webview.getSettings().setUseWideViewPort(true);
			    webview.getSettings().setGeolocationEnabled(true);
			    webview.getSettings().setLoadsImagesAutomatically(true);
			    webview.getSettings().setAppCacheEnabled(false);
			    webview.getSettings().setDatabaseEnabled(true);
			    webview.getSettings().setBuiltInZoomControls(true);	
			    webview.getSettings().setSupportZoom(true);	
			    webview.getSettings().setAllowFileAccess(true);
			    webview.getSettings().setDomStorageEnabled(true); 
		    
				webview.setDownloadListener(new DownloadListener() {

			    	 @Override
				        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
			    		  
						  mServiceIntent =
			    	                new Intent(getApplicationContext(), DownloadFiles.class)
			    	                        .setData(Uri.parse(url));
			    		  mServiceIntent.putExtra("com.MyMobile.mimeType", mimeType);
			    		  
			    		  String filename = URLUtil.guessFileName(url,contentDisposition,mimeType);
			    		  mServiceIntent.putExtra("com.MyMobile.fileName", filename);
			    	        Toast.makeText(getApplicationContext(), "Download started", 
			    	        		Toast.LENGTH_LONG).show();
			    	        
			    		  startService(mServiceIntent);
			    		  
			    	 	}

			    });
				
			    /* Restore the Webview state if previously opened */
			    webview.restoreState(savedInstanceState);
			    
		        webview.setWebViewClient(new WebViewClient() {
		            public boolean shouldOverrideUrlLoading(WebView view, String url) {
		                view.loadUrl(url);
		                return true;
		            }

		            /* Load an error page if there is no Internet connection or in case of any other errors */
		            public void onReceivedError(WebView view, int errorCod,String description, String failingUrl) {
		                view.loadUrl(getString(R.string.noInternetConnectionURL));
		           	}

		        });
			}
			else
			{
			    /* Set Webview properties */ 
			    webview .getSettings().setJavaScriptEnabled(true);
				webview.addJavascriptInterface(new  JavaScriptHandler(this), "mobileHost");
			    webview .getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
			    webview .getSettings().setDomStorageEnabled(true);
			    webview.getSettings().setLoadWithOverviewMode(true);
			    webview.getSettings().setUseWideViewPort(true);
			    webview.getSettings().setGeolocationEnabled(true);
			    webview.getSettings().setLoadsImagesAutomatically(true);
			    webview.getSettings().setAppCacheEnabled(false);
			    webview.getSettings().setDatabaseEnabled(true);
			    webview.getSettings().setBuiltInZoomControls(true);	
			    webview.getSettings().setSupportZoom(true);	
			    webview.getSettings().setAllowFileAccess(true);
			    webview.getSettings().setDomStorageEnabled(true);
				
			  	webview.setDownloadListener(new DownloadListener() {

			    	 @Override
				        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
			    		  
						  mServiceIntent =
			    	                new Intent(getApplicationContext(), DownloadFiles.class)
			    	                        .setData(Uri.parse(url));
			    		  mServiceIntent.putExtra("com.MyMobile.mimeType", mimeType);
			    		  
			    		  String filename = URLUtil.guessFileName(url,contentDisposition,mimeType);
			    		  mServiceIntent.putExtra("com.MyMobile.fileName", filename);
			    	        Toast.makeText(getApplicationContext(), "Download started", 
			    	        		Toast.LENGTH_LONG).show();
			    	        
			    		  startService(mServiceIntent);
			    		  
			    	 	}

			    });
			    /* Load the Webview URL */	    
		  	    webview.loadUrl(getString(R.string.MobileURL));
		  		
		            webview.setWebViewClient(new WebViewClient() {
		            public boolean shouldOverrideUrlLoading(WebView view, String url) {
		                view.loadUrl(url);
		                return true;
		            }

		            /* Load an error page if there is no Internet connection or in case of any other errors */
		            public void onReceivedError(WebView view, int errorCod,String description, String failingUrl) {
		                view.loadUrl(getString(R.string.noInternetConnectionURL));
		           	
		            }

		        });
		        
			}


		message_handler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what=msg.what;
				switch (what) {
					case SHOW_MSG:
						MyShowMsg(msg);
						break;
					default:
						break;
				}

				super.handleMessage(msg);
			}
		};

		chatx = new Chatx(this);
		chatx.addEventListener(this);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String chatServerIP = settings.getString("ChatServerIP", "121.199.10.37");

		chatx.setServerIP(chatServerIP);

		/*
		Preference chatServerIPref = findPreference("ChatServerIP");
		*/


		chatx.setServerPort(7080);
		chatx.Connect();


	}



	private void MyShowMsg(Message msg)
	{
		Bundle bundle = msg.getData();
		String data = bundle.getString("text");

		try {
			Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
		}
		catch(Exception ex){
			Log.d(TAG,"Toast.makeText..." + ex.getMessage());
		}

	}
	private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "IMG_" + String.valueOf(System.currentTimeMillis())  + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }
    // Return here when file selected from camera or from SDcard
    @Override 
    protected void onActivityResult(int requestCode, int resultCode,  
                                       Intent intent) { 
         
     if(requestCode==FILECHOOSER_RESULTCODE)  
     {  
        
            if (null == this.mUploadMessage) {
                return;
 
            }
 
           Uri result=null;
            
           try{
                if (resultCode != RESULT_OK) {
                     
                    result = null;
                     
                } else {
                     
                    // retrieve from the private variable if the intent is null
                    result = intent == null ? mCapturedImageURI : intent.getData(); 
                } 
            }
            catch(Exception e)
            {
                Toast.makeText(getApplicationContext(), "activity :"+e,
                 Toast.LENGTH_LONG).show();
            }
             
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
            return;
     }
     else
     {
         if((requestCode == INPUT_FILE_REQUEST_CODE) && (mFilePathCallback != null))
         {

	         Uri[] results = null;
	
	         // Check that the response is a good one
	         if(resultCode == Activity.RESULT_OK) {
	             if(intent == null) {
	                 // If there is not data, then we may have taken a photo
	                 if(mCameraPhotoPath != null) {
	                     results = new Uri[]{Uri.parse(mCameraPhotoPath)};
	                 }
	             } else {
	                 String dataString = intent.getDataString();
	                 if (dataString != null) {
	                     results = new Uri[]{Uri.parse(dataString)};
	                 }
	
	             }
	         }
	
	         mFilePathCallback.onReceiveValue(results);
	         mFilePathCallback = null;
	         return;
         }
      }
     super.onActivityResult(requestCode, resultCode, intent);
         
    }
	
	/* Check if network is available */
	public boolean isOnline() 
	{
	    /* Getting the ConnectivityManager. */
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

	    /* Getting NetworkInfo from the Connectivity manager. */
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();

	    /* If I received an info and isConnectedOrConnecting return true then there is an Internet connection. */
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) 
	    {
	        return true;
	    }
	    return false;
	}
	

	/* Handle back button pressed event. 
	   Open previous page when back button is pressed. */	
	@Override
	public void onBackPressed()
	{
		WebView webview = (WebView)findViewById(R.id.webView);
		
	    if(webview.canGoBack())
	        webview.goBack();
	    else
	        super.onBackPressed();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/* Inflate the menu; this adds items to the action bar if it is present. */
		return true;
	}
	
	  /* Request updates at startup */
	  @Override
	  protected void onResume() {
		  Log.v(TAG,"onResume");
	    super.onResume();
	  }

	  /* Remove the locationlistener updates when Activity is paused */
	  @Override
	  protected void onPause() {
		  Log.v(TAG,"onPause");
	    super.onPause();
	  }


  
	  /* Save the current state */
	  @Override
	  protected void onSaveInstanceState(Bundle savedInstanceState) {
		  WebView webview = (WebView)findViewById(R.id.webView);
	      webview.saveState(savedInstanceState);
	  }


	  /* Handle the home button pressed event */
	  public void goHome(View view)
	  {
		  WebView webview = (WebView)findViewById(R.id.webView);
		  webview.loadUrl(getString(R.string.MobileURL));
	  }
	  
	  /* handle the back button pressed event */
	  public void goBack(View view)
	  {
		  WebView webview = (WebView)findViewById(R.id.webView);
			
		    if(webview.canGoBack())
		        webview.goBack();
		    else
		        super.onBackPressed();
	  }

	 public void goJoinVideo(View view)
	 {
		 WebView webview = (WebView)findViewById(R.id.webView);
		 webview.loadUrl("javascript:getVideoUserInfoJoin()");

	 }

	public void goSetting(View view)
	{

		final Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void goChat(View view)
	{
		ChatActivity.chatx = chatx;
		final Intent intent = new Intent(this, ChatActivity.class);
		startActivity(intent);

		/*
		Intent intentbrd = new Intent(ChatActivity.broadcastaction);
		intentbrd.putExtra("data", "yes i am data");
		sendBroadcast(intentbrd);
		*/

	}

	  public void goVideo(View view)
	  {

		  //切换到VidyoSampleActivity,登录并呼叫

			/*
		  final Intent intent = new Intent(this, VidyoSampleActivity.class);

		  intent.putExtra("server", "http://vidyochina.cn");
		  intent.putExtra("username","cc03");
		  intent.putExtra("password", "123456");
		  intent.putExtra("Dest", "cc01");
		  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		  startActivity(intent);
			*/

		  WebView webview = (WebView)findViewById(R.id.webView);
		  webview.loadUrl("javascript:getVideoUserInfo()");


	  }

	public  void onDataFromWebView(String data)
	{
		try {
			JSONObject json = new JSONObject(data);
			String cmd = json.getString("cmd").toString();
			if(cmd.equals("LOGIN_AND_CALL")){
				String videoUserID = json.getString("user").toString();
				String videoDestUserID = json.getString("dest").toString();
				String videoUserPwd = json.getString("password").toString();
				String url = json.getString("url").toString();
				Log.v(TAG,"LOGIN_AND_CALL...user->" + videoUserID + "...videoUserPwd->" + videoUserPwd + "...url->" + url + "...Dest->" +  videoDestUserID);

				VidyoSampleActivity.chatx = chatx;
				//切换到VidyoSampleActivity,登录并呼叫
				final Intent intent = new Intent(this, VidyoSampleActivity.class);
				intent.putExtra("server", url);
				intent.putExtra("username",videoUserID);
				intent.putExtra("password", videoUserPwd);
				intent.putExtra("Dest", videoDestUserID);
				intent.putExtra("CallType", "CALLDIRECT");
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);


			}
			else if(cmd.equals("LOGIN_AND_JOIN")) {
				String videoUserID = json.getString("user").toString();
				String videoDestUserID = json.getString("dest").toString();
				String videoUserPwd = json.getString("password").toString();
				String url = json.getString("url").toString();
				Log.v(TAG,"LOGIN_AND_JOIN...user->" + videoUserID + "...videoUserPwd->" + videoUserPwd + "...url->" + url + "...Dest->" +  videoDestUserID);

				VidyoSampleActivity.chatx = chatx;

				//切换到VidyoSampleActivity,登录并呼叫
				final Intent intent = new Intent(this, VidyoSampleActivity.class);
				intent.putExtra("server", url);
				intent.putExtra("username",videoUserID);
				intent.putExtra("password", videoUserPwd);
				intent.putExtra("Dest", videoDestUserID);
				intent.putExtra("CallType", "JOIN");
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			else if(cmd.equals("GUEST_JOIN")) {
				String host = json.getString("host").toString();
				String port = json.getString("port").toString();
				String key = json.getString("key").toString();
				String userName = json.getString("userName").toString();
				String pin = json.getString("pin").toString();

				Log.v(TAG,"GUEST_JOIN...host->" + host + "...port->" + port + "...key->" + key + "...userName->" +  userName + "...pin->" + pin);


				//切换到VidyoSampleActivity,登录并呼叫
				VidyoSampleActivity.chatx = chatx;
				final Intent intent = new Intent(this, VidyoSampleActivity.class);
				intent.putExtra("host", host);
				intent.putExtra("port",port);
				intent.putExtra("key", key);
				intent.putExtra("userName", userName);
				intent.putExtra("pin", "");
				intent.putExtra("CallType", "GUEST_JOIN");
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			else if(cmd.equals("VideoCallAcd")){
				String CallData = json.getString("CallData").toString();
				Log.v(TAG,"VideoCallAcd...CallData->" + CallData);

				chatx.VideoCall("",CallData);

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void OnChatEvtConnected() {
		final ImageButton buttonChat = (ImageButton) findViewById(R.id.imageButtonChat);
		buttonChat.setImageResource(R.drawable.chat);
	}

	@Override
	public void OnChatEvtDisConnected() {
		final ImageButton buttonChat = (ImageButton) findViewById(R.id.imageButtonChat);
		buttonChat.setImageResource(R.drawable.chatoffline);
	}

	@Override
	public void OnNoAgents(String Reason){

	}

	private String getRunningActivityName(){
		ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
		return runningActivity;

	}

	@Override
	public void OnChatEvtNewMsg(String msg) {
		//解析msg
		try {
			StringReader rd = new StringReader(msg);
			InputSource in = new InputSource(rd);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);

			NodeList nl = doc.getElementsByTagName("Action");
			if(nl == null){
				Log.v(TAG,"OnChatEvtNewMsg...Action not found...ignore and return...");
				return ;
			}

			Node nd = nl.item(0);
			String action = nd.getTextContent();
			Log.v(TAG,"OnChatEvtNewMsg...Action->" + action);

			/*
			if(action.equals("CallResult")) {


			}
			else if(action.equals("LoginResponse")) {
				nl = doc.getElementsByTagName("Data");
				nd = nl.item(0);
				String FileUploadUrl = nd.getAttributes().getNamedItem("FileUploadUrl").getNodeValue();
				chatx.setFileUploadUrl(FileUploadUrl);
			}
			else
			*/

			if(action.equals("VideoCallResult")){
				nl = doc.getElementsByTagName("Data");
				nd = nl.item(0);
				String Result = nd.getAttributes().getNamedItem("Result").getNodeValue();
				String Reason = nd.getAttributes().getNamedItem("Reason").getNodeValue();

				if(Result.equals("-1")) {
					Message tmsg = new Message();
					tmsg.what = SHOW_MSG;
					Bundle data = new Bundle();
					if (Reason.equals("No Agent Avail")) {

						data.putString("text", "沒有空閒坐席");

					} else {
						data.putString("text", Reason);
					}

					tmsg.setData(data);

					message_handler.sendMessage(tmsg);


				}

			}
			else if(action.equals("VideoGuestJoin")){
				//VideoGuestJoin

				String currentActivity = getRunningActivityName();
				Log.v(TAG,"currentActivity->" + currentActivity);
				if(currentActivity.contains("VidyoSampleActivity")){
					Log.v(TAG,"currentActivity->" + currentActivity + "...in video now...ignore and return...");
					chatx.VideoGuestJoinFailed("访客已经在视频中");
					return;
				}

				Message tmsg = new Message();
				tmsg.what = SHOW_MSG;
				Bundle data = new Bundle();
				data.putString("text", "坐席已应答，视频会议连线中");
				tmsg.setData(data);

				message_handler.sendMessage(tmsg);

				nl = doc.getElementsByTagName("Data");
				nd = nl.item(0);
				String RoomKey = nd.getAttributes().getNamedItem("RoomKey").getNodeValue();
				String RoomPin = nd.getAttributes().getNamedItem("RoomPin").getNodeValue();
				String VidyoUrl = nd.getAttributes().getNamedItem("VidyoUrl").getNodeValue();

				VidyoUrl = VidyoUrl.replace("http://","");
				//String host = "vidyochina.cn";
				String host = VidyoUrl;

				String port = "80";
				String userName = chatx.getvName();
				Log.v(TAG,"GUEST_JOIN...host->" + host + "...port->" + port + "...key->" + RoomKey + "...userName->" +  userName + "...pin->" + RoomPin);


				VidyoSampleActivity.chatx = chatx;
				//切换到VidyoSampleActivity,登录并呼叫
				final Intent intent = new Intent(this, VidyoSampleActivity.class);
				intent.putExtra("host", host);
				intent.putExtra("port",port);
				intent.putExtra("key", RoomKey);
				intent.putExtra("userName", userName);
				intent.putExtra("pin", RoomPin);
				intent.putExtra("CallType", "GUEST_JOIN");
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);

				chatx.VideoGuestJoinSucc();

			}
			else if(action.equals("VideoQuit")){
				String currentActivity = getRunningActivityName();
				Log.v(TAG,"currentActivity->" + currentActivity);
				if(!currentActivity.contains("VidyoSampleActivity")){
					Log.v(TAG,"currentActivity->" + currentActivity + "...not in video now...ignore and return...");

					return;
				}

				VidyoSampleActivity.chatx = chatx;
				final Intent intent = new Intent(this, VidyoSampleActivity.class);
				intent.putExtra("Cmd", "VideoQuit");

				startActivity(intent);
			}

		}
		catch(Exception ex){
			Log.v(TAG,"OnChatEvtNewMsg..." + ex.getMessage());
			ex.printStackTrace();
		}
	}
}
