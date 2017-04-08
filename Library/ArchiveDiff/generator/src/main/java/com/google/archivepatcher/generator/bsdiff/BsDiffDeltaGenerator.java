/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator.bsdiff;

import com.google.archivepatcher.generator.DeltaGenerator;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An implementation of {@link DeltaGenerator} that uses {@link BsDiffPatchWriter} to write a
 * bsdiff patch that represents the delta between given inputs.
 */
public class BsDiffDeltaGenerator implements DeltaGenerator {
  /**
   * The minimum match length to use for bsdiff.
   */
  private static final int MATCH_LENGTH_BYTES = 16;

  @Override
  public void generateDelta(File oldBlob, File newBlob, OutputStream deltaOut)
      throws IOException, InterruptedException {
    BsDiffPatchWriter.generatePatch(oldBlob, newBlob, deltaOut, MATCH_LENGTH_BYTES);
  }
}
