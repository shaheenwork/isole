package com.ezenit.utils;

import android.os.FileObserver;

public class DownloadsObserver extends FileObserver {

    public static final String LOG_TAG = DownloadsObserver.class.getSimpleName();

    private static final int flags =
            FileObserver.CLOSE_WRITE
            | FileObserver.OPEN
            | FileObserver.MODIFY
            | FileObserver.DELETE
            | FileObserver.MOVED_FROM;
    // Received three of these after the delete event while deleting a video through a separate file manager app:
    // 01-16 15:52:27.627: D/APP(4316): DownloadsObserver: onEvent(1073741856, null)

    public DownloadsObserver(String path) {
        super(path, flags);
    }

    @Override
    public void onEvent(int event, String path) {
//        Log.d(LOG_TAG, "onEvent(" + event + ", " + path + ")");

        if (path == null) {
            return;
        }

        switch (event) {
        case FileObserver.CLOSE_WRITE:
            // Download complete, or paused when wifi is disconnected. Possibly reported more than once in a row.
            // Useful for noticing when a download has been paused. For completions, register a receiver for 
            // DownloadManager.ACTION_DOWNLOAD_COMPLETE.
            break;
        case FileObserver.OPEN:
            // Called for both read and write modes.
            // Useful for noticing a download has been started or resumed.
            break;
        case FileObserver.DELETE:
        case FileObserver.MOVED_FROM:
            // These might come in handy for obvious reasons.
            break;
        case FileObserver.MODIFY:
            // Called very frequently while a download is ongoing (~1 per ms).
            // This could be used to trigger a progress update, but that should probably be done less often than this.
            break;
        }
    }
}