/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.applier;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A stream that simulates EOF after a specified numToRead of bytes has been reached.
 */
public class LimitedInputStream extends FilterInputStream {
  /**
   * Current numToRead.
   */
  private long numToRead;

  /**
   * Buffer used for one-byte reads to keep all code on the same path.
   */
  private byte[] ONE_BYTE = new byte[1];

  /**
   * Creates a new limited stream that delegates operations to the specified stream.
   * @param in the stream to limit
   * @param numToRead the number of reads to allow before returning EOF
   */
  public LimitedInputStream(InputStream in, long numToRead) {
    super(in);
    if (numToRead < 0) {
      throw new IllegalArgumentException("numToRead must be >= 0: " + numToRead);
    }
    this.numToRead = numToRead;
  }

  @Override
  public int read() throws IOException {
    if (read(ONE_BYTE, 0, 1) == 1) {
      return ONE_BYTE[0];
    }
    return -1;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (numToRead == 0) {
      return -1; // Simulate EOF
    }
    int maxRead = (int) Math.min(len, numToRead);
    int numRead = in.read(b, off, maxRead);
    if (numRead > 0) {
      numToRead -= numRead;
    }
    return numRead;
  }
}
