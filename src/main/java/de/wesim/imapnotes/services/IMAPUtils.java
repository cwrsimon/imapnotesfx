package de.wesim.imapnotes.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.util.BASE64DecoderStream;

import de.wesim.imapnotes.HasLogger;

/* Some compatibility utils for handling Apple's 
 * special cases, e.g. notes with embedded TIFF images ...
 */
public class IMAPUtils implements HasLogger {

	private static Pattern p = Pattern.compile( "<object type=\\\".*?\\\" data=\\\"(.*?)\\\"></object>" );

	
	protected byte[] convertTIFF2Jpeg(byte[] tiffImage) throws IOException {
		try (InputStream tiffIS = new ByteArrayInputStream(tiffImage);
				ByteArrayOutputStream pngOS = new ByteArrayOutputStream()	
				) {
			final BufferedImage tiff = ImageIO.read(tiffIS);
			ImageIO.write(tiff, "png", pngOS);
			return pngOS.toByteArray();
		}
	}
	
	// needs more testing with Apple's original Notes application
	// necessary for handling mails with embedded images 
	public String decodeMultipartMails(Message message) throws MessagingException, IOException {
		
		getLogger().debug("Message class: {}", message.getClass().getName());
		getLogger().debug("Content type: {}", message.getContentType());
		String mainContent = "";
		
		final Map<String, String> cidContentMap = new HashMap<>();
		final MimeMultipart multiPart = (MimeMultipart) message.getContent();
		for (int i=0; i<multiPart.getCount();i++) {
			final BodyPart bp = multiPart.getBodyPart(i);
			final String[] cids = bp.getHeader("Content-Id");
			getLogger().debug("Index: {}, Content-Type: {}, Filename: {}, Content-Id: {}", i, 
				bp.getContentType() , bp.getFileName(), cids);
			
			final Object partContent = bp.getContent();
			if (partContent instanceof String) {
				mainContent = (String) partContent;
			} else if (partContent instanceof com.sun.mail.util.BASE64DecoderStream) {

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
					byte[] convertedContent = convertTIFF2Jpeg(originalContent);
					final String pngBase64 = Base64.getEncoder().encodeToString(convertedContent);
					final String base64Content = "<img src=\"data:image/png;base64," + pngBase64 + "\"/>";
					cidContentMap.put(cid, base64Content);
				}
			}
		}
		final Matcher m = p.matcher(mainContent);
		while (m.find()) {
			final String matchedCID = m.group(1);
			final String newContent = cidContentMap.get(matchedCID);
			mainContent = m.replaceFirst(newContent);
			m.reset(mainContent);
		}
		return mainContent;
	}
	
	private static void getChildren(Folder folder, String prefix, List<String> accu) throws MessagingException {
		if (prefix.length() > 1) {
			accu.add(prefix.substring(0, prefix.length() - 1));
		} else {
			accu.add(prefix);
		}
		for (Folder child : folder.list()) {
			final String myprefix = prefix + child.getName() + "/";
			getChildren(child, myprefix, accu);
		}
	}
	
	public static List<String> getIMAPFoldersList(Store store) throws MessagingException {
		List<String> accu = new ArrayList<>();
		getChildren(store.getDefaultFolder(), "/", accu);
		return accu;
	}
}
