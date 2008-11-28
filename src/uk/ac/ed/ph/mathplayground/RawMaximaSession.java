/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple (and somewhat experimental) facade over {@link Runtime#exec(String[]))} that makes
 * it reasonably easy to perform a "conversion" or "session" with Maxima.
 * 
 * <h2>Usage Notes</h2>
 * 
 * <ul>
 *   <li>
 *     An instance of this class should only be used by one Thread at a time but is serially
 *     reusable.
 *   </li>
 *   <li>
 *     Call {@link #open()} to initiate the conversation with Maxima. This will start up a Maxima
 *     process and perform all required initialisation.
 *   </li>
 *   <li>
 *     Call {@link #executeRaw(String)}, {@link #executeExpectingSingleOutput(String)} and
 *     {@link #executeExpectingMultipleLabels(String)} to perform 1 or more calls to Maxima.
 *     (I have provided 3 methods here which differ only in how they process the output from
 *     Maxima. This will probably change in future once I have a clearer idea of what outputs
 *     we need.)
 *   </li>
 *   <li>
 *     Call {@link #close()} to close Maxima and tidy up afterwards.
 *     (You can call {@link #open()} again if you want to and start a new session up.
 *   </li>
 *   <li>
 *     Use the {@link #main(String[])} method for an example.
 *   </li>
 * </ul>
 * 
 * <h2>Bugs!</h2>
 * 
 * <ul>
 *   <li>
 *     Only tested so far on Linux.
 *   </li>
 *   <li>
 *     Need to parametrise the location of the Maxima executable.
 *   </li>
 *   <li>
 *     It's possible to confuse things if you ask Maxima to output something which looks
 *     like the input prompt (e.g. "(%i1)").
 *   </li>
 *   <li>
 *     Some Maxima commands (e.g. the one-argument version of 'tex') also output to STDOUT
 *     as well as returning a result. This will confuse the methods which attempt to grok
 *     Maxima's output.
 *   </li>
 *   <li>
 *     If you send an incomplete command to Maxima, it will hang indefinitely. I need to fix
 *     this!
 *   </li>
 * </ul>
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class RawMaximaSession {

    /** TODO: Change to logging framework? If so, which? */
    private static final Logger log = Logger.getLogger(RawMaximaSession.class.getName());
    
    /**
     * Change this to point to your Maxima executable.
     * 
     * TODO: This should be parametrised!
     */
    public static String MAXIMA_EXECUTABLE_PATH = "/usr/bin/maxima";

    /** 
     * Regexp that matches the Maxima input prompt. Used to determine when to switch between
     * reading and writing.
     */
    private static final Pattern inputPromptPattern = Pattern.compile("^\\(%i\\d+\\)\\s*\\z", Pattern.MULTILINE);

    /** Builds up output from each command */
    private final StringBuilder outputBuilder;

    /** Current Maxima process, or null if no session open */
    private Process maximaProcess;
    
    /** Reads Maxima output, or null if no session open */
    private BufferedReader maximaOutput;
    
    /** Writes to Maxima, or null if no session open */
    private PrintWriter maximaInput;

    public RawMaximaSession() {
        this.outputBuilder = new StringBuilder();
    }

    public void open() throws IOException {
        ensureNotStarted();

        /* Start up Maxima with the -q option (which suppresses the startup
         * message) */
        log.info("Starting Maxima");
        maximaProcess = Runtime.getRuntime().exec(new String[] { MAXIMA_EXECUTABLE_PATH, "-q" });

        /* Get at input and outputs streams, wrapped up as ASCII readers/writers */
        maximaOutput = new BufferedReader(new InputStreamReader(maximaProcess.getInputStream(), "ASCII"));
        maximaInput = new PrintWriter(new OutputStreamWriter(maximaProcess.getOutputStream(), "ASCII"));
        
        /* Wait for first input prompt */
        readUntilInputPrompt();
    }

    private String readUntilInputPrompt() throws IOException {
        log.info("Reading output from Maxima");
        outputBuilder.setLength(0);
        int c;
        do {
            c = maximaOutput.read();
            if (c==-1) {
                /* FIXME: Throw a better Exception */
                throw new IOException("Maxima output ended without finding input prompt");
            }
            outputBuilder.append((char) c);
            
            /* If there's currently no more to read, see if we're now sitting at
             * an input prompt. */
            if (!maximaOutput.ready()) {
                Matcher promptMatcher = inputPromptPattern.matcher(outputBuilder);
                if (promptMatcher.find()) {
                    /* Success. Trim off the prompt and store all of the raw output */
                    String result =  promptMatcher.replaceFirst("");
                    outputBuilder.setLength(0);
                    return result;
                }
            }
        }
        while (true);
    }

    public String executeRaw(String command) throws IOException {
        ensureStarted();
        log.info("Sending command '" + command + "' to Maxima");
        maximaInput.println(command);
        maximaInput.flush();
        if (maximaInput.checkError()) {
            /* FIXME: Throw a better Exception */
            throw new IOException("Could not send command to Maxima");
        }
        return readUntilInputPrompt();
    }
    
    public String executeExpectingSingleOutput(String command) throws IOException {
        String rawOutput = executeRaw(command);
        return rawOutput.trim().replaceFirst("\\(%o\\d+\\)\\s*", "");
    }
    
    public String[] executeExpectingMultipleLabels(String command) throws IOException {
        return executeExpectingSingleOutput(command).split("(?s)\\s*\\(%o\\d+\\)\\s*");
    }

    public int close() {
        ensureStarted();
        try {
            /* Ask Maxima to nicely close down by closing its input */
            log.info("Closing Maxima");
            maximaInput.close();
            if (maximaInput.checkError()) {
                log.warning("Forcibly terminating Maxima");
                maximaProcess.destroy();
                return maximaProcess.exitValue();
            }
            /* Wait for Maxima to shut down */
            try {
                return maximaProcess.waitFor();
            }
            catch (InterruptedException e) {
                log.warning("Interrupted waiting for Maxima to close - forcibly terminating");
                maximaProcess.destroy();
                return maximaProcess.exitValue();
            }
        }
        finally {
            resetState();
        }
    }
    
    private void resetState() {
        maximaProcess = null;
        maximaInput = null;
        maximaOutput = null;
        outputBuilder.setLength(0);
    }

    private void ensureNotStarted() {
        if (maximaProcess!=null) {
            throw new IllegalStateException("Session already opened");
        }
    }

    private void ensureStarted() {
        if (maximaProcess==null) {
            throw new IllegalStateException("Session not open - call open()");
        }
    }
    
    //---------------------------------------------------------------------------------

    public static void main(String[] args) throws IOException {
        RawMaximaSession session = new RawMaximaSession();
        
        session.open();
        
        /* Trivial command, raw output */
        System.out.println("Ex 1:" + session.executeExpectingSingleOutput("1;"));
        
        /* Same, but with output parsed as a single output (as expected) */
        System.out.println("Ex 2:" + session.executeExpectingSingleOutput("1;"));
        
        /* Multiple commands, raw output */
        System.out.println("Ex 3:" + session.executeRaw("2;3;"));
        
        /* Same, but output parsed */
        System.out.println("Ex 4:" + Arrays.toString(session.executeExpectingMultipleLabels("2;3;")));
        
        /* Similar. Note that the last command was terminated with '$' which causes Maxima not
         * to output its result.
         */
        System.out.println("Ex 5:" + Arrays.toString(session.executeExpectingMultipleLabels("x;y;z;4$")));
        
        /* Funny command - returns a result as well as outputting to STDOUT */
        System.out.println("Ex 6:" + session.executeRaw("tex(1/2);"));
        session.close();
    }
}
