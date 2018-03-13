package de.wesim.imapnotes.services;

import javax.mail.Message;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;

import java.util.List;


public class IMAPNoteProvider implements INoteProvider {

	private IMAPBackend backend;

	public IMAPNoteProvider() {
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
		newNote.setImapMessage(newIMAPMsg);
		return newNote;
	}

	@Override
	public void load(Note note) throws Exception  {
		if (note.getContent() == null) {
			note.setContent( this.backend.getMessageContent(note.getImapMessage()) );
		}
	}

	@Override
	public void update(Note note) throws Exception  {
		note.setImapMessage( backend.updateMessageContent(note.getImapMessage(), note.getContent()) );
	}

	@Override
	public void delete(Note note) throws Exception  {
		backend.deleteMessage( note.getImapMessage() );
	}

	@Override
	public List<Note> getNotes() throws Exception {
		return backend.getMessages();
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
		return this.backend.createFolder(name);
	}

	@Override
	public void renameNote(Note note, String newName) throws Exception {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public void renameFolder(Note note, String newName) throws Exception {
		// TODO Auto-generated method stub
		// https://stackoverflow.com/questions/30626233/java-mail-imap-renaming-a-folder-which-has-children
	}



	
}
