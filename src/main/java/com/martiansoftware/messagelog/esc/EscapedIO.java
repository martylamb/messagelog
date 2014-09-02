package com.martiansoftware.messagelog.esc;

/**
 *
 * @author mlamb
 */
interface EscapedIO {
    public static final int ESC = '\\'; // escape character; encoded in stream as ESC ESC
    public static final int MESSAGESEP = 0x0a; // message separator; encoded in stream as MESSAGESEP FALSEMESSAGESEP
    public static final int FALSEMESSAGESEP = 'n'; // "false" message separator; treated as MESSAGESEP when preceded by ESC
}
