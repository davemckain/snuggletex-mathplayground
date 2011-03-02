/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import java.io.FileReader;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

/**
 * FIXME: This is just a sketch!
 * FIXME: Probably need to think about thread-safety and lifecycle...
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class ASCIIMathParser {
    
    public static void main(String[] args) throws Exception {
        ASCIIMathParser t = new ASCIIMathParser(new FileReader("src/main/webapp/includes/ASCIIMathParser.js"));
        System.out.println(MathMLUtilities.serializeElement(t.parseASCIIMath("1/x")));
        System.out.println(MathMLUtilities.serializeElement(t.parseASCIIMath("siny/oo")));
        t.close();
    }
    
    private final Context context;
    private final ScriptableObject scope;
    private final Document document;
    
    public ASCIIMathParser(Reader parserJSFileReader) throws Exception {
        /* Create DOM Document for the parser to use */
        try {
            this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not create DOM Document Object");
        }
        
        /* Evaluate the parser script */
        this.context = Context.enter();
        this.scope = context.initStandardObjects();
        context.evaluateReader(scope, parserJSFileReader, "Parser", 1, null);
        
        /* Tell Rhino about the document */
        ScriptableObject.putProperty(scope, "document", document);
        
        /* Create parser Object */
        context.evaluateString(scope, "var parser = MakeASCIIMathParser(document)", null, 1, null);
    }
    
    public Element parseASCIIMath(String asciiMathInput) {
        ScriptableObject.putProperty(scope, "input", asciiMathInput);
        Object result = context.evaluateString(scope, "parser.parseMath(input)", null, 1, null);
        Element math = (Element) Context.jsToJava(result, Element.class);
        return math;
    }
    
    public void close() {
        Context.exit();
    }
}
