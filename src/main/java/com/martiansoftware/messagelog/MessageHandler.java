package com.martiansoftware.messagelog;

/**
 *
 * @author mlamb
 */
public interface MessageHandler {
    public void handleMessage(byte[] message);
}
