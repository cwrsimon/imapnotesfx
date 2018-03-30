package de.wesim.imapnotes.services;

import java.util.List;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;

public interface INoteProvider {

	public void init(Account account) throws Exception;
	
	// TODO Create dedicated exception type !!!
    public Note createNewNote(String subject) throws Exception;

    public Note createNewFolder(String name) throws Exception;

    public void renameNote(Note note, String newName) throws Exception;
	void renameFolder(Note note, String newName) throws Exception;

    public void openFolder(Note folder) throws Exception;
	public void returnToParent() throws Exception;

    public void load(Note note) throws Exception;

    public void update(Note note) throws Exception;

    public void delete(Note note) throws Exception;

    public List<Note> getNotes() throws Exception;

    public void destroy() throws Exception;

    public boolean move(Note msg, Note folder);
    // TODO
    // public void rename(Note note, String newTitle); 
}