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
 * $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh
 * All Rights Reserved
 */

/************************************************************/

var ASCIIMathInputController = (function() {

    var helpPageUrl = null; /* (Caller should fill this in) */

    var helpDialog = null; /* (Created on first use) */

    var asciiMathParser = new ASCIIMathParser(ASCIIMathParserBrowserUtilities.createXMLDocument());

    /************************************************************/
    /* ASCIIMath calling helpers */

    var callASCIIMath = function(mathModeInput) {
        /* Escape use of backquote symbol to prevent exiting math mode */
        mathModeInput = mathModeInput.replace(/`/g, "\\`");

        var math = asciiMathParser.parseASCIIMathInput(mathModeInput, {
            displayMode: true,
            addSourceAnnotation: true
        });
        return math;
    };

    /**
     * Extracts the source MathML contained within the ASCIIMath-generated
     * <math> element.
     */
    var extractMathML = function(asciiMathElement) {
        var mathml = ASCIIMathParserBrowserUtilities.serializeXMLNode(asciiMathElement);
        return ASCIIMathParserBrowserUtilities.indentMathMLString(mathml);
    };

    var showHelpDialog = function(buttonQuery) {
        if (helpDialog==null) {
            var helpPanel = jQuery("<div></div>");
            if (helpPageUrl!=null) {
                helpPanel.load(helpPageUrl);
            }
            else {
                helpPanel.html("(No Help URL has been set)");
            }
            helpDialog = helpPanel.dialog({
                autoOpen: false,
                draggable: true,
                resizable: true,
                title: 'Input Hints',
                width: '70%'
            });
        }
        if (helpDialog.dialog('isOpen')) {
            helpDialog.dialog('close');
        }
        else {
            var buttonPosition = buttonQuery.position();
            helpDialog.dialog('option', 'position', [ buttonPosition.left, buttonPosition.top + 70 ]);
            helpDialog.dialog('open');
        }
    };

    /************************************************************/

    var Widget = function(_asciiMathInputId, _asciiMathOutputId, _verifierControl) {
        this.asciiMathInputControlId = _asciiMathInputId;
        this.asciiMathOutputControlId = _asciiMathOutputId;
        this.verifierControl = _verifierControl;
        this.mathJaxRenderingContainerId = null;
        this.pmathSourceContainerId = null;
        this.helpButtonId = null;
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
            var asciiMathInput = this.getASCIIMathInput();
            var mathmlSource = null;
            if (asciiMathInput.match(/\S/)) {
                var asciiMathElement = callASCIIMath(this.getASCIIMathInput());
                mathmlSource = extractMathML(asciiMathElement);

                /* Maybe update preview mathmlSource box */
                if (this.pmathSourceContainerId!=null) {
                    UpConversionAJAXController.replaceContainerPreformattedText(jQuery("#" + this.pmathSourceContainerId), mathmlSource);
                }

                /* Maybe insert MathML into the DOM for display */
                if (this.mathJaxRenderingContainerId!=null) {
                    UpConversionAJAXController.replaceContainerMathMLContent(jQuery("#" + this.mathJaxRenderingContainerId),
                        asciiMathElement);
                }
            }
            else {
                /* Blank input */
                if (this.pmathSourceContainerId!=null) {
                    UpConversionAJAXController.replaceContainerPreformattedText(jQuery("#" + this.pmathSourceContainerId), "(Empty Input)");
                }

                /* Maybe insert MathML into the DOM for display */
                if (this.mathJaxRenderingContainerId!=null) {
                    UpConversionAJAXController.replaceContainerPreformattedText(jQuery("#" + this.mathJaxRenderingContainerId), "(Blank input)");
                }
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

            /* Bind help button */
            if (this.helpButtonId!=null) {
                var helpButton = jQuery("#" + this.helpButtonId);
                helpButton.click(function() {
                    showHelpDialog(jQuery(this));
                });
            }

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

    Widget.prototype.setHelpButtonId = function(id) {
        this.helpButtonId = id;
    };

    Widget.prototype.setMathJaxRenderingContainerId = function(id) {
        this.mathJaxRenderingContainerId = id;
    };

    Widget.prototype.setPMathSourceContainerId = function(id) {
        this.pmathSourceContainerId = id;
    };

    return {
        bindInputWidget: function(inputId, outputId, verifierControl) {
            return new Widget(inputId, outputId, verifierControl);
        },

        getHelpPageURL: function() { return helpPageUrl },
        setHelpPageURL: function(id) { helpPageUrl = id }
    };

})();
