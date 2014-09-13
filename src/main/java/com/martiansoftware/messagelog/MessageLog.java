package com.martiansoftware.messagelog;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A simple interface for logging messages, where a "message" is defined to be
 * an arbitrary byte array.  If more than one message is logged in a single call,
 * the entire set of messages is written as a single transaction; any failures
 * to log a single message will result in no messages from that call being logged.
 * 
 * @author mlamb
 */
public interface MessageLog {    
    /**
     * Logs one or more messages as a single transaction
     * 
     * @param messages the message(s) to log
     * @return this MessageLog
     * @throws IOException 
     */
    public MessageLog log(byte[]... messages) throws IOException;
    
    /**
     * Logs one or more messages as a single transaction
     * 
     * @param messages the message(s) to log
     * @return this MessageLog
     * @throws IOException 
     */
    public MessageLog log(List<byte[]> messages) throws IOException;
    
    /**
     * Returns a DataOutputStream that can be written to directly.  When the
     * DataOutputStream is closed, its contents are written as a single message
     * to the log.
     * 
     * @return a DataOutputStream that can be written to directly as a log message.
     */
    public DataOutputStream getLogOutputStream();
    
    /**
     * Plays back the entire log of all messages, supplying them one-by-one
     * in order to the supplied MessageHandler
     * 
     * @param handler a MessageHandler that will receive a callback for each
     * message played back
     * @return this MessageLog
     * @throws IOException
     */
    public MessageLog replay(MessageHandler handler) throws IOException;
}
