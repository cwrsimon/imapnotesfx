package de.wesim.imapnotes.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;

import javax.mail.FetchProfile;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;

import com.sun.mail.imap.IMAPFolder;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;

public class IMAPBackend {
	
	private Session session;
	private Store store;
	private IMAPFolder notesFolder;
	private Stack<String> folderStack;
	private String from_address;

	
	private IMAPBackend() throws IOException {
		Properties props = System.getProperties();
		this.session = Session.getInstance(props, null);
		this.folderStack = new Stack<String>();

	}

	public Session getSession() {
		return this.session;
	}
	
	public Note createFolder(String name) throws MessagingException {
		Folder newFolder = this.notesFolder.getFolder(name);
		newFolder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS | Folder.READ_WRITE);
		final Note newNote = new Note(name);
		newNote.setSubject(name);
		newNote.setImapMessage(newFolder);
		newNote.setIsFolder(true);
		return newNote;
	}
	
	public boolean deleteFolder(String name) throws MessagingException {
		Folder newFolder = this.notesFolder.getFolder(name);
		return newFolder.delete(false);
	}

	public Folder renameFolder(String oldName, String newName) throws MessagingException {
		endTransaction();
		System.out.println("1");
		Folder oldFolder = this.notesFolder.getFolder(oldName);
		System.out.println("2");

		Folder newFolder = this.notesFolder.getFolder(newName);
		System.out.println("3");
		try {
		boolean retValue = oldFolder.renameTo(newFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Status Umbenunng:");
		return newFolder;
	}

	
	private void setFromAddress(String fromAddress) {
		this.from_address = fromAddress;
	}
	
	private void openNotesFolder(String name) throws MessagingException {
		this.notesFolder = (IMAPFolder) this.store.getFolder(name);
	}
	
	private void openSubFolder(String name) throws MessagingException {
		this.notesFolder = (IMAPFolder) this.notesFolder.getFolder(name);
	}
	
	public void switchToSubFolder(String name) throws MessagingException {
		openSubFolder(name);
		this.folderStack.push(name);
	}

	public void switchToParentFolder() throws MessagingException {
		this.notesFolder = (IMAPFolder) this.notesFolder.getParent();
		this.folderStack.pop();
	}
	
	private void connectStore(String hostname, String login, String pw) throws MessagingException {
		// TODO Logging
		System.out.println("Trying to connect:" + hostname + ";" + login + ";" + pw);
		this.store.connect(hostname, -1, login, pw);
	}
	
	public static IMAPBackend initNotesFolder(Account account, String pw) throws MessagingException, IOException {
		final IMAPBackend newInstance = new IMAPBackend();
		if (newInstance.store == null) {
			newInstance.store = newInstance.getSession().getStore("imap");
			newInstance.connectStore(account.getHostname(), account.getLogin(), pw);
		}
		newInstance.setFromAddress (account.getFrom_address() );
		final String[] splitItems = account.getRoot_folder().split("/");
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

	public List<Note> getMessages() throws MessagingException {
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

		List<Note> messages = new ArrayList<>();
		for (Message m : msgs) {
			System.out.println(m.getSubject());
			System.out.println(m.isSet(Flag.DELETED));
			if (m.isSet(Flag.DELETED)) {
				continue;
			}
			final String uuid = this.getUUIDForMessage(m);
			final Note newNote = new Note(uuid);
			newNote.setSubject(m.getSubject());
			newNote.setImapMessage(m);
			messages.add(newNote);
		}
		Folder[] folders = this.notesFolder.list();
		for (Folder f : folders) {
			final String name = f.getName();
			final Note newNote = new Note(name);
			newNote.setSubject(name);
			newNote.setImapMessage(f);
			newNote.setIsFolder(true);
			messages.add(newNote);
		}
		
		if (!this.folderStack.isEmpty()) {
			final String prevFolder = this.folderStack.peek();
			if (prevFolder != null) {
				final Note newNote = new Note("BACKTOPARENT" + String.valueOf(this.folderStack.size()));
				newNote.setIsFolder(true);
				newNote.setImapMessage(null);
				newNote.setSubject("Zurück");
				messages.add(newNote);
			}
			
		}
		// TODO Reintegrate me!
//		Collections.sort(messages, new Comparator<Message>() {
//			@Override
//			public int compare(Message o1, Message o2) {
//				try {
//					return o1.getReceivedDate().compareTo(o2.getReceivedDate());
//				} catch (MessagingException e) {
//					// TODO passt das?
//					return -1;
//				}
//			}
//		});
		this.endTransaction();
		return messages;
	}


	public String getMessageContent(Message message) throws MessagingException, IOException {
		startTransaction();
		final String content = (String) message.getContent();
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
		final String subject = currentMessage.getSubject();
	
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
		final MimeMessage newMsg = createNewMessageObject(subject, newContent, true);
		newMsg.setFlag(Flag.SEEN, true);
		final Message[] newIMAPMessage = new Message[]{newMsg};
		final Message[] resultMessage = this.notesFolder.addMessages(newIMAPMessage);
		endTransaction();
		return resultMessage[0];
	}

	private MimeMessage createNewMessageObject(String subject, String newContent, boolean newUUid) throws MessagingException {
		MimeMessage newMsg = new MimeMessage(this.session);
		newMsg.setContent(newContent, "text/html; charset=utf-8");
		newMsg.setSubject(subject);
		newMsg.setFrom(this.from_address);
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

	public String getUUIDForMessage(Message msg) throws MessagingException {
		this.startTransaction();
	    final String uuid =  msg.getHeader("X-Universally-Unique-Identifier")[0];	
	    this.endTransaction();
	    return uuid;
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
