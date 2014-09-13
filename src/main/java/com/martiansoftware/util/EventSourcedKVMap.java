package com.martiansoftware.util;

import com.martiansoftware.messagelog.FileMessageLog;
import com.martiansoftware.messagelog.MessageHandler;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author mlamb
 */
public class EventSourcedKVMap implements Map<String, String> {

    private final FileMessageLog _fml;
    private final TreeMap<String, String> _map = new TreeMap<>();
    
    public EventSourcedKVMap(File f) throws IOException {
        _fml = new FileMessageLog(f, new MH());
    }
    
    @Override public int size() { return _map.size(); }
    @Override public boolean isEmpty() { return _map.isEmpty(); }
    @Override public boolean containsKey(Object o) { return _map.containsKey(o); }
    @Override public boolean containsValue(Object o) { return _map.containsValue(o); }
    @Override public String get(Object o) { return _map.get(o); }
    @Override public Set<String> keySet() { return Collections.unmodifiableSet(_map.keySet()); }
    @Override public Collection<String> values() { return Collections.unmodifiableCollection(_map.values()); }
    @Override public Set<Entry<String, String>> entrySet() { return Collections.unmodifiableSet(_map.entrySet()); }

    public void close() throws IOException { _fml.close(); }
    public void sync() throws IOException { _fml.sync(); }
    
    // it's utter insanity that writeUTF doesn't handle nulls.
    private void writeString(DataOutput out, String s) throws IOException {
        out.writeBoolean(s == null);
        if (s != null) out.writeUTF(s);
    }    
    private String readString(DataInput in) throws IOException {
        boolean isNull = in.readBoolean();
        return isNull ? null : in.readUTF();
    }
    
    @Override
    public String put(String k, String v) {
        synchronized(_fml) {
            String result = _map.put(k, v);
            try {
                DataOutputStream d = _fml.getLogOutputStream();
                d.writeChar('p');
                writeString(d, k);
                writeString(d, v);
                d.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }            
            return result;
        }
    }

    @Override
    public String remove(Object o) {
        if (!(o instanceof String)) return null;
        String k = (String) o;
        synchronized(_fml) {
            String result = _map.remove(k);
            try {
                DataOutputStream d = _fml.getLogOutputStream();
                d.writeChar('r');
                writeString(d, k);
                d.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }            
            return result;
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        if (map.isEmpty()) return;
        synchronized(_fml) {
            _map.putAll(map);
            try {
                DataOutputStream d = _fml.getLogOutputStream();
                d.writeChar('P');
                d.writeInt(map.size());
                for (Map.Entry<? extends String, ? extends String> e : map.entrySet()) {
                    writeString(d, e.getKey());
                    writeString(d, e.getValue());
                }
                d.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }            
        }
    }

    @Override
    public void clear() {
        synchronized(_fml) {
            if (_map.isEmpty()) return;
            try {
                _fml.log(new byte[] {'c'});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }                        
        }
    }
    
    private class MH implements MessageHandler {

        @Override
        public void handleMessage(byte[] message) {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(message));
            try {
                switch(din.readChar()) {
                    case 'p': _map.put(readString(din), readString(din)); break;
                    case 'r': _map.remove(readString(din)); break;
                    case 'c': _map.clear(); break;
                    case 'P': int count = din.readInt();
                              for (int i = 0; i < count; ++i) _map.put(readString(din), readString(din));
                              break;
                    default:  throw new RuntimeException("Unrecognized command '" + (char) message[0] + "'");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }        
    }
}
