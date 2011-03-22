/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.asciimath.parser.ASCIIMathParser;
import uk.ac.ed.ph.snuggletex.SerializationSpecifier;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptions;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.SerializationOptions;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Trivial base class for servlets in the demo webapp
 *
 * @author  David McKain
 * @version $Revision$
 */
abstract class BaseServlet extends HttpServlet {
    
    private static final long serialVersionUID = -2577813908466694931L;
    
    /**
     * Helper that reads in a resource from the webapp hierarchy, throwing a {@link ServletException}
     * if the resource could not be found.
     * 
     * @param resourcePathInsideWebpp path of Resource to load, relative to base of webapp.
     * @return resulting {@link InputStream}, which will not be null
     * @throws ServletException
     */
    protected InputStream ensureReadResource(String resourcePathInsideWebpp) throws ServletException {
        InputStream result = getServletContext().getResourceAsStream(resourcePathInsideWebpp);
        if (result==null) {
            throw new ServletException("Could not read in required web resource at " + resourcePathInsideWebpp);
        }
        return result;
    }
    
    protected StylesheetManager getStylesheetManager() {
        return (StylesheetManager) getServletContext().getAttribute(ContextInitialiser.STYLESHEET_MANAGER_ATTRIBUTE_NAME);
    }
    
    protected ASCIIMathParser getASCIMathParser() {
        return (ASCIIMathParser) getServletContext().getAttribute(ContextInitialiser.ASCIIMATH_PARSER_ATTRIBUTE_NAME);
    }
    
    protected SnuggleEngine getSnuggleEngine() {
        return (SnuggleEngine) getServletContext().getAttribute(ContextInitialiser.SNUGGLE_ENGINE_ATTRIBUTE_NAME);
    }
    
    protected UpConversionOptions getUpConversionOptions() {
        return (UpConversionOptions) getServletContext().getAttribute(ContextInitialiser.UPCONVERSION_OPTIONS_ATTRIBUTE_NAME);
    }
    
    //-----------------------------------------------------------------------
    
    protected void sendJSONResponse(HttpServletResponse response, JSONObject jsonObject) throws IOException {
        response.setContentType("text/json; charset=UTF-8");
        PrintWriter responseWriter = response.getWriter();
        responseWriter.append(jsonObject.toJSONString());
        responseWriter.flush();
    }
    
    //-----------------------------------------------------------------------
    
    protected LinkedHashMap<String, String> unwrapMathMLElement(Element mathElement) {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        
        /* These options are used to serialize MathML that might get used, i.e. no entities */
        SerializationSpecifier serOptions = new SerializationOptions();
        serOptions.setIndenting(true);
        
        /* These options are used to serialize MathML that will only be displayed as source */
        SerializationSpecifier sourceOptions = new SerializationOptions();
        sourceOptions.setIndenting(true);
        sourceOptions.setUsingNamedEntities(true);
        
        /* Isolate various annotations from the result */
        Document pmathSemanticDocument = MathMLUtilities.isolateFirstSemanticsBranch(mathElement);
        Document pmathBracketedDocument = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.BRACKETED_PRESENTATION_MATHML_ANNOTATION_NAME);
        Document cmathDocument = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME);
        String maximaAnnotation = MathMLUtilities.extractAnnotationString(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
        Document contentFailuresAnnotation = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.CONTENT_FAILURES_ANNOTATION_NAME);
        Document maximaFailuresAnnotation = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.MAXIMA_FAILURES_ANNOTATION_NAME);
        
        /* Build up result */
        maybeAddResult(result, "pmathParallel", mathElement, sourceOptions);
        maybeAddResult(result, "pmathSemantic", pmathSemanticDocument, sourceOptions);
        maybeAddResult(result, "pmathBracketed", pmathBracketedDocument, serOptions);
        maybeAddResult(result, "cmath", cmathDocument, sourceOptions);
        maybeAddResult(result, "maxima", maximaAnnotation);
        maybeAddResult(result, "cmathFailures", contentFailuresAnnotation, sourceOptions);
        maybeAddResult(result, "maximaFailures", maximaFailuresAnnotation, sourceOptions);
        return result;
    }
    
    protected String extractBestUpConversionResult(LinkedHashMap<String, String> unwrappedData) {
        String result;
        if (unwrappedData.containsKey("maxima")) {
            result = "[Maxima] " + unwrappedData.get("maxima");
        }
        else if (unwrappedData.containsKey("cmath")) {
            result = "[Content MathML] " + unwrappedData.get("cmath");
        }
        else {
            result = "[Semantic PMathML] " + unwrappedData.get("pmathSemantic"); 
        }
        return result;
    }
    
    private void maybeAddResult(Map<String, String> resultBuilder, String key, Document value, SerializationSpecifier serializationSpecifier) {
        if (value!=null) {
            resultBuilder.put(key, MathMLUtilities.serializeDocument(value, serializationSpecifier));
        }
    }
    
    private void maybeAddResult(Map<String, String> resultBuilder, String key, Element value, SerializationSpecifier serializationSpecifier) {
        if (value!=null) {
            resultBuilder.put(key, MathMLUtilities.serializeElement(value, serializationSpecifier));
        }
    }
    
    private void maybeAddResult(Map<String, String> resultBuilder, String key, String value) {
        if (value!=null) {
            resultBuilder.put(key, value);
        }
    }
}