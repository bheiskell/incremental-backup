package org.xdxa.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;

/**
 * This callback will recursively write a collection of files to an {@link OutputStream}.
 */
public class StreamingCallback {

    private static final Logger LOG = (Bukkit.getServer() != null) ? Bukkit.getLogger() : Logger.getAnonymousLogger();

    private final String filePrefix;
    private final Collection<File> files;
    private final ArchiveOutputStreamFactory archiveOutputStreamFactory;
    private final ArchiveEntryFactory archiveEntryFactory;
    private final IsDirtyCallback isDirtyCallback;
    private final InputStreamFactory inputStreamFactory;

    /**
     * Constructor of the {@link StreamingCallback}. These values provide the context needed when
     * {@link #write(OutputStream) is called.
     * @param filePrefix root prefix to strip off the filenames before adding them to the container
     * @param files a collection of files which should all share a parent root of filePrefix
     * @param archiveOutputStreamFactory factory used to create the {@link ArchiveOutputStream} for this file format
     * @param archiveEntryFactory factory used to create the {@link ArchiveEntry} for each file
     * @param isDirtyCallback callback used to determine if a given file is dirty and needs to be backed up
     */
    public StreamingCallback(
            final String filePrefix,
            final Collection<File> files,
            final ArchiveOutputStreamFactory archiveOutputStreamFactory,
            final ArchiveEntryFactory archiveEntryFactory,
            final IsDirtyCallback isDirtyCallback) {
        this(
                filePrefix,
                files,
                archiveOutputStreamFactory,
                archiveEntryFactory,
                isDirtyCallback,
                new InputStreamFactory());
    }

    StreamingCallback(
            final String filePrefix,
            final Collection<File> files,
            final ArchiveOutputStreamFactory archiveOutputStreamFactory,
            final ArchiveEntryFactory archiveEntryFactory,
            final IsDirtyCallback isDirtyCallback,
            final InputStreamFactory inputStreamFactory) {
        this.filePrefix = filePrefix;
        this.files = files;
        this.archiveOutputStreamFactory = archiveOutputStreamFactory;
        this.archiveEntryFactory = archiveEntryFactory;
        this.isDirtyCallback = isDirtyCallback;
        this.inputStreamFactory = inputStreamFactory;
    }

    /**
     * Writes the archive to the {@link OutputStream}.
     * @param outputStream output stream
     * @throws IOException in the event that there is either an input or output issue
     */
    public void write(final OutputStream outputStream) throws IOException {
        final ArchiveOutputStream archiveOutputStream = archiveOutputStreamFactory.from(outputStream);

        try {
            for (final File file : files) {
                recursiveAdd(archiveOutputStream, file);
            }
        } finally {
            archiveOutputStream.close();
        }

        LOG.info("Completed writing archive to output stream");
    }

    void recursiveAdd(final ArchiveOutputStream outputStream, final File file) throws IOException {
        if (file.isDirectory()) {
            // Empty directories will not get backed-up
            for (final File child : file.listFiles()) {
                recursiveAdd(outputStream, child);
            }
        } else if (isDirtyCallback.isDirty(file)) {
            addFile(outputStream, file);
        }
    }

    void addFile(final ArchiveOutputStream outputStream, final File file) throws IOException {
        final String relativeFilename = file.getAbsolutePath().substring(filePrefix.length() + 1);

        InputStream fileStream = null;
        try {
            fileStream = inputStreamFactory.from(file);

            final ArchiveEntry archiveEntry = archiveEntryFactory.from(relativeFilename, file.length());

            outputStream.putArchiveEntry(archiveEntry);

            // If the input stream errors, we probably want to continue, but
            // there's no good way to distinguish that from an output stream
            // error, which I suspect is more probable. That's why we're
            // allowing this exception to bubble up.
            IOUtils.copy(fileStream, outputStream);

            outputStream.closeArchiveEntry();
            outputStream.flush();

        } catch (final FileNotFoundException e) {
            // We're catch/logging here because if a file is missing, I'd prefer to continue without it.
            LOG.warning("File not found '" + file.getName() + "'... was it deleted?");

        } finally {
            if (fileStream != null) {
                fileStream.close();
            }
        }
    }

    /**
     * Factory for creating {@link InputStream} objects. This exists only for tests.
     */
    static class InputStreamFactory {
        public InputStream from(final File file) throws FileNotFoundException {
            return new FileInputStream(file);
        }
    }
}