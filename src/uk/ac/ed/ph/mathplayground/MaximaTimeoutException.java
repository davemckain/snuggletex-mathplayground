/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

/**
 * Exception thrown when Maxima takes too long to respond to a command,
 * usually indicating bad input.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class MaximaTimeoutException extends Exception {
    
    private static final long serialVersionUID = 6077105489157609103L;

    private final int timeoutSeconds;

    public MaximaTimeoutException(int timeoutSeconds) {
        super("Timeout of " + timeoutSeconds + "s exceeded waiting for response from Maxima");
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
