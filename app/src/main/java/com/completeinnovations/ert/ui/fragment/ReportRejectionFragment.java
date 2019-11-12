package com.completeinnovations.ert.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.completeinnovations.ert.R;

/**
 *
 */
public class ReportRejectionFragment extends DialogFragment{

    private EditText mEditText;

    public ReportRejectionFragment() {

    }

    public static ReportRejectionFragment newInstance() {
        ReportRejectionFragment reportRejectionFragment = new ReportRejectionFragment();
        return reportRejectionFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Reject Report?");
        alertDialogBuilder.setPositiveButton("OK", null);
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        LayoutInflater i = getActivity().getLayoutInflater();

        View view = i.inflate(R.layout.fragment_dialog_report_rejection,null);
        mEditText = (EditText) view.findViewById(R.id.status_notes);
        mEditText.requestFocus();

        alertDialogBuilder.setView(view);
        final Dialog dialog = alertDialogBuilder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button button = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mEditText.getText().toString().length() > 0) {
                            StatusNoteDialogListener statusNoteDialogListener = (StatusNoteDialogListener) getFragmentManager().getFragments().get(0);
                            statusNoteDialogListener.onFinishEditDialog(mEditText.getText()
                                    .toString());

                            dialog.dismiss();
                        } else {
                            //Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                            //mEditText.startAnimation(shake);
                            //Toast.makeText(getActivity(), "Status notes shouldn't be empty", Toast.LENGTH_SHORT);
                            mEditText.setError("Status Note cannot be empty");
                        }
                    }
                });
            }
        });



        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


        return dialog;
    }

    public interface StatusNoteDialogListener {
        void onFinishEditDialog(String inputText);
    }
}
