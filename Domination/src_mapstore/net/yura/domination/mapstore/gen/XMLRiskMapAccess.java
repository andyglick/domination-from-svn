package net.yura.domination.mapstore.gen;
import net.yura.domination.mapstore.RiskMap;
import java.util.Hashtable;
import java.util.Vector;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlSerializer;
import java.io.IOException;
import net.yura.mobile.io.XMLUtil;
/**
 * THIS FILE IS GENERATED, DO NOT EDIT
 */
public class XMLRiskMapAccess extends XMLUtil {
    public XMLRiskMapAccess() {
    }
    protected void saveObject(XmlSerializer serializer,Object object) throws IOException {
        if (object instanceof RiskMap) {
            serializer.startTag(null,"RiskMap");
            saveRiskMap(serializer,(RiskMap)object);
            serializer.endTag(null,"RiskMap");
        }
        else {
            super.saveObject(serializer, object);
        }
    }
    protected void saveRiskMap(XmlSerializer serializer,RiskMap object) throws IOException {
    }
    protected Object readObject(KXmlParser parser) throws Exception {
        String name = parser.getName();
        if ("RiskMap".equals(name)) {
            return readRiskMap(parser);
        }
        else {
            return super.readObject(parser);
        }
    }
    protected RiskMap readRiskMap(KXmlParser parser) throws Exception {
        RiskMap object = new RiskMap();
        int count = parser.getAttributeCount();
        for (int c=0;c<count;c++) {
            String key = parser.getAttributeName(c);
            String value = parser.getAttributeValue(c);
            System.out.println("unknown item found "+key);
        }
        parser.skipSubTree();
        return object;
    }
}
