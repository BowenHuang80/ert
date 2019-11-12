package com.completeinnovations.ert.restapi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.MultipartEntityBuilder;
//import org.apache.http.entity.mime.content.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.completeinnovations.ert.authentication.NTLMSchemeFactory;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ResponseDelivery;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Provides access to ERT Restful Apis V1
 * http://ertapi.completeinnovations.com/api/v1/
 * {@link NTLMSchemeFactory}.
 * @author brian
 *
 */
public class ERTRestApi {
	
	static final String TAG = ERTRestApi.class.getSimpleName();
	
	static final String RESTAPI_URL_CREATE_REPORT = "http://ertapi.completeinnovations.com/api/v1/expensereports";
	static final String RESTAPI_URL_SUBMIT = "http://ertapi.completeinnovations.com/api/v1/expensereports" + "/%d"; //%d is the report id
	static final String RESTAPI_URL_EXPENSE_ITEM = "http://ertapi.completeinnovations.com/api/v1/expensereports" +"/%d" + "/expenselineitems" + "/%d"; //report.id and expenselineitem.id
	static final String RESTAPI_URL_GET_REPORT = "http://ertapi.completeinnovations.com/api/v1/expensereports?includeLineItems=true";
	static final String RESTAPI_URL_GET_EMPLOYEE = "http://ertapi.completeinnovations.com/api/v1/employees";	
	static final String RESTAPI_URL_UPLOAD_IMAGE = "http://ertapi.completeinnovations.com/api/v1/upload" + "/%d";
	static final String RESTAPI_URL_DOWNLOAD_IMAGE = "http://ertapi.completeinnovations.com/Uploads" + "/%s";
	
	

	
	//static final Gson gson = new Gson(); //new GsonBuilder().serializeNulls().create();
	static final Gson gson = new Gson();//GsonBuilder()..setDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSS").create();
	static Context mContext;
	
	/**
	 * No instance should be created 
	 */
	private ERTRestApi() {
		
	}
	
	private static RequestQueue mRequestQueue;
	private static String mNTLMToken;
	private static  RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {

            //Fix Invalid use of SingleClientConnManager: connection still allocated.
            BasicHttpParams params = new BasicHttpParams();
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
            schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
            ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

        	DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
		    httpClient.getAuthSchemes().register("NTLM", new NTLMSchemeFactory());
		    
            int colon = mNTLMToken.indexOf(':');
		    httpClient.getCredentialsProvider().setCredentials(
		            // Limit the credentials only to the specified domain and port
		            AuthScope.ANY,
		            // Specify credentials, most of the time only user:pass is needed
		            new NTCredentials(mNTLMToken.substring(0, colon), mNTLMToken.substring(colon+1, mNTLMToken.length()), "", "")
		            );				
		    //Volley
            mRequestQueue = Volley.newRequestQueue( mContext, new HttpClientStack(httpClient));
        }
 
