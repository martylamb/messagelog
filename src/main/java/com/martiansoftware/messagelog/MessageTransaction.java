package com.martiansoftware.messagelog;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

/**
 * wraps one or more messages in a structure containing a message crc and length,
 * used for reading and writing from/to the message log
 * 
 * crc (long) (8 bytes)
 * count (int) (4 bytes)
 *     length (int) (4 bytes)
 *     data (byte[]) (n bytes)
 *     length (int) (4 bytes)
 *     data (byte[]) (n bytes)
 *     ...
 * 
 * @author mlamb
 */
class MessageTransaction {

    private final long _crc;
    private final List<byte[]> _messages;
    private final long _len;
    
    MessageTransaction(DataInput in) throws IOException {
        _crc = in.readLong();
        
        int count = in.readInt();
        _messages = new ArrayList<>(count);

        for (int i = 0; i < count; ++i) {
            byte[] b = new byte[in.readInt()];
            in.readFully(b);
            _messages.add(b);
        }

        if (_crc != getCRC()) throw new IOException("bad CRC");
        _len = computeLength();
    }
        
    private long computeLength() {
        long result = 12;
        for (byte[] message : _messages) {
            result += 4;
            result += message.length;
        }
        return result;
    }
    
    MessageTransaction (byte[]... b) {        
        _messages = new java.util.ArrayList<>(b.length);
        for (byte[] msg : b) _messages.add(Arrays.copyOf(msg, msg.length));
        _crc = getCRC();
        _len = computeLength();
    } 

    MessageTransaction (List<byte[]>b) {
        _messages = new java.util.ArrayList<>(b.size());
        for (byte[] msg : b) _messages.add(Arrays.copyOf(msg, msg.length));
        _crc = getCRC();
        _len = computeLength();
    }
    
    void writeTo(DataOutput out) throws IOException {
        out.writeLong(_crc);
        out.writeInt(_messages.size());
        for (byte[] msg : _messages) {
            out.writeInt(msg.length);
            out.write(msg);
        }
    }

    private long getCRC() {
        CRC32 crc = new CRC32();
        for (byte[] msg : _messages) crc.update(msg);
        return crc.getValue();        
    }
    
    // underlying byte arrays are NOT immutable.  Use with care.
    Iterable<byte[]> getMessages() {
        return Collections.unmodifiableList(_messages);
    }
}
