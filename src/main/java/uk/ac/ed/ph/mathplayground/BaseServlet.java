/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.snuggletex.SerializationSpecifier;
import uk.ac.ed.ph.snuggletex.utilities.SaxonTransformerFactoryChooser;
import uk.ac.ed.ph.snuggletex.utilities.SerializationOptions;
import uk.ac.ed.ph.snuggletex.utilities.SimpleStylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Trivial base class for servlets in the demo webapp
 *
 * @author  David McKain
 * @version $Revision$
 */
abstract class BaseServlet extends HttpServlet {
    
    private static final long serialVersionUID = -2577813908466694931L;
    
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
    
    protected StylesheetManager getStylesheetManager() {
        StylesheetManager stylesheetManager = new StylesheetManager();
        stylesheetManager.setStylesheetCache(new SimpleStylesheetCache());
        stylesheetManager.setTransformerFactoryChooser(SaxonTransformerFactoryChooser.getInstance());

        return stylesheetManager;
    }
    
    protected SerializationSpecifier createMathMLSourceSerializationOptions() {
        SerializationSpecifier result = new SerializationOptions();
        result.setIndenting(true);
        result.setUsingNamedEntities(true);
        return result;
    }
}