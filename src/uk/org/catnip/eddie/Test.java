package uk.org.catnip.eddie;

import java.io.*;
import java.util.regex.*;
import java.util.Iterator;
import org.apache.log4j.Logger;
import uk.org.catnip.eddie.parser.Parser;
import org.python.util.PythonInterpreter;
import org.python.core.*;

public class Test {
    static Logger log = Logger.getLogger(Test.class);

    private PythonInterpreter interp = new PythonInterpreter();

    public int total_tests = 0;

    public int failed_tests = 0;

    public int passed_tests = 0;

    public void parse_dir(String dirname) {

        File file_or_dir = new File(dirname);
        if (file_or_dir.isFile()) {
            test(dirname);

        } else {
            String[] dir = file_or_dir.list();
            java.util.Arrays.sort(dir);
            for (int i = 0; i < dir.length; i++) {
                if (!dir[i].equals(".svn")) {

                    File dir_entry = new File(dirname + "/" + dir[i]);
                    if (dir_entry.isDirectory()) {
                        System.out.println("descending into " + dir[i]);
                        parse_dir(dirname + "/" + dir[i]);
                        System.out.println();
                    } else if (dir_entry.isFile()) {

                        test(dirname + "/" + dir[i]);
                    }
                }
            }
        }

    }

    public boolean test(String filename) {
        total_tests++;

        boolean ok = false;

        try {

            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            String description = "";
            String test = "";
            Pattern desc_pattern = Pattern.compile("^Description: (.*)$");
            Pattern test_pattern = Pattern.compile("^Expect: (.*)$");

            while ((line = in.readLine()) != null) {
                Matcher desc_matcher = desc_pattern.matcher(line);
                Matcher test_matcher = test_pattern.matcher(line);
                if (desc_matcher.matches()) {
                    description = desc_matcher.group(1);
                } else if (test_matcher.matches()) {
                    test = test_matcher.group(1);
                }
            }
            in.close();
            if (log.isDebugEnabled()) {
                log.debug(filename);
                log.debug("Description: " + description);
                log.debug("Test: " + test);
            }
            Parser parser = new Parser();
            Feed feed = parser.parse(filename);
            if (log.isDebugEnabled()) {
                log.debug(feed);
            }
            runPython(feed, test);
            if (log.isInfoEnabled()) {
                log.info("passed: "+ filename);
            } else {
                System.out.print(".");
            }
            ok = true;
            passed_tests++;
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("****************************************");
                log.debug("parse failed: " + ex);
            } else if (log.isInfoEnabled()){
                log.info("failed: " + filename);
            } else {
                System.out.print("x");
            }
            
            failed_tests++;
        }
        return ok;

    }

    public void runPython(Feed feed, String test) throws Exception {

        if (feed.error) {
            interp.set("bozo", new PyInteger(1));
        } else {
            interp.set("bozo", new PyInteger(0));
        }

        PyList entries_list = new PyList();
        Iterator entries = feed.entries();
        while (entries.hasNext()) {
            entries_list.append(convertEntry((Entry) entries.next()));
        }


        interp.set("feed", convertFeed(feed));
        interp.set("entries", entries_list);
        interp.exec("ret ="+ test);
        PyInteger ret = (PyInteger)interp.get("ret");
        if (ret.getValue() == 0) { 
            throw new Exception("test failed");
          
        }

    }
    public PyDictionary convertFeed(Feed feed) {
        PyDictionary feed_dict = new PyDictionary();
        Iterator it = feed.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            feed_dict.__setitem__(key, new PyString(feed.get(key)));
        }
        if (feed.getAuthor() != null) {
            feed_dict.__setitem__("author_detail", convertAuthor(feed.getAuthor()));
        }
        if (feed.getTitle() != null) {
            feed_dict.__setitem__("title_detail",convertDetail(feed.getTitle()));
        }
        if (feed.getTagline() != null) {
            feed_dict.__setitem__("tagline_detail",convertDetail(feed.getTagline()));
        }
        if (feed.getGenerator() != null) {
            feed_dict.__setitem__("generator_detail",convertGenerator(feed.getGenerator()));
        }
        PyList contributors_list = new PyList();
        Iterator contributors = feed.contributors();
        while (contributors.hasNext()) {
            contributors_list.append(convertAuthor((Author)contributors.next()));
        }
        feed_dict.__setitem__("contributors",contributors_list);
        
        
        PyList links_list = new PyList();
        Iterator links = feed.links();
        while (links.hasNext()) {
            links_list.append(convertLink((Link)links.next()));
        }
        feed_dict.__setitem__("links",links_list);
        return feed_dict;
        
    }
    public PyDictionary convertAuthor(Author author) {
        PyDictionary author_detail = new PyDictionary();
        author_detail.__setitem__("name", new PyString(author.getName()));
        author_detail.__setitem__("email", new PyString(author.getEmail()));
        author_detail.__setitem__("url", new PyString(author.getUrl()));
        return author_detail;
    }
    

    
    
    public PyDictionary convertEntry(Entry entry) {
        PyDictionary entry_dict = new PyDictionary();
        Iterator entry_it = entry.keys();
        while (entry_it.hasNext()) {
            String key = (String) entry_it.next();
            entry_dict.__setitem__(key, new PyString(entry.get(key)));
        }
        if (entry.getAuthor() != null) {
            entry_dict.__setitem__("author_detail", convertAuthor(entry.getAuthor()));
        }
        if (entry.getTitle() != null) {
            entry_dict.__setitem__("title_detail",convertDetail(entry.getTitle()));
        }
        
        
        PyList contents_list = new PyList();
        Iterator contents = entry.contents();
        while (contents.hasNext()) {
            contents_list.append(convertDetail((Detail)contents.next()));
        }
        entry_dict.__setitem__("content",contents_list);
        
        PyList contributors_list = new PyList();
        Iterator contributors = entry.contributors();
        while (contributors.hasNext()) {
            contributors_list.append(convertAuthor((Author)contributors.next()));
        }
        entry_dict.__setitem__("contributors",contributors_list);
        
        PyList links_list = new PyList();
        Iterator links = entry.links();
        while (links.hasNext()) {
            links_list.append(convertLink((Link)links.next()));
        }
        entry_dict.__setitem__("links",links_list);
        
        return entry_dict;
    }
    
    public PyDictionary convertDetail(Detail detail) {
        PyDictionary detail_dict = new PyDictionary();
        if (!detail.getLanguage().equals("")) {
        detail_dict.__setitem__("language", new PyString(detail.getLanguage()));
        }
        if (!detail.getType().equals("")) {
        detail_dict.__setitem__("type", new PyString(detail.getType()));
        }
        if (!detail.getValue().equals("")) {
        detail_dict.__setitem__("value", new PyString(detail.getValue()));
        }
        return detail_dict;
    }
    public PyDictionary convertLink(Link link) {
        PyDictionary link_dict =  convertDetail(link);
        if (!link.getHref().equals("")) {
        link_dict.__setitem__("href", new PyString(link.getHref()));
        }
        if (!link.getTitle().equals("")) {
        link_dict.__setitem__("title", new PyString(link.getTitle()));
        }
        if (!link.getRel().equals("")) {
        link_dict.__setitem__("rel", new PyString(link.getRel()));
        }
        return link_dict;
    }
    public PyDictionary convertGenerator(Generator generator) {
        PyDictionary link_dict = convertDetail(generator);
        link_dict.__setitem__("name", new PyString(generator.getName()));
        link_dict.__setitem__("url", new PyString(generator.getUrl()));
        link_dict.__setitem__("version", new PyString(generator.getVersion()));
        return link_dict;
    }
}