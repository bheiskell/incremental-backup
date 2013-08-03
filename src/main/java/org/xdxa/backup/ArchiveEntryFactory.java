package org.xdxa.backup;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

/**
 * Factory interface for generating {@link ArchiveEntry}'s.
 */
public interface ArchiveEntryFactory {
    /**
     * Implementations of this method will create {@link ArchiveEntry}'s with a particular filename and size.
     * @param filename name including the path up to the root of the archive
     * @param size file size in bytes
     * @return constructed {@link ArchiveEntry}
     */
    public ArchiveEntry from(String filename, final long size);

    /**
     * Factory for generating {@link TarArchiveEntry}'s.
     */
    public static class TarArchiveEntryFactory implements ArchiveEntryFactory {
        @Override
        public ArchiveEntry from(final String filename, final long size) {
            final TarArchiveEntry entry = new TarArchiveEntry(filename);
            entry.setSize(size);
            return entry;
        }
    }

    /**
     * Factory for generating {@link ZipArchiveEntry}'s.
     */
    public static class ZipArchiveEntryFactory implements ArchiveEntryFactory {
        @Override
        public ArchiveEntry from(final String filename, final long size) {
            final ZipArchiveEntry entry = new ZipArchiveEntry(filename);
            entry.setSize(size);
            return entry;
        }
    }
}