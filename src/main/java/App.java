import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.IMAPBackend;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    
    
    public static void main(String[] args) throws MessagingException, IOException {
        System.out.println(new App().getGreeting());
                
        IMAPBackend backend = IMAPBackend.initNotesFolder("Notes/Playground");
//        backend.getMessages()
//		backend.createNewMessage("Hello World!", "This is THE house of Santa Clause.");
        
        //final Session session = backend.getSession();
        //this.backend.initNotesFolder("Notes");

        List<Note> messages = backend.getMessages();
        System.out.println("Anzahl der NAchrichten:" + messages.size());
        for (Note msg : messages) {
          //  backend.startTransaction();
//             final String content = backend.getMessageContent(msg);
            System.out.println(msg.getContent());
           
        }
//        Message msg = backend.getMessageByUID(7);
//        backend.dumpMessage(msg);
//        Note msg = backend.createNewMessage("Neue Nachricht", "Bla");
//        for (int i = 0; i<5; i++) {
//        	msg = backend.updateMessageContent(msg, "Bla" + String.valueOf(i));
//        }
//        messages = backend.getMessages();
//        System.out.println("Anzahl der NAchrichten:" + messages.size());
//
//        backend.deleteMessage(msg);

        messages = backend.getMessages();
        System.out.println("Anzahl der NAchrichten:" + messages.size());
        
        backend.destroy();
    }
}
