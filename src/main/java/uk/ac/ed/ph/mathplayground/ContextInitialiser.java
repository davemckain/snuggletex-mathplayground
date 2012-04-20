/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.asciimath.parser.AsciiMathParser;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptionDefinitions;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptions;
import uk.ac.ed.ph.snuggletex.upconversion.internal.UpConversionPackageDefinitions;
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
    public static final String SNUGGLE_ENGINE_ATTRIBUTE_NAME = "snuggleEngine";
    public static final String UPCONVERSION_OPTIONS_ATTRIBUTE_NAME = "upconversionOptions";
    public static final String ASCIIMATH_PARSER_ATTRIBUTE_NAME = "asciiMathParser";
    
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        
        /* Create shared AsciiMathParser instance */
        AsciiMathParser asciiMathParser = new AsciiMathParser();
        servletContext.setAttribute(ASCIIMATH_PARSER_ATTRIBUTE_NAME, asciiMathParser);
        
        /* Create and store StylesheetManager, hard-coded to use Saxon with caching
         * turned on as we're loading via the ClassPath so there's no point trying to turn
         * it off. */
        StylesheetManager stylesheetManager = new StylesheetManager();
        stylesheetManager.setStylesheetCache(new SimpleStylesheetCache());
        stylesheetManager.setTransformerFactoryChooser(SaxonTransformerFactoryChooser.getInstance());
        servletContext.setAttribute(STYLESHEET_MANAGER_ATTRIBUTE_NAME, stylesheetManager);
        
        /* Create shared SnuggleEngine, including up-conversion package */
        SnuggleEngine engine = new SnuggleEngine(stylesheetManager);
        engine.addPackage(UpConversionPackageDefinitions.getPackage());
        servletContext.setAttribute(SNUGGLE_ENGINE_ATTRIBUTE_NAME, engine);
        
        /* Set up up-conversion options for demos */
        UpConversionOptions ucOpts = new UpConversionOptions();
        ucOpts.setSpecifiedOption(UpConversionOptionDefinitions.ADD_OPTIONS_ANNOTATION_NAME, "true");
        ucOpts.setSpecifiedOption(UpConversionOptionDefinitions.DO_BRACKETED_PRESENTATION_MATHML, "true");
        ucOpts.assumeSymbol(createUpConversionSymbolElement(engine, "e"), "exponentialNumber");
        ucOpts.assumeSymbol(createUpConversionSymbolElement(engine, "f"), "function");
        ucOpts.assumeSymbol(createUpConversionSymbolElement(engine, "g"), "function");
        ucOpts.assumeSymbol(createUpConversionSymbolElement(engine, "i"), "imaginaryNumber");
        ucOpts.assumeSymbol(createUpConversionSymbolElement(engine, "\\pi"), "constantPi");
        ucOpts.assumeSymbol(createUpConversionSymbolElement(engine, "\\gamma"), "eulerGamma");
        servletContext.setAttribute(UPCONVERSION_OPTIONS_ATTRIBUTE_NAME, ucOpts);
        
        logger.info("Context initialised");
    }
    
    private Element createUpConversionSymbolElement(SnuggleEngine engine, String mathInput) {
        try {
            SnuggleSession session = engine.createSession();
            session.parseInput(new SnuggleInput("$" + mathInput + "$"));
            return (Element) session.buildDOMSubtree().item(0).getFirstChild();
        }
        catch (Exception e) {
            throw new RuntimeException("Unexpected Exception in SnuggleTeX option generation");
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Context destroyed");
    }
}