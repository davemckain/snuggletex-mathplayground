/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.SerializationSpecifier;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptionDefinitions;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptions;
import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

/**
 * Version of {@link ASCIIMathInputDemoServlet} that uses SnuggleTeX input instead.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class SnuggleTeXInputDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 2261754980279697343L;

    /** Logger so that we can log what users are trying out to allow us to improve things */
    private static Logger logger = LoggerFactory.getLogger(SnuggleTeXInputDemoServlet.class);
    
    public static final String DEFAULT_INPUT = "2(x-1)";
    
    /** Generates initial input form with some demo JavaScript. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        request.setAttribute("latexInput", DEFAULT_INPUT);
        request.getRequestDispatcher("/WEB-INF/jsp/views/snuggletex-input-demo.jsp").forward(request, response);
    }
    
    /** Handles the posted raw input & PMathML extracted from ASCIIMathML. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String latexInput = request.getParameter("latexInput");
        
        if (latexInput==null) {
            logger.warn("No latexInput parameter present");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        /* Parse LaTeX */
        SnuggleEngine engine = new SnuggleEngine(getStylesheetManager());
        SnuggleSession session = engine.createSession();
        session.parseInput(new SnuggleInput("$" + latexInput + "$"));

        /* Register Up-converted */
        UpConversionOptions upConversionOptions = (UpConversionOptions) getUpConversionOptions().clone();
        upConversionOptions.setSpecifiedOption(UpConversionOptionDefinitions.ADD_OPTIONS_ANNOTATION_NAME, "true");
        UpConvertingPostProcessor postProcessor = new UpConvertingPostProcessor(upConversionOptions);
        DOMOutputOptions domOptions = new DOMOutputOptions();
        domOptions.setDOMPostProcessors(postProcessor);
        
        /* Build DOM */
        NodeList nodeList = session.buildDOMSubtree(domOptions);
        Element mathmlElement = (Element) nodeList.item(0);
        
        SerializationSpecifier sourceSerializationOptions = createMathMLSourceSerializationOptions();
        String parallelMathML = MathMLUtilities.serializeElement(mathmlElement, sourceSerializationOptions);
        String pMathMLUpConverted = MathMLUtilities.serializeDocument(MathMLUtilities.isolateFirstSemanticsBranch(mathmlElement), sourceSerializationOptions);
        Document cMathMLDocument = MathMLUtilities.isolateAnnotationXML(mathmlElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME);
        String cMathML = cMathMLDocument!=null ? MathMLUtilities.serializeDocument(cMathMLDocument, sourceSerializationOptions) : null;
        String maximaInput = MathMLUtilities.extractAnnotationString(mathmlElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
        
        logger.info("LaTeX Input: {}", latexInput);
        logger.info("Final parallel MathML: {}", parallelMathML);
        
        request.setAttribute("latexInput", latexInput);
        request.setAttribute("parallelMathML", parallelMathML);
        request.setAttribute("pMathML", pMathMLUpConverted);
        request.setAttribute("cMathML", cMathML);
        request.setAttribute("maxima", maximaInput);
        
        request.getRequestDispatcher("/WEB-INF/jsp/views/snuggletex-input-demo-result.jsp").forward(request, response);
    }
}