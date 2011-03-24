/*
 *
 * Requirements:
 *
 * jquery.js (at least version 1.5.0)
 * UpConversionAJAXController.js
 * ASCIIMathParser.js (optional - only needed for geek previews)
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

    /* See if ASCIIMathParser.js was loaded in order to provide live raw
     * previews ACIIMath input.
     */
    var asciiMathParserLoaded = false;
    var asciiMathParser = null;
    try {
        asciiMathParser = new ASCIIMathParser(ASCIIMathParserBrowserUtilities.createXMLDocument());
        asciiMathParserLoaded = true;
    }
    catch (e) {
    }

    /************************************************************/

    var callASCIIMath = function(mathModeInput) {
        /* Escape use of backquote symbol to prevent exiting math mode */
        mathModeInput = mathModeInput.replace(/`/g, "\\`");

        var mathElement = asciiMathParser.parseASCIIMathInput(mathModeInput, {
            displayMode: true,
            addSourceAnnotation: true
        });
        var mathml = ASCIIMathParserBrowserUtilities.serializeXMLNode(mathElement);
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

    var Widget = function(_asciiMathInputId,  _verifierControl) {
        this.asciiMathInputControlId = _asciiMathInputId;
        this.verifierControl = _verifierControl;
        this.rawRenderingContainerId = null;
        this.rawSourceContainerId = null;
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
         * and calls {@link #updateASCIIMathPreview} if its contents have changed since the
         * last call to this.
         */
        this.updatePreviewIfChanged = function() {
            var asciiMathInput = this.getASCIIMathInput();
            if (lastInput==null || asciiMathInput!=lastInput) {
                lastInput = asciiMathInput;
                this.updatePreview();
            }
        };

        this.updatePreview = function() {
            /* Maybe update live preview */
            var asciiMathInput = widget.updateASCIIMathPreview();

            /* Maybe verify the input */
            if (this.verifierControl!=null) {
                this.verifierControl.verifyLater(asciiMathInput);
            }
        };

        this.updateASCIIMathPreview = function() {
            /* Get ASCIIMathML to generate a <math> element */
            var asciiMathInput = this.getASCIIMathInput();
            var mathmlSource = null;
            var message = null;
            if (asciiMathInput.match(/\S/)) {
                if (asciiMathParserLoaded) {
                    mathmlSource = callASCIIMath(this.getASCIIMathInput());
                }
                else {
                    message = "(ASCIIMathParser.js not loaded)";
                }
            }
            else {
                message = "(Blank input)";
            }
            /* Update preview elements */
            if (this.rawRenderingContainerId!=null) {
                if (mathmlSource!=null) {
                    UpConversionAJAXController.replaceContainerMathMLContent(jQuery("#" + this.rawRenderingContainerId), mathmlSource);
                }
                else {
                    UpConversionAJAXController.replaceContainerMathMLContent(jQuery("#" + this.rawRenderingContainerId),
                    "<math><mtext>" + message + "</mtext></math>");
                }
            }
            if (this.rawSourceContainerId!=null) {
                UpConversionAJAXController.replaceContainerPreformattedText(jQuery("#" + this.rawSourceContainerId), mathmlSource || message);
            }
            return asciiMathInput;
        };

        this.doInit = function() {
            /* Bind help button */
            if (this.helpButtonId!=null) {
                var helpButton = jQuery("#" + this.helpButtonId);
                helpButton.click(function() {
                    showHelpDialog(jQuery(this));
                });
            }

            /* Set up handler to update preview when required */
            var inputSelector = jQuery("#" + this.asciiMathInputControlId);
            inputSelector.bind("change keyup keydown", function() {
                widget.updatePreviewIfChanged();
            });

            /* Set up initial preview */
            this.updatePreview();
        };
    };

    Widget.prototype.init = function() {
        this.doInit();
    };

    Widget.prototype.setHelpButtonId = function(id) {
        this.helpButtonId = id;
    };

    Widget.prototype.setRawRenderingContainerId = function(id) {
        this.rawRenderingContainerId = id;
    };

    Widget.prototype.setRawSourceContainerId = function(id) {
        this.rawSourceContainerId = id;
    };

    return {
        bindInputWidget: function(asciiMathInputId, verifierControl) {
            if (asciiMathInputId==null) {
                throw new Error("asciiMathInputId must not be null");
            }
            if (verifierControl==null) {
                throw new Error("verifierControl must not be null");
            }
            return new Widget(asciiMathInputId, verifierControl);
        },

        getHelpPageURL: function() { return helpPageUrl },
        setHelpPageURL: function(id) { helpPageUrl = id }
    };

})();
