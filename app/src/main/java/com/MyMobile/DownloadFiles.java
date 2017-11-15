/**
 * Downloads the files in background.
 */
package com.MyMobile;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;


public class DownloadFiles extends IntentService {

	public DownloadFiles() 
    {
      super(null);
    }
    
	public DownloadFiles(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	// Defines and instantiates an object for handling status updates.
    
	protected void onHandleIntent(Intent intent) {
		
        // Gets data from the incoming Intent
        String url = intent.getDataString();
        String mimeType = intent.getStringExtra("com.MyMobile.mimeType");
        String fileName = intent.getStringExtra("com.MyMobile.fileName");
        
	    final DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Request request = new DownloadManager.Request(Uri.parse(url));
        request.setMimeType(mimeType);
        request.setTitle(fileName);

        //Persist download notification in the status bar after the download completes (Android 3.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        
        dm.enqueue(request);
        
    }
}
