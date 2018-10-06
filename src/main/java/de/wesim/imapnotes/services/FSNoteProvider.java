package de.wesim.imapnotes.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;


public class FSNoteProvider implements INoteProvider, HasLogger {

	private Path rootDirectory;

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

		final Path newFile = parentPath.resolve(uuid.toString() + ".imapnote");
		final Note newNote = new Note(newFile.toAbsolutePath().toString());
		newNote.setSubject(subject);
		newNote.setContent(Consts.EMPTY_NOTE);
		update(newNote);
		return newNote;
	}

	@Override
	public void load(Note note) throws Exception {
		if (note.getContent() == null) {
			final Path path = Paths.get(note.getUuid());
			final String loadedContent = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
			int startIndex = loadedContent.indexOf("<html ");
			if (startIndex == -1) {
                            startIndex = loadedContent.indexOf(System.lineSeparator());
                        }
                        if (startIndex == -1) {
				note.setContent(loadedContent);
			} else {
				note.setContent(loadedContent.substring(startIndex));
			}
		}
	}

	@Override
	public void update(Note note) throws Exception {
		final Path path = Paths.get(note.getUuid());
		// TODO FIXME
		final String content = "#" + note.getSubject() + System.lineSeparator() + note.getContent();
		Files.write(path, content.getBytes("UTF-8"));
	}

	@Override
	public void delete(Note note) throws Exception {
		final Path path = Paths.get(note.getUuid());
		Files.delete(path);
	}

	private String readSubject(Path path) {
		try {
			return readSubject_Impl(path);
		} catch (IOException e) {
			e.printStackTrace();
			return "N/A";
		}
	}

	private String readSubject_Impl(Path path) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {

			String firstLine = br.readLine();
			if (firstLine == null)
				return "N/A";
			firstLine = firstLine.trim();
			if (firstLine.length() < 2)
				return "N/A";
			return firstLine.substring(1);

		}
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
		Path target = Paths.get(folder.getUuid());
		Path newFile = Files.move(itemPath, target);
		msg.setUuid(newFile.toAbsolutePath().toString());
		return msg;
	}

	@Override
	public List<Note> getNotesFromFolder(Note folder) throws Exception {
		final List<Note> notes = new ArrayList<>();
		final Path directory = Paths.get(folder.getUuid());
		try (Stream<Path> fileStream = Files.list(directory)) {

			fileStream.forEach(filePath -> {
				final String fileName = filePath.toAbsolutePath().toString();
				if (Files.isRegularFile(filePath)) {
					// FIXME Dateiname muss geparst werden !!!
					final Note newNote = new Note(fileName);
					final String subject = readSubject(filePath);
					newNote.setSubject(subject);
					notes.add(newNote);
				}
				if (Files.isDirectory(filePath)) {
					// FIXME Dateiname muss geparst werden !!!
					// String uuid = fileName.replace(".imapnote", "");
					// TODO Hier eine UUID verwenden ...
					final Note newNote = new Note(fileName);
					newNote.setIsFolder(true);
					newNote.setSubject(filePath.getFileName().toString());
					notes.add(newNote);
				}
			});
		}
		return notes;
	}
}
