/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator;

import java.io.File;
import java.util.List;

/**
 * Provides a mechanism to review and possibly modify the {@link QualifiedRecommendation}s that will
 * be used to derive a {@link PreDiffPlan}.
 */
public interface RecommendationModifier {
  /**
   * Given a list of {@link QualifiedRecommendation} objects, returns a list of the same type that
   * has been arbitrarily adjusted as desired by the implementation. Implementations must return a
   * list of recommendations that contains the same tuples of (oldEntry, newEntry) but may change
   * the results of {@link QualifiedRecommendation#getRecommendation()} and {@link
   * QualifiedRecommendation#getReason()} to any sane values.
   *
   * @param oldFile the old file that is being diffed
   * @param newFile the new file that is being diffed
   * @param originalRecommendations the original recommendations
   * @return the updated list of recommendations
   */
  public List<QualifiedRecommendation> getModifiedRecommendations(
      File oldFile, File newFile, List<QualifiedRecommendation> originalRecommendations);
}
