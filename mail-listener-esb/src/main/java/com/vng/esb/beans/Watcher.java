package com.vng.esb.beans;


public class Watcher {

	private int id;
	private String watchableType;
	private int watchableId;
	private Integer userId;
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
	 * @return the watchableType
	 */
	public String getWatchableType() {
		return watchableType;
	}
	/**
	 * @param watchableType the watchableType to set
	 */
	public void setWatchableType(String watchableType) {
		this.watchableType = watchableType;
	}
	/**
	 * @return the watchableId
	 */
	public int getWatchableId() {
		return watchableId;
	}
	/**
	 * @param watchableId the watchableId to set
	 */
	public void setWatchableId(int watchableId) {
		this.watchableId = watchableId;
	}
	/**
	 * @return the userId
	 */
	public Integer getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	
}
