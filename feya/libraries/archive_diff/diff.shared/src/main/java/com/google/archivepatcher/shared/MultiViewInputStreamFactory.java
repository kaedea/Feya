/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.shared;

import java.io.IOException;
import java.io.InputStream;

/**
 * A factory that produces multiple independent but identical byte streams exposed via the
 * {@link InputStream} class.
 * @param <T> the type of {@link InputStream} that is produced
 */
public interface MultiViewInputStreamFactory<T extends InputStream> {
  /**
   * Create and return a new {@link InputStream}. The returned stream is guaranteed to independently
   * produce the same byte sequence as any other stream obtained via a call to this method on the
   * same instance of this object.
   * @return the stream
   * @throws IOException if something goes wrong
   */
  public T newStream() throws IOException;
}
