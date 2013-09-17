package com.vng.esb.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.vng.dao.pm.PMDao;
import com.vng.db.ConnectionPool;
import com.vng.esb.beans.VngAttachment;
import com.vng.esb.beans.VngChangeObject;
import com.vng.esb.beans.VngIssue;
import com.vng.esb.beans.VngJournal;
import com.vng.esb.beans.VngJournalDetail;
import com.vng.esb.beans.VngServerInfo;
import com.vng.esb.beans.Watcher;
import com.vng.esb.config.ConfigUtils;
import com.vng.esb.consts.Constants;
import com.vng.esb.helper.VngPmAction;

public class EmailReceiverService implements MessageHandler {
	protected static final Logger logger = LogManager.getLogger(EmailReceiverService.class);
	protected Connection conn;
	private static final int INSERT = 0;
	private String configFile;
	private String consumer;
	private String projectName;

	public EmailReceiverService() {
	}

	public EmailReceiverService(String configFile, String consumer) {
		this.configFile = configFile;
		this.consumer = consumer;
	}

	public Connection getConnection() {
		try {
		    if (!ConnectionPool.isDataSourceExist(Constants.MYSQL_PMTOOL_DATASOURCE)) {
    			final InitialContext ctx = new InitialContext();
    			final DataSource mySqlPMTool = (DataSource) ctx.lookup(Constants.MYSQL_PMTOOL_DATASOURCE);
    			if (mySqlPMTool != null) {
    				logger.info(" My Sql PMTool is OK");
    				ConnectionPool.addDataSource(Constants.MYSQL_PMTOOL_DATASOURCE, mySqlPMTool);
    			}
		    }
			return ConnectionPool.getConnection(Constants.MYSQL_PMTOOL_DATASOURCE);
		} catch (Exception e) {
			logger.error("Fail to get connection for PM", e);
		}
		return null;
	}

