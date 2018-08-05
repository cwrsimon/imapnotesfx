package de.wesim.imapnotes.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.util.BASE64DecoderStream;

import de.wesim.imapnotes.HasLogger;

public class IMAPUtils implements HasLogger {

//	INFORMATION: Content: <html><head></head><body><div>Wunsch</div><div><br></div><object type="application/x-apple-msg-attachment" data="cid:36F2F33F-7B37-4FC6-A109-85B4E77F52BD@localdomain"></object></body></html>
//	Aug. 05, 2018 9:04:36 NACHM. de.wesim.imapnotes.services.IMAPUtils decodeMultipartMails
//	INFORMATION: Index: 1, Content-Type: image/tiff; x-unix-mode=0644; name=image.tiff, Filename: image.tiff, Content-Id: [<36F2F33F-7B37-4FC6-A109-85B4E77F52BD@localdomain>]

	private static Pattern p = Pattern.compile("<object type.*?></object>");

	
	public byte[] convertTIFF2Jpeg(byte[] tiffImage) {
		try (InputStream tiffIS = new ByteArrayInputStream(tiffImage);
				ByteArrayOutputStream pngOS = new ByteArrayOutputStream()	
				) {
			final BufferedImage tiff = ImageIO.read(tiffIS);
			ImageIO.write(tiff, "png", pngOS);
			return pngOS.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
		// TODO !!!!
	public String decodeMultipartMails(Message message) throws MessagingException, IOException {
		
		// TODO MimeMultipart hier unterstützen
		getLogger().info("Message class: {}", message.getClass().getName());
		getLogger().info("Content type: {}", message.getContentType());
		String mainContent = "";
		// TODO Später mal die Bilder auflösen ...
		String base64Content = "";
		Map<String, String> cidContentMap = new HashMap<>();
		final MimeMultipart multiPart = (MimeMultipart) message.getContent();
		for (int i=0; i<multiPart.getCount();i++) {
			BodyPart bp = multiPart.getBodyPart(i);
			String[] cids = bp.getHeader("Content-Id");
			getLogger().info("Index: {}, Content-Type: {}, Filename: {}, Content-Id: {}", i, 
				bp.getContentType() , bp.getFileName(), cids);
			Object partContent = bp.getContent();
			if (partContent instanceof String) {
				mainContent = (String) partContent;
			} else if (partContent instanceof com.sun.mail.util.BASE64DecoderStream) {
				final String cid = cids[0];
				BASE64DecoderStream decoderStream = (com.sun.mail.util.BASE64DecoderStream) partContent;
				byte[] originalContent = decoderStream.readAllBytes();
				// TODO TIFF konvertieren
				String pngBase64 = "";
				//getLogger().info("{}", partContent.getClass().getName());
				// TODO CIDs auflösen
				// TODO Konvertieren nach PNG
//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
//				bp.getDataHandler().writeTo(bos);
//				bos.close();
//				byte[] bytes = bos.toByteArray();
//				String read = new String(Files.readAllBytes(Paths.get("/Users/christian/some-content.base64")));
				//Files.write(Paths.get("/Users/christian/some-content"), bytes);
				// src="data:image/png;base64,iVBOR…Fy/NYpbmRyKWAAAAAElFTkSuQmCC"
				// base64Content = "<img src=\"data:image/jpeg;base64," 
				// 	+ Base64.getEncoder().encodeToString(bytes) + "\"/>";
				base64Content = "<img src=\"data:image/png;base64," + pngBase64 + "\"/>";
				cidContentMap.put(cid, base64Content);
			}

			getLogger().info("Content: {}", partContent);
		}
		getLogger().info("ReturnMe:{}", mainContent);

		Matcher m = p.matcher(mainContent);
		if (m.find()) {
			getLogger().info("Found!");
			// TODO CIDs integrieren
			mainContent = m.replaceAll(base64Content);
		}
		return mainContent;
		// <img src="data:image/jpg;base64,/*base64-data-string here*/" />
		//Files.write(Paths.get("/Users/christian/bla.html"), returnMe.getBytes());
		//logger.info("ReturnMe:{}", returnMe);
	}
	
	
}
