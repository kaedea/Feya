/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.applier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface to be implemented by delta appliers.
 */
public interface DeltaApplier {
  /**
   * Applies a delta from deltaIn to oldBlob and writes the result to newBlobOut.
   *
   * @param oldBlob the old blob
   * @param deltaIn the delta to apply to the oldBlob
   * @param newBlobOut the stream to write the result to
   * @throws IOException in the event of an I/O error reading the input or writing the output
   */
  public void applyDelta(File oldBlob, InputStream deltaIn, OutputStream newBlobOut)
      throws IOException;
}
