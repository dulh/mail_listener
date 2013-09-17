package com.vng.esb.beans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vng.esb.consts.Constants;

public class VngIssue {
	private final Logger logger = LogManager.getLogger(VngIssue.class);

	private int id;
	private int trackerId;
	private int projectId;
	private String subject;
	private String description;
	private String dueDate;
	private Integer categoryId;
	private int statusId;
	private Integer assignedToId;
	private int priorityId;
	private Integer fixedVersionId;
	private int authorId;
	private int lockVersion;
	private String createOn;
	private String updatedOn;
	private String startDate;
	private int doneRatio;
	private Float estimatedHour;
	private Integer parentId;
	private Integer rootId;
	private Integer lft;
	private Integer rgt;
	private boolean isPrivate;
	private Integer userStoryId;
	private Integer position;
	private Float remainingHour;
	private Integer storyPoint;
	private VngAttachment attachmentFile;
	private Map<String, Integer> mapInteger;
	private Map<String, Float> mapFloat;

	public VngIssue() {
		setPriorityId(4);// default = Normal(4)
		setTrackerId(5);// tracker is task.
		setStatusId(1); // status is new.
		setLockVersion(0);
		setDoneRatio(0);
		setPrivate(false);
		setPosition(0);
	}

	private void buildMap() {

		// Build Integer Map
		mapInteger = new HashMap<String, Integer>();
		mapInteger.put("category_id", categoryId);
		mapInteger.put("assigned_to_id", assignedToId);
		mapInteger.put("fixed_version_id", fixedVersionId);
		mapInteger.put("parent_id", parentId);
		mapInteger.put("root_id", rootId);
		mapInteger.put("lft", lft);
		mapInteger.put("rgt", rgt);
		mapInteger.put("user_story_id", userStoryId);
		mapInteger.put("position", position);
		mapInteger.put("story_points", storyPoint);

		// Build Float Map
		mapFloat = new HashMap<String, Float>();
		mapFloat.put("estimated_hours", estimatedHour);
		mapFloat.put("remaining_hours", remainingHour);

	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the trackerId
	 */
	public int getTrackerId() {
		return trackerId;
	}

	/**
	 * @param trackerId
	 *            the trackerId to set
	 */
	public void setTrackerId(int trackerId) {
		this.trackerId = trackerId;
	}

	/**
	 * @return the projectId
	 */
	public int getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId
	 *            the projectId to set
	 */
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the dueDate
	 */
	public String getDueDate() {
		return dueDate;
	}

	/**
	 * @param dueDate
	 *            the dueDate to set
	 */
	public void setDueDate(Date dueDate) {
		DateFormat dateFormat = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
		this.dueDate = dateFormat.format(dueDate);
	}

	/**
	 * @param dueDate
	 *            the dueDate to set
	 */
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * @return the categoryId
	 */
	public Integer getCategoryId() {
		return categoryId;
	}

	/**
	 * @param categoryId
	 *            the categoryId to set
	 */
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * @return the statusId
	 */
	public int getStatusId() {
		return statusId;
	}

	/**
	 * @param statusId
	 *            the statusId to set
	 */
	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	/**
	 * @return the assignedToId
	 */
	public Integer getAssignedToId() {
		return assignedToId;
	}

	/**
	 * @param assignedToId
	 *            the assignedToId to set
	 */
	public void setAssignedToId(Integer assignedToId) {
		this.assignedToId = assignedToId;
	}

	/**
	 * @return the priorityId
	 */
	public int getPriorityId() {
		return priorityId;
	}

	/**
	 * @param priorityId
	 *            the priorityId to set
	 */
	public void setPriorityId(int priorityId) {
		this.priorityId = priorityId;
	}

	/**
	 * @return the fixedVersionId
	 */
	public Integer getFixedVersionId() {
		return fixedVersionId;
	}

	/**
	 * @param fixedVersionId
	 *            the fixedVersionId to set
	 */
	public void setFixedVersionId(Integer fixedVersionId) {
		this.fixedVersionId = fixedVersionId;
	}

	/**
	 * @return the authorId
	 */
	public int getAuthorId() {
		return authorId;
	}

	/**
	 * @param authorId
	 *            the authorId to set
	 */
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	/**
	 * @return the lockVersion
	 */
	public int getLockVersion() {
		return lockVersion;
	}

	/**
	 * @param lockVersion
	 *            the lockVersion to set
	 */
	public void setLockVersion(int lockVersion) {
		this.lockVersion = lockVersion;
	}

	/**
	 * @return the createOn
	 */
	public String getCreateOn() {
		return createOn;
	}

	/**
	 * @param createOn
	 *            the createOn to set
	 */
	public void setCreateOn(Date createOn) {
		DateFormat dateFormat = new SimpleDateFormat(
				Constants.DB_DATETIME_FORMAT);
		this.createOn = dateFormat.format(createOn);
	}

	/**
	 * @param createOn
	 *            the createOn to set
	 */
	public void setCreateOn(String createOn) {
		this.createOn = createOn;
	}

	/**
	 * @return the updatedOn
	 */
	public String getUpdatedOn() {
		return updatedOn;
	}

	/**
	 * @param updatedOn
	 *            the updatedOn to set
	 */
	public void setUpdatedOn(Date updatedOn) {
		DateFormat dateFormat = new SimpleDateFormat(
				Constants.DB_DATETIME_FORMAT);
		this.updatedOn = dateFormat.format(updatedOn);
	}

	/**
	 * @param setUpdatedOn
	 *            the setUpdatedOn to set
	 */
	public void setUpdatedOn(String updatedOn) {
		this.updatedOn = updatedOn;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Date startDate) {
		DateFormat dateFormat = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
		this.startDate = dateFormat.format(startDate);
	}

	/**
	 * @param setStartDate
	 *            the setStartDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the doneRatio
	 */
	public int getDoneRatio() {
		return doneRatio;
	}

	/**
	 * @param doneRatio
	 *            the doneRatio to set
	 */
	public void setDoneRatio(int doneRatio) {
		this.doneRatio = doneRatio;
	}

	/**
	 * @return the estimatedHour
	 */
	public Float getEstimatedHour() {
		return estimatedHour;
	}

	/**
	 * @param estimatedHour
	 *            the estimatedHour to set
	 */
	public void setEstimatedHour(Float estimatedHour) {
		this.estimatedHour = estimatedHour;
	}

	/**
	 * @return the parentId
	 */
	public Integer getParentId() {
		return parentId;
	}

	/**
	 * @param parentId
	 *            the parentId to set
	 */
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the rootId
	 */
	public Integer getRootId() {
		return rootId;
	}

	/**
	 * @param rootId
	 *            the rootId to set
	 */
	public void setRootId(Integer rootId) {
		this.rootId = rootId;
	}

	/**
	 * @return the lft
	 */
	public Integer getLft() {
		return lft;
	}

	/**
	 * @param lft
	 *            the lft to set
	 */
	public void setLft(Integer lft) {
		this.lft = lft;
	}

	/**
	 * @return the rgt
	 */
	public Integer getRgt() {
		return rgt;
	}

	/**
	 * @param rgt
	 *            the rgt to set
	 */
	public void setRgt(Integer rgt) {
		this.rgt = rgt;
	}

	/**
	 * @return the isPrivate
	 */
	public boolean isPrivate() {
		return isPrivate;
	}

	/**
	 * @param isPrivate
	 *            the isPrivate to set
	 */
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	/**
	 * @return the userStoryId
	 */
	public Integer getUserStoryId() {
		return userStoryId;
	}

	/**
	 * @param userStoryId
	 *            the userStoryId to set
	 */
	public void setUserStoryId(Integer userStoryId) {
		this.userStoryId = userStoryId;
	}

	/**
	 * @return the remainingHour
	 */
	public Float getRemainingHour() {
		return remainingHour;
	}

	/**
	 * @param remainingHour
	 *            the remainingHour to set
	 */
	public void setRemainingHour(Float remainingHour) {
		this.remainingHour = remainingHour;
	}

	/**
	 * @return the position
	 */
	public Integer getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Integer position) {
		this.position = position;
	}

