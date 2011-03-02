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

var UpConversionAJAXController = (function() {

    var upConversionServiceUrl = null; /* Caller must fill in */
    var delay = 300;
    var usingMathJax = false;

    var STATUS_WAITING             = 0;
    var STATUS_SUCCESS             = 1;
    var STATUS_PARSE_ERROR         = 2;
    var STATUS_UPCONVERSION_FAILED = 3;
    var STATUS_UNKNOWN_ERROR       = 4;

    /************************************************************/

    var UpConversionAJAXControl = function() {
        this.bracketedRenderingContainerId = null;
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

        this.updateUpConversionContainer = function(status, mathElementString, errorContent) {
            if (this.bracketedRenderingContainerId!=null) {
                var bracketedRenderingContainer = jQuery("#" + this.bracketedRenderingContainerId);
                /* Set up children if not done already */
                if (bracketedRenderingContainer.children().size()==0) {
                    bracketedRenderingContainer.html("<div class='upConversionAJAXControlStatus'></div>"
                        + "<div class='upConversionAJAXControlMessage'></div>"
                        + "<div class='upConversionAJAXControlResult'></div>");
                }
                var statusContainer = bracketedRenderingContainer.children().first();
                var messageContainer = statusContainer.next();
                var resultContainer = messageContainer.next();
                switch(status) {
                    case STATUS_WAITING:
                        statusContainer.attr('class', 'upConversionAJAXControlStatus waiting');
                        this.showMessage(messageContainer, "Checking...");
                        this.showMessage(resultContainer, null);
                        break;

                    case STATUS_SUCCESS:
                        statusContainer.attr('class', 'upConversionAJAXControlStatus success');
                        this.showMessage(messageContainer, "Your input makes sense. It has been interpreted as:");
                        this.showXML(resultContainer, mathElementString);
                        break;

                    case STATUS_PARSE_ERROR:
                        statusContainer.attr('class', 'upConversionAJAXControlStatus failure');
                        this.showMessage(messageContainer, "SnuggleTeX could not parse your input:");
                        this.showMessage(resultContainer, errorContent);
                        break;

                    case STATUS_UPCONVERSION_FAILED:
                        statusContainer.attr('class', 'upConversionAJAXControlStatus failure');
                        this.showMessage(messageContainer, "I could not make sense of your input");
                        this.showMessage(resultContainer, null);
                        break;

                    case STATUS_UNKNOWN_ERROR:
                        statusContainer.attr('class', 'upConversionAJAXControlStatus error');
                        this.showMessage(messageContainer, "Unexpected error");
                        this.showMessage(resultContainer, null);
                        break;
                }
            }

        };

        this.showMessage = function(containerQuery, html) {
            UpConversionAJAXController.replaceContainerContent(containerQuery, html || "\xa0");
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
            UpConversionAJAXController.replaceContainerContent(containerQuery, content);
        };

    };

    UpConversionAJAXControl.prototype.setBracketedRenderingContainerId = function(id) {
        this.bracketedRenderingContainerId = id;
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

                /* Maybe schedule MathJax update if this is a MathML Element */
                if (usingMathJax && (content.nodeType && content.nodeType==1 && content.nodeName=="math")
                        || (content.substr && content.substr(0,6)=='<math ')) {
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
        setDelay: function(newDelay) { delay = newDelay },

        isUsingMathJax: function() { return usingMathJax },
        setUsingMathJax: function(newUsingMathJax) { usingMathJax = newUsingMathJax }
    };

})();

