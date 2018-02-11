package de.wesim.imapnotes;

import de.wesim.models.Note;
import javafx.concurrent.Task;

public class OpenMessageTask extends Task<String> {
    private final IMAPBackend backend;
	private final Note victim;

    public OpenMessageTask( IMAPBackend backend, Note msgToOpen) {
        this.backend = backend;
        this.victim = msgToOpen;
    }

    @Override protected String call() throws Exception {
    	updateProgress(0, 1);
        this.victim.load(this.backend);
        Thread.sleep(2000);
    	updateProgress(1, 1);
    	return this.victim.getContent();
    }
}