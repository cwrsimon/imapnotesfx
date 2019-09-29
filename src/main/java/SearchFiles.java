

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

// TODO Ersetzen durch Spring-SOLR-Lösung
// http://lucene.apache.org/core/8_0_0/demo/overview-summary.html#overview_description
public class SearchFiles {

  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    

    String index = "/home/papa/.imapnotesfx/indexes/";
    String field = "contents";
    int repeat = 0;
    String queryString = null;
    int hitsPerPage = 10;
    
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    Analyzer analyzer = new StandardAnalyzer();

    QueryParser parser = new QueryParser(field, analyzer);
    

      String line = "hidden";

      //
      Query query = parser.parse("account:Blub AND (contents:" + line + " OR subject:" + line + ")"  );
      System.out.println("Searching for: " + query.toString());

      // Collect enough docs to show 5 pages
    TopDocs results = searcher.search(query, 5 * hitsPerPage);
    ScoreDoc[] hits = results.scoreDocs;
    
    int numTotalHits = Math.toIntExact(results.totalHits.value);
    System.out.println(numTotalHits + " total matching documents");

    int start = 0;
    int end = Math.min(numTotalHits, hitsPerPage);
        
      for (int i = start; i < end; i++) {
          
        Document doc = searcher.doc(hits[i].doc);
        String path = doc.get("uuid");
        System.out.println(path + ";" + doc.get("path") +";" + doc.get("subject"));
        System.out.println(doc.get("account"));

      }
    

    reader.close();
  }

}





























































