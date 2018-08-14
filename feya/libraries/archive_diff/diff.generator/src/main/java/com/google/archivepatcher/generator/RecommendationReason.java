/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator;

/**
 * Reasons for a corresponding {@link Recommendation}.
 */
public enum RecommendationReason {
  /**
   * The entry in the new file is compressed in a way that cannot be reliably reproduced (or one of
   * the entries is compressed using something other than deflate, but this is very uncommon).
   */
  UNSUITABLE,

  /**
   * Both the old and new entries are already uncompressed.
   */
  BOTH_ENTRIES_UNCOMPRESSED,

  /**
   * An entry that was uncompressed in the old file is compressed in the new file.
   */
  UNCOMPRESSED_CHANGED_TO_COMPRESSED,

  /**
   * An entry that was compressed in the old file is uncompressed in the new file.
   */
  COMPRESSED_CHANGED_TO_UNCOMPRESSED,

  /**
   * The compressed bytes in the old file do not match the compressed bytes in the new file.
   */
  COMPRESSED_BYTES_CHANGED,

  /** The compressed bytes in the old file are identical to the compressed bytes in the new file. */
  COMPRESSED_BYTES_IDENTICAL,

  /**
   * A resource constraint prohibits touching the old entry, the new entry, or both. For example,
   * there may be a limit on the total amount of temp space that will be available for applying a
   * patch or a limit on the total amount of CPU time that can be expended on recompression when
   * applying a patch, etc.
   */
  RESOURCE_CONSTRAINED;
}
