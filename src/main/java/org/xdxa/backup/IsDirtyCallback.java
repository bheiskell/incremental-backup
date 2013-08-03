package org.xdxa.backup;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

/**
 * Callback used to evaluate if a file should be backed up.
 */
public interface IsDirtyCallback {

    /**
     * Implementors will use the file to determine if it should be backed up.
     * @param file file
     * @return true if dirty
     */
    public boolean isDirty(File file);

    /**
     * Callback that considers any files older than a certain number of days dirty. If used erratically, this is not
     * robust. I.e., if you miss a backup cycle there may be gaps. TODO: replace this with a since unix timestamp.
     */
    public static class DailyIsDirtyCallback implements IsDirtyCallback {
        private static final Logger LOG = Bukkit.getServer() != null ? Bukkit.getLogger() : Logger.getAnonymousLogger();

        private final long interval = TimeUnit.DAYS.toMillis(1);

        @Override
        public boolean isDirty(final File file) {
            final long threshold = System.currentTimeMillis() - interval;
            final long lastModified = file.lastModified();
            final boolean isDirty = lastModified > threshold;

            if (isDirty) {
                LOG.info("Dirty file '" + file.getAbsolutePath() + "' modified on: " + new Date(lastModified));
            } else if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Clean file '" + file.getAbsolutePath() + "' modified on: " + new Date(lastModified));
            }

            return isDirty;
        }
    }
}
