/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator;

/**
 * Recommendations for how to uncompress entries in old and new archives.
 */
public enum Recommendation {

  /**
   * Uncompress only the old entry.
   */
  UNCOMPRESS_OLD(true, false),

  /**
   * Uncompress only the new entry.
   */
  UNCOMPRESS_NEW(false, true),

  /**
   * Uncompress both the old and new entries.
   */
  UNCOMPRESS_BOTH(true, true),

  /**
   * Uncompress neither entry.
   */
  UNCOMPRESS_NEITHER(false, false);

  /**
   * True if the old entry should be uncompressed.
   */
  public final boolean uncompressOldEntry;

  /**
   * True if the new entry should be uncompressed.
   */
  public final boolean uncompressNewEntry;

  /**
   * Constructs a new recommendation with the specified behaviors.
   * @param uncompressOldEntry true if the old entry should be uncompressed
   * @param uncompressNewEntry true if the new entry should be uncompressed
   */
  private Recommendation(boolean uncompressOldEntry, boolean uncompressNewEntry) {
    this.uncompressOldEntry = uncompressOldEntry;
    this.uncompressNewEntry = uncompressNewEntry;
  }
}