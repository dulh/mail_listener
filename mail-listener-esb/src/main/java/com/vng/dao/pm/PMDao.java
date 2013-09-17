package com.vng.dao.pm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vng.db.BaseDao;
import com.vng.esb.beans.VngAttachment;
import com.vng.esb.beans.VngIssue;
import com.vng.esb.beans.VngJournal;
import com.vng.esb.beans.VngJournalDetail;
import com.vng.esb.beans.Watcher;
import com.vng.esb.consts.Constants;

public class PMDao extends BaseDao {
	private static final Logger logger = LogManager.getLogger(PMDao.class);

	public int insertIssues(Connection conn, VngIssue issue) throws Exception {
		logger.info(Constants.BEGIN_METHOD);

		int issueId = 0;
		Map<Integer, String> errorMap = new HashMap<Integer, String>();

		StringBuilder sqlInsert = new StringBuilder();
		StringBuilder subSqlParam = new StringBuilder();
		StringBuilder subSqlProperties = new StringBuilder();
		PreparedStatement prStmInsert = null;

		Map<String, Integer> mapInteger;
		Map<String, Float> mapFloat;
		List<Integer> lsInteger = new ArrayList<Integer>();
		List<Float> lsFloat = new ArrayList<Float>();

		try {
			if (validateIssue(conn, issue, errorMap)) {
				// Insert issue to db.
				subSqlProperties
						.append("insert into issues(tracker_id,project_id,subject,description,due_date,");
				subSqlProperties
						.append("status_id,priority_id,author_id,lock_version,created_on,updated_on,");
				subSqlProperties.append("start_date,done_ratio,is_private");
				subSqlParam.append(" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?");
				getRootId(conn, issue);

				mapInteger = issue.getMapInteger();
				mapFloat = issue.getMapFloat();

				for (String key : mapInteger.keySet()) {
					if (mapInteger.get(key) != null) {
						subSqlProperties.append(",").append(key);
						subSqlParam.append(",?");
						lsInteger.add(mapInteger.get(key));
					}

				}

				for (String key : mapFloat.keySet()) {
					if (mapFloat.get(key) != null) {
						subSqlProperties.append(",").append(key);
						subSqlParam.append(",?");
						lsFloat.add(mapFloat.get(key));
					}
				}

				subSqlProperties.append(")");
				subSqlParam.append(")");
				sqlInsert.append(subSqlProperties).append(subSqlParam);

				prStmInsert = conn.prepareStatement(sqlInsert.toString(),Statement.RETURN_GENERATED_KEYS);
				prStmInsert.setInt(1, issue.getTrackerId());
				prStmInsert.setInt(2, issue.getProjectId());
				prStmInsert.setString(3, issue.getSubject());
				prStmInsert.setString(4, issue.getDescription());
				prStmInsert.setString(5, issue.getDueDate());
				prStmInsert.setInt(6, issue.getStatusId());
				prStmInsert.setInt(7, issue.getPriorityId());
				prStmInsert.setInt(8, issue.getAuthorId());
				prStmInsert.setInt(9, issue.getLockVersion());
				prStmInsert.setString(10, issue.getCreateOn());
				prStmInsert.setString(11, issue.getUpdatedOn());
				prStmInsert.setString(12, issue.getStartDate());
				prStmInsert.setInt(13, issue.getDoneRatio());
				prStmInsert.setBoolean(14, issue.isPrivate());

				for (int i = 0; i < lsInteger.size(); i++) {
					prStmInsert.setInt(15 + i, lsInteger.get(i));
				}

				for (int i = 0; i < lsFloat.size(); i++) {
					prStmInsert.setFloat(15 + lsInteger.size() + i,
							lsFloat.get(i));
				}

				// Insert data to db
				if (prStmInsert.executeUpdate() > 0) {
					// Get issue's id.
					issueId =  getGenerateKey(prStmInsert,1);
					
					// Update root id for issue.
					issue.setId(issueId);
					issue.setRootId(issueId);
					if(updateIssues(conn, issue)>0){
						logger.info("Update root id for issue is successful");
					}else{
						throw new Exception("Update root id for issue is failed");
					}
					
					
				}else{
					logger.info("Cannot insert issue to db");
					logger.debug(sqlInsert);
				}

			} else {
				logger.debug(errorMap.toString());
			}
		} finally {
			if (prStmInsert != null) {
				prStmInsert.close();
			}
			logger.info(Constants.END_METHOD);
		}
		return issueId;
	}
	
