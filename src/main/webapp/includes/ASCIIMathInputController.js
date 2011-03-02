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

/* (Reset certain defaults chosen by ASCIIMathML) */
var mathcolor = "";
var mathfontfamily = "";

var ASCIIMathInputController = (function() {

    var newline = "\r\n";

    /************************************************************/
    /* ASCIIMath calling helpers */

    var callASCIIMath = function(mathModeInput) {
        /* Escape use of backquote symbol to prevent exiting math mode */
        mathModeInput = mathModeInput.replace(/`/g, "\\`");

        var span = AMparseMath(mathModeInput); // This is <span><math>...</math></span>
        var math = span.childNodes[0]; /* This is <math>...</math> */
        math.setAttribute("display", "block");
        math.removeAttribute("title"); // MathJax doesn't like this!
        return math;
    };

    /**
     * Extracts the source MathML contained within the ASCIIMath-generated
     * <math> element.
     */
    var extractMathML = function(asciiMathElement) {
        return AMnode2string(asciiMathElement, "")
            .substring(newline.length); /* Trim off leading newline */
    };

    /* Fixed up version of the function of the same name in ASCIIMathMLeditor.js,
     * with the following changes:
     *
     * * Used newline variable for line breaks
     * * Attribute values are escape correctly
     */
    var AMnode2string = function(inNode, indent) {
        var i, str = "";
        if (inNode.nodeType == 1) {
            var name = inNode.nodeName.toLowerCase(); // (IE fix)
            str = newline + indent + "<" + name;
            for (i=0; i < inNode.attributes.length; i++) {
                var attrValue = inNode.attributes[i].nodeValue;
                if (attrValue!="italic" &&
                        attrValue!="" &&  //stop junk attributes
                        attrValue!="inherit" && // (mostly IE)
                        attrValue!=undefined) {
                    str += " " + inNode.attributes[i].nodeName
                        + "=\"" + AMescapeValue(inNode.attributes[i].nodeValue) + "\"";
                }
            }
            if (name == "math") str += " xmlns=\"http://www.w3.org/1998/Math/MathML\"";
            str += ">";
            for(i=0; i<inNode.childNodes.length; i++) {
                str += AMnode2string(inNode.childNodes[i], indent+"  ");
            }
            if (name != "mo" && name != "mi" && name != "mn") {
                str += newline + indent;
            }
            str += "</" + name + ">";
        }
        else if (inNode.nodeType == 3) {
            str += AMescapeValue(inNode.nodeValue);
        }
        return str;
    };

    var AMescapeValue = function(value) {
        var str = "";
        for (i=0; i<value.length; i++) {
            if (value.charCodeAt(i)<32 || value.charCodeAt(i)>126) str += "&#"+value.charCodeAt(i)+";";
            else if (value.charAt(i)=="<") str += "&lt;";
            else if (value.charAt(i)==">") str += "&gt;";
            else if (value.charAt(i)=="&") str += "&amp;";
            else str += value.charAt(i);
        }
        return str;
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
                UpConversionAJAXController.replaceContainerContent(jQuery("#" + this.mathJaxRenderingContainerId),
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
