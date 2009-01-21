/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

/**
 * Runtime Exception thrown to indicate an unexpected problem
 * encountered when communicating with Maxima.
 * <p>
 * This Exception is unchecked as there's nothing that can reasonably be done
 * to recover from this so ought to bubble right up to a handler near the "top"
 * of your application.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class MaximaRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 7100573731627419599L;

    public MaximaRuntimeException(String message) {
        super(message);
    }

    public MaximaRuntimeException(Throwable cause) {
        super(cause);
    }

    public MaximaRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