	private int getGenerateKey(PreparedStatement prStm,int index) throws Exception {
		int key = 0;
		
		ResultSet rsSet = prStm.getGeneratedKeys();
		if(rsSet!=null && rsSet.next()){
			key = rsSet.getInt(index);
		}
		
		return key;
	}
	
	public int getIssueId(Connection conn, String subject, int projectId)throws Exception{
		logger.info(Constants.BEGIN_METHOD);
		int id =0;
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;
	
		try{		
			// check to update.
			sql.append("select * from issues where `subject` = ? and  project_id = ?");
			prStm = conn.prepareStatement(sql.toString());
			prStm.setString(1, subject);
			prStm.setInt(2, projectId);
			
			ResultSet rsSet = prStm.executeQuery();
			if(rsSet!=null && rsSet.next()){
				id = rsSet.getInt("id");
			}else{
				logger.debug("Cannot get issue id");
				logger.debug(sql);
				logger.debug(String.format("Subject: %s ; PrrojectId: %d" , subject,projectId));
			}
						
		}finally{
			logger.info(Constants.END_METHOD);
			
		}
		return id;
	}
	
	public VngIssue getIssue(final Connection conn, final int issueId) throws Exception {
		logger.info(Constants.BEGIN_METHOD);
		VngIssue issue = new VngIssue();
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;
		try{
			sql.append("select * from issues where id =?");
			prStm = conn.prepareStatement(sql.toString());
			prStm.setInt(1, issueId);
			ResultSet result = prStm.executeQuery();
			if(result!=null && result.next()){
				issue.setId(result.getInt(VngIssue.Column.ID));
				issue.setAssignedToId(result.getInt(VngIssue.Column.ASSIGNED_TO_ID));
				issue.setAuthorId(result.getInt(VngIssue.Column.AUTHOR_ID));
				issue.setCategoryId(result.getInt(VngIssue.Column.CATEGORY_ID));
				issue.setCreateOn(result.getString(VngIssue.Column.CREATED_ON));
				issue.setDescription(result.getString(VngIssue.Column.DESCRIPTION));
				issue.setDoneRatio(result.getInt(VngIssue.Column.DONE_RATIO));
				issue.setDueDate(result.getString(VngIssue.Column.DUE_DATE));
				issue.setEstimatedHour(result.getFloat(VngIssue.Column.ESTIMATED_HOURS));
				issue.setFixedVersionId(result.getInt(VngIssue.Column.FIXED_VERSION_ID));
				issue.setLft(result.getInt(VngIssue.Column.LFT));
				issue.setRgt(result.getInt(VngIssue.Column.RGT));
				issue.setLockVersion(result.getInt(VngIssue.Column.LOCK_VERSION));
				issue.setParentId(result.getInt(VngIssue.Column.PARENT_ID));
				issue.setPosition(result.getInt(VngIssue.Column.POSITION));
				issue.setPriorityId(result.getInt(VngIssue.Column.PRIORITY_ID));
				issue.setPrivate(result.getBoolean(VngIssue.Column.IS_PRIVATE));
				issue.setProjectId(result.getInt(VngIssue.Column.PROJECT_ID));
				issue.setRemainingHour(result.getFloat(VngIssue.Column.REMAINING_HOURS));
				issue.setRootId(result.getInt(VngIssue.Column.ROOT_ID));
				issue.setStartDate(result.getString(VngIssue.Column.START_DATE));
				issue.setStatusId(result.getInt(VngIssue.Column.STATUS_ID));
				issue.setStoryPoint(result.getInt(VngIssue.Column.STORY_POINTS));
				issue.setSubject(result.getString(VngIssue.Column.SUBJECT));
				issue.setTrackerId(result.getInt(VngIssue.Column.TRACKER_ID));
				issue.setUpdatedOn(result.getString(VngIssue.Column.UPDATED_ON));
				issue.setUserStoryId(result.getInt(VngIssue.Column.USER_STORY_ID));
			}else{
				logger.error("Cannot find issue (Id = " + issueId + ") in database"); 
			}
			
		}finally{
			logger.info(Constants.END_METHOD);
		}
		return issue;
	}

