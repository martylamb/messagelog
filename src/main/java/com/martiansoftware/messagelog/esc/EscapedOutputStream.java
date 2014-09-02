package com.martiansoftware.messagelog.esc;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author mlamb
 */
class EscapedOutputStream extends FilterOutputStream implements EscapedIO {

    private boolean started = false;
    
    public EscapedOutputStream(OutputStream out) {
        super(out);
    }
    
    @Override public void write(int i) throws IOException {
        if (!started) {
            super.write(MESSAGESEP);
            started = true;
        }
        switch(i) {
            case ESC: super.write(ESC); super.write(ESC); break;
            case MESSAGESEP: super.write(ESC); super.write(FALSEMESSAGESEP); break;
            default: super.write(i);
        }
    }
    
    @Override public void write(byte[] b, int offset, int len) throws IOException {
        for (int i = offset; i < offset + len; ++i) {
            write(b[i]);
        }
    }
    
    @Override public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    
    // does NOT close the underlying stream!
    @Override public void close() throws IOException {
        super.write(MESSAGESEP);
        started = false;
        flush();
    }
}
