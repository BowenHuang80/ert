package com.completeinnovations.ert.restapi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.VolleyLog;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;

/**
 * A canned request for getting an image at a given URL and calling
 * back with a decoded Bitmap.
 */
public class ImageGetRequest extends Request<File> {
    /** Socket timeout in milliseconds for image requests */
    private static final int IMAGE_TIMEOUT_MS = 1000;

    /** Default number of retries for image requests */
    private static final int IMAGE_MAX_RETRIES = 2;

    /** Default backoff multiplier for image requests */
    private static final float IMAGE_BACKOFF_MULT = 2f;

    private final Listener<File> mListener;
    
    /** Save download image to specified name */
    private final File mSaveTo;
    /** Decoding lock so that we don't decode more than one image at a time (to avoid OOM's) */
    private static final Object sDecodeLock = new Object();

    /**
     * Creates a new image request and save the result to specified path
     *
     * @param url URL of the image
     * @param listener Listener to receive the decoded bitmap
     * @param maxWidth Maximum width to decode this bitmap to, or zero for none
     * @param maxHeight Maximum height to decode this bitmap to, or zero for
     *            none
     * @param decodeConfig Format to decode the bitmap to
     * @param errorListener Error listener, or null to ignore errors
     */
    public ImageGetRequest(String url, File dst, Response.Listener<File> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        setRetryPolicy(
                new DefaultRetryPolicy(IMAGE_TIMEOUT_MS, IMAGE_MAX_RETRIES, IMAGE_BACKOFF_MULT));
        mSaveTo = dst;
        mListener = listener;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    
    @Override
    protected Response<File> parseNetworkResponse(NetworkResponse response) {
        // Serialize all decode on a global lock to reduce concurrent heap usage.
        
    	BufferedOutputStream out = null;

        try {
        	//mSaveTo.createNewFile()
        	synchronized (sDecodeLock) {    	
                out = new BufferedOutputStream( new FileOutputStream(mSaveTo));
                
                out.write( response.data );
        	}
        } catch (Exception e) {
        	return Response.error(new VolleyError(e));
		} finally {
			if( null != out ) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					return Response.error(new VolleyError(e));
				}
			}
		}
        
        return Response.success(mSaveTo, HttpHeaderParser.parseCacheHeaders(response));
    }


    @Override
    protected void deliverResponse(File response) {
        mListener.onResponse(response);
    }
}
