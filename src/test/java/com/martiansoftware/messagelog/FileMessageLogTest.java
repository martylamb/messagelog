package com.martiansoftware.messagelog;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class FileMessageLogTest {
    
    public FileMessageLogTest() {
    }
    
    private FileMessageLog newFML() throws IOException {
        File f = File.createTempFile("messagelog", "test");
        f.deleteOnExit();
        return new FileMessageLog(f);
    }
    
    private void write(FileMessageLog fml, String... messages) throws IOException {
        for (String s : messages) fml.log(s.getBytes());
    }
    
    @Test
    public void TestWrites() throws IOException {
        FileMessageLog fml = newFML();
        write(fml, "this", "is", "a", "test");
        fml.close();
    }
    
    /**
     * Test of setAutoSync method, of class FileMessageLog.
     */
    @Test
    public void testSetAutoSync() throws IOException {
        FileMessageLog fml = newFML().setAutoSync(true);
        write(fml, "this", "is", "a", "test");
        fml.close();
    }

    @Test
    public void testSyncTiming() throws IOException {
        long start, end, dur1, dur2;

        FileMessageLog fml = newFML();
        start = System.currentTimeMillis();
        for (int i = 0; i < 25; ++i) write(fml, "test " + i);
        fml.close();
        end = System.currentTimeMillis();
        dur1 = end - start;
        
        fml = newFML().setAutoSync(true);
        start = System.currentTimeMillis();
        for (int i = 0; i < 25; ++i) write(fml, "test " + i);
        fml.close();
        end = System.currentTimeMillis();
        dur2 = end - start;
        
        assertTrue(dur2 > dur1);
    }
    
    /**
     * Test of replay method, of class FileMessageLog.
     */
    @Test
    public void testReplay() throws Exception {
        
        FileMessageLog fml = newFML();
        for (int i = 0; i < 1000; ++i) write(fml, "test " + i);
        fml.close();

        TestMessageHandler tmh = new TestMessageHandler();
        fml = new FileMessageLog(fml.getFile(), tmh);
        
        assertEquals(1000, tmh.getCount());
    }
    
    private class TestMessageHandler implements MessageHandler {
        int x = 0;
        
        @Override
        public void handleMessage(byte[] message) {
            assertEquals("test " + x, new String(message));
            ++x;
        }
        
        public int getCount() { return x; }
        
    }
}
