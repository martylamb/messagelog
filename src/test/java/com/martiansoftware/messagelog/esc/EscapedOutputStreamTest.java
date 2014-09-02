package com.martiansoftware.messagelog.esc;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class EscapedOutputStreamTest {
    
    public EscapedOutputStreamTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    @org.junit.Test
    public void testWrite1() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        EscapedOutputStream out = new EscapedOutputStream(bout);
        out.write(1);
        out.write(new byte[]{2, 3, 4});
        out.write(new byte[]{4, 5, 6, 7, 8, 9}, 1, 5);
        out.close();
        
        out.write(3);
        out.close();
        
        assertEquals(hex(new byte[] {EscapedIO.MESSAGESEP, 1, 2, 3, 4, 5, 6, 7, 8, 9, EscapedIO.MESSAGESEP, EscapedIO.MESSAGESEP, 3, EscapedIO.MESSAGESEP}),
                hex(bout.toByteArray()));
    }
    
    @org.junit.Test
    public void testWrite2() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        EscapedOutputStream out = new EscapedOutputStream(bout);
        out.write(EscapedIO.ESC);
        out.write(EscapedIO.FALSEMESSAGESEP);
        out.write(EscapedIO.MESSAGESEP);
        out.close();
        
        assertEquals(hex(new byte[] {EscapedIO.MESSAGESEP,
                                    EscapedIO.ESC, EscapedIO.ESC,
                                    'n',
                                    EscapedIO.ESC, EscapedIO.FALSEMESSAGESEP,
                                    EscapedIO.MESSAGESEP}),
                     hex(bout.toByteArray()));
    }
    
    static String hex(byte[] ba) {
        StringBuilder s = new StringBuilder();
        for (byte b : ba) s.append(String.format("%02x", b));
        return s.toString();
    }
}
