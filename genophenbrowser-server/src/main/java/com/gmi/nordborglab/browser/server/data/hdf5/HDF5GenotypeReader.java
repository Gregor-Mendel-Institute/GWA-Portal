package com.gmi.nordborglab.browser.server.data.hdf5;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import com.gmi.nordborglab.browser.server.data.GenotypeReader;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
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
    public List<Byte> getAlleles(String genotype, Integer chr, Integer position, LinkedHashSet<String> passportIds) {
        IHDF5Reader reader = getReader(genotype);
        List<Byte> data = getAlleles(reader, chr, position, passportIds);
        reader.close();
        return data;
    }

    private List<Byte> getAlleles(IHDF5Reader reader, Integer chr, Integer position, LinkedHashSet<String> passportIds) {
        HDF5DataSetInformation info = reader.object().getDataSetInformation("snps");
        final long accessionCount = info.getNumberOfElements();
        List<Byte> alleles;
        List<String> accessionIds = Lists.newArrayList(reader.string().readArray("accessions"));
        List<Integer> indices = Lists.newArrayList();
        final int chr_ix = chr - 1;
        // get the indices of the passportIds
        if (passportIds != null) {
            for (String passportId : passportIds) {
                int ix = accessionIds.indexOf(passportId);
                if (ix >= 0) {
                    indices.add(ix);
                }
            }
        }

        // get the position index;
        final int[] chr_region = reader.int32().getMatrixAttr("positions", "chr_regions")[chr_ix];
        final int block_size = chr_region[1] - chr_region[0];
        final int[] positions = reader.uint32().readArrayBlockWithOffset("positions", block_size, chr_region[0]);
        final int pos_ix = chr_region[0] + Arrays.binarySearch(positions, position);
        if (pos_ix == -1)
            throw new RuntimeException(String.format("no SNP at position %s found", position));
        // get entire snps

        final byte[] snps = reader.int8().readMatrixBlockWithOffset("snps", 1, (int) accessionCount, pos_ix, 0)[0];

        if (passportIds != null) {
            alleles = Lists.newArrayList();
            for (Integer ix : indices) {
                alleles.add(snps[ix]);
            }
        } else {
            alleles = Bytes.asList(snps);
        }
        return alleles;

    }

    protected IHDF5Reader getReader(String file) {
        IHDF5Reader reader = HDF5Factory.openForReading(GENOTYPE_DATA_FOLDER + File.separator + file + "/all_chromosomes_binary.hdf5");
        return reader;
    }
}
