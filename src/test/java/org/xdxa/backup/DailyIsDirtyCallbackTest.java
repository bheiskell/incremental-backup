package org.xdxa.backup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xdxa.backup.IsDirtyCallback;
import org.xdxa.backup.IsDirtyCallback.DailyIsDirtyCallback;

public class DailyIsDirtyCallbackTest {

    @Mock File file;

    private DailyIsDirtyCallback isDirty;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(file.getAbsolutePath()).thenReturn("file");
        when(file.isDirectory()).thenReturn(false);

        isDirty = new IsDirtyCallback.DailyIsDirtyCallback();
    }

    @Test
    public void verifyDirtyAdded() throws IOException {
        when(file.lastModified()).thenReturn(System.currentTimeMillis()); // this will ensure it's dirty

        assertTrue(isDirty.isDirty(file));
    }

    @Test
    public void verifyNotDirtyAdded() throws IOException {
        when(file.lastModified()).thenReturn(0L); // this will ensure it's not dirty

        assertFalse(isDirty.isDirty(file));
    }
}
