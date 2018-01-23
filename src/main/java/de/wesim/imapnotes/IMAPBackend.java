package de.wesim.imapnotes;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMessage;

public class IMAPBackend {
	
	private Session session;
	private Store store;
	private IMAPFolder notesFolder;
	private final Properties imapSettings;
	
	private IMAPBackend() throws IOException {
		Properties props = System.getProperties();
		imapSettings = new Properties();
		imapSettings.load(Files.newBufferedReader(Paths.get(System.getProperty("user.home"), ".imapnotesfx")));
		this.session = Session.getInstance(props, null);
	}

	public Session getSession() {
	
		return this.session;
	}
	
	private void openNotesFolder(String name) throws MessagingException {
		this.notesFolder = (IMAPFolder) this.store.getFolder(name);
	}
	
	private void openSubFolder(String name) throws MessagingException {
		this.notesFolder = (IMAPFolder) this.notesFolder.getFolder(name);
	}

	private void connectStore() throws MessagingException {
		this.store.connect(imapSettings.getProperty("hostname"), 
					-1, imapSettings.getProperty("login"), 
				imapSettings.getProperty("pw"));
	}
	
	public static IMAPBackend initNotesFolder(String name) throws MessagingException, IOException {
		final IMAPBackend newInstance = new IMAPBackend();
		if (newInstance.store == null) {
			newInstance.store = newInstance.getSession().getStore("imap");
			newInstance.connectStore();
		}
		final String[] splitItems = name.split("/");
		newInstance.openNotesFolder(splitItems[0]);
		
		// TODO Fehler abfangen ...
		if (splitItems.length == 1) return newInstance;
		for (int i=1; i<splitItems.length; i++) {
			newInstance.openSubFolder(splitItems[i]);
		}
		return newInstance;
	}

	public IMAPFolder getNotesFolder () {
		return (IMAPFolder) this.notesFolder;
	}

	public void destroy() throws MessagingException {
		this.cleanup();
		this.store.close();
	}

	public List<Message> getMessages() throws MessagingException {
		this.startTransaction();

		//int totalMessages =  this.notesFolder.getMessageCount();
		Message[] msgs =  this.notesFolder.getMessages();
		// Use a suitable FetchProfile
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		fp.add(FetchProfile.Item.FLAGS);
		fp.add("X-Uniform-Type-Identifier");
		fp.add("X-Mail-Created-Date");
		fp.add("X-Universally-Unique-Identifier");
		this.notesFolder.fetch(msgs, fp);
		List<Message> messages = new ArrayList<>(Arrays.asList(msgs));
		Collections.sort(messages, new Comparator<Message>() {
			@Override
			public int compare(Message o1, Message o2) {
				try {
					return o1.getReceivedDate().compareTo(o2.getReceivedDate());
				} catch (MessagingException e) {
					// TODO passt das?
					return -1;
				}
			}
		});
		this.endTransaction();
		return messages;
	}


	public String getMessageContent(Message message) throws MessagingException, IOException {
		startTransaction();
		String content = (String) message.getContent();
		endTransaction();
		return content;
	}

	private void endTransaction() throws MessagingException {
		if (this.notesFolder.isOpen()) {
			this.notesFolder.close(false);
		}
	}

	private void startTransaction() throws MessagingException {
		if (!this.notesFolder.isOpen()) {
			this.notesFolder.open(Folder.READ_WRITE);
		}
	}

	public Message updateMessageContent(Message currentMessage, String newContent) throws MessagingException {
		startTransaction();
		String subject = currentMessage.getSubject();
		MimeMessage newMsg = createNewMessageObject(new String(subject), newContent, false);

		Enumeration<Header> enums = currentMessage.getAllHeaders();
		while (enums.hasMoreElements()) {
			Header next = (Header) enums.nextElement();
			//			X-Mail-Created-Date
			//			X-Universally-Unique-Identifier
			String name = next.getName();
			if (!name.equals("X-Mail-Created-Date") 
					&& !name.equals("X-Universally-Unique-Identifier")) {
				continue;
			}
			newMsg.addHeader(name, next.getValue());
			System.out.println(name + ";" + next.getValue());
		}
		// Flag setzen bevor(!) angehängt wird
		newMsg.setFlag(Flag.SEEN, true);
		Message[] newIMAPMessage = new Message[]{newMsg};
		//newMsg.setFlag(Flag.SEEN, true);
		final Message[] resultMessage = this.notesFolder.addMessages(newIMAPMessage);
		deleteMessageObject(currentMessage);
		endTransaction();
		//markSeen(currentMessage);
//		this.deleteMessageObject(currentMessage);
		return resultMessage[0];
	}

	public void deleteMessage(Message message)  throws MessagingException {
		startTransaction();
		deleteMessageObject(message);
		endTransaction();
	}
	
	private void deleteMessageObject(Message message) throws MessagingException {
		message.setFlag(Flag.DELETED, true);
	}

	public Message createNewMessage(String subject, String newContent) throws MessagingException {
		startTransaction();
		MimeMessage newMsg = createNewMessageObject(subject, newContent, true);
		newMsg.setFlag(Flag.SEEN, true);
		Message[] newIMAPMessage = new Message[]{newMsg};
		final Message[] resultMessage = this.notesFolder.addMessages(newIMAPMessage);
		endTransaction();
		return resultMessage[0];
	}

	private MimeMessage createNewMessageObject(String subject, String newContent, boolean newUUid) throws MessagingException {
		MimeMessage newMsg = new MimeMessage(this.session);
		newMsg.setContent(newContent, "text/html; charset=utf-8");
		newMsg.setSubject(subject);
		newMsg.setFrom(imapSettings.getProperty("from_address"));
		Date date = new Date();
		newMsg.setSentDate(date);
		// think of something here ...
		//		newMsg.addHeader("X-Mail-Created-Date", "Sun, 31 Jan 2016 21:17:36 +0100");
		newMsg.addHeader("X-Uniform-Type-Identifier", "com.apple.mail-note");
		if (newUUid) {
			UUID uuid = UUID.randomUUID();
			newMsg.addHeader("X-Universally-Unique-Identifier", uuid.toString());
		}
		return newMsg;
	}

	public long getUidForMessage(Message msg) throws MessagingException {
		this.startTransaction();
	    final long uid =  ((UIDFolder) this.getNotesFolder()).getUID(msg);		
	    this.endTransaction();
	    return uid;
	}
	
	public void cleanup() throws MessagingException {
		this.startTransaction();
	    this.getNotesFolder().expunge();	
	    this.endTransaction();
	    //return uid;
	}

	public Message getMessageByUID(long uid) throws MessagingException {
		this.startTransaction();
        final Message msg = this.getNotesFolder().getMessageByUID(uid);	
        this.endTransaction();
        return msg;
	}

	public void dumpMessage(Message msg) throws MessagingException, IOException {
		this.startTransaction();
		final Enumeration<Header> enums = msg.getAllHeaders();
		System.out.println("Headers:");
        while (enums.hasMoreElements()) {
            Header next = (Header) enums.nextElement();
//			X-Mail-Created-Date
//			X-Universally-Unique-Identifier
            String name = next.getName();

            System.out.println(name + ";" + next.getValue());
        }
		System.out.println("Content:");
		System.out.println(this.getMessageContent(msg));
        this.endTransaction();
   }
	
	
}
