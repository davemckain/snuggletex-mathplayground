/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground.minimal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.ed.ph.commons.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptionsTemplates;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.SerializationOptions;

/**
 * Servlet demonstrating the up-conversion process on MathML generated
 * dynamically in the browser by ASCIIMathML.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class ASCIIMathMLInputDemoServlet extends HttpServlet {
    
    private static final long serialVersionUID = 2261754980279697343L;

    /** Logger so that we can log what users are trying out to allow us to improve things */
    private static Logger logger = LoggerFactory.getLogger(ASCIIMathMLInputDemoServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xhtml+xml");
        response.setCharacterEncoding("UTF-8");
        request.getRequestDispatcher("/WEB-INF/new-form.xml").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* Get the raw ASCIIMathML input and Presentation MathML created by the ASCIIMathML
         * JavaScript code.
         */
        String asciiMathInput = request.getParameter("asciiMathInput");
        String asciiMathOutput = request.getParameter("asciiMathML");
        if (asciiMathInput==null || asciiMathOutput==null) {
            logger.warn("Could not extract data from ASCIIMath: asciiMathInput={}, asciiMathOutput={}", asciiMathInput, asciiMathOutput);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not extract data passed by ASCIIMathML");
            return;
        }
        asciiMathOutput = asciiMathOutput.trim();
        
        /* Do up-conversion and extract wreckage */
        MathMLUpConverter upConverter = new MathMLUpConverter();
        SerializationOptions serializationOptions = new SerializationOptions();
        serializationOptions.setIndenting(true);
        serializationOptions.setUsingNamedEntities(true);
        Document upConvertedMathDocument = upConverter.upConvertASCIIMathML(asciiMathOutput, null);
        Element mathElement = upConvertedMathDocument.getDocumentElement(); /* NB: Document is <math/> here */
        String parallelMathML = MathMLUtilities.serializeElement(mathElement, serializationOptions);
        String pMathMLUpConverted = MathMLUtilities.serializeDocument(MathMLUtilities.isolateFirstSemanticsBranch(mathElement), serializationOptions);
        Document cMathMLDocument = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME);
        String cMathML = cMathMLDocument!=null ? MathMLUtilities.serializeDocument(cMathMLDocument, serializationOptions) : null;
        String maximaInput = MathMLUtilities.extractAnnotationString(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
        
        logger.info("ASCIIMathML Input: {}", asciiMathInput);
        logger.info("Raw ASCIIMathML Output: {}", asciiMathOutput);
        logger.info("Final parallel MathML: {}", parallelMathML);
        
        /* Generate output page */
        String pageTemplate = IOUtilities.readUnicodeStream(getServletContext().getResourceAsStream("/WEB-INF/result.xml"));
        pageTemplate = pageTemplate.replace("${ascii-math-input}", asciiMathInput)
            .replace("${parallel-mathml}", parallelMathML);
        
        /* ETC... */
        
        /* Generate final page using the same process as other demos, which is a bit
         * cheaty here but saves rewriting code. In this case, we'll pass an empty
         * input to SnuggleTeX and ignore the output it gives!
         */
        WebPageOutputOptions options = WebPageOutputOptionsTemplates.createWebPageOptions(WebPageType.CROSS_BROWSER_XHTML);
        options.setIndenting(true);
        options.setIncludingStyleElement(false);
        
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        session.parseInput(new SnuggleInput("", "Dummy Input"));
        
        /* Create XSLT to generate the resulting page */
        Transformer viewStylesheet = getStylesheet(request, DISPLAY_XSLT_LOCATION);
        viewStylesheet.setParameter("is-mathml-capable", isMathMLCapable(request));
        viewStylesheet.setParameter("is-internet-explorer", isInternetExplorer(request));
        viewStylesheet.setParameter("is-new-form", Boolean.valueOf(isNewForm));
        if (!isNewForm) {
            viewStylesheet.setParameter("ascii-math-input", asciiMathInput);
            viewStylesheet.setParameter("parallel-mathml-element", parallelMathMLElement);
            viewStylesheet.setParameter("parallel-mathml", parallelMathML);
            viewStylesheet.setParameter("pmathml-upconverted", pMathMLUpConverted);
            viewStylesheet.setParameter("cmathml", cMathML);
            viewStylesheet.setParameter("maxima-input", maximaInput);
        }
        options.setStylesheets(viewStylesheet);
        
        /* Generate and serve the resulting web page */
        try {
            session.writeWebPage(options, response, response.getOutputStream());
        }
        catch (Exception e) {
            throw new ServletException("Unexpected Exception", e);
        }
    }
    
    private String substitute(String string, String parameter, String value, boolean escapeXML) {
        if (escapeXML) {
            value = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
        return string.replace("${" + parameter + "}", value);
    }
}