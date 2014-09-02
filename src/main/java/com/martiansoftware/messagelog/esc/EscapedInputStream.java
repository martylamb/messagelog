package com.martiansoftware.messagelog.esc;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author mlamb
 */
public class EscapedInputStream extends FilterInputStream implements EscapedIO {
    
    private boolean escaping = false;
    private boolean eof = false;
    private boolean realEof = false;
    
    public EscapedInputStream(InputStream in) {
        super(in);
    }
    
    @Override public boolean markSupported() { return false; }
    
    @Override public int read() throws IOException {
        if (eof) return -1;
        int i = super.read();
        if (i < 0) {
            realEof = eof = true;
            return -1;
        } else {
            realEof = false;
            if (escaping) {
                escaping = false;
                switch(i) {
                    case ESC: return ESC;
                    case FALSEMESSAGESEP: return MESSAGESEP;
                    default: throw new IOException("Invalid escape character: " + i);
                }
            } else {
                switch(i) {
                    case ESC: escaping = true; return read();
                    case MESSAGESEP: eof = true; return -1;
                    default: return i;
                }
            }
        }
    }
    
    @Override public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    @Override public int read(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < len; ++i) {
            int c = read();
            if (c < 0) return i;
            b[off + i] = (byte) c;
        }
        return len;
    }
    
    @Override public void reset() throws IOException {
        throw new IOException("reset not supported.");
    }
    
    @Override public long skip(long n) throws IOException {
        throw new IOException("skip not supported.");
    }
    
    public void next() throws IOException {
        while (!eof) read();
        escaping = false;
        eof = false;
    }
    
    public boolean eof() {
        return realEof;
    }
}
