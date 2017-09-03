/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator.bsdiff;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DivSuffixSorterTest extends SuffixSorterTestBase {
  
  DivSuffixSorter divSuffixSorter;
  
  @Before
  public void setup() {
    divSuffixSorter =
        new DivSuffixSorter(new RandomAccessObjectFactory.RandomAccessByteArrayObjectFactory());
  }

  @Override
  public SuffixSorter getSuffixSorter() {
    return divSuffixSorter;
  }
}
