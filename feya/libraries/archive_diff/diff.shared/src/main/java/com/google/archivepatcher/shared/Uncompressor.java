/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface for implementing a streaming uncompressor. An uncompressor may be used to uncompress
 * data that was previously compressed by the corresponding {@link Compressor} implementation, and
 * always operates in a streaming manner.
 */
public interface Uncompressor {
  /**
   * Uncompresses data that was previously processed by the corresponding {@link Compressor}
   * implementation, writing the uncompressed data into uncompressedOut.
   *
   * @param compressedIn the compressed data
   * @param uncompressedOut the uncompressed data
   * @throws IOException if something goes awry while reading or writing
   */
  public void uncompress(InputStream compressedIn, OutputStream uncompressedOut) throws IOException;
}
