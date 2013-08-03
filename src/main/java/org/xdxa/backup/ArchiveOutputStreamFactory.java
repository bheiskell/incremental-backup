package org.xdxa.backup;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

/**
 * Factory interface for constructing {@link ArchiveOutputStreams}.
 */
public interface ArchiveOutputStreamFactory {
    /**
     * Implementations of this method will construct {@ArchiveOutputStream}s from another {@link OutputStream}.
     * @param outputStream stream to be wrapped
     * @return constructed {@link ArchiveOutputStream}
     */
    public ArchiveOutputStream from(OutputStream outputStream);

    /**
     * Factory for generating {@link TarArchiveOutputStream}.
     */
    public static class TarArchiveOutputStreamFactory implements ArchiveOutputStreamFactory {
        @Override
        public ArchiveOutputStream from(final OutputStream outputStream) {
            return new TarArchiveOutputStream(outputStream);
        }
    }

    /**
     * Factory for generating {@link GzipCompressorOutputStream} wrapped {@link TarArchiveOutputStream}s.
     */
    public static class GzTarArchiveOutputStreamFactory implements ArchiveOutputStreamFactory {
        @Override
        public ArchiveOutputStream from(final OutputStream outputStream) {
            try {
                return new TarArchiveOutputStream(new GzipCompressorOutputStream(outputStream));
            } catch (final IOException e) {
                throw new IllegalStateException("Failed to create a GzipCompressorOutputStream", e);
            }
        }
    }

    /**
     * Factory for generating {@link ZipArchiveOutputStream}.
     */
    public static class ZipArchiveOutputStreamFactory implements ArchiveOutputStreamFactory {
        @Override
        public ArchiveOutputStream from(final OutputStream outputStream) {
            return new ZipArchiveOutputStream(outputStream);
        }
    }
}