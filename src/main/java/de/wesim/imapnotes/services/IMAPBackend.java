package de.wesim.imapnotes.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.imap.IMAPFolder;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;

public class IMAPBackend {
	
	private static final Logger logger = LoggerFactory.getLogger(IMAPBackend.class);
	
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
	
	// TODO Absoluten Pfad angeben
	public Note createFolder(String name, Map<String, Folder> folderMap) throws MessagingException {
		Folder newFolder = this.notesFolder.getFolder(name);
		newFolder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS | Folder.READ_WRITE);
		final Note newNote = new Note(name);
		newNote.setSubject(name);
		newNote.setDate(new Date());
		folderMap.put(name, newFolder);
		newNote.setIsFolder(true);
		return newNote;
	}
	
	// TODO Absoluten Pfad angeben
	public boolean deleteFolder(String name) throws MessagingException {
		Folder newFolder = this.notesFolder.getFolder(name);
		return newFolder.delete(false);
	}

	// TODO
	// public Folder renameFolder(String oldName, String newName) throws MessagingException {
	// 	endTransaction();
	// 	Folder oldFolder = this.notesFolder.getFolder(oldName);

	// 	Folder newFolder = this.notesFolder.getFolder(newName);
	// 	try {
	// 	boolean retValue = oldFolder.renameTo(newFolder);
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 	}
	// 	return newFolder;
	// }

	
	private void setFromAddress(String fromAddress) {
		this.from_address = fromAddress;
	}
	
	private void openNotesFolder(String name) throws MessagingException {
		this.notesFolder = (IMAPFolder) this.store.getFolder(name);
	}
	
	private void openSubFolder(String name) throws MessagingException {
		this.notesFolder = (IMAPFolder) this.notesFolder.getFolder(name);
	}
	
	// public void switchToSubFolder(String name) throws MessagingException {
	// 	openSubFolder(name);
	// 	this.folderStack.push(name);
	// }

	// public void switchToParentFolder() throws MessagingException {
	// 	this.notesFolder = (IMAPFolder) this.notesFolder.getParent();
	// 	this.folderStack.pop();
	// }
	
	private void connectStore(String hostname, String login, String pw) throws MessagingException {
		logger.info("Trying to connect: {}, {}, {}", hostname, login, pw);
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

	public List<Note> getMessages(Folder folder, Map<String, Message> msgMap, Map<String, Folder> folderMap) throws MessagingException {
		this.startTransaction((IMAPFolder) folder);

		final Message[] msgs =  folder.getMessages();
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		fp.add(FetchProfile.Item.FLAGS);
		fp.add("X-Uniform-Type-Identifier");
		fp.add("X-Mail-Created-Date");
		fp.add("X-Universally-Unique-Identifier");
		folder.fetch(msgs, fp);

		List<Note> messages = new ArrayList<>();
		for (Message m : msgs) {
			if (m.isSet(Flag.DELETED)) {
				continue;
			}
			final String uuid = this.getUUIDForMessage(m);
			final Note newNote = new Note(uuid);
			newNote.setSubject(m.getSubject());
			newNote.setIsFolder(false);
			msgMap.put(uuid, m);
			newNote.setDate(m.getReceivedDate());
			messages.add(newNote);
		}
		Folder[] folders = folder.list();
		for (Folder f : folders) {
			logger.info("Folder full name: {}", f.getFullName());
			final String name = f.getName();
			final Note newNote = new Note(f.getFullName());
			newNote.setSubject(name);
			folderMap.put(f.getFullName(), f);
			newNote.setIsFolder(true);
			newNote.setDate(new Date());
			messages.add(newNote);
		}
		
		// if (!this.folderStack.isEmpty()) {
		// 	final String prevFolder = this.folderStack.peek();
		// 	if (prevFolder != null) {
		// 		final String uuid = "BACKTOPARENT" + String.valueOf(this.folderStack.size());
		// 		final Note newNote = new Note(uuid);
		// 		newNote.setIsFolder(true);
		// 		folderMap.put(uuid, null);
		// 		newNote.setSubject("Zurück");
		// 		newNote.setDate(new Date());
		// 		messages.add(newNote);
		// 	}	
		// }
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
		this.endTransaction(((IMAPFolder) folder));
		return messages;
	}


	public String getMessageContent(Message message) throws MessagingException, IOException {
		IMAPFolder parentFolder = (IMAPFolder) message.getFolder();
		startTransaction(parentFolder);
		final String content = (String) message.getContent();
		endTransaction(parentFolder);
		return content;
	}

	private void endTransaction(IMAPFolder folder) throws MessagingException {
		if (folder.isOpen()) {
			folder.close(false);
		}
	}

	private void startTransaction(IMAPFolder folder) throws MessagingException {
		if (!folder.isOpen()) {
			folder.open(Folder.READ_WRITE);
		}
	}

	public Message updateMessageContent(Message currentMessage, String newContent) throws MessagingException {
		final IMAPFolder myFolder = (IMAPFolder)currentMessage.getFolder();
		startTransaction(myFolder);
		final String subject = currentMessage.getSubject();
	
		final MimeMessage newMsg = createNewMessageObject(new String(subject), newContent, false);

		Enumeration<Header> enums = currentMessage.getAllHeaders();
		while (enums.hasMoreElements()) {
			Header next = (Header) enums.nextElement();
			//			X-Mail-Created-Date
			//			X-Universally-Unique-Identifier
			final String name = next.getName();
			if (!name.equals("X-Mail-Created-Date") 
					&& !name.equals("X-Universally-Unique-Identifier")) {
				continue;
			}
			newMsg.addHeader(name, next.getValue());
			//System.out.println(name + ";" + next.getValue());
		}
		// Flag setzen bevor(!) angehängt wird
		newMsg.setFlag(Flag.SEEN, true);
		final Message[] newIMAPMessage = new Message[]{newMsg};
		final Message[] resultMessage = myFolder.addMessages(newIMAPMessage);
		deleteMessageObject(currentMessage);
		endTransaction(myFolder);
		return resultMessage[0];
	}

	public void deleteMessage(Message message)  throws MessagingException {
		final IMAPFolder myFolder = (IMAPFolder) message.getFolder();
		startTransaction(myFolder);
		deleteMessageObject(message);
		endTransaction(myFolder);
	}
	
	private void deleteMessageObject(Message message) throws MessagingException {
		message.setFlag(Flag.DELETED, true);
	}

	public Message createNewMessage(String subject, String newContent) throws MessagingException {
	//	startTransaction();
		final MimeMessage newMsg = createNewMessageObject(subject, newContent, true);
		newMsg.setFlag(Flag.SEEN, true);
		final Message[] newIMAPMessage = new Message[]{newMsg};
		final Message[] resultMessage = this.notesFolder.addMessages(newIMAPMessage);
	//	endTransaction();
		return resultMessage[0];
	}

	private MimeMessage createNewMessageObject(String subject, String newContent, boolean newUUid) throws MessagingException {
		final MimeMessage newMsg = new MimeMessage(this.session);
		newMsg.setContent(newContent, "text/html; charset=utf-8");
		newMsg.setSubject(subject);
		newMsg.setFrom(this.from_address);
		final Date date = new Date();
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
		//this.startTransaction();
	    final String uuid =  msg.getHeader("X-Universally-Unique-Identifier")[0];	
	   // this.endTransaction();
	    return uuid;
	}
	
	
	public void cleanup() throws MessagingException {
		this.startTransaction(this.notesFolder);
		this.notesFolder.expunge();	
	    this.endTransaction(this.notesFolder);
	}

//	public void dumpMessage(Message msg) throws MessagingException, IOException {
//		this.startTransaction();
//		final Enumeration<Header> enums = msg.getAllHeaders();
//		System.out.println("Headers:");
//        while (enums.hasMoreElements()) {
//            Header next = (Header) enums.nextElement();
////			X-Mail-Created-Date
////			X-Universally-Unique-Identifier
//            String name = next.getName();
//
//            System.out.println(name + ";" + next.getValue());
//        }
//		System.out.println("Content:");
//		System.out.println(this.getMessageContent(msg));
//        this.endTransaction();
//   }
	
	public boolean moveMessage(Message msg, Folder folder) {
		try {
			//this.startTransaction();

			getNotesFolder().copyMessages(new Message[]{msg}, folder);
			//this.endTransaction();

			return true;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
