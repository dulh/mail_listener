package com.vng.esb.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.vng.esb.beans.VngAttachment;
import com.vng.esb.beans.VngServerInfo;
import com.vng.esb.consts.Constants;

public class VngUploader {
	private static final Logger logger = LogManager
			.getLogger(VngUploader.class);

	public boolean uploadFiles(final Session session, VngAttachment vngAttachment, VngServerInfo serverInfo) {
		logger.info(Constants.BEGIN_METHOD);

		FileInputStream fileInputStream = null;
		boolean result = false;
		
		try {
			boolean ptimestamp = true;

			// Upload file to server.
			File sentFile = new File(vngAttachment.getFullPath());

			// Establish connection to server.
			String exeCommand = "scp " + (ptimestamp ? "-p" : "") + " -t "
					+ serverInfo.getDestinationFolder() +  vngAttachment.getDiskFileName();
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(exeCommand);

			// get I/O streams for remote scp
			OutputStream outputStream = channel.getOutputStream();
			InputStream inputStream = channel.getInputStream();

			channel.connect();

			if (checkAck(inputStream) != 0) {
				return false;
			}

			if (ptimestamp) {
				exeCommand = "T " + (sentFile.lastModified() / 1000) + " 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				exeCommand += (" " + (sentFile.lastModified() / 1000) + " 0\n");
				outputStream.write(exeCommand.getBytes());
				outputStream.flush();
				if (checkAck(inputStream) != 0) {
					return false;
				}
			}

			// send "C0644 filesize filename", where filename should not
			// include
			// '/'
			long filesize = sentFile.length();
			logger.debug("File size(bef): " + filesize +"byte(s)");
			
			exeCommand = "C0644 " + filesize + " ";
			if (vngAttachment.getFullPath().lastIndexOf('/') > 0) {
				exeCommand += vngAttachment.getFullPath().substring(vngAttachment.getFullPath()
						.lastIndexOf('/') + 1);
			} else {
				exeCommand += vngAttachment.getFullPath();
			}
			exeCommand += "\n";
			outputStream.write(exeCommand.getBytes());
			outputStream.flush();
			if (checkAck(inputStream) != 0) {
				return false;
			}

			// Send content file to destination.
			fileInputStream = new FileInputStream(vngAttachment.getFullPath());
			byte[] buffer = new byte[1024];
			int byteRead = 0;
			int fileReadCount = 0;
			while ((byteRead=fileInputStream.read(buffer))>0) {
				fileReadCount +=byteRead;
				outputStream.write(buffer,0,byteRead);
			}

			logger.debug("Total size readed: " + fileReadCount + "byte(s)");
			// send '\0'
			buffer[0] = 0;
			outputStream.write(buffer, 0, 1);
			outputStream.flush();

			if (checkAck(inputStream) != 0) {
				return false;
			}

			outputStream.close();
			channel.disconnect();
			result = true;

		} catch (Exception e) {
			logger.error("Error in uploadFiles", e);
		} finally {
			try {

				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (Exception e) {
				logger.error(e);
			}
			
			// Delete file in temp folder.
			File deleteFile = new File(vngAttachment.getFullPath());
			if(deleteFile.delete()){
				logger.debug("Delete temp file is successful");
			}else{
				logger.debug("Cannot delete temp file");
			}
			logger.info(Constants.END_METHOD);
		}
		return result;
	}

	private int checkAck(InputStream in) throws IOException {
		logger.debug(Constants.BEGIN_METHOD);
		int isOk = 0;

		try {
			isOk = in.read();
			// b may be 0 for success,
			// 1 for error,
			// 2 for fatal error,
			// -1
			switch (isOk) {
			case 1:
			case 2:
				StringBuffer sb = new StringBuffer();
				int c;
				do {
					c = in.read();
					sb.append((char) c);
				} while (c != '\n');
				logger.error(sb.toString());

				break;
			default:
				break;
			}

		} catch (Exception e) {
			logger.error(e);
		} finally {
			logger.debug(Constants.END_METHOD);
		}

		return isOk;
	}
}
