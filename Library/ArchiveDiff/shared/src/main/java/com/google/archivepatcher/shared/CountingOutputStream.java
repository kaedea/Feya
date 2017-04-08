/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.shared;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Trivial output stream that counts the bytes written to it.
 */
public class CountingOutputStream extends FilterOutputStream {
  /**
   * Number of bytes written so far.
   */
  private long bytesWritten = 0;

  /**
   * Create a new counting output stream.
   * @param out the output stream to wrap
   */
  public CountingOutputStream(OutputStream out) {
    super(out);
  }

  /**
   * Returns the number of bytes written to this stream so far.
   * @return as described
   */
  public long getNumBytesWritten() {
    return bytesWritten;
  }

  @Override
  public void write(int b) throws IOException {
    bytesWritten++;
    out.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    bytesWritten += b.length;
    out.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    bytesWritten += len;
    out.write(b, off, len);
  }
}
