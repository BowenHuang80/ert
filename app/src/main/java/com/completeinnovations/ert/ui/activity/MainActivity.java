package com.completeinnovations.ert.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.completeinnovations.ert.Constants;
import com.completeinnovations.ert.R;
import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.authentication.AccountGeneral;
import com.completeinnovations.ert.sync.ReportSyncAdapter;
import com.completeinnovations.ert.ui.fragment.ReportFragment;


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private AccountManager mAccountManager;
    //public static final int NOTIFICATION_ID = 1;

    private BroadcastReceiver signinCancelReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(Constants.SIGNIN_CANCELLED)) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                                                                                     
        setContentView(R.layout.activity_main);

        registerReceiver(signinCancelReceiver, new IntentFilter(Constants
                .SIGNIN_CANCELLED));

        mAccountManager = AccountManager.get(this);
        final Account availableAccounts[] = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
            addNewAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral
                    .AUTHTOKEN_TYPE_FULL_ACCESS);
        } else {
            //Toast.makeText(this, "Found account", Toast.LENGTH_SHORT).show();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ReportFragment())
                    .commit();
        }
        //ReportSyncAdapter.initializeSyncAdapter(this);
        // TODO: need to find a way to activate sync when account is present
        //--------------------------------------------------------------------------------------------------

/*        View.OnClickListener handler = new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.notification_btn:
                        Utility.sendNotification(getApplicationContext());
                        break;
                }

            }
        };

        findViewById(R.id.notification_btn).setOnClickListener(handler);*/

    }

    //------------------------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        //ReportSyncAdapter.initializeSyncAdapter(this);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }*/
        if( id == R.id.main_activity_action_search ) {
            Intent searchInt = new Intent(this, SearchActivity.class);

            this.startActivity(searchInt);
//        	FrameLayout viewLyt = (FrameLayout)findViewById(R.id.main_view);
//        	   Button btn = new Button(this);
//        	    btn.setText("MyButton");
//        	    viewLyt.addView(btn, 1, new android.widget.FrameLayout.LayoutParams(
//        	    		android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
//        	    		android.widget.FrameLayout.LayoutParams.WRAP_CONTENT));
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(signinCancelReceiver);
    }

    /**
     * Add new account to the account manager
     * @param accountType
     * @param authTokenType
     */
    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(accountType, authTokenType, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    showMessage("Account was created");
                    Log.d("ERT", "AddNewAccount Bundle is " + bnd);

                } catch (Exception e) {
                    Log.w(LOG_TAG, "Sign in cancelled");
                    //showMessage(e.getMessage());
                }
            }
        }, null);
    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
