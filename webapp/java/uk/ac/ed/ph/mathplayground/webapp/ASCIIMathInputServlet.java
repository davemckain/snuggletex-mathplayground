/* $Id:TryOutServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground.webapp;

import uk.ac.ed.ph.commons.util.StringUtilities;
import uk.ac.ed.ph.mathplayground.RawMaximaSession;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

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
    private static final String XSLT_LOCATION = "/WEB-INF/asciimath.xsl";
    
    public static final String FIXER_LOCATION = "/WEB-INF/asciimathml-fixer.xsl";
    public static final String PMATHML_ENHANCER_LOCATION = "/WEB-INF/pmathml-enhancer.xsl";
    public static final String PMATHML_TO_CMATHML_LOCATION = "/WEB-INF/pmathml-to-cmathml.xsl";
    public static final String CMATHML_TO_MAXIMA_LOCATION = "/WEB-INF/cmathml-to-maxima.xsl";

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
        TransformerFactory transformerFactory = createTransformerFactory();
        
        /* Get inputs, if appropriate */
        String asciiMathInput = request.getParameter("asciimathinput");
        String pmathmlRaw = request.getParameter("mathml");
        String[] resultArray = null;
        if (!StringUtilities.isNullOrEmpty(asciiMathInput) && !StringUtilities.isNullOrEmpty(pmathmlRaw)) {
            /* Something non-trivial was input, so process the raw MathML */
            pmathmlRaw = pmathmlRaw.trim();
            
            /* Fix the raw MathML */
            try {
                resultArray = processMathML(transformerFactory, pmathmlRaw);
            }
            catch (TransformerException e) {
                throw new ServletException(e);
            }
            
            log.info("Success Result:\nInput: " + asciiMathInput
                    + "\nPMathML Raw: " + pmathmlRaw
                    + "\nPMathML Fixed: " + resultArray[0]
                    + "\nPMathML Enhanced: " + resultArray[1]
                    + "\nCMathML: " + resultArray[2]
                    + "\nMaxima Input: " + resultArray[3]
                    + "\nMaxima Output: " + resultArray[4]
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
        Transformer viewStylesheet;
        try {
            viewStylesheet = compileStylesheet(transformerFactory, XSLT_LOCATION).newTransformer();
            viewStylesheet.setParameter("context-path", request.getContextPath());
            viewStylesheet.setParameter("ascii-input", asciiMathInput);
            viewStylesheet.setParameter("pmathml-raw", pmathmlRaw);
            if (resultArray!=null) {
                viewStylesheet.setParameter("pmathml-fixed", resultArray[0]);
                viewStylesheet.setParameter("pmathml-enhanced", resultArray[1]);
                viewStylesheet.setParameter("cmathml", resultArray[2]);
                viewStylesheet.setParameter("maxima-input", resultArray[3]);
                viewStylesheet.setParameter("maxima-output", resultArray[4]);
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
    
    private String[] processMathML(TransformerFactory transformerFactory, String pmathmlRaw)
            throws TransformerException, ServletException {
        /* Fix up the raw MathML */
        DocumentBuilder documentBuilder = XMLUtilities.createNSAwareDocumentBuilder();
        Document fixedDocument = documentBuilder.newDocument();
        Transformer fixerStylesheet = compileStylesheet(transformerFactory, FIXER_LOCATION).newTransformer();
        fixerStylesheet.transform(new StreamSource(new StringReader(pmathmlRaw)), new DOMResult(fixedDocument));
        
        /* Enhance the PMathML */
        Document epmathmlDocument = documentBuilder.newDocument();
        Transformer enhancerStylesheet = compileStylesheet(transformerFactory, PMATHML_ENHANCER_LOCATION).newTransformer();
        enhancerStylesheet.transform(new DOMSource(fixedDocument, "urn:fixed"), new DOMResult(epmathmlDocument));
        
        /* Then try to convert the enhanced PMathML to Content MathML */
        Document cmathmlDocument = documentBuilder.newDocument();
        Transformer ptocStylesheet = compileStylesheet(transformerFactory, PMATHML_TO_CMATHML_LOCATION).newTransformer();
        ptocStylesheet.transform(new DOMSource(epmathmlDocument, "urn:epmathml"), new DOMResult(cmathmlDocument));
        
        /* Serialise intermediate results for geeks */
        StringWriter fixedWriter = new StringWriter();
        StringWriter epmathmlWriter = new StringWriter();
        StringWriter cmathmlWriter = new StringWriter();
        createSerializer(transformerFactory).transform(new DOMSource(fixedDocument, "urn:fixed"), new StreamResult(fixedWriter));
        createSerializer(transformerFactory).transform(new DOMSource(epmathmlDocument, "urn:epmathml"), new StreamResult(epmathmlWriter));
        createSerializer(transformerFactory).transform(new DOMSource(cmathmlDocument, "urn:cmathml"), new StreamResult(cmathmlWriter));
        String fixed = fixedWriter.toString();
        String epmathml = epmathmlWriter.toString();
        String cmathml = cmathmlWriter.toString();
        
        /* Hunt out any failure annotation.
         * TODO: Should use XPath for this!
         */
        String maximaInput;
        String maximaOutput;
        if (cmathml.indexOf("Presentation-to-Content-MathML-failure")!=-1) {
            maximaInput = "(Failed conversion to intermediate Content MathML)";
            maximaOutput = "(N/A)";
        }
        else {
            /* Convert Content MathML to Maxima Input */
            StringWriter maximaWriter = new StringWriter();
            Transformer ctoMaximaStylesheet = compileStylesheet(transformerFactory, CMATHML_TO_MAXIMA_LOCATION).newTransformer();
            ctoMaximaStylesheet.transform(new DOMSource(cmathmlDocument), new StreamResult(maximaWriter));
            maximaInput = maximaWriter.toString();
            
            /* Now pass to Maxima */
            RawMaximaSession maximaSession = new RawMaximaSession();
            try {
                maximaSession.open();
                maximaOutput = maximaSession.executeRaw(maximaInput + ";");
                maximaSession.close();
            }
            catch (Exception e) {
                maximaOutput = "Exception occurred speaking to Maxima: " + e.toString();
            }
        }
        
        /* Return results */
        return new String[] { fixed, epmathml, cmathml, maximaInput, maximaOutput };
    }
}

