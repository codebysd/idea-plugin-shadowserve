package codebysd.idea.plugin.shadowserve;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * In-memory LRU cache for bytes.
 * The cache access is synchronized.
 */
public class SSCache {
    private final LinkedList<Entry> mEntries = new LinkedList<>();
    private final Map<String, Entry> mMapping = new HashMap<>();
    private final long mMaxBytes;
    private long mCurrentBytes = 0;

    /**
     * Constructor
     *
     * @param mMaxBytes Maximum number of bytes to store. Must be a positive number.
     * @throws IllegalArgumentException If max bytes given is not a positive number.
     */
    public SSCache(long mMaxBytes) {
        // max bytes must be valid
        if (mMaxBytes <= 0) {
            throw new IllegalArgumentException("The parameter max bytes must be greater than zero.");
        }

        this.mMaxBytes = mMaxBytes;
    }

    /**
     * Place the given bytes in cache (if enough space is there).
     *
     * @param key  Cache key.
     * @param data Bytes to store.
     * @return True if bytes saved in cache, false if not enough space.
     */
    public synchronized boolean put(String key, byte[] data) {
        // create an entry
        final Entry entry = new Entry(data, key);

        // save entry if enough space is available
        if (makeSpace(entry.getSize()) >= entry.getSize()) {

            // add entry to list and mapping
            mEntries.addFirst(entry);
            mMapping.put(key, entry);

            // update current space
            mCurrentBytes += entry.getSize();

            // ok
            return true;
        } else {
            // not enough space
            return false;
        }

    }

    /**
     * Get bytes for given key.
     *
     * @param key cache key.
     * @return byte array if found, or null.
     */
    @Nullable
    public synchronized byte[] get(String key) {
        // get entry
        final Entry entry = mMapping.get(key);

        // not found
        if (entry == null) {
            return null;
        }

        // LRU: move accessed entry to the head of the list
        mEntries.removeLastOccurrence(entry);
        mEntries.addFirst(entry);

        // Return data
        return entry.data;
    }

    /**
     * Prune cache to make space
     *
     * @param required required space in bytes
     * @return Available space after pruning
     */
    private long makeSpace(long required) {
        // while enough space is available or cache is empty
        while ((mMaxBytes - mCurrentBytes) < required && !mEntries.isEmpty()) {
            // LRU: remove entries from tail end of list
            final Entry entry = mEntries.removeLast();

            // remove mapping, update current space
            mMapping.remove(entry.key);
            mCurrentBytes = mCurrentBytes - entry.getSize();
        }

        // return available space
        return mMaxBytes - mCurrentBytes;
    }

    /**
     * Holds cache entry data
     */
    private static class Entry {
        private final byte[] data;
        private final String key;
        private final long size;

        /**
         * Constructor
         *
         * @param data Entry data
         * @param key  Entry key
         */
        private Entry(byte[] data, String key) {
            this.data = data;
            this.key = key;
            this.size = data.length + key.getBytes().length;
        }

        private long getSize() {
            return this.size;
        }
    }
}
