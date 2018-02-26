package de.wesim.services;

import de.wesim.models.Note;
import de.wesim.models.NoteFolder;
import java.util.List;

public interface INoteProvider {

	public static final String EMPTY_NOTE = 
				"<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>";	

	// TODO Create dedicated exception type !!!
    public Note createNewNote(String subject) throws Exception;

    public NoteFolder createNewFolder(String name) throws Exception;

    public void renameNote(Note note, String newName) throws Exception;

    public void openFolder(NoteFolder folder) throws Exception;
	public void returnToParent() throws Exception;

    public void load(Note note) throws Exception;

    public void update(Note note) throws Exception;

    public void delete(Note note) throws Exception;

    public List<Note> getNotes() throws Exception;

    public void destroy() throws Exception;

    // TODO
    // public void rename(Note note, String newTitle); 
}