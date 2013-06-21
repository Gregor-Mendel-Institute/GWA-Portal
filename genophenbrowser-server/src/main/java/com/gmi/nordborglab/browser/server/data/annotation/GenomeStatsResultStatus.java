package com.gmi.nordborglab.browser.server.data.annotation;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 10:22
 * To change this template use File | Settings | File Templates.
 */
public class GenomeStatsResultStatus extends ResultStatus<List<GenomeStat>> {

    public GenomeStatsResultStatus(String status, String statustext, List<GenomeStat> data) {
        super(status, statustext, data);
    }


    public List<GenomeStat> getStats() {
        return data;
    }

}