	public int getUserId(Connection conn, String mail) throws Exception {
		logger.info(Constants.BEGIN_METHOD);
		int authorId = 0;
		StringBuilder sql = new StringBuilder();
		PreparedStatement prSmt = null;

		try {
			sql.append("select id from users where mail =?");
			prSmt = conn.prepareStatement(sql.toString());
			prSmt.setString(1, mail);
			ResultSet result = prSmt.executeQuery();
			if (result != null && result.next()) {
				authorId = result.getInt("id");
			} else {
				logger.debug(sql);
			}

		} catch (Exception e) {
			logger.error(e);
		}
		return authorId;
	}

	public int getProjectId(Connection conn, String projectName) {
		logger.info(Constants.BEGIN_METHOD);
		int projectId = 0;
		StringBuilder sql = new StringBuilder();
		PreparedStatement prSmt = null;

		try {
			sql.append("select id from projects where name =?");
			prSmt = conn.prepareStatement(sql.toString());
			prSmt.setString(1, projectName);
			ResultSet result = prSmt.executeQuery();
			if (result != null && result.next()) {
				projectId = result.getInt("id");
			} else {
				logger.debug(sql);
			}

		} catch (Exception e) {
			logger.error(e);
		}
		return projectId;
	}

	public int updateIssues(Connection conn, VngIssue issue) throws Exception {
		logger.info(Constants.BEGIN_METHOD);
		int result = 0;
		Map<Integer, String> errorMap = new HashMap<Integer, String>();

		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;

		Map<String, Integer> mapInteger;
		Map<String, Float> mapFloat;
		List<Integer> lsInteger = new ArrayList<Integer>();
		List<Float> lsFloat = new ArrayList<Float>();

		try {
			if (validateIssue(conn, issue, errorMap)) {
				// Update issue to db.
				sql.append("update issues set tracker_id =?,project_id=?,subject=?,description=?,due_date=?,");
				sql.append("status_id=?,priority_id=?,author_id=?,lock_version=?,created_on=?,updated_on=?,");
				sql.append("start_date=?,done_ratio=?,is_private=?");

				mapInteger = issue.getMapInteger();
				mapFloat = issue.getMapFloat();

				for (String key : mapInteger.keySet()) {
					if (mapInteger.get(key) != null) {
						sql.append(",").append(key).append("=?");
						lsInteger.add(mapInteger.get(key));
					}

				}

				for (String key : mapFloat.keySet()) {
					if (mapFloat.get(key) != null) {
						sql.append(",").append(key).append("=?");
						lsFloat.add(mapFloat.get(key));
					}
				}

				sql.append(" where id =?");

				prStm = conn.prepareStatement(sql.toString());
				prStm.setInt(1, issue.getTrackerId());
				prStm.setInt(2, issue.getProjectId());
				prStm.setString(3, issue.getSubject());
				prStm.setString(4, issue.getDescription());
				prStm.setString(5, issue.getDueDate());
				prStm.setInt(6, issue.getStatusId());
				prStm.setInt(7, issue.getPriorityId());
				prStm.setInt(8, issue.getAuthorId());
				prStm.setInt(9, issue.getLockVersion());
				prStm.setString(10, issue.getCreateOn());
				prStm.setString(11, issue.getUpdatedOn());
				prStm.setString(12, issue.getStartDate());
				prStm.setInt(13, issue.getDoneRatio());
				prStm.setBoolean(14, issue.isPrivate());

				for (int i = 0; i < lsInteger.size(); i++) {
					prStm.setInt(15 + i, lsInteger.get(i));
				}

				for (int i = 0; i < lsFloat.size(); i++) {
					prStm.setFloat(15 + lsInteger.size() + i, lsFloat.get(i));
				}

				prStm.setInt(15 + lsInteger.size() + lsFloat.size(),
						issue.getId());

				if (prStm.executeUpdate() > 0) {
					// Insert is successful
					result = prStm.getUpdateCount();
				} else {
					logger.debug(sql);
				}

			} else {
				logger.debug(errorMap.toString());
			}
		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.info(Constants.END_METHOD);
		}
		return result;
	}

