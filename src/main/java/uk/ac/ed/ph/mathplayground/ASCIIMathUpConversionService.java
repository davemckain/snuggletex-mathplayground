/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.asciimath.parser.ASCIIMathParser;
import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Trivial web service that accepts POSTed ASCIIMath input (or the raw Presentation MathML
 * generated by the ASCIIMathParser.js in the browser). This input is passed to the SnuggleTeX
 * up-conversion process and a JSON result is returned to the caller.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class ASCIIMathUpConversionService extends BaseServlet {
    
    private static final long serialVersionUID = 2261754980279697343L;
    
    private static Logger logger = LoggerFactory.getLogger(ASCIIMathUpConversionService.class);
    
    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        /* We'll read data in as UTF-8 */
        request.setCharacterEncoding("UTF-8");
        String input = IOUtilities.readCharacterStream(request.getReader());
        
        /* Parse the input */
        Document asciiMathMLDocument = parsePOSTData(input);
        
        /* Do up-conversion */
        MathMLUpConverter upConverter = new MathMLUpConverter(getStylesheetManager());
        Document upConvertedMathDocument = upConverter.upConvertASCIIMathML(asciiMathMLDocument, getUpConversionOptions());
        Element mathElement = upConvertedMathDocument.getDocumentElement(); /* NB: Document is <math/> here */
        
        /* Log what happened */
        LinkedHashMap<String, String> unwrappedMathML = unwrapMathMLElement(mathElement);
        String asciiMathInput = MathMLUtilities.extractAnnotationString(mathElement, MathMLUpConverter.ASCIIMATH_INPUT_ANNOTATION_NAME);
        logger.info("Input: {}, Best output: {}",
                asciiMathInput!=null ? asciiMathInput : "(No annotation provided)",
                extractBestUpConversionResult(unwrappedMathML));
        
        /* Create and send JSON Object encapsulating result */
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("input", input);
        jsonObject.putAll(unwrappedMathML);
        sendJSONResponse(response, jsonObject);
    }
    
    private Document parsePOSTData(String input) throws IOException {
        Document asciiMathMLDocument = null;
        if (input.startsWith("<")) {
            /* The client probably sent MathML created by ASCIIMathML. Let's invoke XML parser to
             * check that it is really XML.
             */
            try {
                asciiMathMLDocument = XMLUtilities.createNSAwareDocumentBuilder().parse(new InputSource(new StringReader(input)));
            }
            catch (SAXException e) {
                /* Parsing failed, so continue... */
            }
        }
        if (asciiMathMLDocument==null) {
            /* Input wasn't XML, so assume it's ASCIIMath input */
            ASCIIMathParser asciiMathParser = getASCIMathParser();
            Map<String, Object> parsingOptions = new HashMap<String, Object>();
            parsingOptions.put(ASCIIMathParser.OPTION_ADD_SOURCE_ANNOTATION, Boolean.TRUE);
            parsingOptions.put(ASCIIMathParser.OPTION_DISPLAY_MODE, Boolean.TRUE);
            asciiMathMLDocument = asciiMathParser.parseASCIIMath(input, parsingOptions);
        }
        return asciiMathMLDocument;
    }
}