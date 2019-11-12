package com.completeinnovations.ert.restapi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

//import org.apache.http.HttpEntity;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.ContentBody;
//import org.apache.http.entity.mime.content.FileBody;

import android.util.Log;
import com.completeinnovations.ert.restapi.ERTRestApi.ERTRestApiErrorListener;
import com.completeinnovations.ert.restapi.ERTRestApi.ERTRestApiListener;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;


/**
 * 
 * @author brian
 *
 */
public class ImagePOSTRequest extends Request<String> {
	
    private static final String PROTOCOL_CHARSET = "utf-8";
    private static final String BOUNDARY = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE =
    		String.format("multipart/form-data;boundary=%s", BOUNDARY );

    
    
	private static final String crlf = "\r\n";
	private static final String twoHyphens = "--";
    private static final String filePart = "file";

    private final Response.Listener<String> mListener;
    
    /**
     * @deprecated 
     */    
    private final File mFilePart;
    
    private final InputStream mSrcStream;
    private final String mName;
    
    /**
     * @deprecated Use {@link #ImagePOSTRequest(String, String, InputStream, ERTRestApiListener<String>, ERTRestApiErrorListener)}. 
     */    
    public ImagePOSTRequest(String url,File file, ERTRestApiListener<String> listener, ERTRestApiErrorListener errorListener)
    {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mFilePart = file;
        mName = mFilePart.getName();
        FileInputStream srcStream = null;
        try {
        	 srcStream = new FileInputStream( file );
		} catch (FileNotFoundException e) {
			VolleyLog.e(e.getMessage());
		}
        
        mSrcStream = srcStream;
    }

    public ImagePOSTRequest(String url, String name, InputStream file, ERTRestApiListener<String> listener, ERTRestApiErrorListener errorListener)
    {
        super(Method.POST, url, errorListener);
        mName = name;
        mListener = listener;        
        mSrcStream = file;
        
        mFilePart = null;
    }
    
    @Override
    public String getBodyContentType()
    {
        return PROTOCOL_CONTENT_TYPE;
    }

    private String getName() {
    	return mName;
    }
    
    @Override
    public byte[] getBody() throws AuthFailureError
    {
    	
//	    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
//	    	Bitmap bm = BitmapFactory.decodeFile(imageUri.getPath());
//	    	bm.compress(CompressFormat.JPEG, 60, bos);	    	
    	
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*50);
		DataOutputStream request = new DataOutputStream(bos);
		
		try {
			request.writeBytes(twoHyphens + BOUNDARY + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"" + filePart + "\";filename=\"" + getName() + "\"" + crlf);
			request.writeBytes("Content-Type: image/jpeg;" + crlf);
			request.writeBytes(crlf);
			
			//BufferedInputStream bin = null;

			//bin = new BufferedInputStream( mSrcStream);
	        byte[] bts = new byte[1024];
	        int len=0;
	        while( (len=mSrcStream.read(bts)) != -1 ) {
	        	request.write(bts, 0, len);
	        }
	        
	        mSrcStream.close();
	        
		    request.writeBytes(crlf);
		    request.writeBytes(twoHyphens + BOUNDARY + twoHyphens + crlf);
		    
		    request.flush();
		    request.close();			
		
		} catch (IOException e) {
			VolleyLog.e(e.getMessage());
		}
		
		return bos.toByteArray();
    }

    @Override
	public void cancel() {	
		try {
			mSrcStream.close();
		} catch (IOException e) {
			VolleyLog.e(e.getMessage());
		}    	
    }
    
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response)
    {
        return Response.success("Uploaded", getCacheEntry());
    }
    
    @Override
    protected void deliverResponse(String response)
    {
        mListener.onResponse(response);
    }
}