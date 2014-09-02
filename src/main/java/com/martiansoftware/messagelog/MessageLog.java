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
 *
 * @author mlamb
 */
public interface MessageLog {    
    public void log(byte[]... messages) throws IOException;
    public void log(List<byte[]> messages) throws IOException;
}
