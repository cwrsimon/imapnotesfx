package de.wesim.imapnotes.services;

import java.io.IOException;

import org.springframework.stereotype.Service;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class LuceneService implements HasLogger {

    @Autowired
    private Path indexesDir;

    public synchronized void indexNote(Note note, String account_string, String path) throws IOException {

        Directory dir = FSDirectory.open(indexesDir);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        // Add new documents to an existing index:
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        try (IndexWriter writer = new IndexWriter(dir, iwc)) {
            Document doc = createDocument(note, account_string, path);
            getLogger().info("Indexing document with {}, {}, {}", doc.get("uuid"),doc.get("subject_stored"), doc.get("path"));
            System.out.println("updating " + note);
            writer.updateDocument(new Term("uuid", note.getUuid()), doc);
        }

    }

    private Document createDocument(Note note, String account_string, String path) {
        Document doc = new Document();

        // Add the path of the file as a field named "path".  Use a
        // field that is indexed (i.e. searchable), but don't tokenize
        // the field into separate words and don't index term frequency
        // or positional information:
        Field idField = new StringField("uuid", note.getUuid(), Field.Store.YES);
        doc.add(idField);

        //https://stackoverflow.com/questions/14074613/how-to-search-an-int-field-in-lucene-4
        Field accountField = new StringField("account", account_string.toLowerCase(), Field.Store.YES);
        doc.add(accountField);

        Field pathField = new StringField("path", path, Field.Store.YES);
        doc.add(pathField);

        // Add the last modified date of the file a field named "modified".
        // Use a LongPoint that is indexed (i.e. efficiently filterable with
        // PointRangeQuery).  This indexes to milli-second resolution, which
        // is often too fine.  You could instead create a number based on
        // year/month/day/hour/minutes/seconds, down the resolution you require.
        // For example the long value 2011021714 would mean
        // February 17, 2011, 2-3 PM.
        doc.add(new LongPoint("modified", note.getDate().getTime()));

        // Add the contents of the file to a field named "contents".  Specify a Reader,
        // so that the text of the file is tokenized and indexed, but not stored.
        // Note that FileReader expects the file to be in UTF-8 encoding.
        // If that's not the case searching for special characters will fail.
        doc.add(new TextField("contents", new StringReader(note.getContent())));
        doc.add(new TextField("subject", new StringReader(note.getSubject())));
        doc.add(new StringField("subject_stored", note.getSubject(), Field.Store.YES));

        return doc;
    }

    // TODO Komplett überarbeiten!
    public List<LuceneResult> search(String account, String queryTerm) {
        // var indexDir = indexesDir.resolve(account);

        var result = new ArrayList<LuceneResult>();

        try (IndexReader reader = DirectoryReader.open(FSDirectory.open(indexesDir))) {

            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();

            QueryParser parser = new QueryParser("contents", analyzer);
            Query query = parser.parse("account:" + account
                    + " AND (contents:" + queryTerm + " OR subject:" + queryTerm + " )");

            // Collect enough docs to show 5 pages
            TopDocs results = searcher.search(query, 5 * 10);
            ScoreDoc[] hits = results.scoreDocs;

            int numTotalHits = Math.toIntExact(results.totalHits.value);
            //System.out.println(numTotalHits + " total matching documents");

            int start = 0;
            int end = Math.min(numTotalHits, 10);

            for (int i = start; i < end; i++) {
                Document doc = searcher.doc(hits[i].doc);
                //String uuid = doc.get("uuid");
                var resultItem = new LuceneResult();
                resultItem.setPath(doc.get("path"));
                resultItem.setUuid(doc.get("uuid"));
                resultItem.setSubject(doc.get("subject_stored"));
                result.add(resultItem);
                getLogger().info("{}", resultItem);
            }
        } catch (ParseException | IOException ex) {
            // TODO
            Logger.getLogger(LuceneService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
}
