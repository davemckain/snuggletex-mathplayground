/*
 * This provides some basic code for managing the ASCIIMathML input widget
 * used in the ASCIIMathML input demo in SnuggleTeX.
 *
 * The general ideas may be useful in other scenarios, so feel free to use
 * and/or build on this as is necessary.
 *
 * NOTE:
 *
 * This code uses the lovely jQuery library, but avoids using the
 * $(...) function just in case your code also uses some other library
 * like prototype that defines its own $(...) function.
 * (In this case, you will still want to read:
 *
 * http://docs.jquery.com/Using_jQuery_with_Other_Libraries
 *
 * to make sure you do whatver is necessary to make sure that both
 * libraries co-exist correctly.)
 *
 * Requirements:
 *
 * ASCIIMathML.js
 * jquery.js (at least version 1.5.0)
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

/************************************************************/

var VerifierController = (function() {

    var verifierServiceUrl = null; /* Caller must fill in */
    var delay = 300;
    var usingMathJax = true;

    var STATUS_WAITING       = 0;
    var STATUS_SUCCESS       = 1;
    var STATUS_PARSE_ERROR   = 2;
    var STATUS_VERIFY_FAILED = 3;
    var STATUS_UNKNOWN_ERROR = 4;

    /************************************************************/

    var VerifierControl = function() {
        this.verifiedRenderingContainerId = null;
        this.cmathSourceContainerId = null;
        this.maximaSourceContainerId = null;
        var currentXHR = null;
        var currentTimeoutId = null;
        var verifierControl = this;

        this.doVerifyLater = function(verifyInputData) {
            if (currentTimeoutId!=null) {
                window.clearTimeout(currentTimeoutId);
            }
            else {
                this.updateVerifierContainer(STATUS_WAITING); /* Show waiting animation */
            }
            currentTimeoutId = window.setTimeout(function() {
                verifierControl.verify(verifyInputData);
                currentTimeoutId = null;
            }, delay);
        };

        this.verify = function(verifyInputData) {
            if (this.verifiedRenderingContainerId!=null && verifierServiceUrl!=null) {
                currentXHR = jQuery.ajax({
                    type: 'POST',
                    url: verifierServiceUrl,
                    dataType: 'json',
                    data: verifyInputData,
                    success: function(data, textStatus, jqXHR) {
                        if (currentXHR==jqXHR) {
                            currentXHR = null;
                            verifierControl.showVerificationResult(data);
                        }
                    }
                });
            }
        };

        this.showVerificationResult = function(jsonData) {
            if (this.verifiedRenderingContainerId!=null) {
                var verifiedRenderingContainer = jQuery("#" + this.verifiedRenderingContainerId);

                /* We consider "valid" to mean "getting as far as CMathML" here */
                var cmath = jsonData['cmath'];
                if (cmath!=null) {
                    var bracketed = jsonData['pmathBracketed'];
                    this.updateVerifierContainer(STATUS_SUCCESS, bracketed);
                }
                else if (jsonData['cmathFailures']!=null) {
                    this.updateVerifierContainer(STATUS_VERIFY_FAILED);
                }
                else if (jsonData['errors']!=null) {
                    var html = '<ul>';
                    for (var i in jsonData['errors']) {
                        html += '<li>' + jsonData['errors'][i] + '</li>'
                    }
                    html += '</ul>';
                    this.updateVerifierContainer(STATUS_PARSE_ERROR, null, html);
                }
                else {
                    this.updateVerifierContainer(STATUS_UNKNOWN_ERROR);
                }

                /* Maybe show CMath source */
                if (this.cmathSourceContainerId!=null) {
                    jQuery("#" + this.cmathSourceContainerId).text(cmath || 'Could not generate Content MathML');
                }
                /* Maybe show Maxima is we got it */
                if (this.maximaSourceContainerId!=null) {
                    jQuery("#" + this.maximaSourceContainerId).text(jsonData['maxima'] || 'Could not generate Maxima syntax');
                }
            }
        };

        this.updateVerifierContainer = function(status, mathElementString, errorContent) {
            if (this.verifiedRenderingContainerId!=null) {
                var verifiedRenderingContainer = jQuery("#" + this.verifiedRenderingContainerId);
                /* Set up children if not done already */
                if (verifiedRenderingContainer.children().size()==0) {
                    verifiedRenderingContainer.html("<div class='asciiMathWidgetStatus'></div>"
                        + "<div class='asciiMathWidgetMessage'></div>"
                        + "<div class='asciiMathWidgetResult'></div>");
                }
                var statusContainer = verifiedRenderingContainer.children().first();
                var messageContainer = statusContainer.next();
                var resultContainer = messageContainer.next();
                switch(status) {
                    case STATUS_WAITING:
                        statusContainer.attr('class', 'asciiMathWidgetStatus waiting');
                        this.showMessage(messageContainer, "Checking...");
                        this.showMessage(resultContainer, null);
                        break;

                    case STATUS_SUCCESS:
                        statusContainer.attr('class', 'asciiMathWidgetStatus success');
                        this.showMessage(messageContainer, "Your input makes sense. It has been interpreted as:");
                        this.showXML(resultContainer, mathElementString);
                        break;

                    case STATUS_PARSE_ERROR:
                        statusContainer.attr('class', 'asciiMathWidgetStatus failure');
                        this.showMessage(messageContainer, "SnuggleTeX could not parse your input:");
                        this.showMessage(resultContainer, errorContent);
                        break;

                    case STATUS_VERIFY_FAILED:
                        statusContainer.attr('class', 'asciiMathWidgetStatus failure');
                        this.showMessage(messageContainer, "I could not make sense of your input");
                        this.showMessage(resultContainer, null);
                        break;

                    case STATUS_UNKNOWN_ERROR:
                        statusContainer.attr('class', 'asciiMathWidgetStatus error');
                        this.showMessage(messageContainer, "Unexpected error");
                        this.showMessage(resultContainer, null);
                        break;
                }
            }

        };

        this.showMessage = function(containerQuery, html) {
            VerifierController.replaceContainerContent(containerQuery, html || "\xa0");
        };

        this.showXML = function(containerQuery, xml) {
            var content;
            if (document.adoptNode) {
                /* Nice browser */
                var rootElement = jQuery.parseXML(xml).childNodes[0];
                document.adoptNode(rootElement);
                content = rootElement;
            }
            else {
                /* Internet Exploder */
                content = xml;
            }
            VerifierController.replaceContainerContent(containerQuery, content);
        };

    };

    VerifierControl.prototype.setVerifiedRenderingContainerId = function(id) {
        this.verifiedRenderingContainerId = id;
    };

    VerifierControl.prototype.setCMathSourceContainerId = function(id) {
        this.cmathSourceContainerId = id;
    };

    VerifierControl.prototype.setMaximaSourceContainerId = function(id) {
        this.maximaSourceContainerId = id;
    };

    VerifierControl.prototype.verifyLater = function(verifyInputData) {
        this.doVerifyLater(verifyInputData);
    };

    return {
        replaceContainerContent: function(containerQuery, content) {
            containerQuery.empty();
            if (content!=null) {
                containerQuery.append(content);

                /* Maybe schedule MathJax update if this is a MathML Element */
                if (usingMathJax && content instanceof Element && content.nodeType==1 && content.nodeName=="math") {
                    MathJax.Hub.Queue(["Typeset", MathJax.Hub, containerQuery.get(0)]);
                }
            }
        },

        createVerifierControl: function() {
            return new VerifierControl();
        },

        getVerifierServiceUrl: function() { return verifierServiceUrl },
        setVerifierServiceUrl: function(url) { verifierServiceUrl = url },

        getDelay: function() { return delay },
        setDelay: function(newDelay) { delay = newDelay },

        isUsingMathJax: function() { return usingMathJax },
        setUsingMathJax: function(newUsingMathJax) { usingMathJax = newUsingMathJax },
    };

})();

/************************************************************/

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
                VerifierController.replaceContainerContent(jQuery("#" + this.mathJaxRenderingContainerId),
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
        },
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
        },
    };

})();
