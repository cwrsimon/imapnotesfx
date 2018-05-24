package de.wesim.imapnotes.services;

import java.util.List;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;

public interface INoteProvider {

	public void init(Account account) throws Exception;
	
	// TODO Create dedicated exception type !!!
    public Note createNewNote(String subject, Note parentFolder) throws Exception;

    public Note createNewFolder(String name, Note parentFolder) throws Exception;

    public void renameNote(Note note, String newName) throws Exception;
	public void renameFolder(Note note, String newName) throws Exception;

    @Deprecated
    public void openFolder(Note folder) throws Exception;

    // @Deprecated
	// public void returnToParent() throws Exception;

    public void load(Note note) throws Exception;

    public void update(Note note) throws Exception;

    public void delete(Note note) throws Exception;

    public List<Note> getNotes() throws Exception;

    public List<Note> getNotesFromFolder(Note folder) throws Exception;

    public void destroy() throws Exception;

    public boolean move(Note msg, Note folder);
    // TODO
    // public void rename(Note note, String newTitle); 
}