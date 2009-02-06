/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground.webapp;

import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import uk.ac.ed.ph.mathplayground.RawMaximaSession;
import uk.ac.ed.ph.snuggletex.extensions.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

/**
 * Trivial base class for servlets in the demo webapp
 *
 * @author  David McKain
 * @version $Revision$
 */
abstract class BaseServlet extends HttpServlet {
    
    private Logger log = Logger.getLogger(ASCIIMathInputServlet.class);
    
    private static final long serialVersionUID = -2577813908466694931L;
    
    /** Stylesheet to up-convert the raw SnuggleTeX output into various other things */
    public static final String UPCONVERTER_XSLT_LOCATION = "/WEB-INF/snuggletex-upconverter.xsl";
    
    @Override
    public void init() throws ServletException {
        /* TODO: These need to be parametrised properly.. */
        System.setProperty(RawMaximaSession.MAXIMA_EXECUTABLE_PATH_PROPERTY, "/opt/local/bin/maxima");
        System.setProperty(RawMaximaSession.MAXIMA_TIMEOUT_PROPERTY, "10");
        System.setProperty(RawMaximaSession.MAXIMA_ENVIRONMENT_PROPERTY_BASE + "0", "PATH=/usr/bin:/opt/local/bin");
    }

    /**
     * Helper that reads in a resource from the webapp hierarchy, throwing a {@link ServletException}
     * if the resource could not be found.
     * 
     * @param resourcePathInsideWebpp path of Resource to load, relative to base of webapp.
     * @return resulting {@link InputStream}, which will not be null
     * @throws ServletException
     */
    protected InputStream ensureReadResource(String resourcePathInsideWebpp) throws ServletException {
        InputStream result = getServletContext().getResourceAsStream(resourcePathInsideWebpp);
        if (result==null) {
            throw new ServletException("Could not read in required web resource at " + resourcePathInsideWebpp);
        }
        return result;
    }
    
    protected TransformerFactory createTransformerFactory() {
        TransformerFactory transformerFactory = XMLUtilities.createTransformerFactory();
        
        /* Create a cheap URIResolver to help with xsl:import and friends. */
        transformerFactory.setURIResolver(new CheapoURIResolver());
        return transformerFactory;
    }
    
    /**
     * Compiles the XSLT stylesheet at the given location within the webapp. This uses a
     * {@link CheapoURIResolver} to resolve any other stylesheets referenced using
     * <tt>xsl:import</tt> and friends.
     * @param transformerFactory
     * @param xsltPathInsideWebapp location of XSLT to compile.
     * 
     * @return resulting {@link Templates} representing the compiled stylesheet.
     * @throws ServletException if XSLT could not be found or could not be compiled.
     */
    protected Templates compileStylesheet(TransformerFactory transformerFactory, String xsltPathInsideWebapp) throws ServletException {
        StreamSource xsltSource = new StreamSource(ensureReadResource(xsltPathInsideWebapp), xsltPathInsideWebapp);
        
        /* Then compile the XSLT */
        try {
            return transformerFactory.newTemplates(xsltSource);
        }
        catch (TransformerConfigurationException e) {
            throw new ServletException("Could not compile stylesheet at " + xsltPathInsideWebapp);
        }
    }

    protected Transformer createSerializer(TransformerFactory factory) throws TransformerConfigurationException {
        Transformer serializer = factory.newTransformer();
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII");
        return serializer;
    }

    /**
     * Trivial implementation of {@link URIResolver} that assumes that all lookups will be
     * of relative URLs with no '/' or '..' in them.
     * <p>
     * These will be resolved relative to the WEB-INF directory of the webapp.
     * <p>
     * This is quite a poor state of affairs in general but is all we need here!
     */
    protected final class CheapoURIResolver implements URIResolver {
        
        public Source resolve(String href, String base) throws TransformerException {
            InputStream resource = getServletContext().getResourceAsStream("/WEB-INF/" + href);
            if (resource==null) {
                throw new TransformerException("Could not resolve resource at href=" + href
                        + " using cheap resolver");
            }
            return new StreamSource(resource);
        }
    }
    
    protected abstract Document callUpconversionMethod(MathMLUpConverter upconverter, Document pmathmlDocument);
    
    /**
     * Up-converts the given (SnuggleTeX-standard) PMathML in various way. See method for details!
     */
    protected String[] upconvertMathML(Document pmathmlDocument) {
        /* Up-convert the PMathML, letting subclass pick appropriate method */
        MathMLUpConverter upconverter = new MathMLUpConverter();
        Document upconvertedDocument = callUpconversionMethod(upconverter, pmathmlDocument);
        
        /* Serialise result for geeks */
        String upconverted = MathMLUtilities.serializeDocument(upconvertedDocument);
        
        /* Extract Maxima annotation (if available) */
        String maximaAnnotation = MathMLUtilities.extractAnnotationString(upconvertedDocument.getDocumentElement(),
                MathMLUpConverter.MAXIMA_ANNOTATION_NAME);

        /* Do maxima stuff */
        String maximaInput;
        String maximaOutput;
        if (maximaAnnotation==null || maximaAnnotation.length()==0) {
            maximaInput = "(Failed conversion to intermediate Content MathML)";
            maximaOutput = "(N/A)";
        }
        else {
            /* Pass to Maxima */
            RawMaximaSession maximaSession = new RawMaximaSession();
            maximaInput = maximaAnnotation;
            try {
                maximaSession.open();
                maximaOutput = maximaSession.executeRaw(maximaInput + ";");
                maximaSession.close();
            }
            catch (Exception e) {
                log.warn("Exception speaking to Maxima", e);
                maximaOutput = "Exception occurred speaking to Maxima: " + e.toString();
            }
        }
        
        /* Return results */
        return new String[] { upconverted, maximaInput, maximaOutput };
    }
}
