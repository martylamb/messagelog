package com.martiansoftware.messagelog;

import java.io.EOFException;
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
    private volatile boolean _autoSync = false;
    
    public FileMessageLog(File f) throws IOException {
        this(f, null);
    }
    
    public FileMessageLog(File f, MessageHandler h) throws IOException {
        _f = f;
        _raf = new RandomAccessFile(f, "rw");
        try {
            _flock = _raf.getChannel().tryLock();
        } catch (OverlappingFileLockException e) {
            throw new IOException("OverlappingFileLockException on " + f.getAbsolutePath());
        }
        if (_flock == null) throw new IOException("Unable to obtain lock on " + f.getAbsolutePath())     ;

        // need to replay even if h is null so that file pointer is at correct
        // position for next write.
        replay(h);        
    }
    
    public File getFile() {
        return _f;
    }
    
    public FileMessageLog setAutoSync(boolean autoSync) {
        synchronized(_lock) {
            _autoSync = autoSync;
        }
        return this;
    }

    @Override
    public void replay(MessageHandler h) throws IOException {
        long dataLength = 0;
        synchronized(_lock) {
            failIfClosed();
            _raf.seek(0);
            while (dataLength < _raf.length()) {
                try { 
                    MessageTransaction mt = new MessageTransaction(_raf);
                    dataLength = _raf.getFilePointer();
                    if (h != null) for(byte[] msg : mt.getMessages()) h.handleMessage(msg);
                } catch (EOFException e) {
                    // done reading data; looks like the last message write failed.
                    // move file pointer to beginning of truncated message
                    _raf.seek(dataLength);
                    break;
                }            
            }
        }
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
    
    private void failIfClosed() throws IOException {
        if (!_flock.isValid()) throw new IOException("File has been closed.");
    }
    
    private void writeTransaction(MessageTransaction tx) throws IOException {
        synchronized(_lock) {
            failIfClosed();
            tx.writeTo(_raf);
            if (_autoSync) sync();
        }
    }
    
    public void sync() throws IOException {
        synchronized(_lock) {
            _raf.getChannel().force(false);
        }
    }
    
    public void close() throws IOException {
        synchronized(_lock) {
            sync();
            _flock.release();
            _raf.close();
        }
    }
}
