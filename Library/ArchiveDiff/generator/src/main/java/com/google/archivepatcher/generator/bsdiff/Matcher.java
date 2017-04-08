/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator.bsdiff;

import java.io.IOException;

/**
 * Helper class which iterates through |newData| finding the longest valid exact matches between
 * |newData| and |oldData|. The interface exists for the sake of testing.
 */
interface Matcher {
  /**
   * Determine the range for the next match, and store it in member state.
   * @return a {@link NextMatch} describing the result
   */
  NextMatch next() throws IOException, InterruptedException;

  /**
   * Contains a boolean which indicates whether a match was found, the old position (if a match was
   * found), and the new position (if a match was found).
   */
  static class NextMatch {
    final boolean didFindMatch;
    final int oldPosition;
    final int newPosition;

    static NextMatch of(boolean didFindMatch, int oldPosition, int newPosition) {
      return new NextMatch(didFindMatch, oldPosition, newPosition);
    }

    private NextMatch(boolean didFindMatch, int oldPosition, int newPosition) {
      this.didFindMatch = didFindMatch;
      this.oldPosition = oldPosition;
      this.newPosition = newPosition;
    }
  }
}
