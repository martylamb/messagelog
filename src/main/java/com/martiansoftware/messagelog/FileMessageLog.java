package com.martiansoftware.messagelog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.List;

/**
 *
 * @author mlamb
 */
public class FileMessageLog implements MessageLog {

    private final File _f;
    private final RandomAccessFile _raf;
    private final FileLock _flock;
    private final Object _lock = new Object();
    
    // TODO: synchronize messages
    
    public FileMessageLog(File f) throws IOException {
        this(f, null);
    }
    
    public FileMessageLog(File f, MessageHandler h) throws IOException {
        _f = f;
        
        _raf = new RandomAccessFile(f, "rwd");
        try {
            _flock = _raf.getChannel().tryLock();
        } catch (OverlappingFileLockException e) {
            throw new IOException("OverlappingFileLockException on " + f.getAbsolutePath());
        }
        if (_flock == null) throw new IOException("Unable to obtain lock on " + f.getAbsolutePath())     ;
        
        // load file
    }
    
    @Override
    public void log(byte[]... messages) throws IOException {
        if (messages == null || messages.length == 0) return;
        writeTransaction(new MessageTransaction(messages));
    }

    @Override
    public void log(List<byte[]> messages) throws IOException {
        if (messages == null || messages.isEmpty()) return;
        writeTransaction(new MessageTransaction(messages));
    }
    
    private void writeTransaction(MessageTransaction tx) throws IOException {
        synchronized(_lock) {
            tx.writeTo(_raf);
        }
    }
}
