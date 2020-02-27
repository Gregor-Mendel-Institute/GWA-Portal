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
import com.gmi.nordborglab.browser.server.rest.SampleData;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
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
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;
import org.isatools.isacreator.ontologymanager.utils.OntologyUtils;
import org.isatools.isacreator.settings.ISAcreatorProperties;
import org.isatools.isacreator.spreadsheet.model.ReferenceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by uemit.seren on 4/3/14.
 */
@Component
public class IsaTabExporterImpl implements IsaTabExporter {

    private static final Logger logger = LoggerFactory.getLogger(IsaTabExporterImpl.class);
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
    static final String TRAIT_DEF_FILE = "tdf.txt";
    static final String DERVIVED_DATA_FILE = "d_derived_data.txt";
    static URL CONFIG_DIRECTORY;
    static final Pattern derivedTraitPattern = Pattern.compile("Trait Value\\[(.*)\\]");

    final static String[] STUDY_HEADER = new String[]{"Source Name", "Characteristics[Organism]", "Characteristics[Infraspecific name]", "Characteristics[Seed origin]", "Characteristics[Study start]", "Characteristics[Study duration]", "Characteristics[Growth facility]", "Characteristics[Geographic location]", "Sample Name"};
    final static String[] ASSAY_HEADER = new String[]{"Sample Name", "Characteristics[Organism Part]", "Assay Name", "Raw Data File", "Protocol REF", "Parameter Value[Trait Definition File]", "Derived Data File"};
    final static String[] TRAIT_DEF_HEADER = new String[]{"Variable ID", "Trait", "Term Source REF", "Term Accession Number", "Method", "Term Source REF", "Term Accession Number", "Scale", "Term Source REF", "Term Accession Number"};
    private static final OntologySourceRefObject accOntologySource = new OntologySourceRefObject("ARA", "http://gwas.gmi.oeaw.ac.at/#/taxonomy/1/passports?alleleAssayId=0", "", "Catalogue of Arabidopsis accessions");


    public IsaTabExporterImpl() {
        CONFIG_DIRECTORY = getClass().getResource("/ISA_TAB/");
        try {
            ConfigurationManager.loadConfigurations(CONFIG_DIRECTORY.getPath());
            final BioPortal4Client client = new BioPortal4Client();
            Collection<Ontology> ontologies = client.getAllOntologies();
            List<OntologySourceRefObject> list = new ArrayList();
            list.add(accOntologySource);
            for (Ontology ontology : ontologies) {
                list.add(OntologyUtils.convertOntologyToOntologySourceReferenceObject(ontology));
            }
            ISAcreatorProperties.setProperty(ISAcreatorProperties.ONTOLOGY_TERM_URI, "false");
            OntologyManager.setOntologySources(ImmutableSet.copyOf(list));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String save(Experiment experiment) throws IOException {
        return save(experiment, false);
    }


    @Override
    public String save(Experiment experiment, boolean isZip) throws IOException {
        final String baseDirectory = Files.createTempDir().toString();
        Investigation investigation = createInvestigation(experiment, baseDirectory);
        final ISAFileOutput fileOutput = new OutputISAFiles();
        fileOutput.saveISAFiles(false, investigation);
        List<TraitUom> traitUoms = Lists.newArrayList(aclManager.filterByAcl(traitUomRepository.findByExperimentId(experiment.getId())));
        List<String[]> traitDefinitions = createTraitDefinitions(traitUoms);
        List<String[]> dervivedValues = createDerivedValues(experiment, traitUoms);
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
        try (
                FileOutputStream fos = new FileOutputStream(zipFileName);
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
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
            logger.error("Failed to zip ISA-TAB folder", ex);
        }
        return zipFileName;
    }


    private void saveCsvToFile(String filename, List<String[]> rows) throws IOException {
        try (
                FileWriter fout = new FileWriter(filename);
                ICsvListWriter listWriter = new CsvListWriter(fout, CsvPreference.TAB_PREFERENCE)
        ) {
            // write the header
            listWriter.writeHeader(rows.get(0));

            // write the customer lists
            for (int i = 1; i < rows.size(); i++) {
                listWriter.write(rows.get(i));
            }
        }
    }


    @Override
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

    @Override
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
                data.setDoi(publication.getPublicationDOI());
            }
        }
        addPhenotypeUploadDataFromInvestigation(importer.getInvestigation(), parentDir, data);
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

