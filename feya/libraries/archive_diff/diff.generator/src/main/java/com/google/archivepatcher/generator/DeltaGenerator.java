/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface to be implemented by delta generators.
 */
public interface DeltaGenerator {
  /**
   * Generates a delta in deltaOut that can be applied to oldBlob to produce newBlob.
   *
   * @param oldBlob the old blob
   * @param newBlob the new blob
   * @param deltaOut the stream to write the delta to
   * @throws IOException in the event of an I/O error reading the input files or writing to the
   *     delta output stream
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  public void generateDelta(File oldBlob, File newBlob, OutputStream deltaOut)
      throws IOException, InterruptedException;
}
