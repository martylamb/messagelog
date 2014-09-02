package com.martiansoftware.messagelog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class MessageTransactionTest {
    
    public MessageTransactionTest() {
    }
    
    /**
     * Test of readFrom method, of class MessageTransaction.
     */
    @Test
    public void testSimpleWrite() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        
        byte[] b1 = {1, 2, 3, 4, 5};
        byte[] b2 = {0};
        byte[] b3 = {};
        byte[] b4 = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        
        MessageTransaction mt = new MessageTransaction(b1, b2, b3, b4);
        mt.writeTo(out);
        out.close();
        
        byte[] b = bout.toByteArray();
        assertEquals(12 + 16 + b1.length + b2.length + b3.length + b4.length, b.length);
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(b));
        din.readLong(); // crc
        
        assertEquals(4, din.readInt());
        assertEquals(b1.length, din.readInt());
        for (int i = 1; i <= b1.length; ++i) assertEquals(i, din.read());
        
        assertEquals(1, din.readInt());
        assertEquals(0, din.read());
        
        assertEquals(0, din.readInt());
        
        assertEquals(10, din.readInt());
        for (int i = 9; i >= 0; --i) assertEquals(i, din.read());
        
        assertEquals(-1, din.read());
    }
    
    @Test
    public void testRoundTrip() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        
        byte[] b1 = {1};
        byte[] b2 = {2, 2};
        byte[] b3 = {3, 3, 3};
        
        MessageTransaction mt = new MessageTransaction(b1, b2, b3);
        mt.writeTo(out);
        out.close();
        
        MessageTransaction mt2 = new MessageTransaction(new DataInputStream(new ByteArrayInputStream(bout.toByteArray())));
        int i = 0;
        for (byte[] b : mt2.getMessages()) {
            ++i;
            assertEquals(i, b.length);
            for (int j = 0; j < i; ++j) assertEquals(b[j], i);
        }
        assertEquals(3, i);
    }
    
    @Test
    public void testBadCRC() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        
        byte[] b1 = {1};
        MessageTransaction mt = new MessageTransaction(b1);
        mt.writeTo(out);
        out.close();
        
        byte[] b = bout.toByteArray();
        b[0] = (byte) ~b[0];

        try {
            MessageTransaction mt2 = new MessageTransaction(new DataInputStream(new ByteArrayInputStream(b)));
            fail("should have failed crc check!");
        } catch (IOException expected) {}
    }
}
