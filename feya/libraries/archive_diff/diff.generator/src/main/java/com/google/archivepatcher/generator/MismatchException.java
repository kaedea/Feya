/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator;

import java.io.IOException;

/**
 * Thrown when data does not match expected values.
 */
@SuppressWarnings("serial")
public class MismatchException extends IOException {
  /**
   * Construct an exception with the specified message
   * @param message the message
   */
  public MismatchException(String message) {
    super(message);
  }
}
