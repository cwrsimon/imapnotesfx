///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package de.wesim.imapnotes;
//
//import java.io.IOException;
//import java.sql.Clob;
//import java.sql.Connection;
//import java.sql.Date;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.UUID;
//import javax.mail.Header;
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.Session;
//import javax.mail.UIDFolder;
//import org.h2.jdbcx.JdbcConnectionPool;
//import org.jooq.DSLContext;
//import org.jooq.Record;
//import org.jooq.SQLDialect;
//import static org.jooq.h2.generated.Tables.NOTES;
//import org.jooq.h2.generated.tables.Notes;
//import org.jooq.impl.DSL;
//
///**
// *
// * @author christian
// */
//public class SyncDB {
//
//    private static void dumpRow(JdbcConnectionPool cp, UUID uuid, String subject, String content) throws SQLException {
//        Connection conn = cp.getConnection();
//        PreparedStatement stmt = conn.prepareStatement("insert into notes (uid, title, content) values (?,?,?);");
//        stmt.setObject(1, uuid);
//        stmt.setString(2, subject);
//        Clob clob = conn.createClob();
//        clob.setString(1, content);
//        stmt.setClob(3, clob);
//        stmt.execute();
//        clob.free();
//        conn.close();
//    }
//
//    private static void dumpRow2(JdbcConnectionPool cp, String noteUid, long msgUid, 
//            String subject, String content, Date sentDate) throws SQLException {
//
//        try (Connection conn = cp.getConnection()) {
//            DSLContext create = DSL.using(conn, SQLDialect.H2);
//            System.out.println(create.insertInto(NOTES)
//                    .set(NOTES.MSGUID, msgUid)
//                    .set(NOTES.NOTEUID, noteUid)
//                    .set(NOTES.TITLE, subject)
//                    .set(NOTES.CONTENT, content)
//                    .set(NOTES.TIMESTAMP, new Timestamp(sentDate.getTime()))
//                    .execute());
//        }
//
//    }
//
//    public static void main(String[] args) throws SQLException, MessagingException, IOException {
//        JdbcConnectionPool cp = JdbcConnectionPool.create(
//                "jdbc:h2:/Users/christian/test.db", "test", "test");
//
//        IMAPBackend backend = new IMAPBackend();
//
//        final Session session = backend.getSession();
//        backend.initNotesFolder("Notes/Playground");
//
//        final List<Message> messages = backend.getMessages();
//        System.out.println(messages.size());
//        backend.startTransaction();
//
//        for (Message msg : messages) {
//            backend.startTransaction();
//
//            System.out.println(msg.getSentDate());
//            long msgUid = ((UIDFolder) backend.getNotesFolder()).getUID(msg);
//
//            String nodesUid = null;
//            String content = backend.getMessageContent(msg);
//            final Date sentDate = new Date(msg.getSentDate().getTime());
//            backend.startTransaction();
//
//            Enumeration<Header> enums = msg.getAllHeaders();
//            while (enums.hasMoreElements()) {
//                Header next = (Header) enums.nextElement();
////			X-Mail-Created-Date
////			X-Universally-Unique-Identifier
//                String name = next.getName();
//                if (name.equals("X-Universally-Unique-Identifier")) {
//                    nodesUid = next.getValue();
//                }
//                System.out.println(name + ";" + next.getValue());
//            }
//            String subject = msg.getSubject();
//            System.out.println(String.format("%s,%s,%s", nodesUid,
//                    String.valueOf(msgUid),
//                    subject));
//            
//            try {
//
//                dumpRow2(cp, nodesUid, msgUid, subject, content, sentDate);
//            } catch (Exception e) {
//                System.err.println("Es gibt schon eine NOitz mit dieser UID : " + msgUid);
//                e.printStackTrace();
//            }
//        }
//
//        cp.dispose();
//
//    }
//
//}
