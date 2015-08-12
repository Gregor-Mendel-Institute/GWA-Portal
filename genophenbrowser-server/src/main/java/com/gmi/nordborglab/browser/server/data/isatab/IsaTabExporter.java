package com.gmi.nordborglab.browser.server.data.isatab;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import org.isatools.isacreator.model.Investigation;

import java.io.IOException;

/**
 * Created by uemit.seren on 7/8/15.
 */
public interface IsaTabExporter {
    String save(Experiment experiment) throws IOException;

    String save(Experiment experiment, boolean isZip) throws IOException;

    ExperimentUploadData getExperimentUploadDataFromArchive(byte[] data);

    ExperimentUploadData getExperimentUploadDataFromArchive(String parentDir);

    Investigation createInvestigation(Experiment experiment, String baseDirectory);
}