	public boolean isExist(Connection conn, String tableName, int id)
			throws Exception {
		logger.info(Constants.BEGIN_METHOD);
		boolean isExist = false;
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;

		try {
			sql.append("select * from ").append(tableName)
					.append(" where id=?");
			prStm = conn.prepareStatement(sql.toString());
			prStm.setInt(1, id);

			ResultSet resultSet = prStm.executeQuery();
			if (resultSet != null && resultSet.next()) {
				return true;
			}

		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.info(Constants.END_METHOD);
		}

		return isExist;

	}

	public boolean isExistEnumeration(Connection conn, int id) throws Exception {
		logger.info(Constants.BEGIN_METHOD);
		boolean isExist = false;
		String ENUMERATION_TABLE = "enumerations";
		String PRIORITY_TYPE = "IssuePriority";
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;

		try {
			sql.append("select * from ").append(ENUMERATION_TABLE)
					.append(" where id=? and type=?");
			prStm = conn.prepareStatement(sql.toString());
			prStm.setInt(1, id);
			prStm.setString(2, PRIORITY_TYPE);

			ResultSet resultSet = prStm.executeQuery();
			if (resultSet != null && resultSet.next()) {
				return true;
			}

		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.info(Constants.END_METHOD);
		}

		return isExist;

	}

	private Integer getRootId(Connection conn, VngIssue issue) throws Exception {
		logger.info(Constants.BEGIN_METHOD);
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;
		Integer rootId = null;

		try {
			if (issue.getParentId() != null) {
				sql.append("select * from issues where id = ?");
				prStm = conn.prepareStatement(sql.toString());
				prStm.setInt(1, issue.getParentId());

				ResultSet result = prStm.executeQuery();
				if (result != null && result.next()) {
					rootId = result.getInt("root_id");
				}
			}

			issue.setRootId(rootId);

		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.info(Constants.END_METHOD);
		}
		return rootId;
	}

	private boolean validateIssue(Connection conn, VngIssue issue,
			Map<Integer, String> errorMap) throws Exception {
		logger.info(Constants.BEGIN_METHOD);

		boolean isPassed = true;

		try {

			if (errorMap == null) {
				errorMap = new HashMap<Integer, String>();
			}

			// Check project
			if (!isExist(conn, "projects", issue.getProjectId())) {
				errorMap.put(issue.getProjectId(), "projects");
			}

			// Check tracker
			if (!isExist(conn, "trackers", issue.getTrackerId())) {
				errorMap.put(issue.getTrackerId(), "trackers");
			}

			// Check status
			if (!isExist(conn, "issue_statuses", issue.getStatusId())) {
				errorMap.put(issue.getStatusId(), "issue_statuses");
			}

			// Check assign to id
			if (issue.getAssignedToId() != null
					&& !isExist(conn, "users", issue.getAssignedToId())) {
				errorMap.put(issue.getAssignedToId(), "users");
			}

			// Check author id
			if (!isExist(conn, "users", issue.getAuthorId())) {
				errorMap.put(issue.getAuthorId(), "users");
			}

			// Check fix version id
			if (issue.getFixedVersionId() != null
					&& !isExist(conn, "versions", issue.getFixedVersionId())) {
				errorMap.put(issue.getFixedVersionId(), "versions");
			}

			// Check category issue
			if (issue.getCategoryId() != null
					&& !isExist(conn, "issue_categories", issue.getCategoryId())) {
				errorMap.put(issue.getCategoryId(), "issue_categories");
			}

			// Check issue priority
			if (!isExistEnumeration(conn, issue.getPriorityId())) {
				errorMap.put(issue.getPriorityId(), "enumerations");
			}

			// Check parent id
			if (issue.getParentId() != null
					&& !isExist(conn, "issues", issue.getParentId())) {
				errorMap.put(issue.getParentId(), "issues");
			}

		} finally {
			logger.info(Constants.END_METHOD);
		}
		return isPassed;
	}

