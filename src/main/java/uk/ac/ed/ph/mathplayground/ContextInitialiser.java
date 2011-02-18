/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptions;
import uk.ac.ed.ph.snuggletex.utilities.SaxonTransformerFactoryChooser;
import uk.ac.ed.ph.snuggletex.utilities.SimpleStylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Fairly typical {@link ServletContextListener} that just sets up a few shared resources
 * and sticks them in the {@link ServletContext} for access by servlets.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class ContextInitialiser implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ContextInitialiser.class);
    
    public static final String STYLESHEET_MANAGER_ATTRIBUTE_NAME = "stylesheetManager";
    public static final String UPCONVERSION_OPTIONS_ATTRIBUTE_NAME = "upconversionOptions";
    
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        
        /* Create and store StylesheetManager, hard-coded to use Saxon with caching
         * turned on as we're loading via the ClassPath so there's no point trying to turn
         * it off. */
        StylesheetManager stylesheetManager = new StylesheetManager();
        stylesheetManager.setStylesheetCache(new SimpleStylesheetCache());
        stylesheetManager.setTransformerFactoryChooser(SaxonTransformerFactoryChooser.getInstance());
        servletContext.setAttribute(STYLESHEET_MANAGER_ATTRIBUTE_NAME, stylesheetManager);
        
        /* Set up up-conversion options for demos */
        UpConversionOptions ucOpts = new UpConversionOptions();
        ucOpts.assumeSymbol(createUpConversionSymbolElement("e"), "exponentialNumber");
        ucOpts.assumeSymbol(createUpConversionSymbolElement("f"), "function");
        ucOpts.assumeSymbol(createUpConversionSymbolElement("g"), "function");
        ucOpts.assumeSymbol(createUpConversionSymbolElement("i"), "imaginaryNumber");
        ucOpts.assumeSymbol(createUpConversionSymbolElement("\\pi"), "constantPi");
        ucOpts.assumeSymbol(createUpConversionSymbolElement("\\gamma"), "eulerGamma");
        servletContext.setAttribute(UPCONVERSION_OPTIONS_ATTRIBUTE_NAME, ucOpts);
        
        logger.info("Context initialised");
    }
    
    private Element createUpConversionSymbolElement(String mathInput) {
        try {
            SnuggleEngine engine = new SnuggleEngine();
            SnuggleSession session = engine.createSession();
            session.parseInput(new SnuggleInput("$" + mathInput + "$"));
            return (Element) session.buildDOMSubtree().item(0).getFirstChild();
        }
        catch (Exception e) {
            throw new RuntimeException("Unexpected Exception in SnuggleTeX option generation");
        }
    }
    
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Context destroyed");
    }
}