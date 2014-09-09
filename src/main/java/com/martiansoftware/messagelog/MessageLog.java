package com.martiansoftware.messagelog;

import java.io.IOException;
import java.util.List;



/*
public interface Message: 
    byte[] toByteArray

public interface MessageLog:
    log(Message... msgTransation)
    log(List<Message> msgTransaction)
    log(byte[].. msgTransaction)
    log(List<byte[]>) msgTransaction)

    replay(MessageHandler handler) - transactions are not bundled; they are just used to ensure atomic group writes

concrete implementations:
    (package) MessageOutput(DataOutput) // writes individual messages or transactions
    (package) MessageInput(DataInput)   // reads individual messages or transactions
    (package) MessageTransaction(byte[]...) // 
    (public)  FileMessageLog implements MessageLog
                contains a MessageOutput and MessageInput
*/
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
     * @param messages
     * @throws IOException 
     */
    public void log(byte[]... messages) throws IOException;
    
    /**
     * Logs one or more messages as a single transaction
     * 
     * @param messages
     * @throws IOException 
     */
    public void log(List<byte[]> messages) throws IOException;
    
    /**
     * Plays back the entire log of all messages, supplying them one-by-one
     * in order to the supplied MessageHandler
     * 
     * @param handler 
     */
    public void replay(MessageHandler handler) throws IOException;
}
