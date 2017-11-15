package com.MyMobile;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by zhutong on 2016/5/6.
 */
public class JavaScriptHandler {
    Context mContext;

    public JavaScriptHandler(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void onData(String data) {
        Log.v("MyMobile", "result received->" + data);
        ((MainActivity)mContext).onDataFromWebView(data);
    }

}