	/**
	 * @return the storyPoint
	 */
	public Integer getStoryPoints() {
		return storyPoint;
	}

	/**
	 * @param storyPoint
	 *            the storyPoint to set
	 */
	public void setStoryPoint(Integer storyPoint) {
		this.storyPoint = storyPoint;
	}

	/**
	 * @return the attachmentFile
	 */
	public VngAttachment getAttachmentFile() {
		return attachmentFile;
	}

	/**
	 * @param attachmentFile
	 *            the attachmentFile to set
	 */
	public void setAttachmentFile(VngAttachment attachmentFile) {
		this.attachmentFile = attachmentFile;
	}

	/**
	 * @return the mapInteger
	 */
	public Map<String, Integer> getMapInteger() {
		buildMap();
		return mapInteger;
	}

	/**
	 * @return the mapFloat
	 */
	public Map<String, Float> getMapFloat() {
		buildMap();
		return mapFloat;
	}

	public List<VngChangeObject> compare(VngIssue issue) {
		logger.info(Constants.BEGIN_METHOD);
		List<VngChangeObject> lsChange = new ArrayList<VngChangeObject>();
		VngChangeObject changeObj;
		try {
			if(issue!=null){
				if (issue.getDueDate()!=null && !issue.getDueDate().equals(this.dueDate)) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.DUE_DATE);
					changeObj.setOldValue(this.dueDate);
					changeObj.setNewValue(issue.getDueDate());

					lsChange.add(changeObj);
				}

				if (this.statusId != issue.getStatusId()) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.STATUS_ID);
					changeObj.setOldValue(String.valueOf(this.statusId));
					changeObj.setNewValue(String.valueOf(issue.getStatusId()));

