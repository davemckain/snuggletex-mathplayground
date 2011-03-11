/* $Id$
 *
 * Copyright (c) 2011, University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.asciimath.parser.ASCIIMathParser;
import uk.ac.ed.ph.commons.util.IOUtilities;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This serves up the ASCIIMathParser.js script from the ASCIIMathParser JAR file.
 * (Yes, this is a bit of rotten way of doing it!)
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class ASCIIMathParserDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 6735829288698959024L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/javascript");
        response.setCharacterEncoding("UTF-8");
        IOUtilities.transfer(getClass().getClassLoader().getResourceAsStream(ASCIIMathParser.ASCIIMATH_PARSER_JS_LOCATION),
                response.getOutputStream(), false);
    }
}
