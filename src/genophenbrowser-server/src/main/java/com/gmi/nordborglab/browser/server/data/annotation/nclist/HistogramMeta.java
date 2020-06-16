package com.gmi.nordborglab.browser.server.data.annotation.nclist;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 11:05
 * To change this template use File | Settings | File Templates.
 */
public class HistogramMeta {

    public static class ArrayParams {
        Integer length;
        Integer chunkSize;
        String urlTemplate;

        public Integer getLength() {
            return length;
        }

        public Integer getChunkSize() {
            return chunkSize;
        }

        public String getUrlTemplate() {
            return urlTemplate;
        }
    }

    private Integer basesPerBin;
    private ArrayParams arrayParams;

    public Integer getBasesPerBin() {
        return basesPerBin;
    }

    public ArrayParams getArrayParams() {
        return arrayParams;
    }
}
