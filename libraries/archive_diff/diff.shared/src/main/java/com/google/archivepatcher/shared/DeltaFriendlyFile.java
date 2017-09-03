/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.shared;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for generating delta-friendly files.
 */
public class DeltaFriendlyFile {

  /**
   * The default size of the copy buffer to use for copying between streams.
   */
  public static final int DEFAULT_COPY_BUFFER_SIZE = 32768;

  /**
   * Invoke {@link #generateDeltaFriendlyFile(List, File, OutputStream, boolean, int)} with <code>
   * generateInverse</code> set to <code>true</code> and a copy buffer size of {@link
   * #DEFAULT_COPY_BUFFER_SIZE}.
   *
   * @param <T> the type of the data associated with the ranges
   * @param rangesToUncompress the ranges to be uncompressed during transformation to a
   *     delta-friendly form
   * @param file the file to read from
   * @param deltaFriendlyOut a stream to write the delta-friendly file to
   * @return the ranges in the delta-friendly file that correspond to the ranges in the original
   *     file, with identical metadata and in the same order
   * @throws IOException if anything goes wrong
   */
  public static <T> List<TypedRange<T>> generateDeltaFriendlyFile(
      List<TypedRange<T>> rangesToUncompress, File file, OutputStream deltaFriendlyOut)
      throws IOException {
    return generateDeltaFriendlyFile(
        rangesToUncompress, file, deltaFriendlyOut, true, DEFAULT_COPY_BUFFER_SIZE);
  }

  /**
   * Generate one delta-friendly file and (optionally) return the ranges necessary to invert the
   * transform, in file order. There is a 1:1 correspondence between the ranges in the input list
   * and the returned list, but the offsets and lengths will be different (the input list represents
   * compressed data, the output list represents uncompressed data). The ability to suppress
   * generation of the inverse range and to specify the size of the copy buffer are provided for
   * clients that desire a minimal memory footprint.
   *
   * @param <T> the type of the data associated with the ranges
   * @param rangesToUncompress the ranges to be uncompressed during transformation to a
   *     delta-friendly form
   * @param file the file to read from
   * @param deltaFriendlyOut a stream to write the delta-friendly file to
   * @param generateInverse if <code>true</code>, generate and return a list of inverse ranges in
   *     file order; otherwise, do all the normal work but return null instead of the inverse ranges
   * @param copyBufferSize the size of the buffer to use for copying bytes between streams
   * @return if <code>generateInverse</code> was true, returns the ranges in the delta-friendly file
   *     that correspond to the ranges in the original file, with identical metadata and in the same
   *     order; otherwise, return null
   * @throws IOException if anything goes wrong
   */
  public static <T> List<TypedRange<T>> generateDeltaFriendlyFile(
      List<TypedRange<T>> rangesToUncompress,
      File file,
      OutputStream deltaFriendlyOut,
      boolean generateInverse,
      int copyBufferSize)
      throws IOException {
    List<TypedRange<T>> inverseRanges = null;
    if (generateInverse) {
      inverseRanges = new ArrayList<TypedRange<T>>(rangesToUncompress.size());
    }
    long lastReadOffset = 0;
    RandomAccessFileInputStream oldFileRafis = null;
    PartiallyUncompressingPipe filteredOut =
        new PartiallyUncompressingPipe(deltaFriendlyOut, copyBufferSize);
    try {
      oldFileRafis = new RandomAccessFileInputStream(file);
      for (TypedRange<T> rangeToUncompress : rangesToUncompress) {
        long gap = rangeToUncompress.getOffset() - lastReadOffset;
        if (gap > 0) {
          // Copy bytes up to the range start point
          oldFileRafis.setRange(lastReadOffset, gap);
          filteredOut.pipe(oldFileRafis, PartiallyUncompressingPipe.Mode.COPY);
        }

        // Now uncompress the range.
        oldFileRafis.setRange(rangeToUncompress.getOffset(), rangeToUncompress.getLength());
        long inverseRangeStart = filteredOut.getNumBytesWritten();
        // TODO(andrewhayden): Support nowrap=false here? Never encountered in practice.
        // This would involve catching the ZipException, checking if numBytesWritten is still zero,
        // resetting the stream and trying again.
        filteredOut.pipe(oldFileRafis, PartiallyUncompressingPipe.Mode.UNCOMPRESS_NOWRAP);
        lastReadOffset = rangeToUncompress.getOffset() + rangeToUncompress.getLength();

        if (generateInverse) {
          long inverseRangeEnd = filteredOut.getNumBytesWritten();
          long inverseRangeLength = inverseRangeEnd - inverseRangeStart;
          TypedRange<T> inverseRange =
              new TypedRange<T>(
                  inverseRangeStart, inverseRangeLength, rangeToUncompress.getMetadata());
          inverseRanges.add(inverseRange);
        }
      }
      // Finish the final bytes of the file
      long bytesLeft = oldFileRafis.length() - lastReadOffset;
      if (bytesLeft > 0) {
        oldFileRafis.setRange(lastReadOffset, bytesLeft);
        filteredOut.pipe(oldFileRafis, PartiallyUncompressingPipe.Mode.COPY);
      }
    } finally {
      try {
        oldFileRafis.close();
      } catch (Exception ignored) {
        // Nothing
      }
      try {
        filteredOut.close();
      } catch (Exception ignored) {
        // Nothing
      }
    }
    return inverseRanges;
  }
}
