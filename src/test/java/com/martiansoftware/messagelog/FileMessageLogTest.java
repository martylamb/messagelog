package com.martiansoftware.messagelog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
        
        fml = newFML();
        List<byte[]> list = new java.util.ArrayList<>();
        list.add("this".getBytes());
        list.add("is".getBytes());
        list.add("a".getBytes());
        list.add("test".getBytes());
        fml.log(list);
        fml.close();
    }
    
    @Test
    public void TestOverlap() throws IOException {
        FileMessageLog fml = newFML();        
        try {
            FileMessageLog fml2 = new FileMessageLog(fml.getFile());
            fail("Able to open same FileMessageLog twice!");
        } catch (IOException expected) {}
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

    private int countMessages(File f) throws IOException {
        TestMessageHandler tmh = new TestMessageHandler();
        FileMessageLog fml = new FileMessageLog(f, tmh);
        fml.close();
        return tmh.getCount();
    }
    
    private void shrink(File f) throws IOException {
        RandomAccessFile r = new RandomAccessFile(f, "rw");
        r.setLength(r.length() - 1);        
    }
    
    @Test
    public void testTruncatedLog() throws IOException {
        FileMessageLog fml = newFML();
        File f = fml.getFile();
        for (int i = 0; i < 10; ++i) fml.log(("test " + i).getBytes());
        fml.close();
        
        assertEquals(10, countMessages(f));
        long len = f.length();
        
        fml = new FileMessageLog(f);
        fml.log(("test 10").getBytes());
        fml.close();
        long len2 = f.length();        
        assertEquals(11, countMessages(f));
        
        while (f.length() > len) {
            shrink(f);
            assertEquals(10, countMessages(f));
        }
        
        shrink(f);
        assertEquals(9, countMessages(f));
    }
    
    @Test
    public void testSyncTiming() throws IOException {
        long start, end, dur1, dur2;

        FileMessageLog fml = newFML();
        start = System.currentTimeMillis();
        for (int i = 0; i < 2500; ++i) write(fml, "test " + i);
        fml.close();
        end = System.currentTimeMillis();
        dur1 = end - start;
        
        fml = newFML().setAutoSync(true);
        start = System.currentTimeMillis();
        for (int i = 0; i < 2500; ++i) write(fml, "test " + i);
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
        
        TestMessageHandler tmh2 = new TestMessageHandler();
        fml.replay(tmh2);        
        assertEquals(1000, tmh2.getCount());
        
        fml.replay(null);
    }
    
    @Test
    public void testNoMessage() throws Exception {
        FileMessageLog fml = newFML();
        fml.log();
        fml.close();
        assertEquals(0, fml.getFile().length());
        
        fml = newFML();
        fml.log(new java.util.ArrayList<byte[]>());
        fml.close();
        assertEquals(0, fml.getFile().length());
        
        fml = newFML();
        fml.log((List) null);
        fml.close();
        assertEquals(0, fml.getFile().length());
        
        fml = newFML();
        fml.log((byte[][]) null);
        fml.close();
        assertEquals(0, fml.getFile().length());
    }
    
    @Test
    public void noLogAfterClose() throws Exception {        
        FileMessageLog fml = newFML();
        fml.log(new byte[]{1});
        fml.close();
        try {
            fml.log(new byte[]{2});
            fail("Able to write after close!");
        } catch (IOException expected) {}
    }
    
    @Test
    public void testCorruptedFile() throws Exception {
        FileMessageLog fml = newFML();
        fml.log(new byte[]{1, 1, 1, 1, 1, 1, 1});
        fml.sync();
        long pos = fml.getFile().length();
        fml.log(new byte[]{2, 2, 2, 2, 2, 2, 2});
        fml.close();

        RandomAccessFile r = new RandomAccessFile(fml.getFile(), "rw");
        r.seek(pos - 1);
        r.write(2);
        r.close();
        
        try {
            fml = new FileMessageLog(fml.getFile());
            fail("Loaded corrupted file!");
        } catch(IOException expected) {}
        
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
