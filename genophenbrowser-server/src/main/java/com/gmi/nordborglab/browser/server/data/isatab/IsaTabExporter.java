package com.gmi.nordborglab.browser.server.data.isatab;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.isatools.isacreator.configuration.MappingObject;
import org.isatools.isacreator.io.exportisa.ISAFileOutput;
import org.isatools.isacreator.io.exportisa.OutputISAFiles;
import org.isatools.isacreator.managers.ConfigurationManager;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.Investigation;
import org.isatools.isacreator.model.InvestigationContact;
import org.isatools.isacreator.model.InvestigationPublication;
import org.isatools.isacreator.model.Study;
import org.isatools.isacreator.model.StudyDesign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by uemit.seren on 4/3/14.
 */
@Component
public class IsaTabExporter {

    private static final Logger logger = LoggerFactory.getLogger(IsaTabExporter.class);
    @Resource
    protected AclManager aclManager;

    @Resource
    protected TraitUomRepository traitUomRepository;

    static final String INVESTIGATION_FILE = "i_investigation.txt";
    static final String STUDY_FILE = "s_study.txt";
    static final String ASSAY_FILE = "a_assay.txt";
    static final String TRAIT_DEF_FILE = "t_trait_def.txt";
    static final String DERVIVED_DATA_FILE = "d_derived_data.txt";
    static final String CONFIG_DIRECTORY = "ISA_TAB";
    final static String[] STUDY_HEADER = new String[]{"Source Name", "Characteristics[Organism]", "Characteristics[Infra-specific name]", "Characteristics[Organism part]"};
    final static String[] ASSAY_HEADER = new String[]{"Source Name", "Sample Name", "Trait Definition File", "Derived Data File"};
    final static String[] TRAIT_DEF_HEADER = new String[]{"Measurement Type", "Term Source REF", "Term Accession Number", "Technology Type", "Term Source REF", "Term Accession Number", "Unit Name", "Term Source REF", "Term Accession Number"};


