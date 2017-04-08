/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.shared;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link TypedRange}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class TypedRangeTest {

  @Test
  public void testGetters() {
    String text = "hello";
    TypedRange<String> range = new TypedRange<String>(555, 777, text);
    Assert.assertEquals(555, range.getOffset());
    Assert.assertEquals(777, range.getLength());
    Assert.assertSame(text, range.getMetadata());
  }

  @Test
  public void testToString() {
    // Just make sure this doesn't crash.
    TypedRange<String> range = new TypedRange<String>(555, 777, "woohoo");
    Assert.assertNotNull(range.toString());
    Assert.assertFalse(range.toString().length() == 0);
  }

  @Test
  public void testCompare() {
    TypedRange<String> range1 = new TypedRange<String>(1, 777, null);
    TypedRange<String> range2 = new TypedRange<String>(2, 777, null);
    Assert.assertTrue(range1.compareTo(range2) < 0);
    Assert.assertTrue(range2.compareTo(range1) > 0);
    Assert.assertTrue(range1.compareTo(range1) == 0);
  }

  @Test
  public void testHashCode() {
    TypedRange<String> range1a = new TypedRange<String>(123, 456, "hi mom");
    TypedRange<String> range1b = new TypedRange<String>(123, 456, "hi mom");
    Assert.assertEquals(range1a.hashCode(), range1b.hashCode());
    Set<Integer> hashCodes = new HashSet<Integer>();
    hashCodes.add(range1a.hashCode());
    hashCodes.add(new TypedRange<String>(123 + 1, 456, "hi mom").hashCode()); // offset changed
    hashCodes.add(new TypedRange<String>(123, 456 + 1, "hi mom").hashCode()); // length changed
    hashCodes.add(new TypedRange<String>(123 + 1, 456, "x").hashCode()); // metadata changed
    hashCodes.add(new TypedRange<String>(123 + 1, 456, null).hashCode()); // no metadata at all
    // Assert that all 4 hash codes are unique
    Assert.assertEquals(5, hashCodes.size());
  }

  @Test
  public void testEquals() {
    TypedRange<String> range1a = new TypedRange<String>(123, 456, "hi mom");
    Assert.assertEquals(range1a, range1a); // identity case
    TypedRange<String> range1b = new TypedRange<String>(123, 456, "hi mom");
    Assert.assertEquals(range1a, range1b); // equality case
    Assert.assertNotEquals(range1a, new TypedRange<String>(123 + 1, 456, "hi mom")); // offset
    Assert.assertNotEquals(range1a, new TypedRange<String>(123, 456 + 1, "hi mom")); // length
    Assert.assertNotEquals(range1a, new TypedRange<String>(123, 456, "foo")); // metadata
    Assert.assertNotEquals(range1a, new TypedRange<String>(123, 456, null)); // no metadata
    Assert.assertNotEquals(new TypedRange<String>(123, 456, null), range1a); // other code branch
    Assert.assertEquals(
        new TypedRange<String>(123, 456, null),
        new TypedRange<String>(123, 456, null)); // both with null metadata
    Assert.assertNotEquals(range1a, null); // versus null
    Assert.assertNotEquals(range1a, "space channel 5"); // versus object of different class
  }
}