	/** 
	 * Parse email message then create an issue on Redmine PM tool
	 * @see org.springframework.integration.core.MessageHandler#handleMessage(org.springframework.integration.Message)
	 * */
	public void handleMessage(org.springframework.integration.Message<?> message) throws MessagingException {
		conn = getConnection();
		String content = null;
		boolean isHtml = false;
		List<String> listAttachments = new ArrayList<String>();
		List<String> listAttachmentsInline = new ArrayList<String>();
		List<String> listAttachmentsInlineFullPath = new ArrayList<String>();
		List<String> lsCcEmails = new ArrayList<String>();
		List<Integer> lsWUserIds = new ArrayList<Integer>();
		MimeMessage msg = null;
		Object payLoad = message.getPayload();

		logger.info("Config file get from spring file :" + this.configFile);
		try {
			projectName = ConfigUtils.get(configFile, Constants.PROP_PROJECT_NAME).trim();
			logger.info("Project Name:" + projectName);
			
			if (payLoad != null && payLoad instanceof MimeMessage) {
				msg = (MimeMessage) payLoad;
			}
			if (msg == null) {
				logger.warn("Message is null !");
				return;
			}

			MimeMessageHelper helper = new MimeMessageHelper(msg);
			Address[] fromAdds = helper.getMimeMessage().getFrom();
			Address[] ccAdds = msg.getRecipients(Message.RecipientType.CC);
			String ccEmail = ccAdds == null ? null : ((InternetAddress) ccAdds[0]).getAddress();
			// String sendFrom = fromAdds != null && fromAdds.length > 0 ?
			// fromAdds[0].toString() : null;

			// build watcher list
			if (ccAdds != null && ccAdds.length > 0) {
				for (int i = 0; i < ccAdds.length; i++) {
					String watcher = ((InternetAddress) ccAdds[i]).getAddress();
					lsCcEmails.add(watcher);
				}
			}

			// Build user id for watcher.
			lsWUserIds = buildWatcherIds(conn, lsCcEmails);

			String fromEmail = fromAdds == null ? null : ((InternetAddress) fromAdds[0]).getAddress();
			String subject = helper.getMimeMessage().getSubject();
			String sendDate = helper.getMimeMessage().getSentDate().toString();

			// remove misc RE, FW, CT
            if (subject.startsWith(Constants.SUBJECT_EMAIL_RE)
                    || subject.startsWith(Constants.SUBJECT_EMAIL_CT)
                    || subject.startsWith(Constants.SUBJECT_EMAIL_FW)) {
                subject = subject.substring(3).trim();
            }
            
			// new update
            final String prefixProjectSupport = ConfigUtils.get(configFile, Constants.PROP_EMAIL_SUPPORT_SUBJECT_PREFIX);
			if (subject.startsWith(prefixProjectSupport)) {
			    subject = subject.substring(prefixProjectSupport.length()).trim();
			    this.projectName = ConfigUtils.get(configFile, Constants.PROP_PROJECT_SUPPORT);
			}
			
			if (helper.isMultipart()) {
				logger.info("helper is multipart");
				MimeMultipart multiParts = helper.getMimeMultipart();

				logger.info("multipart getcount(): " + multiParts.getCount());

				for (int i = 0; i < multiParts.getCount(); i++) {
					BodyPart part = multiParts.getBodyPart(i);
					content = (String) part.getContent();
					
				}
			} else {
				if (helper.getMimeMessage().isMimeType("text/plain")) {
					content = (String) helper.getMimeMessage().getContent();

				} else if (helper.getMimeMessage().isMimeType("text/html")) {
					content = (String) helper.getMimeMessage().getContent();
					isHtml = true;

				} else {
					if (helper.getMimeMessage().getContent() instanceof MimeMultipart) {

						MimeMultipart multiParts = (MimeMultipart) helper
								.getMimeMessage().getContent();
						logger.debug("Multipart count:" + multiParts.getCount());

						for (int i = 0; i < multiParts.getCount(); i++) {
							Part part = multiParts.getBodyPart(i);

							String diposition = part.getDisposition();
							logger.debug("Dipostion:" + diposition);

							if (diposition == null && i == 0) { // content of
																// message
								BodyPart mbp = (BodyPart) part;
								if (i == 0) {
									content = mbp.getContent().toString();
									isHtml = true;
								} else {
									String fileAttached = processAttachment(mbp);
									logger.info("Attachment out line: "
											+ fileAttached);
									if (fileAttached != null) {
										listAttachments.add(fileAttached);
									}

								}
							} else {
								if (diposition != null && ((diposition.equals(Part.ATTACHMENT) || diposition.equals(Part.INLINE)))) {
									String fileAttached = processAttachment(part);
									if (diposition.equals(Part.INLINE)) {
										logger.debug("Attachment in line : " + part.getFileName());
										listAttachmentsInline.add(part.getFileName());
										listAttachmentsInlineFullPath.add(fileAttached);
									} else {
										if (fileAttached != null) {
											logger.info("Attachment out line: " + fileAttached);
											listAttachments.add(fileAttached);
										}

									}
								}
							}
						}
					}
				}
			}

			if (isHtml) {
				content = getMailContent(subject, content);
			}

			logger.debug("Content email:" + content);
			// Call code to create issue here

			// Check to insert or update
			int issueId = issueIsExist(conn, projectName, subject);
			if (issueId == INSERT) {
				if (createIssue(fromEmail, ccEmail, projectName, subject,
						content, listAttachments, listAttachmentsInline,
						listAttachmentsInlineFullPath, lsWUserIds)) {
					logger.info("Inserting a issue is successful.");
				} else {
					logger.info("Inserting a issue is failed.");
				}

			} else {
				if (updateIssue(issueId, fromEmail, ccEmail, projectName,
						subject, content, listAttachments,
						listAttachmentsInline, listAttachmentsInlineFullPath,
						lsWUserIds)) {
					logger.info("Updating a issue is successful.");
				} else {
					logger.info("Updating a issue is failed.");
				}
			}

			logger.info("MailFrom: " + fromEmail);
			logger.info("MailContent: " + content);
			logger.info("MailDate: " + sendDate);
			for (String attachedFile : listAttachments) {
				logger.info("AttachedFile: " + attachedFile);
			}

		} catch (IOException e) {
			logger.error(stack2string(e));
		} catch (javax.mail.MessagingException e) {
			logger.error(stack2string(e));
		} catch (Exception e) {
			logger.error(stack2string(e));
		}
	}

