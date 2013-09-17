package com.vng.esb.beans;

public class VngJournalDetail {
	private int id;
	private String property;
	private String prop_key;
	private String old_value;
	private String value;
	private VngJournal journal;
	
	public static final String PRO_ATTR = "attr";
	public static final String PRO_ATTACHMENT = "attachment";
	
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
	/**
	 * @return the property
	 */
	public String getProperty() {
		return property;
	}
	/**
	 * @param property the property to set
	 */
	public void setProperty(String property) {
		this.property = property;
	}
	/**
	 * @return the prop_key
	 */
	public String getProp_key() {
		return prop_key;
	}
	/**
	 * @param prop_key the prop_key to set
	 */
	public void setProp_key(String prop_key) {
		this.prop_key = prop_key;
	}
	/**
	 * @return the old_value
	 */
	public String getOld_value() {
		return old_value;
	}
	/**
	 * @param old_value the old_value to set
	 */
	public void setOld_value(String old_value) {
		this.old_value = old_value;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the journal
	 */
	public VngJournal getJournal() {
		return journal;
	}
	/**
	 * @param journal the journal to set
	 */
	public void setJournal(VngJournal journal) {
		this.journal = journal;
	}
	


}
