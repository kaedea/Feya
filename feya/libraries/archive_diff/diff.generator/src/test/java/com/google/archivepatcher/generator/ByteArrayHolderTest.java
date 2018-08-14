/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link ByteArrayHolder}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class ByteArrayHolderTest {

  @Test
  public void testGetters() {
    byte[] data = "hello world".getBytes();
    ByteArrayHolder byteArrayHolder = new ByteArrayHolder(data);
    Assert.assertSame(data, byteArrayHolder.getData());
  }

  @Test
  public void testHashCode() {
    byte[] data1a = "hello world".getBytes();
    byte[] data1b = new String("hello world").getBytes();
    byte[] data2 = "hello another world".getBytes();
    ByteArrayHolder rawText1a = new ByteArrayHolder(data1a);
    ByteArrayHolder rawText1b = new ByteArrayHolder(data1b);
    Assert.assertEquals(rawText1a.hashCode(), rawText1b.hashCode());
    ByteArrayHolder rawText2 = new ByteArrayHolder(data2);
    Assert.assertNotEquals(rawText1a.hashCode(), rawText2.hashCode());
    ByteArrayHolder rawText3 = new ByteArrayHolder(null);
    Assert.assertNotEquals(rawText1a.hashCode(), rawText3.hashCode());
    Assert.assertNotEquals(rawText2.hashCode(), rawText3.hashCode());
  }

  @Test
  public void testEquals() {
    byte[] data1a = "hello world".getBytes();
    byte[] data1b = new String("hello world").getBytes();
    byte[] data2 = "hello another world".getBytes();
    ByteArrayHolder rawText1a = new ByteArrayHolder(data1a);
    Assert.assertEquals(rawText1a, rawText1a);
    ByteArrayHolder rawText1b = new ByteArrayHolder(data1b);
    Assert.assertEquals(rawText1a, rawText1b);
    ByteArrayHolder rawText2 = new ByteArrayHolder(data2);
    Assert.assertNotEquals(rawText1a, rawText2);
    ByteArrayHolder rawText3 = new ByteArrayHolder(null);
    Assert.assertNotEquals(rawText1a, rawText3);
    Assert.assertNotEquals(rawText3, rawText1a);
    Assert.assertNotEquals(rawText1a, 42);
    Assert.assertNotEquals(rawText1a, null);
  }
}
