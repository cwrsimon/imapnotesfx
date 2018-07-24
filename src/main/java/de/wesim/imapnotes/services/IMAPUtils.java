package de.wesim.imapnotes.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import de.wesim.imapnotes.HasLogger;

public class IMAPUtils implements HasLogger {

	// TODO !!!!
	public String decodeMultipartMails(Message message) throws MessagingException, IOException {
		

		// TODO MimeMultipart hier unterstützen
		getLogger().info("Message class: {}", message.getClass().getName());
		getLogger().info("Content type: {}", message.getContentType());
		String returnMe = "MultiPart";
		// TODO Später mal die Bilder auflösen ...
		Pattern p = Pattern.compile("<object type.*?></object>");
		String base64Content = "";
		final MimeMultipart multiPart = (MimeMultipart) message.getContent();
		for (int i=0; i<multiPart.getCount();i++) {
			BodyPart bp = multiPart.getBodyPart(i);
			getLogger().info("Index: {}, Content-Type: {}, Filename: {}, Content-Id: {}", i, 
				bp.getContentType() , bp.getFileName(), bp.getHeader("Content-Id"));
			Object partContent = bp.getContent();
			if (partContent instanceof String) {
				returnMe = (String) partContent;
			} else {
				// TODO CIDs auflösen
				// TODO Konvertieren nach PNG
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bp.getDataHandler().writeTo(bos);
				bos.close();
				byte[] bytes = bos.toByteArray();
				String read = new String(Files.readAllBytes(Paths.get("/Users/christian/some-content.base64")));
				//Files.write(Paths.get("/Users/christian/some-content"), bytes);
				// src="data:image/png;base64,iVBOR…Fy/NYpbmRyKWAAAAAElFTkSuQmCC"
				// base64Content = "<img src=\"data:image/jpeg;base64," 
				// 	+ Base64.getEncoder().encodeToString(bytes) + "\"/>";
				base64Content = "<img src=\"data:image/png;base64," 
					+ read + "\"/>";
				//logger.info("{}", );
			}

			getLogger().info("Content: {}", partContent);
		}
		getLogger().info("ReturnMe:{}", returnMe);

		Matcher m = p.matcher(returnMe);
		if (m.find()) {
			getLogger().info("Found!");
			returnMe = m.replaceAll(base64Content);
		}
		return returnMe;
		// <img src="data:image/jpg;base64,/*base64-data-string here*/" />
		//Files.write(Paths.get("/Users/christian/bla.html"), returnMe.getBytes());
		//logger.info("ReturnMe:{}", returnMe);
	}
	
	
}
