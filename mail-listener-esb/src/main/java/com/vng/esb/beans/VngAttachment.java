package com.vng.esb.beans;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vng.esb.consts.Constants;

public class VngAttachment {
	private static Logger logger = LogManager.getLogger(VngAttachment.class);
	
	private int id;
	private int containerId;
	private String containerType;
	private String fileName;
	private String diskFileName;
	private int fileSize;
	private String contentType;
	private String digest;
	private int downloads;
	private int authorId;
	private String createdOn;
	private String description;
	private String fullPath;

	public VngAttachment(){
		containerType = "Issue";
		downloads = 0;
		digest = "";
	}
	
	private void buildAttachFile(){
		logger.debug("Full Path:"  + fullPath);
		File file = new File(fullPath);
		fileSize = (int) file.length();
		contentType = getContentOfFile(fullPath);
		fileName = file.getName();
		DateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_FILE_NAME);
		diskFileName = format.format(new Date())+"_"+fileName;
		
	}
	
	private String getContentOfFile(String url){
		logger.debug(Constants.BEGIN_METHOD);
		String type ="";
		
		try{
			URL u = new URL("file:"+url);
		    URLConnection uc = u.openConnection();
		    type = uc.getContentType();			
		}catch(Exception ex){
			logger.error(ex);
		}finally{
			logger.debug(Constants.END_METHOD);
		}
		 return type;   
	}
	
	/**
	 * @return the containerId
	 */
	public int getContainerId() {
		return containerId;
	}
	/**
	 * @param containerId the containerId to set
	 */
	public void setContainerId(int containerId) {
		this.containerId = containerId;
	}
	/**
	 * @return the containerType
	 */
	public String getContainerType() {
		return containerType;
	}
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @return the diskFileName
	 */
	public String getDiskFileName() {
		return diskFileName;
	}
	
	/**
	 * @return the fileSize
	 */
	public int getFileSize() {
		return fileSize;
	}
	
	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}
	/**
	 * @return the digest
	 */
	public String getDigest() {
		return digest;
	}
	/**
	 * @param digest the digest to set
	 */
	public void setDigest(String digest) {
		this.digest = digest;
	}
	/**
	 * @return the downloads
	 */
	public int getDownloads() {
		return downloads;
	}
	/**
	 * @param downloads the downloads to set
	 */
	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}
	/**
	 * @return the authorId
	 */
	public int getAuthorId() {
		return authorId;
	}
	/**
	 * @param authorId the authorId to set
	 */
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}
	/**
	 * @return the createOn
	 */
	public String getCreatedOn() {
		return createdOn;
	}
	/**
	 * @param createdOn the createOn to set
	 */
	public void setCreateOn(Date createdOn) {
		
		DateFormat dateFormat = new SimpleDateFormat(Constants.DB_DATETIME_FORMAT);
		this.createdOn = dateFormat.format(createdOn);
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	public void setDiskFileName(String diskFileName) {
        this.diskFileName = diskFileName;
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
	/**
	 * @return the fullPath
	 */
	public String getFullPath() {
		return fullPath;
	}
	/**
	 * @param fullPath the fullPath to set
	 */
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
		buildAttachFile();
	}

}
