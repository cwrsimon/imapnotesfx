package de.wesim.imapnotes.services;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.Message;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;
import javax.mail.MessagingException;

// TODO Exceptions durchgehen ...
// Passwort abfragen ...
public class IMAPNoteProvider implements INoteProvider {

	private IMAPBackend backend;
	private final Map<String, Message> msgMap;
	private final Map<String, Folder> folderMap;

	
	public IMAPNoteProvider() {
		this.msgMap = new HashMap<>();
		this.folderMap = new HashMap<>();
	}
	

	@Override
	public void init(Account account) throws Exception {
            this.backend = new IMAPBackend(account);
            this.backend.initNotesFolder();
	}	
	
	@Override
	public Note createNewNote(String subject, Note parentFolder) throws Exception {
		// TODO wenn null, dann root folder benutzen
		final Folder f;
		if (parentFolder == null) {
			f = this.backend.getNotesFolder();
		} else {
			f = this.folderMap.get(parentFolder.getUuid());
		}
		final Message newIMAPMsg = this.backend.createNewMessage(subject, f);
		final Note newNote = new Note(this.backend.getUUIDForMessage(newIMAPMsg));
		newNote.setSubject(subject);
		newNote.setIsFolder(false);
		newNote.setDate(new Date());
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
			Folder folder = this.folderMap.get(note.getUuid());
			backend.deleteFolder(folder);
		} else {
			backend.deleteMessage( this.msgMap.get(note.getUuid()) );			
		}
	}

	// TODO Nur für die Wurzel ...
	@Override
	public List<Note> getNotes() throws Exception {
		this.msgMap.clear();
		this.folderMap.clear();
		final List<Note> notes = this.backend.getMessages(this.backend.getNotesFolder(), 
			this.msgMap, this.folderMap);
		// TODO
		Collections.sort(notes);
		return notes;
	}

	@Override
	public List<Note> getNotesFromFolder(Note folder) throws Exception {
		Folder f = this.folderMap.get(folder.getUuid());
		final List<Note> notes = this.backend.getMessages(f, 
			this.msgMap, this.folderMap);
		// TODO
		Collections.sort(notes);
		return notes;
	}


	@Override
	public void destroy() throws Exception {
		this.backend.destroy();
	}

	@Override
	public Note createNewFolder(String name, Note parent) throws Exception {
		final Folder parentFolder;
		if (parent != null) {
			parentFolder = this.folderMap.get(parent.getUuid());
		} else {
			parentFolder = this.backend.getNotesFolder();
		}
		final Note newFolder =  this.backend.createFolder(name, parentFolder, this.folderMap);
		return newFolder;
	}

	@Override
	public void renameNote(Note note, String newName) throws Exception {
		System.out.println("Calling " + note.getSubject() + " with new Name " + newName);
		// final Message oldMsg = this.msgMap.get(note.getUuid());
		// this.backend.changeSubject(oldMsg, newName);
		load(note);
		note.setSubject(newName);
		update(note);
	}

	@Override
	public void renameFolder(Note folder, String newName) throws Exception {
		// TODO Folders mit einer anderen UUID versehen ...
		String oldUUID = folder.getUuid();
		Folder oldFolder = this.folderMap.get(oldUUID);

		Folder newFolder = this.backend.renameFolder(oldFolder, newName);
		String newUUID = newFolder.getFullName();
		// note.setImapMessage();
		this.folderMap.put(newUUID, newFolder);
		this.folderMap.remove(oldUUID);
		folder.setSubject(newName);
		folder.setUuid(newUUID);
		getNotesFromFolder(folder);
	}


	@Override
	public Note move(Note message, Note folder) throws MessagingException {
		final Message msg = this.msgMap.get(message.getUuid());
		Folder imapFolder = this.folderMap.get(folder.getUuid());
		this.backend.moveMessage(msg, imapFolder);
		// update message
		try {
			getNotesFromFolder(folder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message;
	}	
	
}
