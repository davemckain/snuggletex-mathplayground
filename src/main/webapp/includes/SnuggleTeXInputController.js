/*
 *
 * Requirements:
 *
 * jquery.js (at least version 1.5.0)
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

var SnuggleTeXInputController = (function() {

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

                /* Nullify blank input to indicate that the display should be empty */
                if (!latexInput.match(/\S/)) {
                    latexInput = null;
                }

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
        createInputWidget: function(snuggleTeXInputControlId, verifierControl) {
            if (snuggleTeXInputControlId==null) {
                throw new Error("snuggleTeXInputControlId must not be null");
            }
            if (verifierControl==null) {
                throw new Error("verifierControl must not be null");
            }
            return new Widget(snuggleTeXInputControlId, verifierControl);
        }
    };

})();
