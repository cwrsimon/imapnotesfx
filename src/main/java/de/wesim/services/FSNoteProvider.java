package de.wesim.services;

import javax.mail.Message;
import de.wesim.imapnotes.IMAPBackend;
import de.wesim.models.Note;
import de.wesim.models.NoteFolder;
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

public class FSNoteProvider implements INoteProvider {

	// Umstellung auf ein TOC

	private Path userHome;
	private Path noteDirectory;
	private Path currentDirectory;

	public FSNoteProvider() throws Exception {
		//		this.backend = IMAPBackend.initNotesFolder("Notes/Playground");
		this.userHome = Paths.get(System.getProperty("user.home"));
		this.noteDirectory = this.userHome.resolve("CurrentProjects").resolve("notes");
		this.currentDirectory = this.noteDirectory;

	}

	@Override
	public Note createNewNote(String subject) throws Exception {
		final UUID uuid = UUID.randomUUID();
		final Path newFile = this.currentDirectory.resolve(uuid.toString() + ".imapnote");
		// TODO Subject ...
		final Note newNote = new Note(uuid.toString());
		newNote.setSubject(subject);
		newNote.setImapMessage(newFile);
		newNote.setContent("<html ><head></head><body></body></html>");
		update(newNote);
		return newNote;
	}

	@Override
	public void openFolder(NoteFolder folder) throws Exception {
		final Path path = (Path) folder.getRawImapMessage();
		if (Files.isDirectory(path)) {
			this.currentDirectory = path;
		}
		// TODO Verheiraten mit load ... und entsprechend zurückgeben ...
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
					final NoteFolder newNote = new NoteFolder(fileName);
					newNote.setImapMessage(filePath);
					newNote.setSubject(fileName);
					newNote.setParentDirectory(this.currentDirectory);
					notes.add(newNote);
				}


			});

		}
		return notes;
	}

	@Override
	public void destroy() throws Exception {
		;
	}

}
