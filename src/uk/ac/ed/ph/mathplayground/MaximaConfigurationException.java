/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

/**
 * Runtime Exception thrown to indicate a problem with the configuration of
 * Maxima, such as a bad path or environment.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class MaximaConfigurationException extends MaximaRuntimeException {

    private static final long serialVersionUID = 7100573731627419599L;

    public MaximaConfigurationException(String message) {
        super(message);
    }

    public MaximaConfigurationException(Throwable cause) {
        super(cause);
    }

    public MaximaConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