	public int insertAttackFile(Connection conn, VngAttachment att)
			throws Exception {
		logger.info(Constants.BEGIN_METHOD);
		int attchmentId = 0;
		StringBuilder sqlInsert = new StringBuilder();
		PreparedStatement prStm = null;

		try {
			sqlInsert.append("insert into attachments(container_id,container_type,filename,disk_filename,filesize,content_type,");
			sqlInsert.append("digest,downloads,author_id,created_on,description)")
					.append(" values(?,?,?,?,?,?,?,?,?,?,?)");
			prStm = conn.prepareStatement(sqlInsert.toString(),Statement.RETURN_GENERATED_KEYS);
			prStm.setInt(1, att.getContainerId());
			prStm.setString(2, att.getContainerType());
			prStm.setString(3, att.getFileName());
			prStm.setString(4, att.getDiskFileName());
			prStm.setInt(5, att.getFileSize());
			prStm.setString(6, att.getContentType());
			prStm.setString(7, att.getDigest());
			prStm.setInt(8, att.getDownloads());
			prStm.setInt(9, att.getAuthorId());
			att.setCreateOn(new Date());
			prStm.setString(10, att.getCreatedOn());
			prStm.setString(11, att.getDescription());

			if (prStm.executeUpdate() > 0) {
				attchmentId = getGenerateKey(prStm, 1);
			} else {
				logger.info("Cannot insert attach file to db");
				logger.debug(sqlInsert);
			}

		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.info(Constants.END_METHOD);

		}
		return attchmentId;
	}

	public boolean updateAttackFile(Connection conn, VngAttachment att)
			throws Exception {
		logger.info(Constants.BEGIN_METHOD);

		boolean result = false;
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;

		try {
			sql.append("update attachments set container_id = ?,container_type=?,filename=?,disk_filename=?,filesize=?,content_type=?,");
			sql.append("digest=?,downloads=?,author_id=?,created_on=?,description=? where id =?)");
			prStm = conn.prepareStatement(sql.toString());
			prStm.setInt(1, att.getContainerId());
			prStm.setString(2, att.getContainerType());
			prStm.setString(3, att.getFileName());
			prStm.setString(4, att.getDiskFileName());
			prStm.setInt(5, att.getFileSize());
			prStm.setString(6, att.getContentType());
			prStm.setString(7, att.getDigest());
			prStm.setInt(8, att.getDownloads());
			prStm.setInt(9, att.getAuthorId());
			prStm.setString(10, att.getCreatedOn());
			prStm.setString(11, att.getDescription());
			prStm.setInt(12, att.getId());

			if (prStm.execute()) {
				result = true;
			} else {
				logger.debug(sql);
			}

		} finally {
			logger.info(Constants.END_METHOD);
		}
		return result;
	}

	public int insertJournal(Connection conn, VngJournal journal)
			throws Exception {
		logger.info(Constants.BEGIN_METHOD);

		int journalId = 0;
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;

		try {
			sql.append("insert into journals(journalized_id,journalized_type,user_id,notes,created_on)");
			sql.append(" values(?,?,?,?,?)");
			prStm = conn.prepareStatement(sql.toString(),Statement.RETURN_GENERATED_KEYS);
			prStm.setInt(1, journal.getJournalizedId());
			prStm.setString(2, "Issue");// Hard code this property.
			prStm.setInt(3, journal.getUserId());
			prStm.setString(4, journal.getNotes());
			prStm.setString(5, journal.getCreatedOn());

			if (prStm.executeUpdate()>0) {
				journalId = getGenerateKey(prStm, 1);
				
			} else {
				logger.info("Cannot insert journal to db");
				logger.debug(sql);
			}

		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.info(Constants.END_METHOD);
		}
		return journalId;
	}

