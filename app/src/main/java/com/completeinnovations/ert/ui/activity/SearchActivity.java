package com.completeinnovations.ert.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.completeinnovations.ert.R;
import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends ActionBarActivity {

	
	private SearchableReportAdapter mSearchAdapter;
    private List<StringifyTable> mSearchableReport = new ArrayList<StringifyTable>();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
			
//		if (savedInstanceState == null) {			
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
		
		//Stringify report and expense item for searching
		Cursor reports = getBaseContext().getContentResolver().query(
			ReportEntry.CONTENT_URI,
			report_search_columns,
			null, null, null);
		
		Cursor expense = getBaseContext().getContentResolver().query(
			ExpenseEntry.CONTENT_URI,
			expense_search_columns,
			null, null, null);
		
		new GenerateSearchableData(this.getApplicationContext() /*getBaseContext()*/)
			.execute(reports, expense);
		
		mSearchAdapter = new SearchableReportAdapter(this);
		
		ListView lv = (ListView)findViewById(R.id.listview_searchresult);
		lv.setAdapter(mSearchAdapter);
        lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String type=null;
                StringifyTable item  = mSearchableReport.get((int)id);
//                for( StringifyTable item : mSearchableReport) {
//                    if( item.getId() == id ) {
//                        type = item.getType();
//                        break;
//                    }
//                }
                Intent viewReport = null;

                Log.d("SA", item.getType());

                if( item.getType().equalsIgnoreCase("EXPENSE")) {
                    viewReport = Utility.buildExpenseDetailsIntent(item.getId());
                }
                else if( item.getType().equalsIgnoreCase("REPORT")) {
                    viewReport = Utility.buildExpenseReportIntent(item.getId());
                }

                startActivity(viewReport);
            }
        });
	}

    @Override
    public void onStop() {
        super.onStop();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.qsearch, menu);
		
		MenuItem searchMenuItem = menu.findItem(R.id.search_activity_qsearch);
		SearchView searchView = (SearchView) searchMenuItem.getActionView();
		//searchMenuItem.expandActionView();
		//MenuItemCompat.expandActionView(searchMenuItem);
		searchView.setIconified(false);
		
		//monitor changes in search bar
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextChange(String filterStr) {			
				mSearchAdapter.getFilter().filter(filterStr);			
				return true;
			}

			@Override
			public boolean onQueryTextSubmit(String arg0) {
				return true;
			}
		});
		
		
