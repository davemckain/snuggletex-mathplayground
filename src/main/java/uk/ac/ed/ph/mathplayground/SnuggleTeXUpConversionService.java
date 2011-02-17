/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.SerializationSpecifier;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptionDefinitions;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptions;
import uk.ac.ed.ph.snuggletex.upconversion.internal.UpConversionPackageDefinitions;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.SerializationOptions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * FIXME: Document this type!
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class SnuggleTeXUpConversionService extends BaseServlet {
    
    private static final long serialVersionUID = 2261754980279697343L;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        /* Read input LaTeX */
        String inputLaTeX = IOUtilities.readCharacterStream(request.getReader());
        doService(response, inputLaTeX);
    }
    
    private void doService(HttpServletResponse response, String inputLaTeX) throws IOException {
        /* Create JSON Object encapsulating result */
        StringBuilder jsonBuilder = new StringBuilder();
        maybeAppendJson(jsonBuilder, "input", inputLaTeX);
        
        /* Parse the assumptions & LaTeX */
        SnuggleEngine engine = new SnuggleEngine(getStylesheetManager());
        engine.addPackage(UpConversionPackageDefinitions.getPackage());
        SnuggleSession session = engine.createSession();
        session.parseInput(new SnuggleInput("\\[ " + inputLaTeX + " \\]", "Math Input"));
        
        DOMOutputOptions domOptions = new DOMOutputOptions();
        domOptions.setMathVariantMapping(true);
        domOptions.setAddingMathSourceAnnotations(true);
        domOptions.setErrorOutputOptions(ErrorOutputOptions.NO_OUTPUT);
        
        Document tempDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        Element root = tempDocument.createElement("root");
        tempDocument.appendChild(root);
        session.buildDOMSubtree(root, domOptions);
        
        if (session.getErrors().isEmpty()) {
            /* No errors so far, so do up-conversion */
            MathMLUpConverter upConverter = new MathMLUpConverter(getStylesheetManager());
            UpConversionOptions upConversionOptions = new UpConversionOptions();
            upConversionOptions.setSpecifiedOption(UpConversionOptionDefinitions.DO_BRACKETED_PRESENTATION_MATHML, "true");
            
            Document upConvertedMathDocument = upConverter.upConvertSnuggleTeXMathML(tempDocument, upConversionOptions);
            Element mathElement = (Element) upConvertedMathDocument.getDocumentElement().getChildNodes().item(0);

            /* Isolate various annotations from the result */
            Document pmathDocument = MathMLUtilities.isolateFirstSemanticsBranch(mathElement);
            Document pmathBracketedDocument = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.BRACKETED_PRESENTATION_MATHML_ANNOTATION_NAME);
            Document cmathDocument = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME);
            String maximaAnnotation = MathMLUtilities.extractAnnotationString(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
            Document contentFailuresAnnotation = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.CONTENT_FAILURES_ANNOTATION_NAME);
            Document maximaFailuresAnnotation = MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);

            maybeAppendJson(jsonBuilder, "pmath", pmathDocument);
            maybeAppendJson(jsonBuilder, "pmathBracketed", pmathBracketedDocument);
            maybeAppendJson(jsonBuilder, "cmath", cmathDocument);
            maybeAppendJson(jsonBuilder, "maxima", maximaAnnotation);
            maybeAppendJson(jsonBuilder, "cmathFailures", contentFailuresAnnotation);
            maybeAppendJson(jsonBuilder, "maximaFailures", maximaFailuresAnnotation);
        }
        else {
            /* Parse/building failure */
            maybeAppendJson(jsonBuilder, "errors", "FILL THIS IN");
        }
        endJson(jsonBuilder);
        response.setContentType("text/json; charset=UTF-8");
        PrintWriter responseWriter = response.getWriter();
        responseWriter.append(jsonBuilder);
        responseWriter.flush();
    }
    
    private void maybeAppendJson(StringBuilder stringBuilder, String key, Document valueDocument) {
        if (valueDocument!=null) {
            SerializationSpecifier options = new SerializationOptions();
            options.setIndenting(true);
            maybeAppendJson(stringBuilder, key, MathMLUtilities.serializeDocument(valueDocument, options));
        }
    }
    
    private void maybeAppendJson(StringBuilder stringBuilder, String key, String value) {
        if (value!=null) {
            if (stringBuilder.length()==0) {
                stringBuilder.append("{\n");
            }
            else {
                stringBuilder.append(",\n");
            }
            stringBuilder.append('"')
                .append(key)
                .append("\": \"")
                .append(value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"))
                .append("\"");
        }
    }
    
    private void endJson(StringBuilder stringBuilder) {
        stringBuilder.append("\n}\n");
    }
}