    public IsaTabExporter() {
        try {
            URL url2 = getClass().getResource("/ISA_TAB/");
            ConfigurationManager.loadConfigurations(url2.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String save(Experiment experiment) throws IOException {
        return save(experiment, false);
    }


    public String save(Experiment experiment, boolean isZip) throws IOException {
        final String baseDirectory = Files.createTempDir().toString();
        Investigation investigation = createInvestigation(experiment, baseDirectory);
        final ISAFileOutput fileOutput = new OutputISAFiles();
        fileOutput.saveISAFiles(true, investigation);
        List<TraitUom> traitUoms = Lists.newArrayList(aclManager.filterByAcl(traitUomRepository.findByExperimentId(experiment.getId())));
        List<String[]> traitDefinitions = createTraitDefinitions(traitUoms);
        List<String[]> dervivedValues = createDervivedValues(experiment, traitUoms);
        saveCsvToFile(baseDirectory + File.separator + TRAIT_DEF_FILE, traitDefinitions);
        saveCsvToFile(baseDirectory + File.separator + DERVIVED_DATA_FILE, dervivedValues);
        if (isZip) {
            return zip(baseDirectory, experiment);
        }
        return baseDirectory;
    }


    private String zip(String baseDirectory, Experiment experiment) {
        String zipFileName = System.getProperty("java.io.tmpdir") + File.separator + String.format("%s_%s.zip", experiment.getName(), experiment.getId());
        File directory = new File(baseDirectory);
        byte[] buffer = new byte[1024];
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFileName);
            zos = new ZipOutputStream(fos);

            logger.debug("Output to Zip:" + zipFileName);
            for (File file : directory.listFiles()) {

                logger.debug("File Added : " + file);
                ZipEntry ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);

                FileInputStream in = new FileInputStream(file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
            }
            zos.closeEntry();
            //remember close it
            zos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("Failed to zip ISA-TAB folder", ex);
        } finally {
        }
        return zipFileName;
    }


    private void saveCsvToFile(String filename, List<String[]> rows) throws IOException {
        ICsvListWriter listWriter = null;
        try {
            listWriter = new CsvListWriter(new FileWriter(filename),
                    CsvPreference.STANDARD_PREFERENCE);

            // write the header
            listWriter.writeHeader(rows.get(0));

            // write the customer lists
            for (int i = 1; i < rows.size(); i++) {
                listWriter.write(rows.get(i));
            }
        } finally {
            if (listWriter != null) {
                listWriter.close();
            }
        }
    }

    public Investigation createInvestigation(Experiment experiment, String baseDirectory) {
        String file = baseDirectory + File.separator + INVESTIGATION_FILE;
        final Investigation investigation = new Investigation(experiment.getId().toString(), experiment.getName(),
                experiment.getDesign(), experiment.getCreated().toString(),
                (experiment.getPublished() != null ? experiment.getPublished().toString() : ""));
        final AppUser user = aclManager.getAppUserFromEntity(experiment);

        final InvestigationContact contact = new InvestigationContact(user.getLastname(), user.getFirstname(), "", user.getEmail(), "", "", "", "", "");
        investigation.addContact(contact);

        for (Publication publication : experiment.getPublications()) {
            investigation.addPublication(new InvestigationPublication("", publication.getDOI(), publication.getFirstAuthor(), publication.getTitle(), "Published"));
        }
        // call createStudy
        investigation.addStudy(createStudy(experiment));
        investigation.setFileReference(file);
        return investigation;
    }


    private Study createStudy(Experiment experiment) {
        final Study study = new Study(experiment.getId().toString(), experiment.getName(), "", "", "", "");
        study.setSampleFileName(STUDY_FILE);
        study.setStudyDesigns(Lists.newArrayList(new StudyDesign(experiment.getDesign())));
        final Assay studySample = new Assay(ASSAY_FILE,
                ConfigurationManager.selectTROForUserSelection(MappingObject.STUDY_SAMPLE));

        for (ObsUnit obsUnit : experiment.getObsUnits()) {
            Passport passport = obsUnit.getStock().getPassport();
            studySample.getTableReferenceObject()
                    .addRowData(
                            STUDY_HEADER,
                            new String[]{"source" + obsUnit.getId(), passport.getTaxonomy().getGenus() + " " + passport.getTaxonomy().getSpecies(), passport.getId().toString(),
                                    ""}
                    );
        }

        study.setStudySamples(studySample);
        List<Assay> assays = createAssays(experiment);
        for (Assay assay : assays) {
            study.addAssay(assay);
        }
        return study;
    }

    private List<Assay> createAssays(Experiment experiment) {
        final Assay assay = new Assay(ASSAY_FILE, "phenotyping", "", "");

        final Vector<String> assayHeaderVector = assay
                .getTableReferenceObject().getHeaders();

        final String[] assayHeaderArray = assayHeaderVector
                .toArray(new String[assayHeaderVector.size()]);
        for (ObsUnit obsUnit : experiment.getObsUnits()) {
            Passport passport = obsUnit.getStock().getPassport();
            assay.getTableReferenceObject().addRowData(
                    ASSAY_HEADER,
                    new String[]{"source" + obsUnit.getId(), "sample" + obsUnit.getId(), TRAIT_DEF_FILE, DERVIVED_DATA_FILE});
        }
        return Lists.newArrayList(assay);
    }

    private List<String[]> createTraitDefinitions(List<TraitUom> traitUoms) {
        List<String[]> traitDefinitions = Lists.newArrayList();
        traitDefinitions.add(TRAIT_DEF_HEADER);
        for (TraitUom traitUom : traitUoms) {
            String[] row = new String[TRAIT_DEF_HEADER.length];
            row[0] = traitUom.getLocalTraitName();
            if (traitUom.getToAccession() != null && !traitUom.getToAccession().isEmpty()) {
                row[1] = "TO";
                row[2] = traitUom.getToAccession();
            }
            row[3] = traitUom.getTraitProtocol();
            if (traitUom.getUnitOfMeasure() != null) {
                row[6] = traitUom.getUnitOfMeasure().getUnitType();
            }
            traitDefinitions.add(row);
        }
        return traitDefinitions;
    }

    private List<String[]> createDervivedValues(Experiment experiment, List<TraitUom> traitUoms) {
        List<String[]> dervivedValues = Lists.newArrayList();
        String[] header = new String[traitUoms.size() + 1];
        header[0] = "Sample Name";
        for (int i = 1; i <= traitUoms.size(); i++) {
            header[i] = String.format("Trait[%s]", traitUoms.get((i - 1)).getLocalTraitName());
        }
        dervivedValues.add(header);
        for (ObsUnit obsUnit : experiment.getObsUnits()) {
            Passport passport = obsUnit.getStock().getPassport();
            String[] row = new String[header.length];
            row[0] = "Sample" + obsUnit.getId().toString();
            for (int i = 1; i < header.length; i++) {
                row[i] = getTraitValue(obsUnit.getTraits(), traitUoms.get(i - 1).getId());
            }
            dervivedValues.add(row);
        }
        return dervivedValues;
    }


    private String getTraitValue(Set<Trait> traits, Long traitUomId) {
        for (Trait trait : traits) {
            //TODO deal with different statistical types
            if (trait.getTraitUom().getId() == traitUomId) {
                return trait.getValue();
            }
        }
        return null;
    }


}
