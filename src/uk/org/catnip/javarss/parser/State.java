package uk.org.catnip.javarss.parser;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import java.lang.StringBuilder;

public class State {
    public State() {}
     public State(String uri, String localName, String qName,
            Attributes atts, State prev) {
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
        this.atts = atts;
        this.type = atts.getValue("type");
        if (this.type == null) {
            this.type = prev.type;
        }
        if (this.type == null) {
            this.type = "plain/text";
        }
        this.language = atts.getValue("xml:lang");
        if (this.language == null) {
            this.language = prev.language;
        }
        if (this.language == null) {
            this.language = "en";
        }
    }
    private String uri = "";
    public String localName = "";
    public String element = "";
    public String qName = "";
    public Attributes atts = new AttributesImpl();
    public String language = "";
    public String base = "";
    public String mode = "";
    public String type = "";
    public boolean expectingText = false;
    public StringBuilder text = new StringBuilder();
    
    public void addText(String str) {
        text.append(str);
    }
public String getAttr(String key) {
    return this.getAttr(key, "");
}
public String getAttr(String key, String default_value) {
    String ret = atts.getValue("", key);
    if (ret == null) {
        ret = default_value;
    }
    return ret;
}
}
