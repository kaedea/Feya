/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.applier.bsdiff;

import com.google.archivepatcher.applier.DeltaApplier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * An implementation of {@link DeltaApplier} that uses {@link BsPatch} to apply a bsdiff patch.
 */
public class BsDiffDeltaApplier implements DeltaApplier {

  @Override
  public void applyDelta(File oldBlob, InputStream deltaIn, OutputStream newBlobOut)
      throws IOException {
    RandomAccessFile oldBlobRaf = null;
    try {
      oldBlobRaf = new RandomAccessFile(oldBlob, "r");
      BsPatch.applyPatch(oldBlobRaf, newBlobOut, deltaIn);
    } finally {
      try {
        oldBlobRaf.close();
      } catch (Exception ignored) {
        // Nothing
      }
    }
  }
}
