package com.completeinnovations.ert.receipt;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.restapi.ERTRestApi;
import com.completeinnovations.ert.restapi.ERTRestApi.ERTRestApiError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * ERT Receipt handler
 * 
 * Usage scenario
 * 
 * Call createTmpReceiptName to generate a tmp file name,
 * Call ReceiptPicker to take a picture and store it as the tmp file
 *  
 * save the expense item on server to get the ID
 * Call renameReceiptFile to rename the tmp file to a name which is consistent with server.
 * 
 * 
 * @author BoH
 */

public class ERTReceipt {
	public static final String TAG = ERTReceipt.class.getSimpleName();
	/**
	 * get the Uri of specified receipt 
	 * 
	 * @param fileName the receipt name with extension
	 * @return Uri An Uri to the receipt in cache
	 */
	public static Uri getUri(String fileName) {
		
		File tmpFile = new File(getERTDirectory() + File.separator + fileName);
		
		return Uri.fromFile(tmpFile);
	}
	
	public static String getAbsolutePath(String fileName) {
		String fullPath;
		fullPath = getERTDirectory() + File.separator + fileName;
		
		return fullPath;
	}
	
	/**
	 * Create a file for the receipt in cache
	 * @param name expense name (without extension such as .jpg)
	 * @return
	 */
	public static Uri createReceiptFile(String name) {
		
	    File mediaFile = new File( getERTDirectory() + File.separator +
	        name + ".jpg");
	    
	    // if the name is taken, try a new one 
	    int i=0;
	    while( mediaFile.exists() ) {
		    mediaFile = new File(getERTDirectory() + File.separator +
			        name + i++ + ".jpg");			    	    	
	    }
	    
	    return Uri.fromFile( mediaFile );
	
	}	
	
	/**
	 * Get ERT external storage directory
	 * @return
	 */
	private static String getERTDirectory() {
		
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "ERT");
	    
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ) {
            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("ERT Receipt", "failed to create directory");
                    return null;
                }
            }
        }
	    
	    return mediaStorageDir.getPath();
	}
	
	

	public static interface ReceiptReadyListener {
		void onReceiptReady(Uri receiptUri) ;
	}
	
	private static final String RECEIPT_IMGAGE_NAME = "%d_%s";
	
	
	
	public static void loadReceipt(long id, String receiptName, final ReceiptReadyListener listener, Context context ) {

		File tgtFile ;		
		tgtFile = new File(getAbsolutePath(receiptName));
		
		if( tgtFile.exists() ) {
			listener.onReceiptReady(Uri.fromFile(tgtFile));
		}
		else {
			if(Utility.isConnectedToInternet(context)) {
				ERTRestApi.apiDownloadImage(format(id, receiptName), tgtFile,
						new ERTRestApi.ERTRestApiListener<File>() {
							@Override
							public void onResponse(File arg0) {
								listener.onReceiptReady(Uri.fromFile(arg0));
							}
						},
						new ERTRestApi.ERTRestApiErrorListener() {
							@Override
							public void onErrorResponse(ERTRestApiError error) {
								Log.d("ERT RECEIPT", error.getMessage());
								listener.onReceiptReady(null);
							}

						});
			} else {
				Toast.makeText(context, "Device is offline. Cannot download receipt.", Toast.LENGTH_SHORT).show();
			}
		}
	}
		
	/**
	 * generate receipt file name based on id and image name
	 * @param id
	 * @param receiptName
	 * @return
	 */
	public static String format(long id, String receiptName) {
		
		String filename = null;
		try {
			filename = String.format(RECEIPT_IMGAGE_NAME, id, URLEncoder.encode(receiptName, "UTF-8").replaceAll("\\+", "%20"));

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filename;
	}
	
	

	/**
	 * 
	 * @param src
	 * @param dest
	 * @return true if saved
	 */
	public synchronized static boolean saveReceipt(InputStream src, String destName) {
		
		File tgt = new File( getAbsolutePath(destName) );
		
		if( tgt.exists() ) {
			return true;
		}
		else {
			FileOutputStream outStream = null;
			try {		
				outStream = new FileOutputStream(tgt);
				
			    byte[] buffer = new byte[1024*60]; // Adjust if you want
			    int bytesRead;
			    while ((bytesRead = src.read(buffer)) != -1) {
			        outStream.write(buffer, 0, bytesRead);
			    }
				
			} catch (IOException e) {
				Log.d(TAG, e.getMessage());				
				e.printStackTrace();
				return false;				
			} finally {
				if( outStream != null ) {
					try {
						outStream.close();
					} catch (IOException e) {
						Log.d(TAG, e.getMessage());
					}
				}
			}
			
		}
		
		return true;		
	}
	
	
}
