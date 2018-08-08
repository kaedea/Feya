/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;

/**
* Tests for {@link TempFileHolder}.
*/
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class TempFileHolderTest {
  @Test
  public void testConstructAndClose() throws IOException {
    // Tests that a temp file can be created and that it is deleted upon close().
    File allocated = null;
    try(TempFileHolder holder = new TempFileHolder()) {
      Assert.assertNotNull(holder.file);
      Assert.assertTrue(holder.file.exists());
      allocated = holder.file;
    }
    Assert.assertFalse(allocated.exists());
  }
}
