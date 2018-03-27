package de.wesim.imapnotes.services;

import javax.mail.Folder;
import javax.mail.Message;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IMAPNoteProvider implements INoteProvider {

	private IMAPBackend backend;
	private Map<String, Message> msgMap;
	private Map<String, Folder> folderMap;

	
	public IMAPNoteProvider() {
		this.msgMap = new HashMap<>();
		this.folderMap = new HashMap<>();

	}
	

	@Override
	public void init(Account account) throws Exception {
		PasswordProvider pp = new PasswordProvider();
		pp.init();
		final String accountName = account.getAccount_name();
		final String pw = pp.retrievePassword(accountName);
				
		this.backend = IMAPBackend.initNotesFolder(account, pw);
	}	
	
	@Override
	public Note createNewNote(String subject) throws Exception {
		final Message newIMAPMsg = this.backend.createNewMessage(subject, Consts.EMPTY_NOTE);
		final Note newNote = new Note(this.backend.getUUIDForMessage(newIMAPMsg));
		newNote.setSubject(subject);
		newNote.setIsFolder(false);
		this.msgMap.put(this.backend.getUUIDForMessage(newIMAPMsg), newIMAPMsg);
		return newNote;
	}

	@Override
	public void load(Note note) throws Exception  {
		if (note.getContent() == null) {
			Message msg = this.msgMap.get(note.getUuid());
			note.setContent( this.backend.getMessageContent(msg) );
		}
	}

	@Override
	public void update(Note note) throws Exception  {
		final String uuid = note.getUuid();
		final Message oldMsg = this.msgMap.get(uuid);
		final Message newMsg = backend.updateMessageContent(oldMsg, note.getContent());
		this.msgMap.put(uuid, newMsg);
	}

	@Override
	public void delete(Note note) throws Exception  {
		if (note.isFolder()) {
			// TODO verify me
			System.out.println(backend.deleteFolder(note.getUuid()));
		} else {
			backend.deleteMessage( this.msgMap.get(note.getUuid()) );			
		}
	}

	@Override
	public List<Note> getNotes() throws Exception {
		this.msgMap.clear();
		this.folderMap.clear();
		return backend.getMessages(this.msgMap, this.folderMap);
	}

	@Override
	public void destroy() throws Exception {
		this.backend.destroy();
	}

	@Override
	public void openFolder(Note folder) throws Exception {
		this.backend.switchToSubFolder(folder.getUuid());
	}

	@Override
	public void returnToParent() throws Exception {
		this.backend.switchToParentFolder();
	}

	@Override
	public Note createNewFolder(String name) throws Exception {
		return this.backend.createFolder(name, this.folderMap);
	}

	@Override
	public void renameNote(Note note, String newName) throws Exception {
		note.setSubject(newName);

		System.out.println("Calling " + note.getSubject() + " with new Name " + newName);
		update(note);
	}

	@Override
	public void renameFolder(Note note, String newName) throws Exception {
		System.out.println("Renaming IMAP FOlder ...");
		// TODO Folders mit einer anderen UUID versehen ...
		Folder newFolder = this.backend.renameFolder(note.getUuid(), newName);
		// note.setImapMessage();
		this.folderMap.put(note.getUuid(), newFolder);
		note.setSubject(newName);
		note.setUuid(newName);

	}



	
}
