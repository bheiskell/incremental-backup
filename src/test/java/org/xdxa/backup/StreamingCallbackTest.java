package org.xdxa.backup;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xdxa.backup.ArchiveEntryFactory;
import org.xdxa.backup.ArchiveOutputStreamFactory;
import org.xdxa.backup.IsDirtyCallback;
import org.xdxa.backup.StreamingCallback;
import org.xdxa.backup.StreamingCallback.InputStreamFactory;

public class StreamingCallbackTest {

    private static final String FILECONTENTS = "contents";
    private static final long FILESIZE = FILECONTENTS.length();
    private static final String PREFIX = "/var/minecraft";
    private static final String FILENAME = "subdir/filename.txt";

    @Mock OutputStream outputStream;
    @Mock ArchiveOutputStreamFactory archiveOutputStreamFactory;
    @Mock ArchiveEntryFactory archiveEntryFactory;
    @Mock InputStreamFactory inputStreamFactory;
    @Mock ArchiveOutputStream archiveOutputStream;
    @Mock ArchiveEntry archiveEntry;
    @Mock IsDirtyCallback isDirtyCallback;
    @Mock File directory;
    @Mock File file;

    private StreamingCallback streamingCallback;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // emulate the factory returning our mocked output stream
        when(archiveOutputStreamFactory.from(outputStream)).thenReturn(archiveOutputStream);

        // emulate the factory returning our mocked archive entry
        when(archiveEntryFactory.from(any(String.class), any(long.class))).thenReturn(archiveEntry);

        // instead of creating a file input stream, return a byte array input stream
        when(inputStreamFactory.from(file)).thenReturn(new ByteArrayInputStream(FILECONTENTS.getBytes("UTF-8")));

        // create a parent directory with our file in it
        when(directory.isDirectory()).thenReturn(true);
        when(directory.listFiles()).thenReturn(new File[]{ file });

        // create one file without specifying its modification time
        when(file.getAbsolutePath()).thenReturn(PREFIX + "/" + FILENAME);
        when(file.length()).thenReturn(FILESIZE);
        when(file.isDirectory()).thenReturn(false);

        // add our directory to the list of files to be pushed into the stream
        final List<File> files = Arrays.asList(directory);

        streamingCallback = new StreamingCallback(
                PREFIX,
                files,
                archiveOutputStreamFactory,
                archiveEntryFactory,
                isDirtyCallback,
                inputStreamFactory);
    }

    @Test
    public void verifyDirtyAdded() throws IOException {
        when(isDirtyCallback.isDirty(file)).thenReturn(true);
        streamingCallback.write(outputStream);

        verify(archiveEntryFactory).from(FILENAME, FILESIZE);
        verify(archiveOutputStream).putArchiveEntry(archiveEntry);
        verify(archiveOutputStream).close();
    }

    @Test
    public void verifyOldFileIgnored() throws IOException {
        when(isDirtyCallback.isDirty(file)).thenReturn(false);

        streamingCallback.write(outputStream);

        verify(archiveEntryFactory, never()).from(FILENAME, FILESIZE);
        verify(archiveOutputStream, never()).putArchiveEntry(any(ArchiveEntry.class));
        verify(archiveOutputStream).close();
    }

    @Test
    public void verifyMissing() throws IOException {
        when(isDirtyCallback.isDirty(file)).thenReturn(true);

        when(inputStreamFactory.from(file)).thenThrow(new FileNotFoundException(""));

        streamingCallback.write(outputStream);

        verify(archiveEntryFactory, never()).from(FILENAME, FILESIZE);
        verify(archiveOutputStream, never()).putArchiveEntry(any(ArchiveEntry.class));
        verify(archiveOutputStream).close();
    }

    @Test(expected = IOException.class)
    public void verifyIOException() throws IOException {
        when(isDirtyCallback.isDirty(file)).thenReturn(true);

        doThrow(new IOException("")).when(archiveOutputStream).write(any(byte[].class), any(int.class), any(int.class));

        try {
            streamingCallback.write(outputStream);
        } finally {
            verify(archiveOutputStream).close();
        }
    }
}
