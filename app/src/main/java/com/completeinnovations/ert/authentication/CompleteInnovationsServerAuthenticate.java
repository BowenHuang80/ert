package com.completeinnovations.ert.authentication;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by Abhinav on 1/10/2015.
 * Handles communication with CompleteInnovation ERT ASP server
 */
public class CompleteInnovationsServerAuthenticate implements
        ServerAuthenticate {


    private String authtoken;

    @Override
    public String userSignIn(String user, String pass,
                             String authType) throws Exception {

        Log.d("ERT", "userSignIn");

        HttpResponse result;

        DefaultHttpClient httpClient = new DefaultHttpClient();


        // register ntlm auth scheme
        httpClient.getAuthSchemes().register("NTLM", new NTLMSchemeFactory());
        httpClient.getCredentialsProvider().setCredentials(
                // Limit the credentials only to the specified domain and port
                AuthScope.ANY,
                // Specify credentials, most of the time only user/pass is
                // needed
                new NTCredentials(user, pass, "", "")
        );


        HttpGet httpGet = new HttpGet("http://ertapi.completeinnovations.com/");

        try {
            result = httpClient.execute(httpGet);//, responseHandler);

        } catch (ClientProtocolException e) {
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        // get response code
        int responseStatusCode = result.getStatusLine().getStatusCode();

        // if the response code is not 200 - OK, or 500 - Internal error,
        // then communication error occurred
//        if (responseStatusCode != 200) {
//            throw new Exception("Invalid username/password");
//        } else {
//            authtoken = pass;
//        }
        authtoken = pass;
        return authtoken;
    }
}
