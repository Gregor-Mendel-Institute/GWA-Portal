package com.gmi.nordborglab.browser.server.data.hdf5;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by uemit.seren on 2/12/15.
 */
public class HDF5GenotypeReaderTest {

    private static final String GENOTYPE_FOLDER = "/net/gmi.oeaw.ac.at/gwasapp/DATA_NEW_BROWSER/PYGWAS_GENOTYPES/";
    private static final String genotype = "4";
    private static final Integer chr = 5;
    private static final Integer position = 18592365;
    private static LinkedHashSet<String> passportIds;


    private HDF5GenotypeReader sut;


    @BeforeClass
    public static void setUpClass() {
        Integer[] ids = new Integer[]{9427, 6220, 9436, 6198, 6138, 6087, 6041, 6240, 6201, 6154, 6107, 6268, 8427, 8231, 6913, 6040, 9404, 6092, 6042, 6090, 6114, 6134, 6194, 9408, 8426, 7516, 6140, 6102, 6013, 9382, 6073, 6108, 9418, 6172, 6111, 9407, 9339, 9390, 6284, 8369, 6128, 6241, 6193, 6039, 6188, 6038, 6238, 5830, 9434, 9386, 5836, 6195, 5860, 1062, 8230, 9363, 6221, 6019, 6276, 8334, 9437, 5832, 6209, 6098, 9323, 6099, 6258, 9470, 6035, 9353, 6235, 6149, 8387, 8307, 6145, 1435, 6244, 6413, 9402, 6100, 6242, 6069, 9476, 6243, 6166, 6118, 1002, 6022, 6191, 9383, 8249, 6192, 1063, 5856, 6020, 9433, 6214, 6184, 9336, 6174, 9453, 8306, 6171, 9450, 7518, 9058, 1006, 6036, 9380, 6231, 6064, 8222, 6012, 5831, 6011, 6141, 6132, 9405, 6133, 9381, 1313, 6095, 6023, 9442, 9399, 9332, 9392, 6086, 9321, 8227, 9343, 9481, 8241, 8256, 8283, 9451, 9057, 7519, 6136, 6046, 6077, 6025, 8242, 6076, 6104, 6189, 997, 1166, 6112, 9455, 8258, 6024, 6163, 6125, 6021, 5867, 6123, 6085, 6151, 6106, 6973, 8386, 9421, 6071, 6203, 6150, 9369, 6109, 9413, 8240, 6094, 9416, 6122, 8247, 6010, 6043, 8237, 6091, 6126, 1317, 5865, 9412, 6017, 6237, 6217, 9409, 8259, 6202, 991, 6137, 9391, 6148, 6127, 6030, 6974, 6218, 6097, 6236, 9454, 6119, 992, 6115, 8376, 6177, 8351, 6169, 6180, 9471, 6131, 6124, 9388, 6210, 6216, 6173, 6074, 6034, 6016, 6142, 8335};
        passportIds = Sets.newLinkedHashSet();
        passportIds.addAll(Lists.transform(Arrays.asList(ids), new Function<Integer, String>() {
            @Nullable
            @Override
            public String apply(Integer input) {
                return String.valueOf(input);
            }
        }));
    }

    @Before
    public void setup() {
        sut = new HDF5GenotypeReader(GENOTYPE_FOLDER);
    }


    @Test
    public void testGetAllelesForAllAccessions() {
        List<Byte> data = sut.getAlleles(genotype, chr, position, null);
        assertThat(data.size(), is(259));
        int allele1 = Collections2.filter(data, new Predicate<Byte>() {
            @Override
            public boolean apply(Byte input) {
                return input == 0;
            }
        }).size();
        int allele2 = Collections2.filter(data, new Predicate<Byte>() {
            @Override
            public boolean apply(Byte input) {
                return input == 1;
            }
        }).size();
        assertThat(allele1, is(148));
        assertThat(allele2, is(111));
    }

    @Test
    public void testGetAllelesForSpecificAccessions() {
        List<Byte> data = sut.getAlleles(genotype, chr, position, passportIds);
        assertThat(data.size(), is(219));
        int allele1 = Collections2.filter(data, new Predicate<Byte>() {
            @Override
            public boolean apply(Byte input) {
                return input == 0;
            }
        }).size();
        int allele2 = Collections2.filter(data, new Predicate<Byte>() {
            @Override
            public boolean apply(Byte input) {
                return input == 1;
            }
        }).size();
        assertThat(allele1, is(124));
        assertThat(allele2, is(95));
    }

}
