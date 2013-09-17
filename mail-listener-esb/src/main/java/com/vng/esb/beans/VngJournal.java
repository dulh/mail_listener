package com.vng.esb.beans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.vng.esb.consts.Constants;

public class VngJournal {
	private int id;
	private int journalizedId;// this is project id.
	private String journalizedType;//
	private int userId;
	private String notes;
	private String cteatedOn;
	/**
	 * @return the journalizedId
	 */
	public int getJournalizedId() {
		return journalizedId;
	}
	/**
	 * @param journalizedId the journalizedId to set
	 */
	public void setJournalizedId(int journalizedId) {
		this.journalizedId = journalizedId;
	}
	/**
	 * @return the journalizedType
	 */
	public String getJournalizedType() {
		return journalizedType;
	}
	/**
	 * @param journalizedType the journalizedType to set
	 */
	public void setJournalizedType(String journalizedType) {
		this.journalizedType = journalizedType;
	}
	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
	/**
	 * @return the cteatedOn
	 */
	public String getCreatedOn() {
		return cteatedOn;
	}
	/**
	 * @param cteatedOn the cteatedOn to set
	 */
	public void setCteatedOn(Date cteatedOn) {
		DateFormat dateFormat = new SimpleDateFormat(Constants.DB_DATETIME_FORMAT);
		this.cteatedOn = dateFormat.format(cteatedOn);
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
}
