/* $Id$
 *
 * Copyright (c) 2011, University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.asciimath.parser.AsciiMathParser;
import uk.ac.ed.ph.asciimath.parser.AsciiMathParserOptions;
import uk.ac.ed.ph.snuggletex.SerializationSpecifier;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.SerializationOptions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Simple front-end demo to {@link AsciiMathParser}
 * 
 * @author David McKain
 */
public final class ServerSideAsciiMathDemoServlet extends BaseServlet {

    private static final long serialVersionUID = -8361217845410879232L;
    private static final Logger logger = LoggerFactory.getLogger(ServerSideAsciiMathDemoServlet.class);
    
    public static final String DEFAULT_INPUT = "2(x-1)";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        request.setAttribute("latexMathInput", DEFAULT_INPUT);
        request.getRequestDispatcher("/WEB-INF/jsp/views/server-side-asciimath-demo.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* Get the raw ASCIIMathML input and Presentation MathML created by the ASCIIMathML
         * JavaScript code.
         */
        request.setCharacterEncoding("UTF-8"); /* (Browsers usually don't set this for us) */
        String asciiMathInput = request.getParameter("asciiMathInput");
        
        /* Call up server-side ASCIIMath parser */
        AsciiMathParser asciiMathParser = getAsciiMathParser();
        AsciiMathParserOptions options = new AsciiMathParserOptions();
        options.setDisplayMode(true);
        options.setAddSourceAnnotation(true);
        Document mathDocument = asciiMathParser.parseAsciiMath(asciiMathInput, options);
        SerializationSpecifier serializationOptions = new SerializationOptions();
        serializationOptions.setIncludingXMLDeclaration(false);
        serializationOptions.setIndenting(true);
        String mathmlOutput = MathMLUtilities.serializeDocument(mathDocument, serializationOptions);
        
        logger.info("ASCIIMathML Input: {}", asciiMathInput);
        logger.info("Resulting Output: {}", mathmlOutput);
        
        request.setAttribute("asciiMathInput", asciiMathInput);
        request.setAttribute("mathmlOutput", mathmlOutput);
        request.getRequestDispatcher("/WEB-INF/jsp/views/server-side-asciimath-demo.jsp").forward(request, response);
    }


}
