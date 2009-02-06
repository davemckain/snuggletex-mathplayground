/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.snuggletex.SnuggleConstants;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.SnuggleEngine.DefaultStylesheetCache;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

/**
 * Fixes the raw MathML produced by Maxima output.
 * 
 * (This is a blatant rip-off of what's in SnuggleTeX!)
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class MaximaMathMLFixer {

    /** Explicit name of the SAXON 9.X TransformerFactoryImpl Class */
    private static final String SAXON_TRANSFORMER_FACTORY_CLASS_NAME = "net.sf.saxon.TransformerFactoryImpl";
    
    /** "Base" location for the XSLT stylesheets used here */
    private static final String BASE_LOCATION = "classpath:/uk/ac/ed/ph/mathplayground";
    
    /** Location of the initial XSLT for fixing up ASCIIMathML */
    private static final String FIXER_XSL_LOCATION = BASE_LOCATION + "/maxima-output-fixer.xsl";
    
    /** XSLT cache to use */
    private final StylesheetCache stylesheetCache;
    
    /**
     * Creates a new up-converter using a simple internal cache.
     * <p>
     * Use this constructor if you don't use XSLT yourself. In this case, you'll want your
     * instance of this class to be reused as much as possible to get the benefits of caching.
     */
    public MaximaMathMLFixer() {
        this(new DefaultStylesheetCache());
    }
    
    /**
     * Creates a new up-converter using the given {@link StylesheetCache} to cache internal XSLT
     * stylesheets.
     * <p>
     * Use this constructor if you do your own XSLT caching that you want to integrate in, or
     * if the default doesn't do what you want.
     */
    public MaximaMathMLFixer(final StylesheetCache stylesheetCache) {
        this.stylesheetCache = stylesheetCache;
    }
    
    public Document fixMaximaMathMLOutput(final Document document, final Map<String, Object> upconversionParameters) {
        Document resultDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        try {
            /* Create required XSLT */
            Templates upconverterStylesheet = getStylesheet(FIXER_XSL_LOCATION);
            Transformer upconverter = upconverterStylesheet.newTransformer();
            
            /* Set any specified parameters */
            if (upconversionParameters!=null) {
                for (Entry<String, Object> entry : upconversionParameters.entrySet()) {
                    /* (Recall that the actual stylesheets assume the parameters are in the SnuggleTeX
                     * namespace, so we need to use {uri}localName format for the parameter name.) */
                    upconverter.setParameter("{" + SnuggleConstants.SNUGGLETEX_NAMESPACE + "}" + entry.getKey(),
                            entry.getValue());
                }
            }
            
            /* Do the transform */
            upconverter.transform(new DOMSource(document), new DOMResult(resultDocument));
        }
        catch (TransformerException e) {
            throw new SnuggleRuntimeException("Fixing failed", e);
        }
        return resultDocument;
    }
    
    //---------------------------------------------------------------------
    // Internal helpers
    
    private TransformerFactory createSaxonTransformerFactory() {
        try {
            /* We call up SAXON explicitly without going through the usual factory path */
            return (TransformerFactory) Class.forName(SAXON_TRANSFORMER_FACTORY_CLASS_NAME).newInstance();
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Failed to explicitly instantiate SAXON "
                    + SAXON_TRANSFORMER_FACTORY_CLASS_NAME
                    + " class - check your ClassPath!", e);
        }
    }
    
    private Templates getStylesheet(String location) {
        Templates result;
        if (stylesheetCache==null) {
            result = compileStylesheet(location);
        }
        else {
            synchronized(stylesheetCache) {
                result = stylesheetCache.getStylesheet(location);
                if (result==null) {
                    result = compileStylesheet(location);
                    stylesheetCache.putStylesheet(location, result);
                }
            }
        }
        return result;
    }
    
    private Templates compileStylesheet(String location) {
        TransformerFactory transformerFactory = createSaxonTransformerFactory();
        return XMLUtilities.compileInternalStylesheet(transformerFactory, location);
    }
}
