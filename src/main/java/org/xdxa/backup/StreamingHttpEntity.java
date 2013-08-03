package org.xdxa.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;

/**
 * Implementation of {@link AbstractHttpEntity} which uses a {@link StreamingCallback#write(OutputStream)} to write
 * the body of an HTTP request. This streams the body to the server, which avoids buffering the entire request in memory
 * prior to flushing to the server.
 */
final class StreamingHttpEntity extends AbstractHttpEntity {
    private final StreamingCallback streamingCallback;

    /**
     * {@link StreamingHttpEntity} constructor.
     * @param streamingCallback callback used by the {@link #writeTo(OutputStream)} method
     */
    public StreamingHttpEntity(final StreamingCallback streamingCallback) {
        this.streamingCallback = streamingCallback;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }
    @Override
    public long getContentLength() {
        return -1;
    }
    @Override
    public boolean isStreaming() {
        return false;
    }
    @Override
    public InputStream getContent() throws IOException {
        throw new UnsupportedOperationException();
    }
    @Override
    public void writeTo(final OutputStream outputStream) throws IOException {
        streamingCallback.write(outputStream);
    }
}