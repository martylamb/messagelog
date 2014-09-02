package com.martiansoftware.messagelog.esc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class EscapedInputStreamTest {
    
    public EscapedInputStreamTest() {
    }
    
    @org.junit.Test
    public void testRoundTrip() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        EscapedOutputStream out = new EscapedOutputStream(bout);
        for (int i = 0; i < 256; ++i) out.write(i);
        out.close();
        byte[] b = bout.toByteArray();
        assertEquals(260, b.length);
        
        EscapedInputStream in = new EscapedInputStream(new ByteArrayInputStream(b));
        assertEquals(-1, in.read());
        assertFalse(in.eof());
        in.next();
        for (int i = 0; i < 256; ++i) {
            assertEquals(i, in.read());
        }
        assertEquals(-1, in.read());
        assertEquals(-1, in.read()); // should keep returning eof until next
        assertFalse(in.eof());
        in.next();
        assertEquals(-1, in.read());
        assertTrue(in.eof());
    }
    
    @Test
    public void testNotImplemented() {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] {1, 2, 3});
        EscapedInputStream in = new EscapedInputStream(bin);
        
        assertFalse(in.markSupported());
            
        try {
            in.skip(2);
            fail("skip should not be supported.");
        } catch (IOException expected) {}
            
        try {
            in.reset();
            fail("reset should not be supported.");
        } catch (IOException expected) {}
    }
    
    @Test
    public void testNext() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] {EscapedIO.MESSAGESEP, 1, 2, 3, EscapedIO.MESSAGESEP, 4});
        EscapedInputStream in = new EscapedInputStream(bin);
        
        assertEquals(-1, in.read());
        in.next();
        assertEquals(1, in.read());
        in.next();
        assertEquals(4, in.read());
        in.next();
    }

    @Test
    public void testNextWithConsecutiveEmptyMessages() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] {EscapedIO.MESSAGESEP, 1, 2, EscapedIO.MESSAGESEP, EscapedIO.MESSAGESEP, 3, EscapedIO.MESSAGESEP, 4});
        EscapedInputStream in = new EscapedInputStream(bin);
        
        assertEquals(-1, in.read());
        in.next();
        assertEquals(1, in.read());
        in.next();
        in.next();
        assertEquals(3, in.read());
        in.next();
    }

    @Test
    public void testInvalidEscapeChar() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] {1, 2, EscapedIO.ESC, 3, EscapedIO.MESSAGESEP, 4});
        EscapedInputStream in = new EscapedInputStream(bin);

        byte[] b = new byte[4];
        assertEquals(1, in.read());
        assertEquals(2, in.read());
        try {
            int i = in.read();
            fail("Invalid escape sequence should have failed.");
        } catch (IOException expected) {}
    }
    
    @Test
    public void readArray() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] {0, 1, 2, 3, 4, 5, EscapedIO.ESC, EscapedIO.FALSEMESSAGESEP, 7, 8, 9, EscapedIO.MESSAGESEP});
        EscapedInputStream in = new EscapedInputStream(bin);

        byte[] b = new byte[10];
        assertEquals(10, in.read(b));
        assertEquals(3, b[3]);
        assertEquals(EscapedIO.MESSAGESEP, b[6]);
        assertEquals(7, b[7]);
        assertEquals(-1, in.read());    
    }
    
    @Test
    public void readShortArray() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] {0, 1, 2, 3, 4, 5, EscapedIO.ESC, EscapedIO.FALSEMESSAGESEP, 7, 8, 9, EscapedIO.MESSAGESEP});
        EscapedInputStream in = new EscapedInputStream(bin);

        byte[] b = new byte[100];
        assertEquals(10, in.read(b));
        assertEquals(3, b[3]);
        assertEquals(EscapedIO.MESSAGESEP, b[6]);
        assertEquals(7, b[7]);
        assertEquals(-1, in.read());            
    }
}
