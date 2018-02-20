package de.wesim.services;

import javax.mail.Message;
import de.wesim.imapnotes.IMAPBackend;
import de.wesim.models.Note;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


public class FSNoteProvider implements INoteProvider {


		
	// Umstellung auf ein TOC

	private Path userHome;
	private Path noteDirectory;

	public FSNoteProvider() throws Exception  {
//		this.backend = IMAPBackend.initNotesFolder("Notes/Playground");
		this.userHome = Paths.get(System.getProperty("user.home"));
		this.noteDirectory = this.userHome.resolve("CurrentProjects").resolve("notes");
	}
	
	@Override
	public Note createNewNote(String subject) throws Exception {
		final UUID uuid = UUID.randomUUID();
		final Path newFile = this.noteDirectory.resolve(uuid.toString() + ".imapnote");
		// TODO Subject ...
		final Note newNote = new Note(uuid.toString());
		newNote.setSubject(subject);
		newNote.setImapMessage(newFile);
		newNote.setContent("");
		update(newNote);
		return newNote;
	}

	@Override
	public void load(Note note) throws Exception  {
		if (note.getContent() == null) {
			final Path path = (Path) note.getRawImapMessage();
			note.setContent( new String( Files.readAllBytes(path) ) );
		}
	}

	@Override
	public void update(Note note) throws Exception  {
		final Path path = (Path) note.getRawImapMessage();
		final String content = note.getContent();
		Files.write(path, content.getBytes("UTF-8"));
	}

	@Override
	public void delete(Note note) throws Exception  {
		Path path = (Path) note.getRawImapMessage();
		// TODO try, etc. 
		Files.delete(path);
	}

	@Override
	public List<Note> getNotes() throws Exception {
		List<Note> notes = new ArrayList<>();
		try( Stream<Path> fileStream = Files.list(this.noteDirectory)) {
			
			fileStream.forEach( filePath -> {
				String uuid = filePath.getFileName().toString();
				final Note newNote = new Note(uuid);
				newNote.setSubject(uuid);
				newNote.setImapMessage(filePath);
				notes.add(newNote);
			});
			
		}
		return notes;
	}
 
	@Override
	public void destroy() throws Exception {
		;
	}
	
}
