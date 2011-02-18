/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.snuggletex.SerializationSpecifier;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptionDefinitions;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptions;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.io.IOException;

import javax.servlet.ServletException;
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
public final class ASCIIMathInputDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 2261754980279697343L;

    /** Logger so that we can log what users are trying out to allow us to improve things */
    private static Logger logger = LoggerFactory.getLogger(ASCIIMathInputDemoServlet.class);
    
    public static final String DEFAULT_INPUT = "2(x-1)";
    
    /** Generates initial input form with some demo JavaScript. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        request.setAttribute("asciiMathInput", DEFAULT_INPUT);
        request.getRequestDispatcher("/WEB-INF/jsp/views/asciimath-input-demo.jsp").forward(request, response);
    }
    
    /** Handles the posted raw input & PMathML extracted from ASCIIMathML. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* Get the raw ASCIIMathML input and Presentation MathML created by the ASCIIMathML
         * JavaScript code.
         */
        String asciiMathInput = request.getParameter("asciiMathInput");
        String asciiMathOutput = request.getParameter("asciiMathOutput");
        
        if (asciiMathInput==null || asciiMathOutput==null) {
            logger.warn("Could not extract data from ASCIIMath: asciiMathInput={}, asciiMathOutput={}", asciiMathInput, asciiMathOutput);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not extract data passed by ASCIIMathML");
            return;
        }
        asciiMathOutput = asciiMathOutput.trim();
        
        /* Do up-conversion and extract wreckage */
        MathMLUpConverter upConverter = new MathMLUpConverter(getStylesheetManager());
        UpConversionOptions upConversionOptions = (UpConversionOptions) getUpConversionOptions().clone();
        upConversionOptions.setSpecifiedOption(UpConversionOptionDefinitions.ADD_OPTIONS_ANNOTATION_NAME, "true");
        Document upConvertedMathDocument = upConverter.upConvertASCIIMathML(asciiMathOutput, upConversionOptions);
        Element mathElement = upConvertedMathDocument.getDocumentElement(); /* NB: Document is <math/> here */
        SerializationSpecifier sourceSerializationOptions = createMathMLSourceSerializationOptions();
        String parallelMathML = MathMLUtilities.serializeElement(mathElement, sourceSerializationOptions);
        String pMathMLUpConverted = MathMLUtilities.serializeDocument(MathMLUtilities.isolateFirstSemanticsBranch(mathElement), sourceSerializationOptions);
        Document cMathMLDocument = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME);
        String cMathML = cMathMLDocument!=null ? MathMLUtilities.serializeDocument(cMathMLDocument, sourceSerializationOptions) : null;
        String maximaInput = MathMLUtilities.extractAnnotationString(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
        
        logger.info("ASCIIMathML Input: {}", asciiMathInput);
        logger.info("Raw ASCIIMathML Output: {}", asciiMathOutput);
        logger.info("Final parallel MathML: {}", parallelMathML);
        
        request.setAttribute("asciiMathInput", asciiMathInput);
        request.setAttribute("asciiMathOutput", asciiMathOutput);
        request.setAttribute("parallelMathML", parallelMathML);
        request.setAttribute("pMathML", pMathMLUpConverted);
        request.setAttribute("cMathML", cMathML);
        request.setAttribute("maxima", maximaInput);
        
        request.getRequestDispatcher("/WEB-INF/jsp/views/asciimath-input-demo-result.jsp").forward(request, response);
    }
}