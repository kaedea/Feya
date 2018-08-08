/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator;

import java.util.Arrays;

/**
 * Holds an array of bytes, implementing {@link #equals(Object)}, {@link #hashCode()} with deep
 * comparisons. This is intended primarily to allow raw, uninterpreted paths from
 * {@link MinimalZipEntry#getFileNameBytes()} to be used as map keys safely.
 */
public class ByteArrayHolder {
  /**
   * The backing byte array.
   */
  private final byte[] data;

  /**
   * Construct a new wrapper around the specified bytes.
   * @param data the byte array
   */
  public ByteArrayHolder(byte[] data) {
    this.data = data;
  }

  /**
   * Returns the actual byte array that backs the text.
   * @return the array
   */
  public byte[] getData() {
    return data;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(data);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ByteArrayHolder other = (ByteArrayHolder) obj;
    if (!Arrays.equals(data, other.data)) return false;
    return true;
  }
}
