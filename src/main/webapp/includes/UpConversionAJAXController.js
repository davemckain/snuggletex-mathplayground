/* Controller for AJAX Up-Conversion / Semantic Enrichment functionality
 *
 * Requirements:
 *
 * jquery.js (at least version 1.5.0)
 *
 * Author: David McKain
 *
 * $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh
 * All Rights Reserved
 */

/************************************************************/

/* (Simple jQuery extension to append MathML to a document. This requires
 * MathJax to work correctly so needs a bit more thought...
 */
(function($) {
    $.fn.appendMathML = function(mathmlContent) {
        if (mathmlContent!=null) {
            if (mathmlContent.nodeType) {
                /* This is (assumed to be a) MathML DOM Element */
                if (document.adoptNode) {
                    /* Gecko, Webkit, Opera: we adopt the MathML Element into the
                     * document and append it as a child */
                    document.adoptNode(mathmlContent);
                    this.append(mathmlContent);
                }
                else {
                    /* Internet Explorer: We just append the Element's XML source,
                     * which is quite easy to extract in this case. */
                    this.append(mathmlContent.xml);
                }
            }
            else if (typeof mathmlContent=="string") {
                /* MathML String */
                if (document.adoptNode) {
                    /* Gecko, Webkit, Opera: We parse the XML, adopt it and
                     * append as child. */
                    var mathElement = $.parseXML(mathmlContent).childNodes[0];
                    document.adoptNode(mathElement);
                    this.append(mathElement);
                }
                else {
                    /* Internet Exploder */
                    this.append(mathmlContent);
                }
            }
            else {
                throw "Unexpected Math Content: " + (typeof mathmlContent);
            }

// TODO: Decide whether to have MathJax at this level, or within the Controller
//            /* Maybe schedule MathJax update */
//            if (MathJax) {
//                this.each(function() {
//                    MathJax.Hub.Queue(["Typeset", MathJax.Hub, this]);
//               });
//            }
        }
    };
})(jQuery);

