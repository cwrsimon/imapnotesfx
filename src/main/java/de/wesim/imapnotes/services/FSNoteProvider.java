package de.wesim.imapnotes.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;


public class FSNoteProvider implements INoteProvider {

	private Logger logger = LoggerFactory.getLogger(FSNoteProvider.class);
	
	// TODO Umstellung auf einen UUID -> Path-Mapper 

	private Path noteDirectory;
	private Path currentDirectory;
	private Stack<Path> folderStack;
	private final Map<String, Path> fsMap;

	public FSNoteProvider() {
		this.folderStack = new Stack<Path>();
		this.fsMap = new HashMap<>();
	}

	@Override
	public Note createNewNote(String subject, Note parentFolder) throws Exception {
		// TODO !!!
		final UUID uuid = UUID.randomUUID();
		final Path newFile = this.currentDirectory.resolve(uuid.toString() + ".imapnote");
		// TODO Subject ...
		final Note newNote = new Note(uuid.toString());
		newNote.setSubject(subject);
		this.fsMap.put(uuid.toString(), newFile);
		newNote.setContent(Consts.EMPTY_NOTE);
		update(newNote);
		return newNote;
	}

	@Override
	public void openFolder(Note folder) throws Exception {
		final Path path = this.fsMap.get(folder.getUuid());
		if (Files.isDirectory(path)) {
			this.folderStack.push(this.currentDirectory);
			this.currentDirectory = path;
		}
		// TODO Verheiraten mit load ... und entsprechend zurückgeben ...
	}

	// @Override
	// public void returnToParent() throws Exception {
	// 	this.currentDirectory = this.folderStack.pop();
	// 	logger.info("returning to parent dir {}", this.currentDirectory.toString());
	// }

	@Override
	public void load(Note note) throws Exception {
		if (note.getContent() == null) {
			final Path path = this.fsMap.get(note.getUuid());
			final String loadedContent = new String(Files.readAllBytes(path));
			int startIndex = loadedContent.indexOf("<html ");
			if (startIndex == -1) {
				note.setContent(loadedContent);
			} else {
				note.setContent(loadedContent.substring(startIndex));
			}
		}
	}

	@Override
	public void update(Note note) throws Exception {
		final Path path = this.fsMap.get(note.getUuid());
		final String content = "#" + note.getSubject() + System.lineSeparator() + note.getContent();
		Files.write(path, content.getBytes("UTF-8"));
	}

	@Override
	public void delete(Note note) throws Exception {
		final Path path = this.fsMap.get(note.getUuid());
		// TODO try, etc. 
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
		final List<Note> notes = new ArrayList<>();
		this.fsMap.clear();
		try (Stream<Path> fileStream = Files.list(this.currentDirectory)) {

			fileStream.forEach(filePath -> {
				final String fileName = filePath.getFileName().toString();
				if (Files.isRegularFile(filePath)) {
					// FIXME Dateiname muss geparst werden !!!
					String uuid = fileName.replace(".imapnote", "");
					final Note newNote = new Note(uuid);
					this.fsMap.put(uuid, filePath);
					final String subject = readSubject(filePath);
					newNote.setSubject(subject);
					notes.add(newNote);
				}
				if (Files.isDirectory(filePath)) {
					// FIXME Dateiname muss geparst werden !!!
					// String uuid = fileName.replace(".imapnote", "");
					// TODO Hier eine UUID verwenden ...
					final Note newNote = new Note(filePath.toAbsolutePath().toString());
					newNote.setIsFolder(true);
					this.fsMap.put(filePath.toAbsolutePath().toString(), filePath);
					newNote.setSubject(fileName);
					notes.add(newNote);
				}
			});

		}
		if (this.folderStack.isEmpty()) return notes;

		final Path prevFolder = this.folderStack.peek();
		if (prevFolder != null) {
			final String pseudoUUID = "BACKTOPARENT" + String.valueOf(this.folderStack.size());
			final Note newNote = new Note(pseudoUUID);
			newNote.setIsFolder(true);
			this.fsMap.put(pseudoUUID, null);
			newNote.setSubject("Zurück");
			notes.add(newNote);
		}
		return notes;
	}

	@Override
	public void destroy() throws Exception {
		;
	}

	@Override
	public Note createNewFolder(String name, Note parent) throws Exception {
		// TODO FIXME 
		// TODO Auf existierenden Ordernamen prüfen und Exception werfen
		final Path newFolderPath = this.currentDirectory.resolve(name);
		Files.createDirectory(newFolderPath);
		final Note newNote = new Note(newFolderPath.toAbsolutePath().toString());
		newNote.setIsFolder(true);
		this.fsMap.put(newFolderPath.toAbsolutePath().toString(), newFolderPath);
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
		final Path oldPath = this.fsMap.get(note.getUuid());
		final Path newFolderPath = oldPath.getParent().resolve(newName);
		this.fsMap.put(note.getUuid(), newFolderPath);
		Files.move(oldPath, newFolderPath);
	}

	@Override
	public void init(Account account) throws Exception {
		this.noteDirectory = Paths.get(account.getRoot_folder());
		System.out.println(this.noteDirectory.toString());
		this.currentDirectory = this.noteDirectory;
	}

	@Override
	public boolean move(Note msg, Note folder) {
		// TODO 
		return false;
	}

	@Override
	public List<Note> getNotesFromFolder(Note folder) throws Exception {
		// TODO
		return null;
	}
}
