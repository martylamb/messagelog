package com.martiansoftware.util;

import com.martiansoftware.messagelog.FileMessageLog;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
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
public class EventSourcedKVMapTest {
    
    public EventSourcedKVMapTest() {
    }
    
    private File newTestFile() throws IOException {
        File f = File.createTempFile(this.getClass().getName(), "test");
        f.deleteOnExit();
        return f;
    }
    
    @Test
    public void testGetsAndPuts() throws IOException {
        File f = newTestFile();
        EventSourcedKVMap m = new EventSourcedKVMap(f);
        m.put("1", "one");
        m.put("2", "two");
        m.put("3", "trois");
        m.put("3", "three");
        m.close();
        
        m = new EventSourcedKVMap(f);
        assertEquals("three", m.get("3"));
        assertEquals(3, m.size());
        m.put("4", "four");
        m.close();

        m = new EventSourcedKVMap(f);
        assertEquals("four", m.get("4"));
        assertEquals(4, m.size());
        m.put("1", "un");
        m.close();
        
        m = new EventSourcedKVMap(f);
        assertEquals("un", m.get("1"));
        assertEquals("two", m.get("2"));
        assertEquals("three", m.get("3"));
        assertEquals("four", m.get("4"));
        assertEquals(4, m.size());
    }
}
