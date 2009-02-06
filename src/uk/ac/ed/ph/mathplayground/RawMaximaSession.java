/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.mathplayground;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

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
 *     Only tested so far on Linux with Maxima running on SBCL. This revision should fix
 *     issues reported by Graham Smith whereby each expression in the input was causing
 *     an input prompt to be displayed, which was messing up my "expect"-style code.
 *     FIXME: I now need to update the stripping of the output to remove any input prompts
 *     that may appear here!!
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
 * </ul>
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class RawMaximaSession {

    private static final Logger log = Logger.getLogger(RawMaximaSession.class);
    
    public static final String MAXIMA_EXECUTABLE_PATH_PROPERTY = "uk.ac.ed.ph.mathplayground.maxima.path";
    public static final String MAXIMA_ENVIRONMENT_PROPERTY_BASE = "uk.ac.ed.ph.mathplayground.maxima.env";
    public static final String MAXIMA_TIMEOUT_PROPERTY = "uk.ac.ed.ph.mathplayground.maxima.timeout";
    
    /** Default timeout (in seconds) to use if not specified by property */
    public static int DEFAULT_TIMEOUT = 5;
    
    private final ExecutorService executor;

    /** Current Maxima process, or null if no session open */
    private Process maximaProcess;
    
    /** Reads Maxima standard output, or null if no session open */
    private BufferedReader maximaOutput;
    
    /** Reads Maxima standard error, or null if no session open */
    private BufferedReader maximaErrorStream;
    
    /** Writes to Maxima, or null if no session open */
    private PrintWriter maximaInput;
    
    /** Timeout in seconds to wait for response from Maxima before killing session */
    private int timeout;
    
    /** Builds up output from each command */
    private final StringBuilder outputBuilder;
    private final StringBuilder errorOutputBuilder;

    public RawMaximaSession() {
        this.executor = Executors.newFixedThreadPool(1);
        this.timeout = getDefaultTimeout();
        this.outputBuilder = new StringBuilder();
        this.errorOutputBuilder = new StringBuilder();
    }
    
    /**
     * @throws MaximaConfigurationException
     * @return
     */
    public int getDefaultTimeout() {
        String systemDefaultTimeout = System.getProperty(MAXIMA_TIMEOUT_PROPERTY);
        int result;
        if (systemDefaultTimeout!=null) {
            try {
                result = Integer.parseInt(systemDefaultTimeout);
            }
            catch (NumberFormatException e) {
                throw new MaximaConfigurationException("Default timeout " + systemDefaultTimeout + " must be an integer");
            }
        }
        else {
            result = DEFAULT_TIMEOUT;
        }
        return result;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    /**
     * 
     * @throws MaximaTimeoutException
     * @throws MaximaRuntimeException
     * @throws MaximaConfigurationException
     */
    public void open() throws MaximaTimeoutException {
        ensureNotStarted();
        
        /* Extract relevant system properties required to get Maxima running */
        String maximaPath = System.getProperty(MAXIMA_EXECUTABLE_PATH_PROPERTY);
        if (maximaPath==null) {
            throw new MaximaConfigurationException("System property " + MAXIMA_EXECUTABLE_PATH_PROPERTY
                    + " must be set to point to the Maxima executable");
        }
        
        List<String> environmentList = new ArrayList<String>();
        String environmentProperty;
        for (int i=0; ;i++) {
            environmentProperty = System.getProperty(MAXIMA_ENVIRONMENT_PROPERTY_BASE + i);
            if (environmentProperty!=null) {
                environmentList.add(environmentProperty);
            }
            else {
                break;
            }
        }

        /* Start up Maxima with the -q option (which suppresses the startup message) */
        log.info("Starting Maxima at " + maximaPath);
        log.info("Using Environment " + environmentList);
        try {
            maximaProcess = Runtime.getRuntime().exec(new String[] { maximaPath, "-q" },
                    environmentList.toArray(new String[environmentList.size()]));
        }
        catch (IOException e) {
            throw new MaximaConfigurationException("Could not launch Maxima process", e);
        }

        /* Get at input and outputs streams, wrapped up as ASCII readers/writers */
        try {
            maximaOutput = new BufferedReader(new InputStreamReader(maximaProcess.getInputStream(), "ASCII"));
            maximaErrorStream = new BufferedReader(new InputStreamReader(maximaProcess.getErrorStream(), "ASCII"));
            maximaInput = new PrintWriter(new OutputStreamWriter(maximaProcess.getOutputStream(), "ASCII"));
        }
        catch (UnsupportedEncodingException e) {
            throw new MaximaRuntimeException("Could not extract Maxima IO stream", e);
        }

        /* Wait for first input prompt */
        readUntilFirstInputPrompt("%i");
    }

    private String readUntilFirstInputPrompt(String inchar) throws MaximaTimeoutException {
        Pattern promptPattern = Pattern.compile("^\\(\\Q" + inchar + "\\E\\d+\\)\\s*\\z", Pattern.MULTILINE);
        FutureTask<String> maximaCall = new FutureTask<String>(new MaximaCallable(promptPattern));

        executor.execute(maximaCall);
        
        String result = null;
        try {
            if (timeout > 0) {
                /* Wait until timeout */
                log.info("Doing Maxima call with timeout " + timeout + "s");
                result = maximaCall.get(timeout, TimeUnit.SECONDS);
            }
            else {
                /* Wait indefinitely (this can be dangerous!) */
                log.info("Doing Maxima call without timeout");
                result = maximaCall.get();
            }
        }
        catch (TimeoutException e) {
            log.error("Timeout was exceeded waiting for Maxima - killing the session");
            close();
            throw new MaximaTimeoutException(timeout);
        }
        catch (Exception e) {
            throw new MaximaRuntimeException("Unexpected Exception", e);
        }
        return result;
    }
    
    private class MaximaCallable implements Callable<String> {
        
        private final Pattern promptPattern;
        
        public MaximaCallable(Pattern promptPattern) {
            this.promptPattern = promptPattern;
        }
        
        private void handleReadFailure(String message) {
            throw new MaximaRuntimeException(message + "\nOutput buffer at this time was '"
                    + outputBuilder.toString()
                    + "'\nError buffer at this time was '"
                    + errorOutputBuilder.toString()
                    + "'");
        }
        
        private boolean absorbErrors() throws IOException {
            int errorChar;
            while (maximaErrorStream.ready()) {
                errorChar = maximaErrorStream.read();
                if (errorChar!=-1) {
                    errorOutputBuilder.append((char) errorChar);
                }
                else {
                    /* STDERR has closed */
                    return true;
                }
            }
            return false;
        }
        
        public String call() {
            log.info("Reading output from Maxima until first prompt matching " + promptPattern);
            outputBuilder.setLength(0);
            errorOutputBuilder.setLength(0);
            int outChar;
            try {
                for (;;) {
                    /* First absorb anything the error stream wants to say */
                    absorbErrors();
                    
                    /* Block on standard output */
                    outChar = maximaOutput.read();
                    if (outChar==-1) {
                        /* STDOUT has finished. See if there are more errors */
                        absorbErrors();
                        handleReadFailure("Maxima STDOUT and STDERR closed before finding an input prompt");
                    }
                    outputBuilder.append((char) outChar);
                    
                    /* If there's currently no more to read, see if we're now sitting at
                     * an input prompt. */
                    if (!maximaOutput.ready()) {
                        Matcher promptMatcher = promptPattern.matcher(outputBuilder);
                        if (promptMatcher.find()) {
                            /* Success. Trim off the prompt and store all of the raw output */
                            String result =  promptMatcher.replaceFirst("");
                            outputBuilder.setLength(0);
                            return result;
                        }
                        /* If we're here then we're not at a prompt - Maxima must still be thinking
                         * so loop through again */
                        continue;
                    }
                }
            }
            catch (MaximaRuntimeException e) {
                close();
                throw e;
            }
            catch (IOException e) {
                /* If anything has gone wrong, we'll close the Session */
                throw new MaximaRuntimeException("IOException occurred reading from Maxima", e);
            }
        }
    }
    
    private String doMaximaUntil(String input, String inchar) throws MaximaTimeoutException {
        ensureStarted();
        log.info("Sending input '" + input + "' to Maxima");
        maximaInput.println(input);
        maximaInput.flush();
        if (maximaInput.checkError()) {
            throw new MaximaRuntimeException("An error occurred sending input to Maxima");
        }
        return readUntilFirstInputPrompt(inchar);
    }

    public String executeRaw(String command) throws MaximaTimeoutException {
        String rawOutput = doMaximaUntil(command + "inchar: %x$", "%x");
        
        /* Reset prompt and kill off last 3 results */
        doMaximaUntil("inchar: %i$ kill(3)$", "%i");
        
        return rawOutput;
    }
    
    /**
     * TODO: Not sure how useful this is here?
     * 
     * @throws MaximaTimeoutException
     * @throws MaximaRuntimeException
     */
    public String executeExpectingSingleOutput(String command) throws MaximaTimeoutException {
        String rawOutput = executeRaw(command);
        return rawOutput.trim().replaceFirst("\\(%o\\d+\\)\\s*", "");
    }
    
    
    /**
     * TODO: Not sure how useful this is here?
     * 
     * @throws MaximaTimeoutException
     * @throws MaximaRuntimeException
     */
    public String[] executeExpectingMultipleLabels(String command) throws MaximaTimeoutException {
        return executeExpectingSingleOutput(command).split("(?s)\\s*\\(%o\\d+\\)\\s*");
    }

    public int close() {
        ensureStarted();
        try {
            /* Close down executor */
            executor.shutdown();
            
            /* Ask Maxima to nicely close down by closing its input */
            log.info("Closing Maxima nicely");
            maximaInput.close();
            if (maximaInput.checkError()) {
                log.warn("Forcibly terminating Maxima");
                maximaProcess.destroy();
                return maximaProcess.exitValue();
            }
            /* Wait for Maxima to shut down */
            try {
                return maximaProcess.waitFor();
            }
            catch (InterruptedException e) {
                log.warn("Interrupted waiting for Maxima to close - forcibly terminating");
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
        maximaErrorStream = null;
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

    public static void main(String[] args) throws MaximaTimeoutException {
        /* Set up local environment */
        System.setProperty(MAXIMA_EXECUTABLE_PATH_PROPERTY, "/opt/local/bin/maxima");
        System.setProperty(MAXIMA_TIMEOUT_PROPERTY, "1");
        System.setProperty(MAXIMA_ENVIRONMENT_PROPERTY_BASE + "0", "PATH=/usr/bin:/opt/local/bin");
        
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
        
        /* Command that hangs Maxima */
        System.out.println("Ex 7:" + session.executeRaw("1"));
        
        session.close();
    }
}
