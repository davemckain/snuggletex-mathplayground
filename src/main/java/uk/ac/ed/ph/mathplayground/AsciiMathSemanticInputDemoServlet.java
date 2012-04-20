/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.asciimath.parser.AsciiMathParser;
import uk.ac.ed.ph.asciimath.parser.AsciiMathParserOptions;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;

import java.io.IOException;
import java.util.LinkedHashMap;

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
 * @author David McKain
 */
public final class AsciiMathSemanticInputDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 2261754980279697343L;
    private static final Logger logger = LoggerFactory.getLogger(AsciiMathSemanticInputDemoServlet.class);
    
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
            logger.warn("No asciiMathInput request parameter specified");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameter asciiMathInput was not provided");
            return;
        }
        logger.info("ASCIIMathML Input: {}", asciiMathInput);
        request.setAttribute("asciiMathInput", asciiMathInput);

        /* Parse the incoming ASCIIMath */
        AsciiMathParser asciiMathParser = getAsciiMathParser();
        AsciiMathParserOptions asciiMathParserOptions = new AsciiMathParserOptions();
        asciiMathParserOptions.setAddSourceAnnotation(true);
        Document asciiMathMLDocument = asciiMathParser.parseAsciiMath(asciiMathInput, asciiMathParserOptions);
        
        /* Do up-conversion and extract wreckage */
        MathMLUpConverter upConverter = new MathMLUpConverter(getStylesheetManager());
        Document upConvertedMathDocument = upConverter.upConvertASCIIMathML(asciiMathMLDocument, getUpConversionOptions());
        Element mathElement = upConvertedMathDocument.getDocumentElement(); /* NB: Document is <math/> here */
        
        LinkedHashMap<String, String> unwrappedMathML = unwrapMathmlElement(mathElement);

        logger.info("Final parallel MathML: {}", unwrappedMathML.get("pmathParallel"));
        request.setAttribute("pmathParallel", unwrappedMathML.get("pmathParallel"));
        request.setAttribute("pmathSemantic", unwrappedMathML.get("pmathSemantic"));
        request.setAttribute("cmath", unwrappedMathML.get("cmath"));
        request.setAttribute("maxima", unwrappedMathML.get("maxima"));
        request.getRequestDispatcher("/WEB-INF/jsp/views/asciimath-semantic-input-demo-result.jsp").forward(request, response);
    }
}