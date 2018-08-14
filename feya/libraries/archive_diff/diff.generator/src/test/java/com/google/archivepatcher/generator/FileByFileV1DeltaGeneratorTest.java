/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator;

import com.google.archivepatcher.shared.UnitTestZipArchive;
import java.io.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link FileByFileV1DeltaGenerator}. This relies heavily on the correctness of {@link
 * PatchWriterTest}, which validates the patch writing process itself, {@link PreDiffPlannerTest},
 * which validates the decision making process for delta-friendly blobs, and {@link
 * PreDiffExecutorTest}, which validates the ability to create the delta-friendly blobs. The {@link
 * FileByFileV1DeltaGenerator} <em>itself</em> is relatively simple, combining all of these pieces
 * of functionality together to create a patch; so the tests here are just ensuring that a patch can
 * be produced.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class FileByFileV1DeltaGeneratorTest {

  @Test
  public void testGenerateDelta_BaseCase() throws Exception {
    // Simple test of generating a patch with no changes.
    FileByFileV1DeltaGenerator generator = new FileByFileV1DeltaGenerator();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try (TempFileHolder oldArchive = new TempFileHolder();
        TempFileHolder newArchive = new TempFileHolder()) {
      UnitTestZipArchive.saveTestZip(oldArchive.file);
      UnitTestZipArchive.saveTestZip(newArchive.file);
      generator.generateDelta(oldArchive.file, newArchive.file, buffer);
    }
    byte[] result = buffer.toByteArray();
    Assert.assertTrue(result.length > 0);
  }
}
