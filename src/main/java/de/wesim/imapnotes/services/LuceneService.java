package de.wesim.imapnotes.services;

import java.io.IOException;

import org.springframework.stereotype.Service;
import de.wesim.imapnotes.HasLogger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class LuceneService implements HasLogger {

    @Autowired
    private Path indexesDir;


    public List<LuceneResult> search(String account, String queryTerm) {
        var indexDir = indexesDir.resolve(account);
      
        var result = new ArrayList<LuceneResult>();
        
        try (IndexReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();

            QueryParser parser = new QueryParser("contents", analyzer);
            Query query = parser.parse("contents:" + queryTerm + " OR subject:" + queryTerm );
            
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
