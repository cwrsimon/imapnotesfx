package de.wesim.imapnotes.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
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

	private static Pattern p = Pattern.compile( "<object type=\\\".*?\\\" data=\\\"(.*?)\\\"></object>" );

	
	public byte[] convertTIFF2Jpeg(byte[] tiffImage) throws IOException {
		try (InputStream tiffIS = new ByteArrayInputStream(tiffImage);
				ByteArrayOutputStream pngOS = new ByteArrayOutputStream()	
				) {
			final BufferedImage tiff = ImageIO.read(tiffIS);
			ImageIO.write(tiff, "png", pngOS);
			return pngOS.toByteArray();
		}
		
	}
	
	// TODO !!!!
	public String decodeMultipartMails(Message message) throws MessagingException, IOException {
		
		// TODO MimeMultipart hier unterstützen
		getLogger().info("Message class: {}", message.getClass().getName());
		getLogger().info("Content type: {}", message.getContentType());
		String mainContent = "";
		// TODO Später mal die Bilder auflösen ...
		final Map<String, String> cidContentMap = new HashMap<>();
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
				// TODO ABsichern
				final String cid = "cid:" + cids[0].replace("<", "").replace(">", "");
				try (BASE64DecoderStream decoderStream = (com.sun.mail.util.BASE64DecoderStream) partContent;
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();)	{
					
					int nRead;
					byte[] data = new byte[16384];
					while ((nRead = decoderStream.read(data, 0, data.length)) != -1) {
					  buffer.write(data, 0, nRead);
					}
					buffer.flush();
					byte[] originalContent = buffer.toByteArray();
					byte[] convertedCrap = convertTIFF2Jpeg(originalContent);
					final String pngBase64 = Base64.getEncoder().encodeToString(convertedCrap);
					final String base64Content = "<img src=\"data:image/png;base64," + pngBase64 + "\"/>";
					cidContentMap.put(cid, base64Content);
				}
			}
		}
		getLogger().info("ReturnMe:{}", mainContent);
		getLogger().info("{}", cidContentMap);
		Matcher m = p.matcher(mainContent);
		while (m.find()) {
			getLogger().info("Found!");
			final String matchedCID = m.group(1);
			// TODO Absichern
			final String newContent = cidContentMap.get(matchedCID);
			mainContent = m.replaceFirst(newContent);
			m.reset(mainContent);
		}
		return mainContent;
	}
	
	
}
