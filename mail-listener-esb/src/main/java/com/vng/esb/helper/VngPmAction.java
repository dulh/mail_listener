package com.vng.esb.helper;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.vng.dao.pm.PMDao;
import com.vng.esb.beans.VngAttachment;
import com.vng.esb.beans.VngServerInfo;
import com.vng.esb.consts.Constants;

public class VngPmAction {
	private static final Logger logger = LogManager.getLogger(VngPmAction.class);
	
	public Map<Integer,String> insertAttachment(List<VngAttachment> lsVngAttachment, VngServerInfo serverInfo,Connection conn) throws Exception {
		logger.info(Constants.BEGIN_METHOD);
		VngUploader vngUploader = new VngUploader();
		PMDao pmDao = new PMDao();

		// Map attachment Id and attachment name 
		Map<Integer,String> mapAttachment = new HashMap<Integer, String>();
		Session session = null;
		
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(serverInfo.getUserName(), serverInfo.getHost(), serverInfo.getPort());
			// Setting for connect to server.
			session.setUserInfo(serverInfo);
			session.connect();
			
			// Process check, upload & insert attachment to db.
			for (VngAttachment vngAttachment : lsVngAttachment) {
				// Upload to server.
				if(vngUploader.uploadFiles(session, vngAttachment, serverInfo)) {
					// Upload success full. Insert to db.
					int id = pmDao.insertAttackFile(conn, vngAttachment);
					if(id<=0) {
						logger.error("Cannot inssert attachment to DB");
					} else {
						mapAttachment.put(id, vngAttachment.getFileName());
					}
				} else {
					// have a attachment cannot upload to host.
					throw new Exception("Cannot upload file to host");
				}
			}
		} finally {
			if (session != null) {
				session.disconnect();
			}
			logger.info(Constants.END_METHOD);
		}
		return mapAttachment;
		
	}
	
}
