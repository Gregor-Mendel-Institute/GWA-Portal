package com.gmi.nordborglab.browser.server.data.isatab;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadValue;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.isatools.isacreator.configuration.MappingObject;
import org.isatools.isacreator.configuration.Ontology;
import org.isatools.isacreator.io.exportisa.ISAFileOutput;
import org.isatools.isacreator.io.exportisa.OutputISAFiles;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;
import org.isatools.isacreator.managers.ConfigurationManager;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.Contact;
import org.isatools.isacreator.model.Investigation;
import org.isatools.isacreator.model.InvestigationContact;
import org.isatools.isacreator.model.InvestigationPublication;
import org.isatools.isacreator.model.Study;
import org.isatools.isacreator.model.StudyDesign;
import org.isatools.isacreator.ontologymanager.BioPortal4Client;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.isacreator.ontologymanager.utils.OntologyUtils;
import org.isatools.isacreator.settings.ISAcreatorProperties;
import org.isatools.isacreator.spreadsheet.model.ReferenceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
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
    protected HelperService helperService;

    @Resource
    private TermRepository termRepository;

    @Resource
    protected TraitUomRepository traitUomRepository;

    final static Format dateFormatter = new SimpleDateFormat("dd/MM/YYYY");
    static final String INVESTIGATION_FILE = "i_investigation.txt";
    static final String STUDY_FILE = "s_study.txt";
    static final String ASSAY_FILE = "a_assay.txt";
    static final String TRAIT_DEF_FILE = "t_trait_def.txt";
    static final String DERVIVED_DATA_FILE = "d_derived_data.txt";
    static URL CONFIG_DIRECTORY;
    static final Pattern derivedTraitPattern = Pattern.compile("Trait Value\\[(.*)\\]");

    final static String[] STUDY_HEADER = new String[]{"Source Name", "Characteristics[Organism]", "Term Source REF", "Term Accession Number", "Characteristics[Infra-specific name]", "Term Source REF", "Term Accession Number", "Characteristics[Organism part]", "Term Source REF", "Term Accession Number", "Protocol REF", "Sample Name"};
    final static String[] ASSAY_HEADER = new String[]{"Sample Name", "Protocol REF", "Assay Name", "Comment[Trait Definition File]", "Raw Data File", "Protocol REF", "Normalization Name", "Data Transformation Name", "Derived Data File"};
    final static String[] TRAIT_DEF_HEADER = new String[]{"Measurement Type", "Term Source REF", "Term Accession Number", "Technology Type", "Term Source REF", "Term Accession Number", "Unit Name", "Term Source REF", "Term Accession Number"};
    private static ImmutableSet<OntologySourceRefObject> ontologySources = null;
    //private static final OntologySourceRefObject traitOntologySource = new OntologySourceRefObject("TO","http://www.ontobee.org/browser/index.php?o=TO","","Gramene trait ontology");
    //private static final OntologySourceRefObject accOntologySource = new OntologySourceRefObject("ARA","http://www.ontobee.org/browser/index.php?o=TO","","Taxonomy trait ontology");
    private static final OntologySourceRefObject obiOntologySource = new OntologySourceRefObject("OBI", "http://data.bioontology.org/ontologies/OBI", "", "Ontology for Biomedical Investigations\n");
    private static final OntologySourceRefObject taxOntologySource = new OntologySourceRefObject("NCBITAXON", "http://data.bioontology.org/ontologies/NCBITAXON", "", "National Center for Biotechnology Information (NCBI) Organismal Classification");

    public IsaTabExporter() {
        CONFIG_DIRECTORY = getClass().getResource("/ISA_TAB/");
        try {
            ConfigurationManager.loadConfigurations(CONFIG_DIRECTORY.getPath());
            if (ontologySources == null) {
                ontologySources = ImmutableSet.<OntologySourceRefObject>builder()
                        .add(obiOntologySource)
                                //  .add(traitOntologySource)
                                //   .add(accOntologySource)
                        .add(taxOntologySource)
                        .build();
            }

            final BioPortal4Client client = new BioPortal4Client();
            Collection<Ontology> ontologies = client.getAllOntologies();
            List<OntologySourceRefObject> list = new ArrayList();
            for (Ontology ontology : ontologies) {
                list.add(OntologyUtils.convertOntologyToOntologySourceReferenceObject(ontology));
            }
            ISAcreatorProperties.setProperty(ISAcreatorProperties.ONTOLOGY_TERM_URI, "true");
            OntologyManager.setOntologySources(ImmutableSet.copyOf(list));
            //OntologyManager.getOntologySelectionHistory().put("OBI",OntologyManager.getOntologyTerm("OBI"));

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
        fileOutput.saveISAFiles(false, investigation);
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


    public ExperimentUploadData getExperimentUploadDataFromArchive(byte[] data) {
        final File newTempDir = Files.createTempDir();
        ExperimentUploadData uploadData = null;
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(new ByteArrayInputStream(data));
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {

                String entryName = entry.getName();
                File file = new File(entryName);
                if (file.isDirectory()) {
                    continue;
                }
                FileOutputStream out = new FileOutputStream(newTempDir + File.separator + file.getName());


                byte[] byteBuff = new byte[4096];
                int bytesRead = 0;
                while ((bytesRead = zipStream.read(byteBuff)) != -1) {
                    out.write(byteBuff, 0, bytesRead);
                }

                out.close();
                zipStream.closeEntry();
            }
            zipStream.close();
            uploadData = getExperimentUploadDataFromArchive(newTempDir.getAbsolutePath());
        } catch (IOException ex) {
            logger.error("Parse ISA-TAB Zip failed", ex);
        } finally {
            newTempDir.delete();
        }
        return uploadData;
    }

    public ExperimentUploadData getExperimentUploadDataFromArchive(String parentDir) {
        // Required because FileImporter is not threadsafe.
        logger.info(String.format("Starting ISA-TAB import from %s", parentDir));
        ISAtabFilesImporter importer = new ISAtabFilesImporter(CONFIG_DIRECTORY.getPath());
        boolean isSuccess = importer.importFile(parentDir);
        ExperimentUploadData data = null;
        if (!isSuccess) {
            String message = String.format("Failed to import ISA-TAB archive from %s. Error: %s", parentDir, importer.getMessagesAsString());
            logger.error(message, importer);
            throw new RuntimeException(message);
        }
        data = ExperimentUploadData.createFromInvestigation(importer.getInvestigation());
        if (importer.getInvestigation().getPublications() != null) {
            for (org.isatools.isacreator.model.Publication publication : importer.getInvestigation().getPublications()) {

            }
        }
        data.setPhenotypes(getPhenotypeUploadDataFromInvestigation(importer.getInvestigation(), parentDir));
        return data;
    }

    private Map<String, String> getPassportLookupMap(Assay assay) {
        Map<String, String> map = Maps.newHashMap();
        ReferenceData data = assay.getTableReferenceObject().getReferenceData();
        int infraspecificColId = assay.getTableReferenceObject().getFieldByName("Characteristics[Infra-specific name]").getColNo();
        int sampleColId = assay.getTableReferenceObject().getFieldByName("Sample Name").getColNo();
        for (List<String> row : data.getData()) {
            map.put(row.get(sampleColId), row.get(infraspecificColId));
        }
        return ImmutableMap.copyOf(map);
    }


    private List<PhenotypeUploadData> getPhenotypeUploadDataFromInvestigation(Investigation inv, String parentDir) {
        List<PhenotypeUploadData> phenotypes = Lists.newArrayList();
        for (Study study : inv.getStudies().values()) {
            Map<String, String> passportLookupMap = getPassportLookupMap(study.getStudySample());
            for (Assay assay : study.getAssays().values()) {
                Map<String, String[]> assayLookupMap = getAssayLookupMap(assay);
                Map<String, Map<String, PhenotypeUploadData>> traitDefLookupMap = Maps.newHashMap();
                Map<String, IsaTabDerivedData> traitDerivedLookupMap = Maps.newHashMap();
                for (Map.Entry<String, String[]> entry : assayLookupMap.entrySet()) {
                    String[] assayRow = entry.getValue();
                    Map<String, PhenotypeUploadData> phenotypeUploadDataList = traitDefLookupMap.get(assayRow[1]);
                    if (phenotypeUploadDataList == null) {
                        //create PhenotypeUploadData
                        phenotypeUploadDataList = parseTraitDefFile(parentDir + File.separator + assayRow[1]);
                        traitDefLookupMap.put(assayRow[1], phenotypeUploadDataList);
                    }
                    IsaTabDerivedData isaTabData = traitDerivedLookupMap.get(assayRow[2]);
                    if (isaTabData == null) {
                        // Load deriveed data file
                        isaTabData = parseDerivedDataFile(parentDir + File.separator + assayRow[2]);
                        for (int i = 0; i < isaTabData.getData().size(); i++) {
                            List<String> values = isaTabData.getData().get(i);
                            PhenotypeUploadData phenotypeUploadData = phenotypeUploadDataList.get(isaTabData.getPhenotypes().get(i));
                            for (int j = 0; j < values.size(); j++) {
                                String value = values.get(j);
                                if (value == null || value.equals(""))
                                    continue;
                                PhenotypeUploadValue phenotypeUploadValue = new PhenotypeUploadValue();
                                phenotypeUploadValue.setSourceId(passportLookupMap.get(assayLookupMap.get(isaTabData.getAssays().get(j))[0]));
                                phenotypeUploadValue = helperService.parseAndUpdateAccession(phenotypeUploadValue);
                                phenotypeUploadValue.setValues(Lists.newArrayList(value));
                                phenotypeUploadData.addPhenotypeValue(phenotypeUploadValue);
                            }
                        }
                        traitDerivedLookupMap.put(assayRow[2], isaTabData);
                        phenotypes.addAll(Lists.newArrayList(phenotypeUploadDataList.values()));
                    }
                }
            }
        }
        return phenotypes;
    }

    private Map<String, PhenotypeUploadData> parseTraitDefFile(String filename) {
        ICsvListReader reader = null;
        Map<String, PhenotypeUploadData> traitDefLookupMap = Maps.newHashMap();
        try {
            reader = new CsvListReader(new FileReader(filename), CsvPreference.STANDARD_PREFERENCE);
            final String[] header = reader.getHeader(true);
            PhenotypeUploadData phenotypeUploadData;
            List<String> traitDefList;
            while ((traitDefList = reader.read()) != null) {
                phenotypeUploadData = new PhenotypeUploadData();
                String measurementName = traitDefList.get(0);
                phenotypeUploadData.setName(measurementName);

                // get ontology
                String traitOntology = String.format("%s:%s", traitDefList.get(1), traitDefList.get(2));
                phenotypeUploadData.setTraitOntology(termRepository.findByAcc(traitOntology));
                phenotypeUploadData.setProtocol(traitDefList.get(3));
                phenotypeUploadData.setUnitOfMeasure(traitDefList.get(6));
                phenotypeUploadData.setValueHeader(Lists.newArrayList("MEASURE"));
                traitDefLookupMap.put(measurementName, phenotypeUploadData);
            }
        } catch (IOException e) {
        }
        return traitDefLookupMap;
    }


    private IsaTabDerivedData parseDerivedDataFile(String filename) {
        ICsvListReader reader = null;
        IsaTabDerivedData isaTabDerivedData = null;
        try {
            reader = new CsvListReader(new FileReader(filename), CsvPreference.STANDARD_PREFERENCE);
            final String[] header = reader.getHeader(true);
            // regex:
            List<String> phenotypes = Lists.newArrayList();
            for (int i = 1; i < header.length; i++) {
                Matcher matcher = derivedTraitPattern.matcher(header[i]);
                if (matcher.matches()) {
                    phenotypes.add(matcher.group(1));
                }
            }
            int numberOfPhenotypes = phenotypes.size();
            List<String> assays = Lists.newArrayList();
            List<List<String>> data = Lists.newArrayList();
            List<String> traitDefList;
            while ((traitDefList = reader.read()) != null) {
                assays.add(traitDefList.get(0));
                List<String> dataPercolumn = null;
                for (int i = 1; i < header.length; i++) {
                    if (data.size() < numberOfPhenotypes) {
                        dataPercolumn = Lists.newArrayList();
                        data.add(dataPercolumn);
                    } else {
                        dataPercolumn = data.get((i - 1));
                    }
                    dataPercolumn.add(traitDefList.get(i));
                }
            }
            isaTabDerivedData = new IsaTabDerivedData(phenotypes, assays, data);
        } catch (IOException e) {
        }

        return isaTabDerivedData;
    }

    private Map<String, String[]> getAssayLookupMap(Assay assay) {
        Map<String, String[]> map = Maps.newHashMap();
        ReferenceData data = assay.getTableReferenceObject().getReferenceData();
        int assayColId = assay.getTableReferenceObject().getFieldByName("Assay Name").getColNo();
        int sampleColId = assay.getTableReferenceObject().getFieldByName("Sample Name").getColNo();
        int traitDefFileColId = assay.getTableReferenceObject().getFieldByName("Comment[Trait Definition File]").getColNo();
        int dervivedDataFileColId = assay.getTableReferenceObject().getFieldByName("Derived Data File").getColNo();
        for (List<String> row : data.getData()) {
            String[] value = new String[3];
            value[0] = row.get(sampleColId);
            value[1] = row.get(traitDefFileColId);
            value[2] = row.get(dervivedDataFileColId);
            map.put(row.get(assayColId), value);
        }
        return ImmutableMap.copyOf(map);
    }


    public Investigation createInvestigation(Experiment experiment, String baseDirectory) {
        String file = baseDirectory + File.separator + INVESTIGATION_FILE;
        final Investigation investigation = new Investigation(experiment.getId().toString(), experiment.getName(),
                experiment.getDesign(), dateFormatter.format(experiment.getCreated()),
                (experiment.getPublished() != null ? dateFormatter.format(experiment.getPublished()) : ""));
        final AppUser user = aclManager.getAppUserFromEntity(experiment);

        final InvestigationContact contact = new InvestigationContact(user.getLastname(), user.getFirstname(), "", user.getEmail(), "", "", "", "", "");
        investigation.setContacts(Lists.<Contact>newArrayList(contact));

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
        String[] header = studySample.getTableReferenceObject().getHeaders().toArray(new String[]{"Source Name", "Characteristics[Organism]", "Term Source REF", "Term Accession Number", "Characteristics[Infra-specific name]", "Term Source REF", "Term Accession Number", "Characteristics[Organism part]", "Term Source REF", "Term Accession Number", "Protocol REF", "Sample Name"});
        for (ObsUnit obsUnit : experiment.getObsUnits()) {
            Passport passport = obsUnit.getStock().getPassport();
            studySample.getTableReferenceObject()
                    .addRowData(
                            STUDY_HEADER,
                            new String[]{"source" + obsUnit.getId(),
                                    //passport.getTaxonomy().getGenus() + " " + passport.getTaxonomy().getSpecies(),
                                    "4932",
                                    "NCBITAXON",
                                    "http://purl.obolibrary.org/obo/NCBITaxon_3702",
                                    passport.getId().toString(),
                                    "ARA", passport.getId().toString(), // change to div_passport_acc
                                    "", "", "", "", "sample" + obsUnit.getId()
                            }
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
                .toArray(ASSAY_HEADER);
        for (ObsUnit obsUnit : experiment.getObsUnits()) {
            Passport passport = obsUnit.getStock().getPassport();
            assay.getTableReferenceObject().addRowData(
                    ASSAY_HEADER,
                    new String[]{"sample" + obsUnit.getId(),
                            "",
                            "phenotyping" + obsUnit.getId(),
                            TRAIT_DEF_FILE,
                            "",
                            "",
                            "",
                            "",
                            DERVIVED_DATA_FILE}
            );
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
        header[0] = "Assay Name";
        for (int i = 1; i <= traitUoms.size(); i++) {
            header[i] = String.format("Trait Value[%s]", traitUoms.get((i - 1)).getLocalTraitName());
        }
        dervivedValues.add(header);
        for (ObsUnit obsUnit : experiment.getObsUnits()) {
            Passport passport = obsUnit.getStock().getPassport();
            String[] row = new String[header.length];
            row[0] = "phenotyping" + obsUnit.getId().toString();
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
