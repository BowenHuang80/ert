package com.completeinnovations.ert.ui.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.completeinnovations.ert.Constants;
import com.completeinnovations.ert.R;
import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;
import com.completeinnovations.ert.data.SyncStatus;
import com.completeinnovations.ert.model.Status;
import com.completeinnovations.ert.sync.ReportSyncAdapter;
import com.completeinnovations.ert.ui.activity.ExpenseReportActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ReportFragment.class.getSimpleName();

    public static final String EXTRA_REPORT_NAME = "extra_reportFragment_report_name";
    public static final String EXTRA_SUBMITTER_EMAIL = "extra_reportFragment_submitter_email";
    public static final String EXTRA_COMMENT = "extra_reportFragment_comment";
    public static final String EXTRA_APPROVER_EMAIL = "extra_reportFragment_approver_email";


    private static final int REPORT_LOADER = 0;

    public static final String[] REPORT_COLUMNS = {
            ReportEntry.TABLE_NAME + "." + ReportEntry._ID,
            ReportEntry.COLUMN_NAME,
            //ReportEntry.COLUMN_TOTAL_EXPENSE,
            ReportEntry.COLUMN_SUBMITTER_EMAIL,
            ReportEntry.COLUMN_CREATED_ON,
            ReportEntry.COLUMN_SUBMITTED_ON,
            ReportEntry.COLUMN_STATUS,
            ReportEntry.COLUMN_COMMENT,
            ReportEntry.COLUMN_IS_DELETED,
            ReportEntry.COLUMN_APPROVER_EMAIL,
            ReportEntry.COLUMN_REPORT_ID,
            ReportEntry.COLUMN_STATUS_NOTE
    };


    //indices for the REPORT_COLUMNS

    public static final int COL_BASE_REPORT_ID = 0;
    public static final int COL_REPORT_NAME = 1;
    //public static final int COL_REPORT_TOTAL_EXPENSE = 2;
    public static final int COL_REPORT_SUBMITTER_EMAIL = 2;
    public static final int COL_REPORT_CREATED_ON = 3;
    public static final int COL_REPORT_DATE_SUBMITTED = 4;
    public static final int COL_REPORT_STATUS = 5;
    public static final int COL_REPORT_COMMENT = 6;
    public static final int COL_REPORT_APPROVER_EMAIL = 8;
    public static final int COL_REPORT_ID = 9;
    public static final int COL_REPORT_STATUS_NOTES = 10;

    private ReportAdapter mReportAdapter;
    private ContentResolver mContentResolver;
    private Menu menu;

    BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    public ReportFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //registerReportContentObserver();

        /*mContentResolver = getActivity().getContentResolver();
        mContentResolver.registerContentObserver(
                ReportEntry.CONTENT_URI,
                true,
                new ReportObserver(getActivity())
        );*/
//--------------------------------------------------------------------------------------------------


//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setContentTitle("My notification")
//                        .setContentText("Hello World!");
//        // Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, ReportFragment.class);
//
//        // The stack builder object will contain an artificial back stack for the
//        // started Activity.
//        // This ensures that navigating backward from the Activity leads out of
//        // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(ReportFragment.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        // mId allows you to update the notification later on.
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        //--------------------------------------------------------------------------------------------------
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.report_fragment, menu);
        this.menu = menu;
        //manageAddNewReportMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.main_activity_action_new : {
                Intent intent = new Intent(getActivity(), ExpenseReportActivity.class);
                startActivity(intent);
            }

            case R.id.main_activity_action_search: {
                //TODO: implement search
                Log.d(LOG_TAG, "Search reports. To be implemented");
            }

            /*case R.id.action_refresh: {
                updateReport();
                return true;
            }*/
        }
