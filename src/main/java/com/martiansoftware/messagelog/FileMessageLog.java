package com.martiansoftware.messagelog;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.List;

/**
 * A robust file-based log of byte-array-based messages.  This can be used as 
 * a simple command-sourcing or event-sourcing.  Simplicity is prioritized
 * above performance.  It is possible, at the expense of performance, to force
 * all writes to synchronize to the underlying disk before returning from the
 * log() call.  See sync() and setAutoSync().
 * 
 * @author mlamb
 */
public class FileMessageLog implements MessageLog {

    private final File _f;
    private final RandomAccessFile _raf;
    private final FileLock _flock;
    private final Object _lock = new Object();
    private volatile boolean _autoSync = false;
   
    /**
     * Creates a new FileMessageLog using the specified File, creating the file
     * on disk if necessary.
     * 
     * @param f the file to use for message storage
     * @throws IOException 
     */
    public FileMessageLog(File f) throws IOException {
        this(f, null);
    }
    
    /**
     * Creates a new FileMessageLog using the specified File, creating the file
     * on disk if necessary.
     * 
     * 
     * @param f the file to use for message storage
     * @param h a MessageHandler that will be called for each message read
     * from the file when it is opened (e.g., to restore application
     * state from the log)
     */
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
    
    /**
     * Returns the underlying file used for message storage.
     * @return the underlying file used for message storage
     */
    public File getFile() {
        return _f;
    }
    
    /**
     * If set to true, all writes will be forced out to disk before returning from
     * log().  This provides greate robustness in the event of e.g. power failure,
     * but comes at the cost of performance.  Default is false.
     * 
     * @param autoSync if true, automatically force all writes to disk
     * @return this FileMessageLog
     */
    public FileMessageLog setAutoSync(boolean autoSync) {
        synchronized(_lock) {
            _autoSync = autoSync;
        }
        return this;
    }

    /**
     * Replays all messages from this FileMessageLog to the specified MessageHandler
     * 
     * @param h the MessageHandler to receive replayed messages
     * @return this FileMessageLog
     * @throws IOException 
     */
    @Override
    public FileMessageLog replay(MessageHandler h) throws IOException {
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
        return this;
    }
    
    /**
     * Writes messages to the log.  More than one message can be supplied; multiple
     * messages will be written atomically together (if one fails, they all fail,
     * and subsequent replays of the log will not replay any of the messages in
     * a set that failed).
     * 
     * @param messages the messages to write
     * @return this FileMessageLog
     * @throws IOException 
     */
    @Override
    public FileMessageLog log(byte[]... messages) throws IOException {
        if (messages == null || messages.length == 0) return this;
        writeTransaction(new MessageTransaction(messages));
        return this;
    }

    /**
     * Writes messages to the log.  More than one message can be supplied; multiple
     * messages will be written atomically together (if one fails, they all fail,
     * and subsequent replays of the log will not replay any of the messages in
     * a set that failed).
     * 
     * @param messages the messages to write
     * @return this FileMessageLog
     * @throws IOException 
     */
    @Override
    public FileMessageLog log(List<byte[]> messages) throws IOException {
        if (messages == null || messages.isEmpty()) return this;
        writeTransaction(new MessageTransaction(messages));
        return this;
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
    
    /**
     * Forces any pending writes out to disk.  May be used explicitly instead
     * of relying on setAutoSync(true) in situations where performance is
     * important and the impact on the application of failed writes is well
     * understood by the developer.
     * 
     * @return this FileMessageLog
     * @throws IOException 
     */
    public FileMessageLog sync() throws IOException {
        synchronized(_lock) {
            _raf.getChannel().force(false);
        }
        return this;
    }
    
    /**
     * Closes this FileMessageLog and releases any locks/resources.  Once
     * closed, no more logging or replays are permitted.
     * 
     * @return this FileMessageLog
     * @throws IOException 
     */
    public FileMessageLog close() throws IOException {
        synchronized(_lock) {
            sync();
            _flock.release();
            _raf.close();
        }
        return this;
    }
}
