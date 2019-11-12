package com.completeinnovations.ert.ui.activity;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import com.completeinnovations.ert.ui.fragment.ExpenseDetailsFragment;

import com.completeinnovations.ert.R;


public class ExpenseDetailsActivity extends ActionBarActivity {

    ExpenseDetailsFragment edf = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);


        if (savedInstanceState == null) {
            edf = new ExpenseDetailsFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, edf )
                    .commit();
        }
    }

    /**
     * Added to inform user of errors before leaving this activity
     */
    @Override
    public void onBackPressed() {

        if( edf != null ) {
            if( edf.onBackPressed() ) {
                super.onBackPressed();
            }
        }
    }

}
