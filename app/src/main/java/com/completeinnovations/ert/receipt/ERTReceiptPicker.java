package com.completeinnovations.ert.receipt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

public class ERTReceiptPicker {
	
	private static final String TITLE_PICK_RECEIPT = "Select or take a new picture"; // Or get from strings.xml
	private Uri mTgtUri;
	private String mReceiptName;
	
	
	
	public Intent createReceiptPicker() {
    
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		return createReceiptPicker(timeStamp);
	}
	
	/**
	 * Call startActivityForResult upon the returned Intent to get a receipt image
	 * 
	 * @param a unique name for the expense item
	 * @return An Intent to take a picture or select one from storage
	 */
	public Intent createReceiptPicker(String receiptName) {
		

		mTgtUri = ERTReceipt.createReceiptFile(receiptName);		
		
		mReceiptName = mTgtUri.getLastPathSegment();

		//Use the image browser
		Intent pickIntent = new Intent();
		pickIntent.setType("image/*");
		pickIntent.setAction(Intent.ACTION_GET_CONTENT);

//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, "New Picture");
//        values.put(MediaStore.Images.Media.DESCRIPTION,"From your Camera");
//        Uri mImageUri = .getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
//        mCurrentPhotoPath = mImageUri.getPath();


		//Use the camera
		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);		
	    
	    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTgtUri); // set the image file name
		
		//Intent picker to allow user to choose one 
		Intent chooserIntent = Intent.createChooser(pickIntent, TITLE_PICK_RECEIPT);
		chooserIntent.putExtra
		(
		  Intent.EXTRA_INITIAL_INTENTS, new Intent[] { takePhotoIntent }
		);
				
		return chooserIntent;
	}
	
	
	/**
	 * Process the data returned by ReceiptPicker 
	 * @param ctx
	 * @param data
	 * @return String the image file name for receipt
	 * @throws IOException
	 */
	public String getSelectedReceipt(final Context ctx, final Intent data) throws IOException { //, final Uri cameraUri) {		
			
        if( data != null ) {
        	//the file selected from storage
            final String action = data.getAction();
            if(action == null) {          	
                //copy the file to our ERT directory
				InputStream inStream = ctx.getContentResolver().openInputStream(data.getData());				
//				OutputStream outStream = ctx.getContentResolver().openOutputStream(this.mTgtUri);
//				
//			    byte[] buffer = new byte[1024]; // Adjust if you want
//			    int bytesRead;
//			    while ((bytesRead = inStream.read(buffer)) != -1) {
//			        outStream.write(buffer, 0, bytesRead);
//			    }

				ERTReceipt.saveReceipt(inStream, mReceiptName);
				
			    inStream.close();
//			    outStream.close();
            }
        }
        
        return this.mReceiptName;
	}
}
