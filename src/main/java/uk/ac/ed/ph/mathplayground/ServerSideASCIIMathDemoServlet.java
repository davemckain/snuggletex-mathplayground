/* $Id$
 *
 * Copyright (c) 2011, University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

/**
 * FIXME: Document this type!
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class ServerSideASCIIMathDemoServlet extends BaseServlet {

    private static final long serialVersionUID = -8361217845410879232L;
    
    private static Logger logger = LoggerFactory.getLogger(ServerSideASCIIMathDemoServlet.class);
    
    public static final String ASCIIMATH_PARSER_JS_LOCATION = "/includes/ASCIIMathParser.js";
    
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
        Reader scriptReader = new InputStreamReader(ensureReadResource(ASCIIMATH_PARSER_JS_LOCATION), "UTF-8");
        ASCIIMathParser asciiMathParser = new ASCIIMathParser(scriptReader);
        
        Map<String,Object> options = new HashMap<String,Object>();
        options.put("displayMode", Boolean.TRUE);
        options.put("addSourceAnnotation", Boolean.TRUE);
        Element mathElement = asciiMathParser.parseASCIIMath(asciiMathInput, options);
        String mathMLOutput = MathMLUtilities.serializeElement(mathElement, true);
        
        logger.info("ASCIIMathML Input: {}", asciiMathInput);
        logger.info("Resulting Output: {}", mathMLOutput);
        
        request.setAttribute("asciiMathInput", asciiMathInput);
        request.setAttribute("mathMLOutput", mathMLOutput);
        request.getRequestDispatcher("/WEB-INF/jsp/views/server-side-asciimath-demo.jsp").forward(request, response);
    }


}
