/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.google.archivepatcher.generator.similarity;

import com.google.archivepatcher.generator.MinimalZipEntry;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Detects identical files on the basis of the CRC32 of uncompressed content. All entries that have
 * the same CRC32 will be identified as similar (and presumably are identical, in the absence of
 * hash collisions).
 */
public class Crc32SimilarityFinder extends SimilarityFinder {

  /**
   * All entries in the base archive, organized by CRC32.
   */
  private final Map<Long, List<MinimalZipEntry>> baseEntriesByCrc32 = new HashMap<>();

  /**
   * Constructs a new similarity finder with the specified parameters.
   * @param baseArchive the base archive that contains the entries to be searched
   * @param baseEntries the entries in the base archive that are eligible to be searched
   */
  public Crc32SimilarityFinder(File baseArchive, Collection<MinimalZipEntry> baseEntries) {
    super(baseArchive, baseEntries);
    for (MinimalZipEntry oldEntry : baseEntries) {
      long crc32 = oldEntry.getCrc32OfUncompressedData();
      List<MinimalZipEntry> entriesForCrc32 = baseEntriesByCrc32.get(crc32);
      if (entriesForCrc32 == null) {
        entriesForCrc32 = new LinkedList<>();
        baseEntriesByCrc32.put(crc32, entriesForCrc32);
      }
      entriesForCrc32.add(oldEntry);
    }
  }

  @Override
  public List<MinimalZipEntry> findSimilarFiles(File newArchive, MinimalZipEntry newEntry) {
    List<MinimalZipEntry> matchedEntries =
        baseEntriesByCrc32.get(newEntry.getCrc32OfUncompressedData());
    if (matchedEntries == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(matchedEntries);
  }
}