	public int insertJournalDetail(Connection conn,
			VngJournalDetail journalDetail) throws Exception {
		logger.info(Constants.BEGIN_METHOD);

		int journalDetailId = 0;
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;

		try {
			sql.append("insert into journal_details(journal_id,property,prop_key,old_value,value)");
			sql.append(" values(?,?,?,?,?)");
			prStm = conn.prepareStatement(sql.toString(),Statement.RETURN_GENERATED_KEYS);
			prStm.setInt(1, journalDetail.getJournal().getId());
			prStm.setString(2, journalDetail.getProperty());// Hard code this
															// property.
			prStm.setString(3, journalDetail.getProp_key());
			prStm.setString(4, journalDetail.getOld_value());
			prStm.setString(5, journalDetail.getValue());

			if (prStm.executeUpdate()>0) {
				journalDetailId = getGenerateKey(prStm, 1);
			} else {
				logger.info("Cannot insert detail info to db");
				logger.debug(sql);
			}

		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.info(Constants.END_METHOD);
		}
		return journalDetailId;
	}

	public int deleteIssue(Connection conn, int issueId) throws Exception{
		logger.info(Constants.BEGIN_METHOD);
		StringBuilder sql = new StringBuilder();
		PreparedStatement pStm = null;
		int effectRow = 0;
		try{
			sql.append("delete from issues where id = ?");
			pStm = conn.prepareStatement(sql.toString());
			pStm.setInt(1, issueId);
			
			effectRow = pStm.executeUpdate();
		}finally{
			if(pStm!=null){
				pStm.close();
			}
			logger.info(Constants.END_METHOD);
		}
		return effectRow;
	}
	
	public int insertWatcher(Connection conn,Watcher watcher) throws Exception{
		logger.info(Constants.BEGIN_METHOD);

		int watcherId = 0;
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;

		try {
			if(watcher==null || isExistWatcher(conn,watcher.getWatchableId(),watcher.getUserId()))
			{
				StringBuilder builder = new StringBuilder();
				builder.append("Watcher is exist.").append("[Issue:").append(watcher.getWatchableId());
				builder.append(" ,UserId:").append(watcher.getUserId()).append("]");
				logger.debug(builder.toString());
			}else
			{
				sql.append("insert into watchers(watchable_type,watchable_id,user_id)");
				sql.append(" values(?,?,?)");
				prStm = conn.prepareStatement(sql.toString(),Statement.RETURN_GENERATED_KEYS);
				prStm.setString(1, watcher.getWatchableType());
				prStm.setInt(2, watcher.getWatchableId());
				prStm.setInt(3, watcher.getUserId());

				if (prStm.executeUpdate()>0) {
					watcherId = getGenerateKey(prStm, 1);
				} else {
					logger.info("Cannot insert detail info to db");
					logger.debug(sql);
				}
			
			}
			

		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.info(Constants.END_METHOD);
		}
		return watcherId;
	}

	private boolean isExistWatcher(Connection conn,Integer issueId, Integer userId) throws Exception{
		
		logger.debug(Constants.BEGIN_METHOD);
		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;
		boolean returnVal = false;
		try {
			sql.append("select * from watchers where watchable_id=? and user_id=? ");
			prStm = conn.prepareStatement(sql.toString());
			prStm.setInt(1, issueId);
			prStm.setInt(2, userId);

			ResultSet resultSet = prStm.executeQuery();
			if (resultSet != null && resultSet.next()) {
				returnVal = true;
			}
			logger.debug(Constants.OK_METHOD);
		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.debug(Constants.END_METHOD);
		}
		logger.debug("Return value:" + returnVal);
		return returnVal;
	}
	
	public int updateJournal(Connection conn, VngJournal journal) throws Exception {
		logger.info(Constants.BEGIN_METHOD);
		int result = 0;

		StringBuilder sql = new StringBuilder();
		PreparedStatement prStm = null;
		try {
				// Update journal to db.
				sql.append("update journals set journalized_id =?,user_id=?,notes=?,created_on=?");
				sql.append(" where id =?");

				prStm = conn.prepareStatement(sql.toString());
				prStm.setInt(1, journal.getJournalizedId());
				prStm.setInt(2, journal.getUserId());
				prStm.setString(3, journal.getNotes());
				prStm.setString(4, journal.getCreatedOn());
				prStm.setInt(5, journal.getId());

				if (prStm.executeUpdate() > 0) {
					// Insert is successful
					result = prStm.getUpdateCount();
				} else {
					logger.debug(sql);
				}
			
		} finally {
			if (prStm != null) {
				prStm.close();
			}
			logger.info(Constants.END_METHOD);
		}
		return result;
	}


}
