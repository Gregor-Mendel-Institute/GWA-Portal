package com.gmi.nordborglab.browser.server.data;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by uemit.seren on 10/22/14.
 */
public interface GenotypeReader {

    List<Byte> getAlleles(String genotype, Integer chr, Integer position, LinkedHashSet<String> passportIds);
}
