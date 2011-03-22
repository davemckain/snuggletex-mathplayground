/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.asciimath.parser.ASCIIMathParser;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
public final class ASCIIMathSemanticInputDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 2261754980279697343L;

    /** Logger so that we can log what users are trying out to allow us to improve things */
    private static Logger logger = LoggerFactory.getLogger(ASCIIMathSemanticInputDemoServlet.class);
    
    public static final String DEFAULT_INPUT = "2(x-1)";
    
    /** Generates initial input form with some demo JavaScript. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        request.setAttribute("asciiMathInput", DEFAULT_INPUT);
        request.getRequestDispatcher("/WEB-INF/jsp/views/asciimath-semantic-input-demo.jsp").forward(request, response);
    }
    
    /** Handles the posted raw input & PMathML extracted from ASCIIMathML. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* Get the raw ASCIIMathML input sent from the form */
        request.setCharacterEncoding("UTF-8"); /* (Browsers usually don't set this for us) */
        String asciiMathInput = request.getParameter("asciiMathInput");
        if (asciiMathInput==null) {
            logger.warn("Could not extract data from ASCIIMath: asciiMathInput={}", asciiMathInput);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameter asciiMathInput was not provided");
            return;
        }
        logger.info("ASCIIMathML Input: {}", asciiMathInput);
        request.setAttribute("asciiMathInput", asciiMathInput);

        /* Parse the incoming ASCIIMath */
        ASCIIMathParser asciiMathParser = getASCIMathParser();
        Map<String, Object> parsingOptions = new HashMap<String, Object>();
        parsingOptions.put(ASCIIMathParser.OPTION_ADD_SOURCE_ANNOTATION, Boolean.TRUE);
        Document asciiMathMLDocument = asciiMathParser.parseASCIIMath(asciiMathInput, parsingOptions);
        
        /* Do up-conversion and extract wreckage */
        MathMLUpConverter upConverter = new MathMLUpConverter(getStylesheetManager());
        Document upConvertedMathDocument = upConverter.upConvertASCIIMathML(asciiMathMLDocument, getUpConversionOptions());
        Element mathElement = upConvertedMathDocument.getDocumentElement(); /* NB: Document is <math/> here */
        
        LinkedHashMap<String, String> unwrappedMathML = unwrapMathMLElement(mathElement);

        logger.info("Final parallel MathML: {}", unwrappedMathML.get("pmathParallel"));
        request.setAttribute("pmathParallel", unwrappedMathML.get("pmathParallel"));
        request.setAttribute("pmathSemantic", unwrappedMathML.get("pmathSemantic"));
        request.setAttribute("cmath", unwrappedMathML.get("cmath"));
        request.setAttribute("maxima", unwrappedMathML.get("maxima"));
        request.getRequestDispatcher("/WEB-INF/jsp/views/asciimath-semantic-input-demo-result.jsp").forward(request, response);
    }
}