        return mRequestQueue;		
	}
	

	/**
	 * add a Volley.Request to the queue 
	 * @param req
	 */
    public static <T> void addToRequestQueue(Request<T> req) {
        //req.setTag(TAG);
        getRequestQueue().add(req);
    }	
	
	
	/**
	 * Cancel all request and destroy Volley's request queue
	 * Should only be used when the NTLM credentials are changed and needs to re-initialize the HttpStack.
	 * Call init with new Credentials before makeing any request. 
	 */
	public static void destroyApiService() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
        mRequestQueue = null;
	}	

	
	/**
	 * Configs some parameters for Volley
	 * @param context	Application context
	 * @param ertAccount	User:Pass pair
	 */	
	public static void init(Context context, String ertAccount) {
		mContext = context;
		mNTLMToken = ertAccount;
	}
	
	/** expensereports GET
	 * fetch all existing reports from server
	 * @param responseListener Handler when http-request returns the result
	 * @param errorListener	Handler when Http-request returns an error
	 * @throws ERTRestApiException
	 * @throws JSONException
	 */
	public static void apiGetReport(ERTRestApiListener<JSONArray> responseListener, ERTRestApiErrorListener errorListener) 
			throws ERTRestApiException, JSONException 
	{
		// Adding request to Volley's request queue
		addToRequestQueue( new JsonArrayRequest(RESTAPI_URL_GET_REPORT, responseListener, errorListener) );
	}
	
	/** expensereports POST
	 * create a draft expense report on server
	 * @param draftReport
	 * @param resListener
	 * @param errorListener
	 * @throws JSONException
	 */
	public static void apiSaveReport(ExpenseReport draftReport, ERTRestApiListener<JSONObject> resListener, 
			ERTRestApiErrorListener errorListener) 
		throws JSONException 
	{
		addToRequestQueue(new JsonObjectRequest(Method.POST, 
				RESTAPI_URL_CREATE_REPORT, new JSONObject(gson.toJson(draftReport).toString()), 
				resListener, errorListener));
	}

	/** expensereports PUT
	 * Modify an exiting draft report
	 * @param draftReport
	 * @param resListener
	 * @param errorListener
	 * @throws JSONException
	 */
	public static void apiEditReport(ExpenseReport eReport, ERTRestApiListener<JSONObject> resListener, 
			ERTRestApiErrorListener errorListener) 
		throws JSONException 
	{
		String restApiUrl = String.format(RESTAPI_URL_SUBMIT, eReport.getId());
				
		addToRequestQueue(new JsonObjectRequest(Method.PUT, 
				restApiUrl, new JSONObject(gson.toJson(eReport).toString()), 
				resListener, errorListener));
	}
	
	/** expensereports PUT
	 * submit a completed report
	 * Note:Before submitting a report, first you have to save a draft report to get the report id, 
	 * as well as save expense items for that report
	 * @param eReport
	 * @param resListener
	 * @param errorListener
	 * @throws ERTRestApiException
	 * @throws JSONException
	 */
	public static void apiSubmitReport(ExpenseReport eReport, ERTRestApiListener<JSONObject> resListener, 
			ERTRestApiErrorListener errorListener) 
		throws ERTRestApiException, JSONException 
	{
		apiEditReport(eReport, resListener, errorListener );
//		String restApiUrl = String.format(RESTAPI_URL_SUBMIT, eReport.getId());
//		
//		addToRequestQueue(new JsonObjectRequest(Method.PUT, 
//				restApiUrl, new JSONObject(gson.toJson(eReport).toString()), 
//				resListener, errorListener));
	}
	
	/**
	 * Delete a specified report on server
	 * @param eReport id is necessary
	 * @param resListener
	 * @param errorListener
	 * @throws ERTRestApiException
	 * @throws JSONException
	 */
	public static void apiDeleteReport(int id,ERTRestApiListener<JSONObject> resListener, 
			ERTRestApiErrorListener errorListener) 
		throws ERTRestApiException, JSONException 
	{
		
		String restApiUrl = String.format(RESTAPI_URL_SUBMIT, id);
		
		addToRequestQueue(new JsonObjectRequest(Method.DELETE, 
				restApiUrl, null, resListener, errorListener));
	}
	
	
	/**
	 * Add an expense item to an existing report
	 * TODO upload images
	 * @param eItem
	 * @param resListener
	 * @param errorListener
	 * @throws ERTRestApiException
	 * @throws JSONException
	 */
	public static void apiAddExpenseLineItem(ExpenseLineItem eItem, ERTRestApiListener<JSONObject> resListener, 
			ERTRestApiErrorListener errorListener) 
		throws ERTRestApiException, JSONException 
	{
		//This should not be happening if everything goes right.
		if( eItem.getExpenseReportId() <= 0 ) {
			throw new ERTRestApiException("Invalid Report Id");
		}
		
		String restApiUrl = String.format(RESTAPI_URL_EXPENSE_ITEM, eItem.getExpenseReportId(), 0); 
		addToRequestQueue(new JsonObjectRequest(Method.POST, 
				restApiUrl, new JSONObject(gson.toJson(eItem).toString()), 
				resListener, errorListener));
	}
	
	/**
	 * update an existing expense-item
	 * @param eItem
	 * @param resListener
	 * @param errorListener
	 * @throws ERTRestApiException
	 * @throws JSONException
	 */
	public static void apiEditExpenseLineItem(ExpenseLineItem eItem, ERTRestApiListener<JSONObject> resListener, 
			ERTRestApiErrorListener errorListener) 
		throws ERTRestApiException, JSONException 
	{
		//This should not be happening if everything goes right.
		if( eItem.getExpenseReportId() <= 0 ) {
			throw new ERTRestApiException("Invalid Report Id");
		}
		
		String restApiUrl = String.format(RESTAPI_URL_EXPENSE_ITEM, eItem.getExpenseReportId(), eItem.getId());
		
		addToRequestQueue(new JsonObjectRequest(Method.PUT, 
				restApiUrl, new JSONObject(gson.toJson(eItem).toString()), 
				resListener, errorListener));
		
	}
	
	/**
	 * delete an existing expense item
	 * @param id
	 * @param resListener
	 * @param errorListener
	 * @throws ERTRestApiException
	 */
	public static void apiDeleteExpenseLineItem(int id, ERTRestApiListener<JSONObject> resListener, 
			ERTRestApiErrorListener errorListener) 
		throws ERTRestApiException 
	{
		//This should not be happening if everything goes right.
		if( id <= 0 ) {
			throw new ERTRestApiException("Invalid Report Id");
		}
		
		String restApiUrl = String.format(RESTAPI_URL_EXPENSE_ITEM, 0, id);
		
		addToRequestQueue(new JsonObjectRequest(Method.DELETE, restApiUrl, null, resListener, errorListener));
		
	}
	
	/**
	 * 
	 * @param resListener
	 * @param errorListener
	 * @throws ERTRestApiException
	 */
	public static void apiGetEmployeeList(ERTRestApiListener<JSONArray> resListener, 
			ERTRestApiErrorListener errorListener)
		throws ERTRestApiException 
	{
		String restApiUrl = RESTAPI_URL_GET_EMPLOYEE ;
		addToRequestQueue(new JsonArrayRequest(restApiUrl, resListener, errorListener));
	}
	
	/**
	 * 
	 * @param imageFile
	 * @param resListener
	 * @param errorListener
	 * @throws ERTRestApiException
	 * @throws JSONException
	 * @deprecated
	 */	
	public static void apiUploadImage(int id, File imageFile, ERTRestApiListener<String> resListener, 
			ERTRestApiErrorListener errorListener)
	{
		String restApiUrl = String.format(RESTAPI_URL_UPLOAD_IMAGE, id);
		
		addToRequestQueue(new ImagePOSTRequest(restApiUrl, imageFile, resListener, errorListener));
	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @param imageFile
	 * @param resListener
	 * @param errorListener
	 */
	public static void apiUploadImage(int id, String name, InputStream imageFile, ERTRestApiListener<String> resListener, 
			ERTRestApiErrorListener errorListener)
	{
		String restApiUrl = String.format(RESTAPI_URL_UPLOAD_IMAGE, id);
		
		addToRequestQueue(new ImagePOSTRequest(restApiUrl, name, imageFile, resListener, errorListener));
	}
		
	/**
	 * Download image file from server
	 * If you want to open an receipt image, call ERTReceipt.loadReceiptByUri instead.
	 * @param receiptImg image name, usually it looks like "expenseId" + "_" + "receiptId"
	 * @param resListener
	 * @param maxWidth
	 * @param maxHeight
	 * @param ertRestApiErrorListener
	 * @throws ERTRestApiException
	 * @throws JSONException
	 */
	public static void apiDownloadImage(String receiptImg, File dst, ERTRestApiListener<File> resListener, 
		ERTRestApiErrorListener ertRestApiErrorListener)		
	{
		String restApiUrl = String.format(RESTAPI_URL_DOWNLOAD_IMAGE, receiptImg);
		
		addToRequestQueue(new ImageGetRequest(restApiUrl, dst, resListener, ertRestApiErrorListener));
	}	
	
	/**
	 * Handler for Successful result 
	 * A wrapper interface to hide the implementation of Volley
	 */
	public interface ERTRestApiListener<T> extends Response.Listener<T> {
		
	}
	
	
	/**
	 *  Error message for the request
	 * A wrapper interface to hide the implementation of Volley
	 *
	 */
	public static class ERTRestApiError extends Exception { 
		public ERTRestApiError(VolleyError arg0) {
			super(arg0);
		}
	}
	
	/**
	 * Error handler for Volley's http request
	 * @author brian
	 *
	 */
	public static abstract class ERTRestApiErrorListener implements Response.ErrorListener {
		
		abstract public void onErrorResponse(ERTRestApiError error);

		@Override
		public void onErrorResponse(VolleyError arg0) {
			// TODO Auto-generated method stub
			onErrorResponse(new ERTRestApiError(arg0));
		}
	}	
	
}