/*        if (id == R.id.action_refresh) {
            updateReport();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mReportAdapter = new ReportAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container,
                false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.listview_report);
        //mRecyclerView = (RecyclerView) rootView.findViewById(R.id.activity_main_recyclerview);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "Refreshing");
                ReportSyncAdapter.syncImmediately(getActivity());
                //ReportSyncAdapter.
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //setupAdapter();
//                        mSwipeRefreshLayout.setRefreshing(false);
//                    }
//                }, 2500);
                if(Utility.isConnectedToInternet(getActivity())) {
                    ReportSyncAdapter.syncImmediately(getActivity());
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast toast = Toast.makeText(getActivity(), "No internet connectivity", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });




        ListView reportListView = (ListView) rootView.findViewById(R.id
                .activity_main_listview);
        reportListView.setAdapter(mReportAdapter);

        reportListView.setOnItemClickListener(new AdapterView
                .OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Cursor cursor = mReportAdapter.getCursor();
                cursor.moveToPosition(position);

                startActivity(Utility.buildExpenseReportIntent(cursor.getInt(COL_BASE_REPORT_ID)));
            }
        });

        reportListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final Cursor CURSOR = mReportAdapter.getCursor();
                CURSOR.moveToPosition(position);

                Log.i(LOG_TAG,"Status : "+CURSOR.getString(COL_REPORT_STATUS));
                if(CURSOR.getInt(COL_REPORT_STATUS)==4){  //TODO: change 4 to the Status enum

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                    // set title
                    alertDialogBuilder.setTitle("Are you sure you want to delete this report?");

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("OK",new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,int id) {
                                    // if this button is clicked, delete
                                    // the report


                                    Log.i(LOG_TAG,"Report ID : "+CURSOR.getInt(COL_BASE_REPORT_ID));
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(ReportEntry.COLUMN_SYNC_STATUS,
                                            SyncStatus.DELETED_REPORT.toString()
                                    );

                                    contentValues.put(ReportEntry.COLUMN_IS_DELETED, "true");

                                    getActivity().getContentResolver().update(
                                            ReportEntry.buildReportUri(CURSOR.getInt(COL_BASE_REPORT_ID)),
                                            contentValues,
                                            null,
                                            null
                                    );

                                    Toast toast = Toast.makeText(getActivity(), "Report " +
                                            "Deleted", Toast.LENGTH_SHORT);
                                    toast.show();
                                    //getActivity().finish();
                                    Log.d("ERT-delete", String.valueOf(CURSOR.getInt(COL_BASE_REPORT_ID)));

                                }
                            })
                            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // if this button is clicked, just close
                                    // the dialog box and do nothing
                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();

                }

                else{
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Cannot delete submitted report");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }





                return true;
            }
        });

        return rootView;
    }

    private String dateFormatter(String dateNumberString) {
        long dateNumber = Long.parseLong(dateNumberString);
        return new SimpleDateFormat("MMM dd, yyyy").format(new Date(dateNumber));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(REPORT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        //updateReport();
        //registerReportContentObserver();
    }

    private void updateReport() {
        /*Intent intent = new Intent(getActivity(), ReportService.class);
        getActivity().startService(intent);*/

/*        Intent alarmIntent = new Intent(getActivity(),
                ReportService.AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),
                0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getActivity()
                .getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +
                5000, pendingIntent);*/

        //ReportSyncAdapter.syncImmediately(getActivity());

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                ReportEntry.CONTENT_URI,
                REPORT_COLUMNS,
                //null,
                ReportEntry.COLUMN_IS_DELETED + "=?",
                //null,
                new String[] {"false"},
                ReportEntry._ID + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mReportAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mReportAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(syncFinishedReceiver, new IntentFilter
                (Constants.SYNC_FINISHED));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(syncFinishedReceiver);
    }

    private void manageAddNewReportMenu() {
        MenuItem newReportMenu = menu.findItem(R.id
                .main_activity_action_new);
        //newReportMenu.setVisible(!newReportMenu.isVisible());

        Cursor cursor = getActivity().getContentResolver().query(
                ReportEntry.CONTENT_URI,
                null,
                ReportEntry.COLUMN_STATUS + "=?" +
                        " AND " + ReportEntry.COLUMN_SYNC_STATUS + "!=?"
                ,
                new String[] {
                        String.valueOf(Status.SAVED.getNumber()),
                        SyncStatus.DELETED_REPORT.toString()
                },
                null
        );

        // means that there is at least a saved report
        if(cursor.getCount() > 0) {
            newReportMenu.setVisible(false);
        } else {
            newReportMenu.setVisible(true);
        }

        cursor.close();
    }

    private void registerReportContentObserver() {
        ContentResolver contentResolver = getActivity().getContentResolver();

        ReportContentObserver reportContentObserver = new
                ReportContentObserver(new Handler());

        contentResolver.registerContentObserver(ReportEntry.CONTENT_URI,
                true, reportContentObserver);
    }

    class ReportContentObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ReportContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            //Log.d(LOG_TAG, "Report Changed");
            //TODO: Manage the display of the add report (+) icon from here

            manageAddNewReportMenu();

        }

        public void alertMessage() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        Toast.makeText(getActivity(), "Yes Clicked", Toast.LENGTH_LONG).show();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        // do nothing
                        Toast.makeText(getActivity(), "No Clicked", Toast.LENGTH_LONG).show();
                            break; }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure?") .setPositiveButton("Yes", dialogClickListener) .setNegativeButton("No", dialogClickListener).show();
        }
    }
}

