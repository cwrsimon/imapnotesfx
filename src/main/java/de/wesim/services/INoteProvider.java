package de.wesim.services;

import de.wesim.models.Note;
import java.util.List;

public interface INoteProvider {

	// TODO Create dedicated exception type !!!
    public Note createNewNote(String subject) throws Exception;

    public void load(Note note) throws Exception;

    public void update(Note note) throws Exception;

    public void delete(Note note) throws Exception;

    public List<Note> getNotes() throws Exception;

    public void destroy() throws Exception;

    // TODO
    // public void rename(Note note, String newTitle); 
}