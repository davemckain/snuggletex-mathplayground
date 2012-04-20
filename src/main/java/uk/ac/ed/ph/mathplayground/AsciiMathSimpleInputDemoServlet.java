/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for the "Simple ASCIIMath input" demo
 * 
 * @author David McKain
 */
public final class AsciiMathSimpleInputDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4887360456110333274L;
    private static final Logger logger = LoggerFactory.getLogger(AsciiMathSimpleInputDemoServlet.class);
    
    public static final String DEFAULT_INPUT = "(-b pm sqrt(b^2-4ac))/(2a)";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        request.setAttribute("asciiMathInput", DEFAULT_INPUT);
        request.getRequestDispatcher("/WEB-INF/jsp/views/asciimath-simple-input-demo.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8"); /* (Browsers usually don't set this for us) */
        String asciiMathInput = request.getParameter("asciiMathInput");
        
        logger.info("ASCIIMathML Input: {}", asciiMathInput);
        request.setAttribute("asciiMathInput", asciiMathInput);
        request.getRequestDispatcher("/WEB-INF/jsp/views/asciimath-simple-input-demo.jsp").forward(request, response);
    }
}