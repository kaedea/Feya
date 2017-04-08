/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator.bsdiff;

import java.io.IOException;

/**
 * An algorithm that performs a suffix sort on a given input and returns a suffix array.
 * See https://en.wikipedia.org/wiki/Suffix_array
 */
public interface SuffixSorter {

  /**
   * Perform a "suffix sort". Note: the returned {@link RandomAccessObject} should be closed by the
   * caller.
   *
   * @param data the data to sort
   * @return the suffix array, as a {@link RandomAccessObject}
   * @throws IOException if unable to read data
   * @throws InterruptedException if any thread interrupts this thread
   */
  RandomAccessObject suffixSort(RandomAccessObject data) throws IOException, InterruptedException;
}

