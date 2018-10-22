package de.wesim.imapnotes.mainview.services;

import org.springframework.stereotype.Component;

import de.wesim.imapnotes.models.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

// FIXME TODO kann das so bleiben???
@Component
public class DeleteMessageTask extends AbstractNoteService<Void> {

	private ObjectProperty<TreeItem<Note>> note = new SimpleObjectProperty<TreeItem<Note>>(this, "note");

	public final void setNote(TreeItem<Note> value) {
		note.set(value);
	}

	public final TreeItem<Note> getNote() {
		return note.get();
	}

	public final ObjectProperty<TreeItem<Note>> noteProperty() {
		return note;
	}

	private ObjectProperty<TreeItem<Note>> parentFolder = new SimpleObjectProperty<TreeItem<Note>>(this, "parentFolder");

	public final void setParentFolder(TreeItem<Note> value) {
		parentFolder.set(value);
	}

	public final TreeItem<Note> getParentFolder() {
		return parentFolder.get();
	}

	public final ObjectProperty<TreeItem<Note>> parentFolderProperty() {
		return parentFolder;
	}

	public DeleteMessageTask() {
		super();
	}

	@Override
	protected Task<Void> createTask() {
		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				updateProgress(0, 1);
				updateMessage(i18N.getMessageAndTranslation("user_message_start_deleting",
						note.getValue().getValue().getSubject()));

				mainViewController.getBackend().delete(getNote().getValue());

				updateMessage(i18N.getMessageAndTranslation("user_message_finished_deleting",
						note.getValue().getValue().getSubject()));

				updateProgress(1, 1);

				return null;
			}
		};
		return task;
	}
	
	@Override
	protected void succeeded() {
		final TreeItem<Note> parentNote = getParentFolder();
		final TreeItem<Note> deletedItem = getNote();
		final Note deletedNote = deletedItem.getValue();
		mainViewController.closeTab(deletedNote);
		final int index = parentNote.getChildren().indexOf(deletedItem);

		parentNote.getChildren().remove(deletedItem);

		final int previousItem = Math.max(0, index - 1);
		if (parentNote.getChildren().isEmpty()) return;
		final TreeItem<Note> previous = parentNote.getChildren().get(previousItem);
		mainViewController.openNote(previous.getValue());

	}

	@Override
	public String getActionName() {
		return "Delete Message";
	}
}

