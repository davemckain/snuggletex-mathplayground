/* $Id:TryOutServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground.webapp;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.extensions.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Trivial servlet demonstrating the conversion of LaTeX input into other forms.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class LaTeXInputServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4376587500238353176L;
    
    /** Logger so that we can log what users are trying out to allow us to improve things */
    private Logger log = Logger.getLogger(LaTeXInputServlet.class);
    
    /** Location of XSLT controlling page layout */
    private static final String DISPLAY_XSLT_LOCATION = "/WEB-INF/latexinput.xsl";
    
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
        /* Read in input LaTeX, using some placeholder text if nothing was provided */
        String rawInputLaTeX = request.getParameter("input");
        String resultingInputLaTeX;
        if (rawInputLaTeX!=null) {
            /* Tidy up line endings */
            resultingInputLaTeX = rawInputLaTeX.replaceAll("(\r\n|\r|\n)", "\n");
        }
        else {
            /* Use some default input */
            resultingInputLaTeX = "1+x";
        }
        
        /* Parse the LaTeX */
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        SnuggleInput input = new SnuggleInput("\\[ " + resultingInputLaTeX + " \\]", "Form Input");
        session.parseInput(input);
        
        /* Set up web output options */
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
        
        /* Get text results */
        String[] resultArray;
        TransformerFactory transformerFactory = createTransformerFactory();
        try {
            resultArray = pipelineMathML(transformerFactory, session, options);
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
        
        /* Log what was done */
        if (resultArray!=null) {
            log.info("Success Result:\nInput: " + resultingInputLaTeX
                    + "\nMathML: " + resultArray[0]
                    + "\nMaxima Input: " + resultArray[1]
                    + "\nMaxima Output: " + resultArray[2]
                    + "\n======================================");
        }
        else {
            log.warn("Failed on input " + resultingInputLaTeX);
        }
        
        /* Create XSLT to generate the resulting page */
        Transformer viewStylesheet;
        try {
            viewStylesheet = compileStylesheet(transformerFactory, DISPLAY_XSLT_LOCATION).newTransformer();
            viewStylesheet.setParameter("context-path", request.getContextPath());
            viewStylesheet.setParameter("latex-input", resultingInputLaTeX);
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
    
    private String[] pipelineMathML(TransformerFactory transformerFactory, SnuggleSession session, DOMOutputOptions options)
            throws TransformerException {
        /* Create MathML doc with temp fake root */
        DocumentBuilder documentBuilder = XMLUtilities.createNSAwareDocumentBuilder();
        Document pmathmlDocument = documentBuilder.newDocument();
        Element temporaryRoot = pmathmlDocument.createElementNS(Globals.MATHML_NAMESPACE, "temp");
        pmathmlDocument.appendChild(temporaryRoot);
        
        /* Build document content using SnuggleTeX */
        session.buildDOMSubtree(temporaryRoot, options);
        
        /* See if there were any errors */
        List<InputError> errors = session.getErrors();
        if (!errors.isEmpty()) {
            return null;
        }
        
        /* If no errors, make sure we got a single <math/> element */
        NodeList nodeList = temporaryRoot.getChildNodes();
        if (nodeList.getLength()!=1) {
            throw new RuntimeException("Expected SnuggleTeX output to be a single <math/> element");
        }
        Node resultNode = nodeList.item(0);
        if (resultNode.getNodeType()!=Node.ELEMENT_NODE && !resultNode.getLocalName().equals("math")) {
            throw new RuntimeException("Expected SnuggleTeX output to be a single <math/> element");
        }
        
        /* Make the <math/> element the new document root */
        Element mathmlElement = (Element) resultNode;
        pmathmlDocument.removeChild(temporaryRoot);
        pmathmlDocument.appendChild(mathmlElement);
        
        return upconvertMathML(transformerFactory, pmathmlDocument);
    }
    
    @Override
    protected Document callUpconversionMethod(MathMLUpConverter upconverter,
            Document pmathmlDocument) {
        return upconverter.upConvertSnuggleTeXMathML(pmathmlDocument, null);
    }
}