    private void addPhenotypeUploadDataFromInvestigation(Investigation inv, String parentDir, ExperimentUploadData experimentUploadData) {
        List<PhenotypeUploadData> phenotypes = Lists.newArrayList();
        if (inv.getStudies().size() > 1) {
            throw new RuntimeException(String.format("Only Investigations with one Study are allowed. This investigation contains %s studies", inv.getStudies().size()));
        }
        Study study = Iterables.getFirst(inv.getStudies().values(), null);
        if (study.getAssays().size() > 1) {
            throw new RuntimeException(String.format("Only Investigations with one Assay are allowed. This investigation contains %s assays", study.getAssays().size()));
        }
        Map<String, String> passportLookupMap = getPassportLookupMap(study.getStudySample());
        Assay assay = Iterables.getFirst(study.getAssays().values(), null);
        Map<String, String[]> assayLookupMap = getAssayLookupMap(assay);
        String traitDefFilename = null;
        String derivedFilename = null;
        Map<String, PhenotypeUploadData> phenotypeUploadDataMap = null;
        IsaTabDerivedData isaTabData = null;
        // check if there is only one trait and dervived data file
        for (Map.Entry<String, String[]> entry : assayLookupMap.entrySet()) {
            String[] assayRow = entry.getValue();
            if (traitDefFilename == null) {
                traitDefFilename = assayRow[1];
                phenotypeUploadDataMap = parseTraitDefFile(parentDir + File.separator + traitDefFilename);
            } else {
                if (!traitDefFilename.equals(assayRow[1]))
                    throw new RuntimeException("Only 1 Trait Definition File is allowed");
            }
            if (derivedFilename == null) {
                derivedFilename = assayRow[2];
                isaTabData = parseDerivedDataFile(parentDir + File.separator + derivedFilename);
            } else {
                if (!derivedFilename.equals(assayRow[2]))
                    throw new RuntimeException("Only 1 Derived Data File is allowed");
            }
        }
        Preconditions.checkNotNull(isaTabData);
        List<SampleData> samples = Lists.newArrayList();
        for (int i = 0; i < isaTabData.getData().size(); i++) {
            List<String> values = isaTabData.getData().get(i);
            SampleData sample = new SampleData(passportLookupMap.get(assayLookupMap.get(isaTabData.getAssays().get(i))[0]));
            helperService.parseAndUpdateAccession(sample);
            samples.add(sample);
            for (int j = 0; j < values.size(); j++) {
                PhenotypeUploadData phenotype = phenotypeUploadDataMap.get(isaTabData.getPhenotypes().get(j));
                String value = values.get(j);
                boolean parseError = checkPhentoypeParseError(value);
                if (value != null && !value.isEmpty()) {
                    phenotype.incValueCount();
                }
                if (parseError || (sample.hasIdError() && value != null && !value.isEmpty())) {
                    phenotype.addParseError(j);
                }
                sample.addValue(value, parseError);
            }
        }
        experimentUploadData.setPhenotypes(Lists.newArrayList(phenotypeUploadDataMap.values()));
        experimentUploadData.setSampleData(samples);
    }


    private boolean checkPhentoypeParseError(String value) {
        if (value == null)
            return false;
        try {
            Double.parseDouble(value);
            return false;
        } catch (NumberFormatException e) {

        }
        return true;
    }

    private Map<String, PhenotypeUploadData> parseTraitDefFile(String filename) {

        //LinkedHashMap required because we rely on map.values() to be in the right order
        Map<String, PhenotypeUploadData> traitDefLookupMap = Maps.newLinkedHashMap();
        try (FileReader fin = new FileReader(filename);
             ICsvListReader reader = new CsvListReader(fin, CsvPreference.STANDARD_PREFERENCE)) {
            final String[] header = reader.getHeader(true);
            PhenotypeUploadData phenotypeUploadData;
            List<String> traitDefList;
            while ((traitDefList = reader.read()) != null) {
                phenotypeUploadData = new PhenotypeUploadData();
                String measurementName = traitDefList.get(0);
                phenotypeUploadData.setName(measurementName);

                // get ontology
                String traitOntology = String.format("%s", traitDefList.get(2));
                phenotypeUploadData.setTraitOntology(traitOntology);
                phenotypeUploadData.setProtocol(traitDefList.get(3));
                phenotypeUploadData.setUnitOfMeasure(traitDefList.get(6));
                traitDefLookupMap.put(measurementName, phenotypeUploadData);
            }
        } catch (IOException e) {
            logger.error("Error reading trait definition file", e);
        }
        return traitDefLookupMap;
    }