	/**
	 * Get file from part object.
	 *
	 * @param part the part
	 * @return the string which is the 
	 */
	private String processAttachment(Part part) {
		// create temp file at local then return the file path
		final String tempDir = System.getProperty("java.io.tmpdir")
				+ File.separator + this.consumer;
		String resultFilePath = null;
		File fileTemp = null;
		FileOutputStream fos = null;
		InputStream inputStream = null;
		PrintStream printStream = null;
		try {
			if (part != null) {

				// Create temp directory for each consume.
				logger.debug("Temp dir: " + tempDir);
				File tempFile = new File(tempDir);
				if (!tempFile.exists()) {
					tempFile.setReadable(true, false);
					tempFile.setWritable(true, false);
					tempFile.setExecutable(true, false);
					if (tempFile.mkdirs()) {
						logger.debug("Create temp directory is successful!");
					} else {
						logger.debug("Create temp directory is failed!");
					}
				}

				fileTemp = new File(tempDir + File.separator + part.getFileName().replace(" ", ""));
				logger.info("Attach file :" + tempDir + File.pathSeparator + part.getFileName());
				fos = new FileOutputStream(fileTemp);

				if (part.getContent() instanceof InputStream) {
					// attachment is binary format
				    inputStream = (InputStream) part.getContent();

					int read = 0;
					byte[] bytes = new byte[1024];
					while ((read = inputStream.read(bytes)) != -1) {
						fos.write(bytes, 0, read);
					}
				} else {
					// attachment is text format
					printStream = new PrintStream(fileTemp);
					printStream.print(part.getContent());
				}
			}
		} catch (Exception e) {
			logger.error("Error in processAttachment", e);
		} finally {
			try {
				if (fos != null) {
				    fos.flush();
					fos.close();
				}
				if (printStream != null) {
				    printStream.flush();
				    printStream.close();
				}
				if (inputStream != null) {
				    inputStream.close();
				}
				logger.info("File size of temp file:" + fileTemp.length() + "byte(s)");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// return file path at local
		if (fileTemp != null) {
			resultFilePath = fileTemp.getAbsolutePath();
		}
		return resultFilePath;
	}

	/**
	 * Gets the mail content.
	 *
	 * @param subject the subject
	 * @param content the content
	 * @return the mail content
	 * @throws Exception the exception
	 */
	private String getMailContent(String subject, String content)
			throws Exception {
		if (content != null) {
			// get content in body tag
			int beginIdx = content.indexOf("<body");
			int endIdx = content.lastIndexOf("</body>");

			if (beginIdx > 0 && endIdx > 0) {
				content = content.substring(beginIdx, endIdx);
			}
			// Get content email.
			content = convertHtmlTagToRemind(content);

			// ignore previous content
			if (subject.contains("RE:")) {
				subject = subject.substring(3).trim();
			}
			// check issue here.
			int result = issueIsExist(conn, projectName, subject);
			if (INSERT != result) {
				int lastIdx = content.indexOf(subject);
				if (lastIdx > 0) {
					// remove all previous content
					content = content.substring(0, lastIdx);

					// search the last 'From:' tag
					lastIdx = content.indexOf("From:");
					if (lastIdx > 0) {
						content = content.substring(0, lastIdx);
					}
					// Remove the last character "*"
					content = content.substring(0, content.length() - 1);
				}
			}

		}
		return content;
	}

	/**
	 * Creates the issue on redmine.
	 *
	 * @param fromEmail the from email
	 * @param ccEmail the cc email
	 * @param projectName the project name
	 * @param subject the subject
	 * @param description the description
	 * @param attachedFiles the attached files
	 * @param attachedFileInline the attached file inline
	 * @param attachedFileInlineFull the attached file inline full
	 * @param lsWUserIds the ls w user ids
	 * @return true, if successful
	 */
	protected boolean createIssue(String fromEmail, String ccEmail,
			String projectName, String subject, String description,
			List<String> attachedFiles, List<String> attachedFileInline,
			List<String> attachedFileInlineFull, List<Integer> lsWUserIds) {
		final String method = "createIssue";
		logger.info(method + Constants.BEGIN_METHOD);

		boolean result = false;
		PMDao pmDao = new PMDao();
		VngPmAction pmAction = new VngPmAction();
		List<VngAttachment> lsVngAttachment = new ArrayList<VngAttachment>();
		List<VngAttachment> lsVngAttachmentInline = new ArrayList<VngAttachment>();

		VngIssue issue = new VngIssue();
		int authorId;
		int projectId;
		int issueId = 0;
		int assignedToId = 0;
		Map<Integer, String> mapAttachement = new HashMap<Integer, String>();
		try {
			authorId = pmDao.getUserId(conn, fromEmail);
			projectId = pmDao.getProjectId(conn, projectName);
			if (ccEmail != null && !ccEmail.isEmpty()) {
				assignedToId = pmDao.getUserId(conn, ccEmail);
				issue.setAssignedToId(assignedToId);
			} else {
				issue.setAssignedToId(authorId);
			}

			issue.setAuthorId(authorId);
			issue.setSubject(subject);
			issue.setDescription(description);
			issue.setProjectId(projectId);
			Date date = new Date();
			issue.setCreateOn(date);
			issue.setUpdatedOn(date);

			issue.setLft(1);
			issue.setRgt(2);

			// 1. Insert issue to db.
			issueId = pmDao.insertIssues(conn, issue);

			if (issueId <= 0) {
				logger.error("Cannot insert Issue to db");
				return false;
			}

			// 2. Insert attachment into db & upload to server.
			String host = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_HOST_IP);
			String userName = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_USER_NAME);
			String passWord = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_PASS_WORD);
			String destination = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_DESTINATION_FOLDER);
			int port = Integer.valueOf(ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_PORT));
			String hostName = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_HOST_NAME);
			VngServerInfo serverInfo = new VngServerInfo(host, userName, passWord, destination, port);

			lsVngAttachmentInline = buildAttachment(attachedFileInlineFull, authorId, issueId);
			lsVngAttachment = buildAttachment(attachedFiles, authorId, issueId);
			transferAttInNotImageToOut(lsVngAttachmentInline, lsVngAttachment);

			if (lsVngAttachment.size() > 0) {
				pmAction.insertAttachment(lsVngAttachment, serverInfo, conn);
			}

			// 3. Upload in line attachment to host.
			if (lsVngAttachmentInline != null && lsVngAttachmentInline.size() > 0) {
				mapAttachement = pmAction.insertAttachment(lsVngAttachmentInline, serverInfo, conn);
			}

			// 4. Update issue description.
			if (attachedFileInline != null && mapAttachement != null) {
				updateDescriptionWithImage(attachedFileInline, mapAttachement, issue, conn, hostName);
			}

			// 5. Update watcher for issue.
			for (Integer userId : lsWUserIds) {
				Watcher watcher = new Watcher();
				watcher.setUserId(userId);
				watcher.setWatchableId(issueId);
				watcher.setWatchableType("Issue");// Hard code for this.

				int watcherId = pmDao.insertWatcher(conn, watcher);
				if (watcherId > 0) {
					logger.debug("Insert watcher: " + watcherId);
				} else {
					logger.error(String.format("Cannot insert watcher with IssueId:%d and UserId:%d", issueId, userId));
				}
			}

			result = true;
		} catch (Exception e) {
			// Error occur
			if (issueId > 0) {
				try {
					pmDao.deleteIssue(conn, issueId);
				} catch (Exception e1) {
					logger.error(stack2string(e1));
				}
			}
			logger.error(stack2string(e));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e2) {
					logger.error(stack2string(e2));
				}
			}
			logger.info(method + Constants.END_METHOD);
		}
		return result;
	}

	private void updateDescriptionWithImage(List<String> attachedFileInline,
			Map<Integer, String> mapAttachement, VngIssue issue,
			Connection conn, final String hostName) throws Exception {
		logger.debug(Constants.BEGIN_METHOD);

		try {
			if (mapAttachement != null && attachedFileInline != null
					&& attachedFileInline.size() > 0 && issue != null) {

				String des = issue.getDescription();
				logger.debug("Descrpiton before update:" + des);

				int indexImage = 0;
				for (String attfile : attachedFileInline) {
					logger.debug("Inline image:" + attfile);

					StringBuilder linkImage = new StringBuilder();
					linkImage.append(" !").append("https://").append(hostName).append("/attachments/download/");
					int attId = 0;
					for (Integer item : mapAttachement.keySet()) {
						if (mapAttachement.get(item).equals(attfile)) {
							attId = item;
							logger.debug("Image Id:" + item);
							break;
						}
					}

					if (attId > 0) {
						linkImage.append(attId).append("/").append(mapAttachement.get(attId)).append("! ");
						indexImage = des.indexOf(Constants.IMAGE, indexImage + 1);
						if (indexImage > 0) {
							String subBefDes = des.substring(0, indexImage);
							String subAfDes = des.substring(indexImage + Constants.IMAGE.length(), des.length());
							subBefDes += linkImage;
							des = subBefDes + subAfDes;
						}
					}
				}

				logger.debug("Description after replace:" + des);

				// Update description.
				PMDao pmDao = new PMDao();
				issue.setDescription(des);
				if (pmDao.updateIssues(conn, issue) > 0) {
					logger.debug(Constants.OK_METHOD);
				}
			}

		} catch (Exception e) {
			throw e;
		}
	}

	private void updateNotesWithImage(List<String> attachedFileInline,
			Map<Integer, String> mapAttachement, VngJournal journal,
			Connection conn, final String hostName) throws Exception {
		logger.debug(Constants.BEGIN_METHOD);

		try {
			if (mapAttachement != null && attachedFileInline != null && attachedFileInline.size() > 0) {

				String des = journal.getNotes();
				logger.debug("Descrpiton before update:" + des);

				int indexImage = 0;
				if (des != null) {
					for (String attfile : attachedFileInline) {
						logger.debug("Inline image:" + attfile);

						StringBuilder linkImage = new StringBuilder();
						linkImage.append(" !").append("https://").append(hostName).append("/attachments/download/");
						int attId = 0;
						for (Integer item : mapAttachement.keySet()) {
							if (mapAttachement.get(item).equals(attfile)) {
								attId = item;
								logger.debug("Image Id:" + item);
								break;
							}
						}

						if (attId > 0) {
							linkImage.append(attId).append("/").append(mapAttachement.get(attId)).append("! ");
							indexImage = des.indexOf(Constants.IMAGE, indexImage + 1);
							if (indexImage > 0) {
								String subBefDes = des.substring(0, indexImage);
								String subAfDes = des.substring(indexImage + Constants.IMAGE.length(), des.length());
								subBefDes += linkImage;
								des = subBefDes + subAfDes;
							}
						}
					}

					logger.debug("Description after replace:" + des);

					// Update notes.
					PMDao pmDao = new PMDao();
					journal.setNotes(des);
					if (pmDao.updateJournal(conn, journal) > 0) {
						logger.debug(Constants.OK_METHOD);
					}
				}
			}

		} catch (Exception e) {
			throw e;
		}
	}

	private List<VngAttachment> buildAttachment(List<String> lsAttachmentFiles, int authorId, int issueId) {
		logger.debug(Constants.BEGIN_METHOD);

		List<VngAttachment> lsAttachment = new ArrayList<VngAttachment>();

		for (String attachFile : lsAttachmentFiles) {
			logger.debug("Attach file:" + attachFile);
			VngAttachment vngAtt = new VngAttachment();
			vngAtt.setAuthorId(authorId);
			vngAtt.setContainerId(issueId);
			vngAtt.setFullPath(attachFile);
			lsAttachment.add(vngAtt);
		}
		logger.debug(Constants.END_METHOD);
		return lsAttachment;
	}

	protected boolean updateIssue(int issueId, String creator, String assigned,
			String projectName, String subject, String notes,
			List<String> attachedFiles, List<String> attachedFileInline,
			List<String> attachedFileInlineFull, List<Integer> lsWUserId) {
		final String method = "updateIssue";
		logger.info(method + Constants.BEGIN_METHOD);

		boolean result = false;
		PMDao pmDao = new PMDao();
		VngPmAction pmAction = new VngPmAction();
		List<VngAttachment> lsVngAttachment = new ArrayList<VngAttachment>();
		List<VngAttachment> lsVngAttachmentFull = new ArrayList<VngAttachment>();

		VngIssue oldissue = new VngIssue();
		VngIssue newIssue = new VngIssue();
		int updater;
		int assigner = 0;

		VngJournal journal = null;
		VngJournalDetail journalDetail;

		List<VngChangeObject> lsChange = new ArrayList<VngChangeObject>();
		Map<Integer, String> mapAttachment = null;
		Map<Integer, String> mapAttachmentInline = null;

		try {

			updater = pmDao.getUserId(conn, creator);
			if (assigned != null && !assigned.isEmpty()) {
				assigner = pmDao.getUserId(conn, assigned);
			}

			newIssue.setId(issueId);
			newIssue.setAuthorId(updater);

			String hostName = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_HOST_NAME);
			String host = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_HOST_IP);
			String userName = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_USER_NAME);
			String passWord = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_PASS_WORD);
			String destination = ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_DESTINATION_FOLDER);
			int port = Integer.valueOf(ConfigUtils.get(this.configFile, Constants.PROP_PMTOOL_PORT));

			VngServerInfo serverInfo = new VngServerInfo(host, userName, passWord, destination, port);

			// Insert attachment into db & upload to server.
			lsVngAttachment = buildAttachment(attachedFiles, updater, issueId);
			lsVngAttachmentFull = buildAttachment(attachedFileInlineFull, updater, issueId);
			transferAttInNotImageToOut(lsVngAttachmentFull, lsVngAttachment);

			if (lsVngAttachment.size() > 0) {
				// Upload file to server & write attachment to db.
				mapAttachment = pmAction.insertAttachment(lsVngAttachment, serverInfo, conn);

			}

			// Count image in content mail.
			int countImangeInline = 0;
			String subNotes = notes;
			while (true) {
				int index = subNotes.indexOf(Constants.IMAGE);
				if (index < 0) {
					break;
				}
				subNotes = subNotes.substring(index + Constants.IMAGE.length());
				countImangeInline++;
			}
			logger.debug("Number of image in email content:" + countImangeInline);

			// remove attache files that was not updated.
			for (int i = lsVngAttachmentFull.size() - 1; i > countImangeInline - 1; i--) {
				lsVngAttachmentFull.remove(i);
			}

			if (lsVngAttachmentFull.size() > 0 && countImangeInline > 0) {
				// Upload file to server & write attachment to db.
				mapAttachmentInline = pmAction.insertAttachment(lsVngAttachmentFull, serverInfo, conn);
			}

			if (issueId > 0) {
				oldissue = pmDao.getIssue(conn, issueId);
				//set value from old issue
				newIssue.setStatusId(oldissue.getStatusId());
				newIssue.setStartDate(oldissue.getStartDate());
				newIssue.setDueDate(oldissue.getDueDate());
				newIssue.setAssignedToId(oldissue.getAssignedToId());
				newIssue.setFixedVersionId(oldissue.getFixedVersionId());
				newIssue.setDoneRatio(oldissue.getDoneRatio());
				newIssue.setEstimatedHour(oldissue.getEstimatedHour());
				newIssue.setPriorityId(oldissue.getPriorityId());
				newIssue.setParentId(oldissue.getParentId());
				newIssue.setRemainingHour(oldissue.getRemainingHour());
				newIssue.setStoryPoint(oldissue.getStoryPoints());
			}

			// Insert to journal.
			if (oldissue != null && newIssue != null) {

				lsChange = oldissue.compare(newIssue);
				journal = new VngJournal();
				journal.setCteatedOn(new Date());
				journal.setJournalizedId(issueId);
				journal.setNotes(notes);
				journal.setUserId(updater);

				int journalId = pmDao.insertJournal(conn, journal);
				journal.setId(journalId);

				if (journalId > 0) {
					for (VngChangeObject changeObj : lsChange) {

						journalDetail = new VngJournalDetail();
						journalDetail.setJournal(journal);
						journalDetail.setOld_value(changeObj.getOldValue());
						journalDetail.setProperty(VngJournalDetail.PRO_ATTR);
						journalDetail.setProp_key(changeObj.getProperty());
						journalDetail.setValue(changeObj.getNewValue());

						pmDao.insertJournalDetail(conn, journalDetail);
					}
				}

				// Update assigner.
				if (assigner > 0 && assigner != oldissue.getAssignedToId()) {
					oldissue.setAssignedToId(assigner);
					pmDao.updateIssues(conn, oldissue);
				}

				// Update note.
				if (journal != null && attachedFileInline != null
						&& mapAttachmentInline != null) {
					updateNotesWithImage(attachedFileInline,
							mapAttachmentInline, journal, conn, hostName);
				}
			}

			// Track attachment in journal detail table.
			if (mapAttachment != null) {
				for (Integer attId : mapAttachment.keySet()) {
					journalDetail = new VngJournalDetail();
					journalDetail.setJournal(journal);
					journalDetail.setProperty(VngJournalDetail.PRO_ATTACHMENT);
					journalDetail.setProp_key(String.valueOf(attId));
					journalDetail.setValue(mapAttachment.get(attId));
					pmDao.insertJournalDetail(conn, journalDetail);
				}
			}

			// Update watcher for issue.
			for (Integer userId : lsWUserId) {
				Watcher watcher = new Watcher();
				watcher.setUserId(userId);
				watcher.setWatchableId(oldissue.getId());
				watcher.setWatchableType("Issue");// Hard code for this.
				pmDao.insertWatcher(conn, watcher);
			}

			logger.info(Constants.OK_METHOD);
			result = true;
		} catch (Exception e) {
			logger.error(stack2string(e));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e2) {
					logger.error(stack2string(e2));
				}
			}
			logger.info(Constants.END_METHOD);
		}
		return result;
	}

	private int issueIsExist(Connection conn, String projectName, String subject) throws Exception {
		logger.debug(Constants.BEGIN_METHOD);
		int result = 0;
		int projectId = 0;
		PMDao pmDao = new PMDao();
		try {

			projectId = pmDao.getProjectId(conn, projectName);
			if (projectId > 0) {
				int issueId = pmDao.getIssueId(conn, subject, projectId);
				if (issueId > 0) {
					// Update.
					result = issueId;
				} else {
					// Insert.
					issueId = parseSubjectToUpdate(conn, subject);
					if (issueId > 0) {
						result = issueId;
					} else {
						result = INSERT;
					}

				}
			} else {
				logger.debug(String.format("Cannot find [project = %s ] in db", projectName));
			}
		} finally {
			logger.debug(Constants.END_METHOD);
		}

		return result;
	}

	private String convertHtmlTagToRemind(String htmlText) {
		logger.debug(Constants.BEGIN_METHOD);
		String result = "";
		String subBefore = "";
		String subAfter = "";
		String subProcess = "";

		try {

			// Bold
			result = htmlText.replace("<strong>", " *");
			result = result.replace("</strong>", "* ");
			result = result.replace("<b>", "*");
			result = result.replace("</b>", "*");

			// H1
			result = result.replace("<h1>", " h1. ");
			result = result.replace("</h1>", "\n");
			result = result.replace("<h2>", " h2. ");
			result = result.replace("</h2>", "\n");
			result = result.replace("<h3>", " h3. ");
			result = result.replace("</h3>", "\n");

			// New line
			result = result.replace("<p class=MsoNormal>", Constants.ENTER);
			result = result.replace("<p>", Constants.ENTER);
			result = result.replace("</p>", Constants.ENTER);
			result = result.replace("</br>", Constants.ENTER);
			result = result.replace("<br>", Constants.ENTER);

			// italic
			result = result.replace("<i>", " _");
			result = result.replace("</i>", "_ ");

			// underline
			result = result.replace("<u>", " +");
			result = result.replace("</u>", "+ ");

			// Delete
			result = result.replace("<strike>", " -");
			result = result.replace("</strike>", "- ");

			// Unordered list
			int indexBegin;
			int indexEnd;
			indexBegin = result.indexOf("<ul>");
			indexEnd = result.indexOf("</ul>");
			while (indexBegin >= 0) {
				subBefore = result.substring(0, indexBegin);
				subProcess = "\n" + result.substring(indexBegin, indexEnd + 5);
				if ((indexEnd + 5) < result.length()) {
					subAfter = result.substring(indexEnd + 5, result.length());
				}

				subProcess = subProcess.replace("<li>", "* ");
				subProcess = subProcess.replace("</li>", "\n");
				subProcess = subProcess.replace("<ul>", "\n");
				subProcess = subProcess.replace("</ul>", "\n");
				result = subBefore + subProcess + subAfter;
				indexBegin = result.indexOf("<ul>");
				indexEnd = result.indexOf("</ul>");
				subBefore = "";
				subAfter = "";
			}

			// Oredered list.
			indexBegin = result.indexOf("<ol>");
			indexEnd = result.indexOf("</ol>");
			while (indexBegin >= 0) {

				subBefore = result.substring(0, indexBegin - 1);
				subProcess = "\n" + result.substring(indexBegin, indexEnd + 5);
				if ((indexEnd + 5) < result.length()) {
					subAfter = result.substring(indexEnd + 5, result.length());
				}
				subProcess = subProcess.replace("<li>", "# ");
				subProcess = subProcess.replace("</li>", "\n");
				subProcess = subProcess.replace("<ol>", "\n");
				subProcess = subProcess.replace("</ol>", "\n");
				result = subBefore + subProcess + subAfter;
				indexBegin = result.indexOf("<ol>");
				indexEnd = result.indexOf("</ol>");
				subBefore = "";
				subAfter = "";
			}
			;
			// Process Image
			logger.debug("Before replace Image:" + result);
			int indexImg = result.indexOf("<img");
			int idexClose = result.indexOf(">", indexImg);

			while (indexImg >= 0) {
				String subBefImg = result.substring(0, indexImg);
				String subAftImg = result.substring(idexClose + 1,
						result.length());
				subBefImg = subBefImg + Constants.IMAGE;
				result = subBefImg + subAftImg;

				indexImg = result.indexOf("<img");
				idexClose = result.indexOf(">", indexImg);
			}
			logger.debug("After replace Image(HTML):" + result);
			result = Jsoup.parse(result).text();
			result = result.replace(Constants.ENTER, "\n");
			logger.debug("After replace Image(None HTML):" + result);

			logger.debug(Constants.OK_METHOD);
		} catch (Exception e) {
			logger.error(stack2string(e));
		} finally {
			logger.debug(Constants.END_METHOD);
		}

		return result;
	}

	private int parseSubjectToUpdate(Connection conn, String subject) {
		logger.info(Constants.BEGIN_METHOD);
		int issueId = 0;
		String projectName = "";
		int projectId = 0;
		PMDao pmDao = new PMDao();

		try {
			int indexBegin = subject.indexOf("[");
			int indexEnd = subject.indexOf("]");
			String strParse = subject.substring(indexBegin, indexEnd + 1);
			issueId = Integer.valueOf(strParse.substring(
					strParse.indexOf("#") + 1, strParse.length() - 1).trim());
			logger.debug("Issue id from subject:" + issueId);
			int lastIndex = 0;
			int loopIndex = 0;
			do {
				lastIndex = loopIndex;
				loopIndex = strParse.indexOf("-", lastIndex + 1);

			} while (loopIndex > 0);

			projectName = strParse.substring(1, lastIndex - 1);

			logger.debug("Project name from subject:" + projectName);
			// get Issue.
			VngIssue issue = pmDao.getIssue(conn, issueId);
			if (issue != null) {
				projectId = pmDao.getProjectId(conn, projectName);
			}

			// Compare project name
			if (projectId <= 0 || projectId != issue.getProjectId()) {
				issueId = 0;
			}

			logger.info(Constants.OK_METHOD);
		} catch (Exception e) {
			return issueId;
		} finally {
			logger.debug("Return issue id:" + issueId);
			logger.info(Constants.END_METHOD);
		}
		return issueId;
	}

	public static String stack2string(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		} catch (Exception e2) {
			return "bad stack2string";
		}
	}

	private List<Integer> buildWatcherIds(Connection conn, List<String> lsCcEmail) {
		logger.debug(Constants.BEGIN_METHOD);
		List<Integer> lsWatcherIds = new ArrayList<Integer>();
		PMDao pmDao = new PMDao();
		
		try {

			for (String email : lsCcEmail) {
				Integer userId = pmDao.getUserId(conn, email);
				lsWatcherIds.add(userId);
			}

			logger.debug(Constants.OK_METHOD);
		} catch (Exception ex) {
			logger.error(stack2string(ex));
		} finally {
			logger.debug(Constants.END_METHOD);
		}
		return lsWatcherIds;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getConsumer() {
		return consumer;
	}

	public void setConsumer(String consumer) {
		this.consumer = consumer;
	}

	public void transferAttInNotImageToOut(List<VngAttachment> attInline, List<VngAttachment> outLine) {
		for (int i = attInline.size() - 1; i >= 0; i--) {
			if (!attInline.get(i).getContentType().matches("image.*")) {
				logger.debug("Not image file:" + attInline.get(i).getFileName());
				outLine.add(attInline.get(i));
				attInline.remove(attInline.get(i));
			}

		}
	}
}
