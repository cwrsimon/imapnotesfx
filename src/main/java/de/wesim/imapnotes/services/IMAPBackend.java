package de.wesim.imapnotes.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import com.sun.mail.imap.IMAPFolder;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;

public class IMAPBackend implements HasLogger {

    private Session session;
    private Store store;
    private IMAPFolder notesFolder;
    private String from_address;
    private final Account account;

    public IMAPBackend(Account account) {
        this.account = account;
    }

    public Note createFolder(String name, Folder parentFolder, Map<String, Folder> folderMap) throws MessagingException {
        startTransaction((IMAPFolder) parentFolder);
        Folder newFolder = parentFolder.getFolder(name);
        newFolder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS | Folder.READ_WRITE);
        final Note newNote = new Note(newFolder.getFullName());
        newNote.setSubject(name);
        newNote.setDate(new Date());
        newNote.setIsFolder(true);

        folderMap.put(newNote.getUuid(), newFolder);

        endTransaction((IMAPFolder) parentFolder);

        return newNote;
    }

    public boolean deleteFolder(Folder folder) throws MessagingException {
        this.endTransaction((IMAPFolder) folder);
        //Folder newFolder = this.notesFolder.getFolder(folder);
        return folder.delete(true);
    }

    public Folder renameFolder(Folder oldFolder, String newName) throws MessagingException {
    	// make sure the folder is closed
        this.endTransaction((IMAPFolder) oldFolder);
        final Folder parentFolder = oldFolder.getParent();
        Folder newFolder = parentFolder.getFolder(newName);
        oldFolder.renameTo(newFolder);        
        return newFolder;
    }

    private void openNotesFolder(String name) throws MessagingException {
        this.notesFolder = (IMAPFolder) this.store.getFolder(name);
    }

    private void openSubFolder(String name) throws MessagingException {
        this.notesFolder = (IMAPFolder) this.notesFolder.getFolder(name);
    }

    private void connectStore(Properties props, Authenticator authenticator) throws MessagingException {
        getLogger().info("Trying to connect ...");
        // TODO Integrate me one day ...
        // props.setProperty("mail.imap.ssl.enable", "true");

        this.session = Session.getInstance(props, authenticator);
        this.store = this.session.getStore(
                new URLName("imap://" + account.getHostname())
//                + ":587"
        );
        this.store.connect();
    }

    public void initNotesFolder() throws Exception {
        final Properties props = System.getProperties();
        final MyAuthenticator myAuthenticator = new MyAuthenticator(account);

        try {
            connectStore(props, myAuthenticator);
        } catch (javax.mail.AuthenticationFailedException e) {
            myAuthenticator.setTryAgain(true);
            connectStore(props, myAuthenticator);
        }
        this.from_address = account.getFrom_address();
        final String[] splitItems = this.account.getRoot_folder().split("/");
        if (splitItems.length == 0) {
            throw new Exception(String.format("Invalid root folder: %s", this.account.getRoot_folder()));
        }

        openNotesFolder(splitItems[0]);

        for (int i = 1; i < splitItems.length; i++) {
            openSubFolder(splitItems[i]);
        }
    }

    public IMAPFolder getNotesFolder() {
        return (IMAPFolder) this.notesFolder;
    }

    public void destroy() throws MessagingException {
        this.cleanup();
        this.store.close();
    }

    public List<Note> getMessages(Folder folder, Map<String, Message> msgMap, Map<String, Folder> folderMap) throws MessagingException {
        this.startTransaction((IMAPFolder) folder);

        final Message[] msgs = folder.getMessages();
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
            if (uuid == null) {
                getLogger().error("Message without uuid field! Skipping.");
                continue;
            }
            final Note newNote = new Note(uuid);
            newNote.setSubject(m.getSubject());
            newNote.setIsFolder(false);
            msgMap.put(uuid, m);
            newNote.setDate(m.getReceivedDate());
            messages.add(newNote);
        }
        Folder[] folders = folder.list();
        for (Folder f : folders) {

            getLogger().info("Folder full name: {}", f.getFullName());
            final String name = f.getName();
            final Note newNote = new Note(f.getFullName());
            newNote.setSubject(name);
            folderMap.put(f.getFullName(), f);
            newNote.setIsFolder(true);
            newNote.setDate(new Date());
            messages.add(newNote);
        }

        this.endTransaction(((IMAPFolder) folder));
        return messages;
    }

    public String getMessageContent(Message message) throws MessagingException, IOException {
        IMAPFolder parentFolder = (IMAPFolder) message.getFolder();
        startTransaction(parentFolder);

        final Object content = message.getContent();
        final String returnContent;
        if (content instanceof String) {
            returnContent = (String) content;
        } else {
            final IMAPUtils utils = new IMAPUtils();
            returnContent = utils.decodeMultipartMails(message);
        }

        endTransaction(parentFolder);
        return returnContent;
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

    public Message updateMessageContent(Message currentMessage, String newContent, String newSubject) throws MessagingException {
        final IMAPFolder myFolder = (IMAPFolder) currentMessage.getFolder();
        startTransaction(myFolder);

        final MimeMessage newMsg = createNewMessageObject(newSubject, newContent, false);

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
        }
        // Flag setzen bevor(!) angehängt wird
        newMsg.setFlag(Flag.SEEN, true);
        final Message[] newIMAPMessage = new Message[]{newMsg};
        final Message[] resultMessage = myFolder.addMessages(newIMAPMessage);
        deleteMessageObject(currentMessage);
        endTransaction(myFolder);
        return resultMessage[0];
    }

    public void deleteMessage(Message message) throws MessagingException {
        final IMAPFolder myFolder = (IMAPFolder) message.getFolder();
        startTransaction(myFolder);
        deleteMessageObject(message);
        endTransaction(myFolder);
    }

    private void deleteMessageObject(Message message) throws MessagingException {
        message.setFlag(Flag.DELETED, true);
    }

    public Message createNewMessage(String subject, Folder parentFolder) throws MessagingException {
        IMAPFolder folder = (IMAPFolder) parentFolder;
        startTransaction(folder);
        final MimeMessage newMsg = createNewMessageObject(subject, "", true);
        newMsg.setFlag(Flag.SEEN, true);
        final Message[] newIMAPMessage = new Message[]{newMsg};
        final Message[] resultMessage = folder.addMessages(newIMAPMessage);
        endTransaction(folder);
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
        this.startTransaction((IMAPFolder) msg.getFolder());
        final String[] header = msg.getHeader("X-Universally-Unique-Identifier");
        if (header == null) return null;
        if (header.length == 0) return null;
        final String uuid = header[0];
        this.endTransaction((IMAPFolder) msg.getFolder());
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
    public void moveMessage(Message msg, Folder folder) throws MessagingException {
        final IMAPFolder sourceFolder = (IMAPFolder) msg.getFolder();
        this.startTransaction(sourceFolder);
        sourceFolder.copyMessages(new Message[]{msg}, folder);
        this.endTransaction(sourceFolder);
    }

    public void changeSubject(Message msg, String newName) throws MessagingException {
        final IMAPFolder sourceFolder = (IMAPFolder) msg.getFolder();
        this.startTransaction(sourceFolder);

        msg.setSubject(newName);
        //msg.saveChanges();

        this.endTransaction(sourceFolder);

    }

}
