package com.google.android.exoplayer2.util;

public final class DvrMiddleware {

    /**
     * The time, in milliseconds, to use as the end position for a live DVR window.
     */
    public static final long LIVE_DVR_END_POSITION = 0;
    /**
     * The length of one video segment in milliseconds.
     */
    private static final long SEGMENT_LENGTH_MS = 6000;

    /**
     * Converts a positive position, in milliseconds, to a negative position. A negative position
     * is typically required for live DVR windows.
     *
     * @param positionMs The positive position, in milliseconds, to be converted.
     * @param durationMs The duration of the current content window, in milliseconds.
     * @return A {@code long} position in milliseconds. This will always be negative.
     * @throws IllegalArgumentException If {@param positionMs} isn't positive.
     */
    public static long convertToNegativePosition(long positionMs, long durationMs) {
        Assertions.checkArgument(positionMs >= -1 * SEGMENT_LENGTH_MS);
        return positionMs - durationMs;
    }

    /**
     * Converts a negative position, in milliseconds, to a positive position. A positive position
     * is typically required for VOD windows.
     *
     * @param positionMs The negative position, in milliseconds, to be converted.
     * @param durationMs The duration of the current content window, in milliseconds.
     * @return A {@code long} position in milliseconds. This will always be positive.
     * @throws IllegalArgumentException If {@param positionMs} isn't negative.
     */
    public static long convertToPositivePosition(long positionMs, long durationMs) {
        Assertions.checkArgument(positionMs <= 0);
        return positionMs + durationMs;
    }
}
