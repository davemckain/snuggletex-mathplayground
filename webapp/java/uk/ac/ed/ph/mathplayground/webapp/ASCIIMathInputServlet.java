/* $Id:TryOutServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground.webapp;

import uk.ac.ed.ph.commons.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.extensions.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Trivial servlet demonstrating the use of ASCIIMathML for submitting wodges of MathML.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class ASCIIMathInputServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4376587500238353176L;
    
    /** Logger so that we can log what users are trying out to allow us to improve things */
    private Logger log = Logger.getLogger(ASCIIMathInputServlet.class);
    
    /** Location of XSLT controlling page layout */
    private static final String DISPLAY_XSLT_LOCATION = "/WEB-INF/asciimath.xsl";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        doRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response);
    }

    private void doRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* Get inputs, if appropriate */
        String asciiMathInput = request.getParameter("asciimathinput");
        String pmathmlRaw = request.getParameter("mathml");
        String[] resultArray = null;
        if (!StringUtilities.isNullOrEmpty(asciiMathInput) && !StringUtilities.isNullOrEmpty(pmathmlRaw)) {
            /* Something non-trivial was input, so process the raw MathML */
            pmathmlRaw = pmathmlRaw.trim();
            
            /* Fix the raw MathML */
            try {
                resultArray = processMathML(pmathmlRaw);
            }
            catch (Exception e) {
                throw new ServletException(e);
            }
            log.info("Success Result:\nInput: " + asciiMathInput
                    + "\nPMathML Raw: " + pmathmlRaw
                    + "\nMathML Fixed: " + resultArray[0]
                    + "\nMaxima Input: " + resultArray[1]
                    + "\nMaxima Output: " + resultArray[2]
                    + "\n======================================");
        }
        
        /* We'll cheat slightly and use SnuggleTeX to generate the resulting page */
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        session.parseInput(new SnuggleInput(""));
        
        MathMLWebPageOptions options = new MathMLWebPageOptions();
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setPageType(WebPageType.CROSS_BROWSER_XHTML);
        options.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        options.setTitle("LaTeX to MathML and Maxima");
        options.setAddingTitleHeading(false); /* We'll put our own title in */
        options.setIndenting(true);
        options.setCSSStylesheetURLs(
                request.getContextPath() + "/includes/physics.css"
        );
        
        /* Create XSLT to generate the resulting page */
        TransformerFactory transformerFactory = createTransformerFactory();
        Transformer viewStylesheet;
        try {
            viewStylesheet = compileStylesheet(transformerFactory, DISPLAY_XSLT_LOCATION).newTransformer();
            viewStylesheet.setParameter("context-path", request.getContextPath());
            viewStylesheet.setParameter("ascii-input", asciiMathInput);
            viewStylesheet.setParameter("pmathml-raw", pmathmlRaw);
            if (resultArray!=null) {
                viewStylesheet.setParameter("mathml", resultArray[0]);
                viewStylesheet.setParameter("maxima-input", resultArray[1]);
                viewStylesheet.setParameter("maxima-output", resultArray[2]);
            }
        }
        catch (TransformerConfigurationException e) {
            throw new ServletException("Could not create stylesheet from Templates", e);
        }
        options.setStylesheet(viewStylesheet);
        
        /* Generate and serve the resulting web page */
        try {
            session.writeWebPage(options, response, response.getOutputStream());
        }
        catch (Exception e) {
            throw new ServletException("Unexpected Exception", e);
        }
    }
    
    private String[] processMathML(String pmathmlRaw)
            throws IOException, SAXException {
        /* Parse the raw MathML */
        Document rawDocument = MathMLUtilities.parseMathMLDocumentString(pmathmlRaw);
        return upconvertMathML(rawDocument);
    }
    
    @Override
    protected Document callUpconversionMethod(MathMLUpConverter upconverter, Document pmathmlDocument) {
        return upconverter.upConvertASCIIMathML(pmathmlDocument, null);
    }
}

