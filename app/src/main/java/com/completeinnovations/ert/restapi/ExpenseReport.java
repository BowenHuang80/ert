package com.completeinnovations.ert.restapi;

import android.content.ContentValues;

import com.completeinnovations.ert.data.ReportContract;
import com.completeinnovations.ert.model.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExpenseReport {
	int id;
	String name;
	String submitterEmail;
	String approverEmail;
	String comment;
	String status;
	String statusNote;
	String text;
	String createdOn;
	String submittedOn;
	String approvedOn;
	boolean isDeleted = false;
	boolean flagged; //nullable
	ExpenseLineItem[] expenseLineItems;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubmitterEmail() {
		return submitterEmail;
	}
	public void setSubmitterEmail(String submitterEmail) {
		this.submitterEmail = submitterEmail;
	}
	public String getApproverEmail() {
		return approverEmail;
	}
	public void setApproverEmail(String approverEmail) {
		this.approverEmail = approverEmail;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusNote() {
		return statusNote;
	}
	public void setStatusNote(String statusNote) {
		this.statusNote = statusNote;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public String getSubmittedOn() {
		return submittedOn;
	}
	public void setSubmittedOn(String submittedOn) {
		this.submittedOn = submittedOn;
	}
	public String getApprovedOn() {
		return approvedOn;
	}
	public void setApprovedOn(String approvedOn) {
		this.approvedOn = approvedOn;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	public boolean isFlagged() {
		return flagged;
	}
	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}
	public ExpenseLineItem[] getExpenseLineItems() {
		return expenseLineItems;
	}
	public void setExpenseLineItems(ExpenseLineItem[] expenseLineItems) {
		this.expenseLineItems = expenseLineItems;
	}
	
//	public enum EnumReportStatus
//	{
//	    Accepted = 1,
//	    Pending = 2,
//	    Rejected = 3,
//	    Saved = 4,
//	    Submitted = 5,
//	    Paid = 6
//	}

    /**
     * Construct ContentValues from an array of ExpenseReport
     *
     * @param expenseReports an Array of ExpenseReport
     * @return an array of ContentValues
     */
    public static ContentValues[] getExpenseReportContentValues(ExpenseReport[]
                                                                        expenseReports) {

        List<ContentValues> contentValuesList = new ArrayList<ContentValues>();
        for (ExpenseReport expenseReport : expenseReports) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ReportContract.ReportEntry.COLUMN_NAME,
                    expenseReport.getName());

            contentValues.put(ReportContract.ReportEntry.COLUMN_COMMENT,
                    expenseReport.getComment());

            contentValues.put(ReportContract.ReportEntry
                    .COLUMN_SUBMITTER_EMAIL, expenseReport.getSubmitterEmail());
            contentValues.put(ReportContract.ReportEntry
                    .COLUMN_APPROVER_EMAIL, expenseReport.getApproverEmail());
            contentValues.put(ReportContract.ReportEntry.COLUMN_COMMENT,
                    expenseReport.getComment());
            contentValues.put(ReportContract.ReportEntry.COLUMN_STATUS,
                    Status.statusFactory(expenseReport.getStatus()).getNumber
                            ());
            contentValues.put(ReportContract.ReportEntry.COLUMN_STATUS_NOTE,
                    expenseReport.getStatusNote());
            contentValues.put(ReportContract.ReportEntry.COLUMN_TEXT,
                    expenseReport.getText());

            if(expenseReport.getCreatedOn() != null) {
                contentValues.put(ReportContract.ReportEntry.COLUMN_CREATED_ON,
                        expenseReport.getCreatedOn().toString());
            }

            if(expenseReport.getSubmittedOn() != null) {
                contentValues.put(ReportContract.ReportEntry.COLUMN_SUBMITTED_ON,
                        expenseReport.getSubmittedOn().toString());
            }
            contentValues.put(ReportContract.ReportEntry.COLUMN_IS_DELETED,
                    String.valueOf(expenseReport.isDeleted()));
            contentValues.put(ReportContract.ReportEntry.COLUMN_FLAG,
                    expenseReport.isFlagged());
            contentValues.put(ReportContract.ReportEntry.COLUMN_REPORT_ID,
                    expenseReport.getId());


            contentValuesList.add(contentValues);
        }
        ContentValues[] contentValuesArray = new
                ContentValues[contentValuesList.size()];
        return contentValuesList.toArray(contentValuesArray);
    }
}