var UpConversionAJAXController = (function() {

    var upConversionServiceUrl = null; /* Caller must fill in */
    var delay = 300;

    var STATUS_WAITING             = 0;
    var STATUS_SUCCESS             = 1;
    var STATUS_PARSE_ERROR         = 2;
    var STATUS_UPCONVERSION_FAILED = 3;
    var STATUS_UNKNOWN_ERROR       = 4;

    /************************************************************/

    var UpConversionAJAXControl = function() {
        this.bracketedRenderingContainerId = null;
        this.pmathSemanticSourceContainerId = null;
        this.pmathBracketedSourceContainerId = null;
        this.cmathSourceContainerId = null;
        this.maximaSourceContainerId = null;
        var currentXHR = null;
        var currentTimeoutId = null;
        var verifierAJAXControl = this;

        this.doVerifyLater = function(verifyInputData) {
            if (currentTimeoutId!=null) {
                window.clearTimeout(currentTimeoutId);
            }
            else {
                this.updateUpConversionContainer(STATUS_WAITING); /* Show waiting animation */
            }
            currentTimeoutId = window.setTimeout(function() {
                verifierAJAXControl.verify(verifyInputData);
                currentTimeoutId = null;
            }, delay);
        };

        this.verify = function(verifyInputData) {
            if (this.bracketedRenderingContainerId!=null && upConversionServiceUrl!=null) {
                currentXHR = jQuery.ajax({
                    type: 'POST',
                    url: upConversionServiceUrl,
                    dataType: 'json',
                    data: verifyInputData,
                    success: function(data, textStatus, jqXHR) {
                        if (currentXHR==jqXHR) {
                            currentXHR = null;
                            verifierAJAXControl.showVerificationResult(data);
                        }
                    }
                });
            }
        };

        this.showVerificationResult = function(jsonData) {
            if (this.bracketedRenderingContainerId!=null) {
                var bracketedRenderingContainer = jQuery("#" + this.bracketedRenderingContainerId);

                /* We consider "valid" to mean "getting as far as CMathML" here */
                var cmath = jsonData['cmath'];
                if (cmath!=null) {
                    var bracketed = jsonData['pmathBracketed'];
                    this.updateUpConversionContainer(STATUS_SUCCESS, bracketed);
                }
                else if (jsonData['cmathFailures']!=null) {
                    this.updateUpConversionContainer(STATUS_UPCONVERSION_FAILED);
                }
                else if (jsonData['errors']!=null) {
                    var html = '<ul>';
                    for (var i in jsonData['errors']) {
                        html += '<li>' + jsonData['errors'][i] + '</li>'
                    }
                    html += '</ul>';
                    this.updateUpConversionContainer(STATUS_PARSE_ERROR, null, html);
                }
                else {
                    this.updateUpConversionContainer(STATUS_UNKNOWN_ERROR);
                }

                /* Maybe show source, if requested */
                if (this.pmathSemanticSourceContainerId!=null) {
                    this.showPreformatted(jQuery("#" + this.pmathSemanticSourceContainerId),
                        jsonData['pmathSemantic'] || 'Could not generate Semantic Presentation MathML');
                }
                if (this.pmathBracketedSourceContainerId!=null) {
                    this.showPreformatted(jQuery("#" + this.pmathBracketedSourceContainerId),
                        jsonData['pmathBracketed'] || 'Could not generate Bracketed Presentation MathML');
                }
                if (this.cmathSourceContainerId!=null) {
                    this.showPreformatted(jQuery("#" + this.cmathSourceContainerId),
                        cmath || 'Could not generate Content MathML');
                }
                if (this.maximaSourceContainerId!=null) {
                    this.showPreformatted(jQuery("#" + this.maximaSourceContainerId),
                        jsonData['maxima'] || 'Could not generate Maxima syntax');
                }
            }
        };

        this.updateUpConversionContainer = function(status, mathElementString, errorContent) {
            if (this.bracketedRenderingContainerId!=null) {
                var bracketedRenderingContainer = jQuery("#" + this.bracketedRenderingContainerId);
                /* Set up children if not done already */
                if (bracketedRenderingContainer.children().size()==0) {
                    bracketedRenderingContainer.html("<div class='upConversionAJAXControlMessage'></div>"
                        + "<div class='upConversionAJAXControlResult'></div>");
                }
                var messageContainer = bracketedRenderingContainer.children().first();
                var resultContainer = messageContainer.next();
                switch(status) {
                    case STATUS_WAITING:
                        messageContainer.attr('class', 'upConversionAJAXControlMessage waiting');
                        this.showMessage(messageContainer, "Verifying your input...");
                        this.showMessage(resultContainer, null);
                        break;

                    case STATUS_SUCCESS:
                        messageContainer.attr('class', 'upConversionAJAXControlMessage success');
                        this.showMessage(messageContainer, "I have interpreted your input as:");
                        this.showMathML(resultContainer, mathElementString);
                        break;

                    case STATUS_PARSE_ERROR:
                        messageContainer.attr('class', 'upConversionAJAXControlMessage failure');
                        this.showMessage(messageContainer, "SnuggleTeX could not parse your input:");
                        this.showMessage(resultContainer, errorContent);
                        break;

                    case STATUS_UPCONVERSION_FAILED:
                        messageContainer.attr('class', 'upConversionAJAXControlMessage failure');
                        this.showMessage(messageContainer, "Sorry, I could not make sense of your input");
                        this.showMessage(resultContainer, null);
                        break;

                    case STATUS_UNKNOWN_ERROR:
                        messageContainer.attr('class', 'upConversionAJAXControlMessage error');
                        this.showMessage(messageContainer, "Unexpected error");
                        this.showMessage(resultContainer, null);
                        break;
                }
            }

        };

        this.showMessage = function(containerQuery, html) {
            UpConversionAJAXController.replaceContainerContent(containerQuery, html || "\xa0");
        };

        this.showMathML = function(containerQuery, mathmlString) {
            UpConversionAJAXController.replaceContainerMathMLContent(containerQuery, mathmlString);
        };

        this.showPreformatted = function(containerQuery, text) {
            UpConversionAJAXController.replaceContainerPreformattedText(containerQuery, text);
        };
    };

    UpConversionAJAXControl.prototype.setBracketedRenderingContainerId = function(id) {
        this.bracketedRenderingContainerId = id;
    };

    UpConversionAJAXControl.prototype.setPMathSemanticSourceContainerId = function(id) {
        this.pmathSemanticSourceContainerId = id;
    };

    UpConversionAJAXControl.prototype.setPMathBracketedSourceContainerId = function(id) {
        this.pmathBracketedSourceContainerId = id;
    };

    UpConversionAJAXControl.prototype.setCMathSourceContainerId = function(id) {
        this.cmathSourceContainerId = id;
    };

    UpConversionAJAXControl.prototype.setMaximaSourceContainerId = function(id) {
        this.maximaSourceContainerId = id;
    };

    UpConversionAJAXControl.prototype.verifyLater = function(verifyInputData) {
        this.doVerifyLater(verifyInputData);
    };

    return {
        replaceContainerContent: function(containerQuery, content) {
            containerQuery.empty();
            if (content!=null) {
                containerQuery.append(content);
            }
        },

        replaceContainerPreformattedText: function(containerQuery, content) {
            if (navigator.platform.substr(0,3)=='Win') { /* Convert to Windows line endings first */
                content = content.replace(/\n/g, "\r\n");
            }
            containerQuery.text(content);
        },

        replaceContainerMathMLContent: function(containerQuery, mathmlContent) {
            containerQuery.empty();
            containerQuery.appendMathML(mathmlContent);
            if (mathmlContent!=null) {
                /* Maybe schedule MathJax update */
                if (MathJax) {
                    MathJax.Hub.Queue(["Typeset", MathJax.Hub, containerQuery.get(0)]);
                }
            }
        },

        createUpConversionAJAXControl: function() {
            return new UpConversionAJAXControl();
        },

        getUpConversionServiceUrl: function() { return upConversionServiceUrl },
        setUpConversionServiceUrl: function(url) { upConversionServiceUrl = url },

        getDelay: function() { return delay },
        setDelay: function(newDelay) { delay = newDelay }
    };

})();

