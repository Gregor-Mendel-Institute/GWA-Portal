package com.gmi.nordborglab.browser.server.data.hdf5;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import com.gmi.nordborglab.browser.server.data.GenotypeReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;


/**
 * Created by uemit.seren on 10/22/14.
 */

@Component
public class HDF5GenotypeReader implements GenotypeReader {


    @Value("${GENOTYPE.data_folder}")
    private String GENOTYPE_DATA_FOLDER;

    public HDF5GenotypeReader() {
    }

    public HDF5GenotypeReader(String GENOTYPE_DATA_FOLDER) {
        this.GENOTYPE_DATA_FOLDER = GENOTYPE_DATA_FOLDER;
    }


    @Override
    public byte[] getAlleles(String genotype, Integer chr, Integer position, LinkedHashSet<String> passportIds) {
        IHDF5Reader reader = getReader(genotype);
        byte[] snps = getAlleles(reader, chr, position, position, passportIds)[0];
        reader.close();
        return snps;
    }

    @Override
    public int[] getAlleleCount(String genotype, Integer chr, Integer start, Integer end, LinkedHashSet<String> passportIds) {
        IHDF5Reader reader = getReader(genotype);
        byte[][] snps = getAlleles(reader, chr, start, end, passportIds);
        int[] allelesCount = new int[snps.length];
        for (int i = 0; i < snps.length; i++) {
            int sum = 0;
            for (int j = 0; j < snps[i].length; j++) {
                sum += snps[i][j];
            }
            allelesCount[i] = sum;
        }
        reader.close();
        return allelesCount;
    }

    @Override
    public int[] getPositions(String genotype, Integer chr, int start, int end) {
        IHDF5Reader reader = getReader(genotype);
        int[] positions = getPositions(reader, chr, start, end);
        reader.close();
        return positions;
    }

    @Override
    public List<String> getAccessionIds(String genotype, LinkedHashSet<String> passportIds) {
        IHDF5Reader reader = getReader(genotype);
        List<String> accessionIds = getAccessionIds(reader, passportIds);
        reader.close();
        return accessionIds;
    }

    private List<String> getAccessionIds(IHDF5Reader reader, LinkedHashSet<String> passportIds) {
        LinkedHashSet<String> accessionIds = Sets.newLinkedHashSet(Lists.newArrayList(reader.string().readArray("accessions")));
        if (passportIds == null)
            return ImmutableList.copyOf(accessionIds);
        // get the indices of the passportIds
        List<String> filteredList = Lists.newArrayList();
        if (passportIds != null) {
            for (String passportId : passportIds) {
                if (accessionIds.contains(passportId)) {
                    filteredList.add(passportId);
                }
            }
        }
        return filteredList;
    }

    private int[] getPositions(IHDF5Reader reader, Integer chr, int start, int end) {
        final int[] chr_region = getChrRegionFromChr(reader, chr);
        int[] positions = reader.uint32().readArrayBlockWithOffset("positions", chr_region[1] - chr_region[0], chr_region[0]);
        final int start_pos_ix = getPositionIndex(positions, start);
        final int end_pos_ix = getPositionIndex(positions, end) + 1;
        return Arrays.copyOfRange(positions, start_pos_ix, end_pos_ix);
    }


    private byte[][] getAlleles(IHDF5Reader reader, Integer chr, Integer start, Integer end, LinkedHashSet<String> passportIds) {
        List<String> accessionIds = Lists.newArrayList(reader.string().readArray("accessions"));
        List<Integer> passportIndices = Lists.newArrayList();
        // get the indices of the passportIds
        if (passportIds != null) {
            for (String passportId : passportIds) {
                int ix = accessionIds.indexOf(passportId);
                if (ix >= 0) {
                    passportIndices.add(ix);
                }
            }
        }
        // get the position index;
        final int[] chr_region = getChrRegionFromChr(reader, chr);
        final int block_size = chr_region[1] - chr_region[0];
        final int[] positions = reader.uint32().readArrayBlockWithOffset("positions", block_size, chr_region[0]);
        final int start_pos_ix = chr_region[0] + getPositionIndex(positions, start);
        final int end_pos_ix = chr_region[0] + getPositionIndex(positions, end);
        int numberOfSnps = (end_pos_ix - start_pos_ix) + 1;
        // get entire snps
        final byte[][] snps = reader.int8().readMatrixBlockWithOffset("snps", numberOfSnps, -1, start_pos_ix, 0);
        if (passportIds != null) {
            final byte[][] filteredSNPs = new byte[numberOfSnps][passportIds.size()];
            for (int i = 0; i < filteredSNPs.length; i++) {
                for (int j = 0; j < passportIndices.size(); j++) {
                    filteredSNPs[i][j] = snps[i][passportIndices.get(j)];
                }
            }
            return filteredSNPs;
        }
        return snps;
    }

    private static int getPositionIndex(int[] positions, Integer position) {
        int ix = Arrays.binarySearch(positions, position);
        if (ix < 0) {
            ix = -ix - 1;
        }
        return ix;
    }

    private static int[] getChrRegionFromChr(IHDF5Reader reader, Integer chr) {
        int chrIdx = chr - 1;
        if (reader.hasAttribute("positions", "chrs")) {
            int[] chrs = reader.int32().getArrayAttr("positions", "chrs");
            chrIdx = Ints.indexOf(chrs, chr);
        }
        return reader.int32().getMatrixAttr("positions", "chr_regions")[chrIdx];
    }


    protected IHDF5Reader getReader(String file) {
        IHDF5Reader reader = HDF5Factory.openForReading(GENOTYPE_DATA_FOLDER + File.separator + file + "/all_chromosomes_binary.hdf5");
        return reader;
    }
}
