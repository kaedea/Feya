/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.shared;

import java.io.File;
import java.io.IOException;

/**
 * An implementation of {@link MultiViewInputStreamFactory} that produces instances of
 * {@link RandomAccessFileInputStream}.
 */
public class RandomAccessFileInputStreamFactory
    implements MultiViewInputStreamFactory<RandomAccessFileInputStream> {

  /**
   * Argument for {@link RandomAccessFileInputStream#RandomAccessFileInputStream(File, long, long)}.
   */
  private final File file;

  /**
   * Argument for {@link RandomAccessFileInputStream#RandomAccessFileInputStream(File, long, long)}.
   */
  private final long rangeOffset;

  /**
   * Argument for {@link RandomAccessFileInputStream#RandomAccessFileInputStream(File, long, long)}.
   */
  private final long rangeLength;

  /**
   * Constructs a new factory that will create instances of {@link RandomAccessFileInputStream} with
   * the specified parameters.
   * @param file the file to use in {@link #newStream()}
   * @param rangeOffset the range offset to use in {@link #newStream()}
   * @param rangeLength the range length to use in {@link #newStream()}
   */
  public RandomAccessFileInputStreamFactory(File file, long rangeOffset, long rangeLength) {
    this.file = file;
    this.rangeOffset = rangeOffset;
    this.rangeLength = rangeLength;
  }

  @Override
  public RandomAccessFileInputStream newStream() throws IOException {
    return new RandomAccessFileInputStream(file, rangeOffset, rangeLength);
  }
}