    private IsaTabDerivedData parseDerivedDataFile(String filename) {

        IsaTabDerivedData isaTabDerivedData = null;
        try (FileReader fin = new FileReader(filename);
             ICsvListReader reader = new CsvListReader(fin, CsvPreference.STANDARD_PREFERENCE)) {
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
                List<String> dataPercRow = Lists.newArrayList();
                for (int i = 1; i < header.length; i++) {
                    dataPercRow.add(traitDefList.get(i));
                }
                data.add(dataPercRow);
            }
            isaTabDerivedData = new IsaTabDerivedData(phenotypes, assays, data);
        } catch (IOException e) {
            logger.error("Error reading dervived data file", e);
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


    @Override
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
        final Assay studySample = new Assay(STUDY_FILE,
                ConfigurationManager.selectTROForUserSelection(MappingObject.STUDY_SAMPLE));
        //String[] header = studySample.getTableReferenceObject().getHeaders().toArray(STUDY_HEADER);
        for (ObsUnit obsUnit : experiment.getObsUnits()) {
            Passport passport = obsUnit.getStock().getPassport();
            String taxonomy = passport.getTaxonomy().getGenus() + " " + passport.getTaxonomy().getSpecies();
            // FIXME required to add passport id because otherwise OntologyManager outputs the wrong id
            String accename = passport.getAccename() + "_" + passport.getId();
            // TODO don't harcode it
            OntologyManager.addToOntologyTerms(taxonomy, new OntologyTerm(taxonomy, "3702", "http://purl.obolibrary.org/obo/NCBITaxon_3702", OntologyManager.getOntologySourceReferenceObjectByAbbreviation("NCBITaxon")));
            OntologyManager.addToOntologyTerms(accename, new OntologyTerm(accename, passport.getId().toString(), String.format("https://gwas.gmi.oeaw.ac.at/#/passport/%s/overview", passport.getId()), OntologyManager.getOntologySourceReferenceObjectByAbbreviation("ARA")));
            studySample.getTableReferenceObject()
                    .addRowData(
                            STUDY_HEADER,
                            new String[]{
                                    "source" + obsUnit.getId(),
                                    taxonomy,
                                    accename,
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "sample" + obsUnit.getId()
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

        /*final String[] assayHeaderArray = assayHeaderVector
                .toArray(ASSAY_HEADER);*/
        for (ObsUnit obsUnit : experiment.getObsUnits()) {
            assay.getTableReferenceObject().addRowData(
                    ASSAY_HEADER,
                    new String[]{"sample" + obsUnit.getId(),
                            "",
                            "assay" + obsUnit.getId(),
                            "",
                            "Data transformation",
                            TRAIT_DEF_FILE,
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
            row[1] = row[0];
            if (traitUom.getToAccession() != null && !traitUom.getToAccession().isEmpty()) {
                row[2] = "TO";
                row[3] = traitUom.getToAccession();
            }
            row[4] = traitUom.getTraitProtocol();
            if (traitUom.getUnitOfMeasure() != null) {
                row[5] = traitUom.getUnitOfMeasure().getUnitType();
                //TODO retrieve Unit of measure
            }
            traitDefinitions.add(row);
        }
        return traitDefinitions;
    }

    private List<String[]> createDerivedValues(Experiment experiment, List<TraitUom> traitUoms) {
        Map<TraitUom, Long> statisticMap = getStatisticTypeFromTrait(traitUoms);
        List<String[]> dervivedValues = Lists.newArrayList();
        String[] header = new String[traitUoms.size() + 1];
        header[0] = "Assay Name";
        for (int i = 1; i <= traitUoms.size(); i++) {
            header[i] = String.format("%s", traitUoms.get((i - 1)).getLocalTraitName());
        }
        dervivedValues.add(header);
        for (ObsUnit obsUnit : experiment.getObsUnits()) {
            Passport passport = obsUnit.getStock().getPassport();
            String[] row = new String[header.length];
            row[0] = "assay" + obsUnit.getId().toString();
            for (int i = 1; i < header.length; i++) {
                TraitUom traitUom = traitUoms.get(i - 1);
                row[i] = getTraitValue(obsUnit.getTraits(), traitUom.getId(), statisticMap.get(traitUom));
            }
            dervivedValues.add(row);
        }
        return dervivedValues;
    }


    private Map<TraitUom, Long> getStatisticTypeFromTrait(List<TraitUom> traitUoms) {
        final Function<Trait, Long> toStatisticId = new Function<Trait, Long>() {
            @Nullable
            @Override
            public Long apply(Trait input) {
                Preconditions.checkNotNull(input);
                return input.getStatisticType().getId();
            }
        };
        return ImmutableMap.copyOf(FluentIterable.from(traitUoms).toMap(new Function<TraitUom, Long>() {
            @Nullable
            @Override
            public Long apply(TraitUom input) {
                Preconditions.checkNotNull(input);
                return Ordering.natural().onResultOf(toStatisticId).min(input.getTraits()).getStatisticType().getId();
            }
        }));
    }


    private String getTraitValue(Set<Trait> traits, Long traitUomId, Long statisticTypeId) {
        for (Trait trait : traits) {
            //TODO deal with different statistical types
            if (trait.getTraitUom().getId() == traitUomId && trait.getStatisticType().getId() == statisticTypeId) {
                return trait.getValue();
            }
        }
        return null;
    }

}
