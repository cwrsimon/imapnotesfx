package de.wesim.imapnotes.services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;


public class FSNoteProvider implements INoteProvider, HasLogger {

	private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

	private static final String DEFAULT_FILE_ENDING = ".json";

	private Path rootDirectory;
	
	// TODO implement me!
	private Map<String, Path> uuid2Path = new HashMap<>();

	public FSNoteProvider() {
	}

	@Override
	public Note createNewNote(String subject, Note parentFolder) throws Exception {
		final UUID uuid = UUID.randomUUID();
		final Path parentPath;
		if (parentFolder != null) {
			parentPath = Paths.get(parentFolder.getUuid()); 
		} else {
			parentPath = rootDirectory;
		}
		final Path newFile = parentPath.resolve(uuid.toString() + DEFAULT_FILE_ENDING);
		final Note newNote = new Note(newFile.toAbsolutePath().toString());
		newNote.setSubject(subject);
		newNote.setContent(Consts.EMPTY_NOTE);
		update(newNote);
		return newNote;
	}

	@Override
	public void load(Note note) throws Exception {
	}

	@Override
	public void update(Note note) throws Exception {
		final Path path = Paths.get(note.getUuid());
		final Gson gson = new Gson();
		final String json = gson.toJson(note);
		Files.write(path, json.getBytes(DEFAULT_ENCODING));
	}

	@Override
	public void delete(Note note) throws Exception {
		final Path path = uuid2Path.get(note.getUuid());
		Files.delete(path);
		uuid2Path.remove(note.getUuid());
	}

	@Override
	public List<Note> getNotes() throws Exception {
		return getNotesFromFolder(new Note(rootDirectory.toAbsolutePath().toString()));
	}

	@Override
	public void destroy() throws Exception {
		;
	}

	@Override
	public Note createNewFolder(String name, Note parent) throws Exception {
		// TODO FIXME 
		// TODO Auf existierenden Ordernamen prüfen und Exception werfen
		final Path parentPath;
		if (parent != null) {
			parentPath = Paths.get(parent.getUuid());
		} else {
			parentPath = rootDirectory;
		}
		final Path newFolderPath = parentPath.resolve(name);
		// Gibt es den Folder bereits?
		Files.createDirectory(newFolderPath);
		final Note newNote = new Note(newFolderPath.toAbsolutePath().toString());
		newNote.setIsFolder(true);
		newNote.setSubject(name);	
		return newNote;
	}

	@Override
	public void renameNote(Note note, String newName) throws Exception {
		note.setSubject(newName);
		update(note);
	}

	@Override
	public void renameFolder(Note note, String newName) throws Exception {
		note.setSubject(newName);
		final Path oldPath = Paths.get(note.getUuid());
		final Path newFolderPath = oldPath.getParent().resolve(newName);
		Path newPath = Files.move(oldPath, newFolderPath);
		note.setUuid(newPath.toAbsolutePath().toString());
	}

	@Override
	public void init(Account account) throws Exception {
		this.rootDirectory = Paths.get(account.getRoot_folder());
	}

	@Override
	public Note move(Note msg, Note folder) throws Exception {
		Path itemPath = Paths.get(msg.getUuid());
		Path target = Paths.get(folder.getUuid()).resolve(itemPath.getFileName());
		Path newFile = Files.move(itemPath, target);
		msg.setUuid(newFile.toAbsolutePath().toString());
		return msg;
	}

	@Override
	public List<Note> getNotesFromFolder(Note folder) throws Exception {
		final List<Note> notes = new ArrayList<>();
		final Path directory = Paths.get(folder.getUuid());
		final Gson gson = new Gson();

		try (Stream<Path> fileStream = Files.list(directory)) {

			fileStream.forEach(filePath -> {
				final String fileName = filePath.toAbsolutePath().toString();
				Note newNote = null;
				if (Files.isRegularFile(filePath) 
						&& fileName.endsWith(DEFAULT_FILE_ENDING)) {
					try {
						byte[] rawContent = Files.readAllBytes(filePath);
						final String jsonContent = new String(rawContent, DEFAULT_ENCODING);
						newNote = gson.fromJson(jsonContent, Note.class);
						
					} catch (JsonSyntaxException | IOException e) {
						getLogger().error("Reading note {} has failed.", fileName);
					}
				}
				if (Files.isDirectory(filePath)) {
					newNote = new Note(fileName);
					newNote.setIsFolder(true);
					newNote.setSubject(filePath.getFileName().toString());
				}
				if (newNote != null) {
					notes.add(newNote);		
					uuid2Path.put(newNote.getUuid(), filePath);
				}
			});
		}
		return notes;
	}
}