					lsChange.add(changeObj);

				}

				if (issue.getAssignedToId() != null && !issue.getAssignedToId().equals(this.assignedToId)) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.ASSIGNED_TO_ID);
					changeObj.setOldValue(String.valueOf(this.assignedToId));
					changeObj.setNewValue(String.valueOf(issue.getAssignedToId()));

					lsChange.add(changeObj);
				}

				if (issue.getPriorityId() != this.priorityId ) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.PRIORITY_ID);
					changeObj.setOldValue(String.valueOf(this.priorityId));
					changeObj.setNewValue(String.valueOf(issue.getPriorityId()));

					lsChange.add(changeObj);
				}

				if (issue.getFixedVersionId()!=null && !issue.getFixedVersionId().equals(this.fixedVersionId)) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.FIXED_VERSION_ID);
					changeObj.setOldValue(String.valueOf(this.fixedVersionId));
					changeObj.setNewValue(String.valueOf(issue.getFixedVersionId()));

					lsChange.add(changeObj);
				}
				if (issue.getStartDate()!=null && !issue.getStartDate().equals(this.startDate)) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.START_DATE);
					changeObj.setOldValue(String.valueOf(this.startDate));
					changeObj.setNewValue(String.valueOf(issue.getStartDate()));
					lsChange.add(changeObj);
				}
				if (this.doneRatio != issue.getDoneRatio()) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.DONE_RATIO);
					changeObj.setOldValue(String.valueOf(this.doneRatio));
					changeObj.setNewValue(String.valueOf(issue.getDoneRatio()));
					lsChange.add(changeObj);
				}
				if (issue.getEstimatedHour()!=null && !issue.getEstimatedHour().equals(this.estimatedHour)) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.ESTIMATED_HOURS);
					changeObj.setOldValue(String.valueOf(this.estimatedHour));
					changeObj.setNewValue(String.valueOf(issue.getEstimatedHour()));
					lsChange.add(changeObj);
				}

				if (issue.getParentId()!=null && !issue.getParentId().equals(this.parentId)) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.PARENT_ID);
					changeObj.setOldValue(String.valueOf(this.parentId));
					changeObj.setNewValue(String.valueOf(issue.getParentId()));
					lsChange.add(changeObj);
				}

				if (issue.getRemainingHour()!=null && !issue.getRemainingHour().equals(this.remainingHour)) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.REMAINING_HOURS);
					changeObj.setOldValue(String.valueOf(this.remainingHour));
					changeObj.setNewValue(String.valueOf(issue.getRemainingHour()));
					lsChange.add(changeObj);
				}

				if (issue.getStoryPoints()!=null && !issue.getStoryPoints().equals(this.storyPoint)) {
					changeObj = new VngChangeObject();
					changeObj.setProperty(Column.STORY_POINTS);
					changeObj.setOldValue(String.valueOf(this.storyPoint));
					changeObj.setNewValue(String.valueOf(issue.getStoryPoints()));
					lsChange.add(changeObj);
				}
			}
			

		} catch (Exception ex) {
			logger.error(ex);
		} finally {
			logger.info(Constants.END_METHOD);
		}
		return lsChange;
	}

	public class Column {
		// Region for constants.
		public static final String ID = "id";
		public static final String TRACKER_ID = "tracker_id";
		public static final String PROJECT_ID = "project_id";
		public static final String SUBJECT = "subject";
		public static final String DESCRIPTION = "description";
		public static final String DUE_DATE = "due_date";
		public static final String CATEGORY_ID = "category_id";
		public static final String STATUS_ID = "status_id";
		public static final String ASSIGNED_TO_ID = "assigned_to_id";
		public static final String PRIORITY_ID = "priority_id";
		public static final String FIXED_VERSION_ID = "fixed_version_id";
		public static final String AUTHOR_ID = "author_id";
		public static final String LOCK_VERSION = "lock_version";
		public static final String CREATED_ON = "created_on";
		public static final String UPDATED_ON = "updated_on";
		public static final String START_DATE = "start_date";
		public static final String DONE_RATIO = "done_ratio";
		public static final String ESTIMATED_HOURS = "estimated_hours";
		public static final String PARENT_ID = "parent_id";
		public static final String ROOT_ID = "root_id";
		public static final String LFT = "lft";
		public static final String RGT = "rgt";
		public static final String IS_PRIVATE = "is_private";
		public static final String USER_STORY_ID = "user_story_id";
		public static final String POSITION = "position";
		public static final String REMAINING_HOURS = "remaining_hours";
		public static final String STORY_POINTS = "story_points";

	}
}
