package de.wesim.imapnotes.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Stream;

import de.wesim.imapnotes.models.Note;


public class FSNoteProvider implements INoteProvider {

	// TODO Umstellung auf einen UUID -> Path-Mapper 

	private Path userHome;
	private Path noteDirectory;
	private Path currentDirectory;
	private Stack<Path> folderStack;

	public FSNoteProvider() throws Exception {
		this.userHome = Paths.get(System.getProperty("user.home"));
		this.noteDirectory = this.userHome.resolve("CurrentProjects").resolve("notes");
		this.currentDirectory = this.noteDirectory;
		this.folderStack = new Stack<Path>();
	}

	@Override
	public Note createNewNote(String subject) throws Exception {
		final UUID uuid = UUID.randomUUID();
		final Path newFile = this.currentDirectory.resolve(uuid.toString() + ".imapnote");
		// TODO Subject ...
		final Note newNote = new Note(uuid.toString());
		newNote.setSubject(subject);
		newNote.setImapMessage(newFile);
		newNote.setContent(INoteProvider.EMPTY_NOTE);
		update(newNote);
		return newNote;
	}

	@Override
	public void openFolder(Note folder) throws Exception {
		final Path path = (Path) folder.getRawImapMessage();
		if (Files.isDirectory(path)) {
			this.folderStack.push(this.currentDirectory);
			this.currentDirectory = path;
		}
		System.out.println("currentDir:" + this.currentDirectory.toString());
		// TODO Verheiraten mit load ... und entsprechend zurückgeben ...
	}

	@Override
	public void returnToParent() throws Exception {
		this.currentDirectory = this.folderStack.pop();
		System.out.println("returnToParent " + this.currentDirectory.toString());
	}

	@Override
	public void load(Note note) throws Exception {
		if (note.getContent() == null) {
			final Path path = (Path) note.getRawImapMessage();
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
		final Path path = (Path) note.getRawImapMessage();
		final String content = "#" + note.getSubject() + System.lineSeparator() + note.getContent();
		Files.write(path, content.getBytes("UTF-8"));
	}

	@Override
	public void delete(Note note) throws Exception {
		Path path = (Path) note.getRawImapMessage();
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
		List<Note> notes = new ArrayList<>();
		try (Stream<Path> fileStream = Files.list(this.currentDirectory)) {

			fileStream.forEach(filePath -> {
				final String fileName = filePath.getFileName().toString();
				if (Files.isRegularFile(filePath)) {
					// FIXME Dateiname muss geparst werden !!!
					String uuid = fileName.replace(".imapnote", "");
					final Note newNote = new Note(uuid);
					newNote.setImapMessage(filePath);
					final String subject = readSubject(filePath);
					newNote.setSubject(subject);
					notes.add(newNote);
				}
				if (Files.isDirectory(filePath)) {
					// FIXME Dateiname muss geparst werden !!!
					// String uuid = fileName.replace(".imapnote", "");
					final Note newNote = new Note(fileName);
					newNote.setIsFolder(true);
					newNote.setImapMessage(filePath);
					newNote.setSubject(fileName);
					notes.add(newNote);
				}
			});

		}
		if (this.folderStack.isEmpty()) return notes;

		final Path prevFolder = this.folderStack.peek();
		if (prevFolder != null) {
			final Note newNote = new Note("BACKTOPARENT" + String.valueOf(this.folderStack.size()));
			newNote.setIsFolder(true);
			newNote.setImapMessage(null);
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
	public Note createNewFolder(String name) throws Exception {
		// TODO Auf existierenden Ordernamen prüfen und Exception werfen
		final Path newFolderPath = this.currentDirectory.resolve(name);
		Files.createDirectory(newFolderPath);
		final Note newNote = new Note(name);
		newNote.setIsFolder(true);
		newNote.setImapMessage(newFolderPath);
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
		final Path oldPath = (Path) note.getImapMessage();
		final Path newFolderPath = oldPath.getParent().resolve(newName);
		note.setImapMessage(newFolderPath);
		Files.move(oldPath, newFolderPath);
	}
}
