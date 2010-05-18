/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.commons.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.SerializationOptions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
            throws IOException {
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
        
        /* Do up-conversion */
        MathMLUpConverter upConverter = new MathMLUpConverter();
        SerializationOptions serializationOptions = new SerializationOptions();
        serializationOptions.setIndenting(true);
        serializationOptions.setUsingNamedEntities(true);
        Document upConvertedMathDocument = upConverter.upConvertASCIIMathML(asciiMathOutput, null);
        
        /* Extract results to display */
        Element mathElement = upConvertedMathDocument.getDocumentElement(); /* NB: Document is <math/> here */
        String parallelMathML = MathMLUtilities.serializeElement(mathElement, serializationOptions);
        String pMathMLDisplay = MathMLUtilities.serializeDocument(MathMLUtilities.isolateFirstSemanticsBranch(mathElement), serializationOptions);
        Document cMathMLDocument = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME);
        String cMathMLDisplay = cMathMLDocument!=null ? MathMLUtilities.serializeDocument(cMathMLDocument, serializationOptions) : "(Input could not be converted to Content MathML)";
        String maxima = MathMLUtilities.extractAnnotationString(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
        String maximaDisplay = maxima!=null ? maxima : "(Input could not be converted to Maxima form)";
        
        logger.info("ASCIIMathML Input: {}", asciiMathInput);
        logger.info("Raw ASCIIMathML Output: {}", asciiMathOutput);
        logger.info("Final parallel MathML: {}", parallelMathML);
        
        /* Generate output page */
        String pageTemplate = IOUtilities.readUnicodeStream(getServletContext().getResourceAsStream("/WEB-INF/result.xml"));
        pageTemplate = pageTemplate.replace("${asciiMathInput}", asciiMathInput)
            .replace("${pMathMLDisplay}", pMathMLDisplay)
            .replace("${cMathMLDisplay}", cMathMLDisplay)
            .replace("${maximaDisplay}", maximaDisplay)
            .replace("${parallelMathML}", parallelMathML)
            ;
        
        response.setContentType("application/xhtml+xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(pageTemplate);
        response.getWriter().flush();
    }
}