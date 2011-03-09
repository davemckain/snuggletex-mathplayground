/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

/**
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class ASCIIMathParser {
    
    public static void main(String[] args) throws Exception {
        ASCIIMathParser t = new ASCIIMathParser(new FileReader("tw/includes/ASCIIMathParser.js"));
        
        Map<String, Object> opts = new HashMap<String, Object>();
        opts.put("displayMode", Boolean.TRUE);
        opts.put("addSourceAnnotation", Boolean.TRUE);
        System.out.println(MathMLUtilities.serializeElement(t.parseASCIIMath("1+43", opts)));
        
        System.out.println(MathMLUtilities.serializeElement(t.parseASCIIMath("oo")));
    }
    
    private final ScriptableObject sharedScope;
    
    public ASCIIMathParser(Reader parserJSFileReader) throws IOException {
        /* Evaluate the parser script and store away the results */
        Context context = Context.enter();
        this.sharedScope = context.initStandardObjects();
        context.evaluateReader(sharedScope, parserJSFileReader, "ASCIIMathParser.js", 1, null);
        Context.exit();
    }
    
    public Element parseASCIIMath(String asciiMathInput) {
        return parseASCIIMath(asciiMathInput, null);
    }
    
    public Element parseASCIIMath(String asciiMathInput, Map<String,Object> options) {
        /* Create DOM Document for the parser to use */
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not create DOM Document Object");
        }
        
        Context context = Context.enter();
        try {
            Scriptable newScope = context.newObject(sharedScope);
            
            Scriptable parser = context.newObject(newScope, "ASCIIMathParser", new Object[] { document });
            Scriptable optionsJS = context.newObject(newScope);
            if (options!=null) {
                for (Entry<String,Object> option : options.entrySet()) {
                    ScriptableObject.putProperty(optionsJS, option.getKey(), option.getValue());
                }
            }
            Object result = ScriptableObject.callMethod(parser, "parseASCIIMathInput", new Object[] { asciiMathInput, optionsJS });
            
            return (Element) Context.jsToJava(result, Element.class);
        }
        finally {
            Context.exit();
        }
    }
}
