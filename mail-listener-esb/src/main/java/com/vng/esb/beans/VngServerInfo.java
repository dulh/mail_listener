package com.vng.esb.beans;

import com.jcraft.jsch.UserInfo;

public class VngServerInfo implements UserInfo {

	private String userName;
	private String passWord;
	private String host;
	private String destinationFolder;
	private int port ;
	
	public VngServerInfo(String host, String userName,String passWord,String destinationFolder,int port){
		this.userName = userName;
		this.host = host;
		this.destinationFolder = destinationFolder;
		this.passWord = passWord;
		this.port = port;
		
	}
	public String getPassphrase() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getPassword() {
		
		return passWord;
	}
	public boolean promptPassphrase(String arg0) {
		// Should change to false.
		return false;
	}
	public boolean promptPassword(String arg0) {
		// Should change to true.
		return true;
	}
	public boolean promptYesNo(String arg0) {
		// Should change to true.
		return true;
	}
	public void showMessage(String arg0) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * @return the destinationFolder
	 */
	public String getDestinationFolder() {
		return destinationFolder;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}	
	
}
