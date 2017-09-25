/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface for implementing a streaming compressor. A compressor may be used to compress
 * arbitrary binary data, but is always capable of doing so in a streaming manner.
 */
public interface Compressor {
  /**
   * Compresses data, writing the compressed data into compressedOut.
   *
   * @param uncompressedIn the uncompressed data
   * @param compressedOut the compressed data
   * @throws IOException if something goes awry while reading or writing
   */
  public void compress(InputStream uncompressedIn, OutputStream compressedOut) throws IOException;
}