//		EditText st = (EditText)findViewById(R.id.);		
//		st.addTextChangedListener(new TextWatcher() {
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before,int count) {}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//				String filterStr = s.toString();				
//				mSearchAdapter.getFilter().filter(filterStr);
//				//mSearchAdapter.notifyDataSetChanged();
//			}			
//		});
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.search_activity_qsearch) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	String[] report_search_columns = new String[]{ ReportEntry._ID,
			ReportEntry.COLUMN_REPORT_ID, ReportEntry.COLUMN_NAME,
			ReportEntry.COLUMN_COMMENT, ReportEntry.COLUMN_STATUS,
			ReportEntry.COLUMN_APPROVER_EMAIL, ReportEntry.COLUMN_CREATED_ON,
			ReportEntry.COLUMN_APPROVED_ON,ReportEntry.COLUMN_SUBMITTED_ON };

	String[] expense_search_columns = new String[]{ ExpenseEntry._ID,
			ExpenseEntry.COLUMN_EXPENSE_ID, ExpenseEntry.COLUMN_REPORT_ID,
			ExpenseEntry.COLUMN_DESCRIPTION,ExpenseEntry.COLUMN_CATEGORY,
			ExpenseEntry.COLUMN_EXPENSE_DATE, ExpenseEntry.COLUMN_CREATED_ON,
			ExpenseEntry.COLUMN_COST, ExpenseEntry.COLUMN_GST, ExpenseEntry.COLUMN_HST, ExpenseEntry.COLUMN_QST,
			ExpenseEntry.COLUMN_CURRENCY, ExpenseEntry.COLUMN_REGION,
			ExpenseEntry.COLUMN_RECEIPT_ID
	};
	
	private static abstract class StringifyTable {
	
		protected int pos;	//position in Cursor
		protected int _id;		
		protected int ertId;
		protected final String type;
		protected String string;
		protected SpannableString htmlString;
				
		protected static StringBuilder builder = new StringBuilder();

        public StringifyTable(String type) { this.type = type;}

		public abstract void fromCursor(Cursor rowData);
		
		public int getId(){
			return _id;
		}
		
		public int getERTid() {
			return ertId;
		}
		
//		public void setType(String type) {
//			this.type = type;
//		}

		public String getType(){
			return type;
		}
		
		public String getString() {
			return string;
		}
		
		public void setSpannableString(SpannableString str) {
			htmlString = str;
		}
		public SpannableString getSpannableString() {
			return htmlString;
		}
	}
	private static class StringifyReport extends StringifyTable {
        public StringifyReport() { super("REPORT");};
		public void fromCursor(Cursor rowData) {
			builder.setLength(0);
			
			pos = rowData.getPosition();
			
			_id  = rowData.getInt( rowData.getColumnIndex(ReportEntry._ID));
			ertId = rowData.getInt( rowData.getColumnIndex(ReportEntry.COLUMN_REPORT_ID));
			
			builder.append( rowData.getString(rowData.getColumnIndex(ReportEntry.COLUMN_NAME)) ).append(' ')
			.append( rowData.getString(rowData.getColumnIndex(ReportEntry.COLUMN_COMMENT)) ).append(' ')
			.append( rowData.getString(rowData.getColumnIndex(ReportEntry.COLUMN_STATUS)) ).append(' ')
			.append( rowData.getString(rowData.getColumnIndex(ReportEntry.COLUMN_STATUS)) ).append(' ')
			.append( rowData.getString(rowData.getColumnIndex(ReportEntry.COLUMN_APPROVER_EMAIL)) );

			//delete 'null' from converted string
			int start=0;			
			int found;
			while( 	(found = builder.indexOf("null", start)) != -1 ) {
				builder.delete(found, found+4);
				start = found; //the length changed		
			}			
			
			string = builder.toString();
		}
	}
	private static class StringifyExpense extends StringifyTable {
        public StringifyExpense() { super("EXPENSE");};

		public void fromCursor(Cursor rowData) {
			builder.setLength(0);
			
			pos = rowData.getPosition();
			
			_id  = rowData.getInt( rowData.getColumnIndex(ExpenseEntry._ID));
			ertId = rowData.getInt( rowData.getColumnIndexOrThrow(ExpenseEntry.COLUMN_EXPENSE_ID));
			
			builder.append( rowData.getString(rowData.getColumnIndex(ExpenseEntry.COLUMN_DESCRIPTION)) ).append(' ')
			.append( rowData.getString(rowData.getColumnIndex(ExpenseEntry.COLUMN_CATEGORY)) ).append(' ')
			.append( rowData.getFloat(rowData.getColumnIndex(ExpenseEntry.COLUMN_COST)) ).append(' ')
			.append( rowData.getString(rowData.getColumnIndex(ExpenseEntry.COLUMN_EXPENSE_DATE)) ).append(' ')
			.append( rowData.getString(rowData.getColumnIndex(ExpenseEntry.COLUMN_CURRENCY)) ).append(' ')
			.append( rowData.getString(rowData.getColumnIndex(ExpenseEntry.COLUMN_REGION)) ).append(' ')
			.append( rowData.getFloat(rowData.getColumnIndex(ExpenseEntry.COLUMN_HST)) ).append(' ')
			.append( rowData.getFloat(rowData.getColumnIndex(ExpenseEntry.COLUMN_GST)) ).append(' ')
			.append( rowData.getFloat(rowData.getColumnIndex(ExpenseEntry.COLUMN_QST)) ).append(' ');
			
			//delete 'null' from converted string
			int start=0;			
			int found;
			while( 	(found = builder.indexOf("null", start)) != -1 ) {
				builder.delete(found, found+4);
				start = found; //the length changed		
			}
			
			string = builder.toString();
			
		}
	}
	
	
	private class GenerateSearchableData extends AsyncTask<Cursor, Integer, List<StringifyTable>> {
		private final Context mContext;
		private List<StringifyTable> mTmpLst = new ArrayList<StringifyTable>();
		private ProgressDialog pDialog;	
		
		private void showpDialog(boolean show) {
			if (show && !pDialog.isShowing()) {
				pDialog.show();
			}
			else if( !show && pDialog.isShowing()) {
				pDialog.dismiss();
			}
		}
		
	    //Used as handler to cancel task if back button is pressed
	    private GenerateSearchableData updateTask = null;

	    public GenerateSearchableData(Context context) {
	    	mContext = context;
	    }
	    
	    @Override
	    protected void onPreExecute(){
	    	updateTask = this;
	    	
	    	pDialog = new ProgressDialog(SearchActivity.this);	        
	        //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        pDialog.setOnCancelListener(new OnCancelListener() {               
				@Override
				public void onCancel(DialogInterface dialog) {
					updateTask.cancel(true);	
				}
	        });
	        //dialog.setMessage("Updating Library...");
	        showpDialog(true);
	    }

	    @SuppressWarnings("unused")
		@Override
	    protected List<StringifyTable> doInBackground(Cursor... tgtCursor) {
	    	
	    	//stringify report table
	    	Cursor reportsCursor = tgtCursor[0];
	    	if( reportsCursor != null ) {

	    		//convert all columns of a row into a single string
				reportsCursor.moveToFirst();
				for(int i = 0; i< reportsCursor.getCount(); i++) {
					
					StringifyTable tmpReport = new StringifyReport();
					
					//tmpReport.setType("REPORT");
					tmpReport.fromCursor(reportsCursor);
					
					mTmpLst.add(tmpReport);
					
					publishProgress((int)(i/reportsCursor.getCount())/100);
					
					reportsCursor.moveToNext();
					
					if( isCancelled() ) {
						mTmpLst.clear();						
					}
				}
				reportsCursor.close();
	    	}
	    	
	    	//stringify expense table
	    	Cursor expenseCursor;
	    	if( tgtCursor.length == 2 && (expenseCursor = tgtCursor[1])!= null) {
				expenseCursor.moveToFirst();
				for(int i = 0; i< expenseCursor.getCount(); i++) {
					
					StringifyTable tmpReport = new StringifyExpense();
					
					//tmpReport.setType("EXPENSE");
					tmpReport.fromCursor(expenseCursor);
					
					mTmpLst.add(tmpReport);
					
					publishProgress((int)(i/expenseCursor.getCount())/100);
					
					expenseCursor.moveToNext();
					
					if( isCancelled() ) {
						mTmpLst.clear();						
					}
				}
				expenseCursor.close();
	    	}	    	
	    		    	
			return mTmpLst;				
        }
	     
	    @Override
	    protected void onProgressUpdate(Integer... progress){
	        pDialog.setProgress(progress[0]);
	    }

	    @Override
	    protected void onPostExecute(List<StringifyTable> reports){	    	
	    	showpDialog(false);	        
	        mSearchableReport.addAll(mTmpLst);
	    }
	}	
	
	private static class ViewHolder {
		TextView targetText;
	}
	
	class SearchableReportAdapter extends BaseAdapter implements Filterable {
		ArrayList mListIndex = new ArrayList();
        int size=0;

        List<StringifyTable> mListData = new ArrayList<StringifyTable>();
		LayoutInflater mInflater;
		
		public SearchableReportAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			return mListData.size();
            //return size;
		}

		@Override
		public Object getItem(int position) {
			return mListData.get(position);
            //return mSearchableReport.get((int)getItemId(position));
		}

		@Override
		public long getItemId(int position) {
			return mSearchableReport.indexOf( mListData.get(position) );
            //return mListIndex.;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if( convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_item_search_result, null);
				
				viewHolder.targetText = (TextView)convertView.findViewById(R.id.tv_search_result);
				
				convertView.setTag(viewHolder);
			}
			else {
				viewHolder = (ViewHolder)convertView.getTag();
			}
			
			viewHolder.targetText.setText(mListData.get(position).getSpannableString(),  BufferType.SPANNABLE);
            //viewHolder.targetText.setText( mSearchableReport.get( mListIndex[position] ).getSpannableString(),  BufferType.SPANNABLE);
			return convertView;
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {	
	            @SuppressWarnings("unchecked")
	            @Override
	            protected void publishResults(CharSequence constraint, FilterResults results) {
	                //mListData.clear();
	                //mListData.addAll();
	            	if( results.count == 0 ) {
	            		mListData.clear();
	            	}
	            	else {
	            		mListData = (List<StringifyTable>) results.values;
	            	}

	                notifyDataSetChanged();
	            }

	            @Override
	            protected FilterResults performFiltering(CharSequence constraint) {

	                FilterResults results = new FilterResults();
	                ArrayList<StringifyTable> filteredReports = new ArrayList<StringifyTable>();

	                // perform your search here using the searchConstraint String.
	                String filterStr = constraint.toString().trim();
	                if( filterStr.isEmpty() ) {
	                	results.count = 0;
	                	return results;
	                }

	                for(StringifyTable report : SearchActivity.this.mSearchableReport ) {
	                	String tmp = report.getString();
	                	int startPos = tmp.toLowerCase().indexOf( filterStr.toLowerCase() );
	                    if ( startPos != -1)  {
	                    	//TODO try SpannableStringBuilder
	                    	SpannableString str = new SpannableString( tmp );
	                    	str.setSpan(new ForegroundColorSpan(Color.RED), startPos, startPos+ filterStr.length(), 0);
	                    	report.setSpannableString(str);

	                        filteredReports.add(report);
	                    }
	                }

	                results.count = filteredReports.size();
	                results.values = filteredReports;

	                return results;
	            }
	        };
//	            @SuppressWarnings("unchecked")
//	            @Override
//	            protected void publishResults(CharSequence constraint, FilterResults results) {
//	                notifyDataSetChanged();
//	            }
//
//                protected Filter.FilterResults performFiltering(CharSequence constraint) {
//
//                    // perform your search here using the searchConstraint String.
//                    String filterStr = constraint.toString().trim();
//                    if( filterStr.isEmpty() ) {
//                        return null;
//                    }
//
//                    size=0;
//
//                    for(int j=0; j < mSearchableReport.size(); j++) {
//                        StringifyTable report = mSearchableReport.get(j);
//
//                        String tmp = report.getString();
//                        int startPos = tmp.indexOf( filterStr );
//                        if ( startPos != -1)  {
//                            //TODO try SpannableStringBuilder
//                            SpannableString str = new SpannableString( tmp );
//                            str.setSpan(new ForegroundColorSpan(Color.RED), startPos, startPos+ filterStr.length(), 0);
//                            report.setSpannableString(str);
//
//                            mListIndex[size++] = j;
//                        }
//                    }
//                    return null;
//                }
//            };

	        return filter;
		}
    }
}
