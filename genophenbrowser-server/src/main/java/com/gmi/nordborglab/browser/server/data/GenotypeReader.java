package com.gmi.nordborglab.browser.server.data;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by uemit.seren on 10/22/14.
 */
public interface GenotypeReader {

    byte[] getAlleles(String genotype, Integer chr, Integer position, LinkedHashSet<String> passportIds);

    int[] getAlleleCount(String genotype, Integer chr, Integer start, Integer end, LinkedHashSet<String> passportIds);

    int[] getPositions(String s, Integer chr, int start, int end);

    List<String> getAccessionIds(String genotype, LinkedHashSet<String> passportIds);
}
