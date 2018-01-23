///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//import com.sun.mail.imap.IMAPFolder;
//import com.sun.mail.imap.IMAPMessage;
//import de.wesim.imapsync.backend.IMAPBackend;
//import java.io.IOException;
//import java.util.Enumeration;
//import java.util.List;
//import javax.mail.Header;
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.Session;
//import javax.mail.UIDFolder;
//import javax.mail.internet.MimeMessage;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import static org.junit.Assert.*;
//import org.junit.Ignore;
//
//
//// http://www.oracle.com/technetwork/java/javamail/faq/index.html#webmail
//
///**
// *
// * @author christian
// */
//public class ImapBackendTests {
//
//    private IMAPBackend backend;
//
//    public ImapBackendTests() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() {
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//    }
//
//    @Before
//    public void setUp() {
//        this.backend = new IMAPBackend();
//
//    }
//
//    @After
//    public void tearDown() throws MessagingException {
//        if (this.backend != null) {
//            backend.destroy();
//        }
//
//    }
//
//    // TODO add test methods here.
//    // The methods must be annotated with annotation @Test. For example:
//    //
//    @Ignore
//    @Test
//    public void hello() throws MessagingException, IOException {
//        final Session session = backend.getSession();
//        this.backend.initNotesFolder("Notes");
//
//        final List<Message> messages = backend.getMessages();
//        System.out.println(messages.size());
//        for (Message msg : messages) {
//            this.backend.startTransaction();
//            final long uid = ((UIDFolder) this.backend.getNotesFolder()).getUID(msg);
////             final String content = backend.getMessageContent(msg);
//            System.out.println(uid);
//            Enumeration<Header> enums = msg.getAllHeaders();
//            while (enums.hasMoreElements()) {
//                Header next = (Header) enums.nextElement();
////			X-Mail-Created-Date
////			X-Universally-Unique-Identifier
//                String name = next.getName();
//
//                System.out.println(name + ";" + next.getValue());
//            }
//        }
//        IMAPFolder folder = (IMAPFolder) this.backend.getNotesFolder();
//        folder.getMessageByUID(1452);
//    }
//
//    @Test
//    public void getMessageByUID() throws MessagingException, IOException {
//        final Session session = backend.getSession();
//        this.backend.initNotesFolder("Notes/Playground");
//
//        this.backend.startTransaction();
//        IMAPFolder folder = (IMAPFolder) this.backend.getNotesFolder();
//       // Message msg = folder.getMessageByUID(1452);
////        Enumeration<Header> enums = msg.getAllHeaders();
////        while (enums.hasMoreElements()) {
////            Header next = (Header) enums.nextElement();
//////			X-Mail-Created-Date
//////			X-Universally-Unique-Identifier
////            String name = next.getName();
////
////            System.out.println(name + ";" + next.getValue());
////        }
//                final List<Message> messages = backend.getMessages();
//        System.out.println(messages.size());
////        for (Message msg : messages) {
////            this.backend.startTransaction();
////            final long uid = ((UIDFolder) this.backend.getNotesFolder()).getUID(msg);
//////             final String content = backend.getMessageContent(msg);
////            System.out.println(uid);
////            Enumeration<Header> enums = msg.getAllHeaders();
////            while (enums.hasMoreElements()) {
////                Header next = (Header) enums.nextElement();
//////			X-Mail-Created-Date
//////			X-Universally-Unique-Identifier
////                String name = next.getName();
////
////                System.out.println(name + ";" + next.getValue());
////            }
////        }
//
//        IMAPMessage firstMessage = (IMAPMessage) messages.get(0);
//        Message newMessage =  backend.updateMessageContent(firstMessage, "Bla");
//        backend.startTransaction();
//        folder.expunge();
//        
////        this.backend.startTransaction();
////        IMAP messages are read-only, just like POP3 messages. It's a limitation of the protocol, not JavaMail. The closest you can get to modifying a message is to read the message, make a local copy using the MimeMessage copy constructor, modify the copy, append the copy to the folder, and delete the original message.
////
////The javadocs for IMAPMessage were accidentally omitted. But then, it doesn't have any methods that will help you solve this problem
////        System.out.println(firstMessage.getSentDate());
////        System.out.println(firstMessage.getContent());
// 
//        this.backend.endTransaction();
//
////         }
//    }
//}
