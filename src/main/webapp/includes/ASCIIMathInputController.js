/*
 *
 * Requirements:
 *
 * jquery.js (at least version 1.5.0)
 * ASCIIMathParser.js
 * UpConversionAJAXController.js
 *
 * Author: David McKain
 *
 * $Id:web.xml 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh
 * All Rights Reserved
 */

/************************************************************/

var ASCIIMathInputController = (function() {

    var createXMLDocument = function() {
        var doc;
        if (document.implementation && document.implementation.createDocument) {
            /* Gecko, Webkit, Opera */
            doc = document.implementation.createDocument("", "", null);
        }
        else {
            try {
                /* Internet Explorer */
                doc = new ActiveXObject("Microsoft.XMLDOM");
            }
            catch (e) {
                alert("I don't know how to create a DOM Document in this browser");
            }
        }
        return doc;
    };

    var asciiMathParser = new ASCIIMathParser(createXMLDocument());

    /************************************************************/
    /* ASCIIMath calling helpers */

    var callASCIIMath = function(mathModeInput) {
        /* Escape use of backquote symbol to prevent exiting math mode */
        mathModeInput = mathModeInput.replace(/`/g, "\\`");

        var math = asciiMathParser.parseASCIIMathInput(mathModeInput);
        math.setAttribute("display", "block");
        return math;
    };

    /**
     * Extracts the source MathML contained within the ASCIIMath-generated
     * <math> element.
     */
    var extractMathML = function(asciiMathElement) {
        var xml;
        try {
            /* Gecko, Webkit, Opera */
            var serializer = new XMLSerializer();
            xml = serializer.serializeToString(asciiMathElement);
        }
        catch (e) {
            try {
                /* Internet Explorer */
                xml = asciiMathElement.xml;
            }
            catch (e) {
                alert("I don't know how to serialize XML in this browser");
            }
        }
        return xml;
    };

    /************************************************************/

    var Widget = function(_asciiMathInputId, _asciiMathOutputId, _verifierControl) {
        this.asciiMathInputControlId = _asciiMathInputId;
        this.asciiMathOutputControlId = _asciiMathOutputId;
        this.verifierControl = _verifierControl;
        this.mathJaxRenderingContainerId = null;
        this.pmathSourceContainerId = null;
        var lastInput = null;
        var currentXHR = null;
        var currentTimeoutId = null;
        var widget = this;

        this.getASCIIMathInput= function() {
            var inputSelector = jQuery("#" + this.asciiMathInputControlId);
            return inputSelector.get(0).value;
        };

        /**
         * Checks the content of the <input/> element having the given asciiMathInputControlId,
         * and calls {@link #updatePreview} if its contents have changed since the
         * last call to this.
         */
        this.updatePreviewIfChanged = function() {
            var asciiMathInput = this.getASCIIMathInput();
            if (lastInput==null || asciiMathInput!=lastInput) {
                /* Something has changed */
                lastInput = asciiMathInput;

                /* Update live preview */
                var mathmlSource = widget.updatePreview();

                /* Maybe verify the input */
                if (this.verifierControl!=null) {
                    this.verifierControl.verifyLater(mathmlSource);
                }
            }
        };

        /**
         * Hacked version of AMdisplay() from ASCIIMathMLeditor.js that allows
         * us to specify which element to display the resulting MathML
         * in and where the raw input is going to come from.
         */
        this.updatePreview = function() {
            /* Get ASCIIMathML to generate a <math> element */
            var asciiMathElement = callASCIIMath(this.getASCIIMathInput());
            var mathmlSource = extractMathML(asciiMathElement);

            /* Maybe update preview mathmlSource box */
            if (this.pmathSourceContainerId!=null) {
                jQuery("#" + this.pmathSourceContainerId).text(mathmlSource);
            }

            /* Maybe insert MathML into the DOM for display */
            if (this.mathJaxRenderingContainerId!=null) {
                UpConversionAJAXController.replaceContainerMathMLContent(jQuery("#" + this.mathJaxRenderingContainerId),
                    asciiMathElement);
            }
            return mathmlSource;
        };

        this.doInit = function() {
            /* Set up submit handler for the form */
            var inputSelector = jQuery("#" + this.asciiMathInputControlId);
            inputSelector.closest("form").bind("submit", function(evt) {
                /* We'll redo the ASCIIMathML process, just in case we want to allow auto-preview to be disabled in future */
                var asciiMathInput = inputSelector.get(0).value;
                var asciiMathElement = callASCIIMath(asciiMathInput);
                var asciiMathSource = extractMathML(asciiMathElement);
                var mathmlResultControl = document.getElementById(widget.asciiMathOutputControlId);
                mathmlResultControl.value = asciiMathSource;
                return true;
            });

            /* Set up initial preview */
            var mathmlSource = widget.updatePreview();

            /* Maybe do verification on the initial input */
            if (this.verifierControl!=null) {
                this.verifierControl.verifyLater(mathmlSource);
            }

            /* Set up handler to update preview when required */
            inputSelector.bind("change keyup keydown", function() {
                widget.updatePreviewIfChanged();
            });
        };
    };

    Widget.prototype.init = function() {
        this.doInit();
    };

    Widget.prototype.setMathJaxRenderingContainerId = function(id) {
        this.mathJaxRenderingContainerId = id;
    }

    Widget.prototype.setPMathSourceContainerId = function(id) {
        this.pmathSourceContainerId = id;
    }

    return {
        createInputWidget: function(inputId, outputId, verifierControl) {
            return new Widget(inputId, outputId, verifierControl);
        }
    };

})();

/************************************************************/

var SnuggleTeXInputController = (function() {

    /************************************************************/

    var Widget = function(_snuggleTeXInputControlId, _verifierControl) {
        this.snuggleTeXInputControlId = _snuggleTeXInputControlId;
        this.verifierControl = _verifierControl;
        var lastInput = null;
        var currentXHR = null;
        var currentTimeoutId = null;
        var widget = this;

        this.getLaTeXInput= function() {
            var inputSelector = jQuery("#" + this.snuggleTeXInputControlId);
            return inputSelector.get(0).value;
        };

        this.updatePreviewIfChanged = function() {
            var latexInput = this.getLaTeXInput();
            if (lastInput==null || latexInput!=lastInput) {
                /* Something has changed */
                lastInput = latexInput;

                /* Maybe verify the input */
                if (this.verifierControl!=null) {
                    this.verifierControl.verifyLater(latexInput);
                }
            }
        };

        this.doInit = function() {
            var inputSelector = jQuery("#" + this.snuggleTeXInputControlId);

            /* Maybe do verification on the initial input */
            if (this.verifierControl!=null) {
                this.verifierControl.verifyLater(this.getLaTeXInput());
            }

            /* Set up handler to update preview when required */
            inputSelector.bind("change keyup keydown", function() {
                widget.updatePreviewIfChanged();
            });
        };
    };

    Widget.prototype.init = function() {
        this.doInit();
    };

    return {
        createInputWidget: function(inputId, verifierControl) {
            return new Widget(inputId, verifierControl);
        }
    };

})();
