package de.wesim.imapnotes.services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;

@Component
@Scope("prototype")
public class FSNoteProvider implements INoteProvider, HasLogger {

    private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

    private static final String DEFAULT_FILE_ENDING = ".json";

    private Path rootDirectory;

    private final Map<String, Path> uuid2Path = new HashMap<>();

    public FSNoteProvider() {
    }

    @Override
    public Note createNewNote(String subject, Note parentFolder) throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Path parentPath;
        if (parentFolder != null) {
            parentPath = uuid2Path.get(parentFolder.getUuid());
        } else {
            parentPath = rootDirectory;
        }
        final Path newFile = parentPath.resolve(uuid.toString() + DEFAULT_FILE_ENDING);
        final Note newNote = new Note(uuid.toString());
        uuid2Path.put(uuid.toString(), newFile);
        newNote.setSubject(subject);
        newNote.setContent("");
        update(newNote);
        return newNote;
    }

    @Override
    public void load(Note note) throws Exception {
        // nothing to do
    }

    @Override
    public void update(Note note) throws Exception {
        final Path path;
        if (!note.isFolder()) {
            path = uuid2Path.get(note.getUuid());
        } else {
            final Path parent = uuid2Path.get(note.getUuid()).getParent();
            path = parent.resolve(note.getUuid() + DEFAULT_FILE_ENDING);
        }
        // set current timestamp as date
        note.setDate(new Date());

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
        this.uuid2Path.clear();
        var notes = loadNotesFromFSDirectory(rootDirectory);
        return notes;
    }

    private List<Note> loadNotesFromFSDirectory(Path directory) throws Exception {
        final List<Note> notes = new ArrayList<>();
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
                if (newNote != null) {
                    notes.add(newNote);
                    if (!newNote.isFolder()) {
                        uuid2Path.put(newNote.getUuid(), filePath);
                    } else {
                        uuid2Path.put(newNote.getUuid(),
                                filePath.getParent().resolve(newNote.getUuid()));
                    }
                }
            });
        }
        return notes;
    }

    @Override
    public void destroy() throws Exception {
    }

    @Override
    public Note createNewFolder(String name, Note parent) throws Exception {
        final Path parentPath;
        if (parent != null) {
            parentPath = uuid2Path.get(parent.getUuid());
        } else {
            parentPath = rootDirectory;
        }
        final UUID uuid = UUID.randomUUID();
        final Path newFolderPath = parentPath.resolve(uuid.toString());
        Files.createDirectory(newFolderPath);
        final Note newNote = new Note(uuid.toString());
        uuid2Path.put(newNote.getUuid(), newFolderPath);
        newNote.setIsFolder(true);
        newNote.setSubject(name);
        // save as json, too, although folder
        update(newNote);
        return newNote;
    }

    @Override
    public void renameNote(Note note, String newName) throws Exception {
        note.setSubject(newName);
        update(note);
    }

    @Override
    public void renameFolder(Note folder, String newName) throws Exception {
        renameNote(folder, newName);
    }

    @Override
    public void init(Account account) throws Exception {
        this.rootDirectory = Paths.get(account.getRoot_folder());
    }

    @Override
    public Note move(Note msg, Note folder) throws Exception {
        // not supported, yet
        if (msg.isFolder()) {
            return null;
        }
        final Path itemPath = uuid2Path.get(msg.getUuid());
        final Path targetFolder;
        if (folder != null) {
            targetFolder = uuid2Path.get(folder.getUuid());
        } else {
            targetFolder = rootDirectory;
        }
        final Path target = targetFolder.resolve(itemPath.getFileName());
        // update reference
        uuid2Path.put(msg.getUuid(), target);
        Files.move(itemPath, target);
        return msg;
    }

    @Override
    public List<Note> getNotesFromFolder(Note folder) throws Exception {
        getLogger().info("getNotesFromFolder: {}", folder.toString());
        final Path jsonFile = uuid2Path.get(folder.getUuid());
        final Path directory = jsonFile.getParent().resolve(folder.getUuid());
        var notes = loadNotesFromFSDirectory(directory);
        return notes;
    }
}
