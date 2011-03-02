/*
 *
 * Requirements:
 *
 * jquery.js (at least version 1.5.0)
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
