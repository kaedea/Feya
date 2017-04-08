/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.applier;

import java.io.IOException;

/**
 * Thrown when there is an error in the format of a patch.
 */
@SuppressWarnings("serial")
public class PatchFormatException extends IOException {

  /**
   * Constructs a new exception with the specified message.
   * @param message the message
   */
  public PatchFormatException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified message and cause.
   * @param message the message
   * @param cause the cause of the error
   */
  public PatchFormatException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }
}
