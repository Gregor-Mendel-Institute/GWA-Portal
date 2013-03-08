package com.gmi.nordborglab.browser.server.data.annotation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmi.nordborglab.browser.server.data.annotation.nclist.NCListItem;
import com.gmi.nordborglab.browser.server.data.annotation.nclist.TrackData;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class JBrowseChrTrackData {

    private static ObjectMapper om = new ObjectMapper();

    //private List<HistogramMeta> histogramMetaList;
    private List<NCListItem> featureNcList;
    private String lazyfeatureUrlTemplate;
    private String baseFolder;

    public JBrowseChrTrackData(TrackData trackData,String baseFolder) {
        this.featureNcList = trackData.getFeatureNCList();
        this.lazyfeatureUrlTemplate = trackData.getLazyfeatureUrlTemplate().replace("{chunk}","%s");
        this.baseFolder = baseFolder;
    }

    public List<Gene> getGenes(Long start, Long end,boolean isFeatures) throws IOException{
        List<Gene> genes = Lists.newArrayList();
        getGenesFromNCList(featureNcList,start,end,isFeatures,genes);
        return genes;
    }

    private List<Gene> getGenesFromNCList(List<NCListItem> ncList,Long start, Long end,boolean isFeatures,List<Gene> genes) throws IOException{
        int listLength = ncList.size();
        int index = binarySearch(ncList,start);
        while ((index < listLength) && (index >= 0) && ncList.get(index).getStart() < end) {
            if (ncList.get(index).getChunk() != null)  {
                List<NCListItem> lazyFeatures= loadLazyFeatures(ncList.get(index).getChunk());
                genes = getGenesFromNCList(lazyFeatures,start,end,isFeatures,genes);
            }
            else {
                genes.add(getGeneFromNCListItem(ncList.get(index), isFeatures));
            }
            if (ncList.get(index).getSubNCList() != null) {
                genes = getGenesFromNCList(ncList.get(index).getSubNCList(), start, end, isFeatures, genes);
            }
            index = index +1 ;
        }
        return genes;
    }

    private Gene getGeneFromNCListItem(NCListItem item,boolean isFeature) {
        Gene gene =  item.getGene(isFeature);
        return gene;
    }

    private List<NCListItem> loadLazyFeatures(Long chunk) throws IOException {
        List<NCListItem> list = om.readValue(new File(getLazyFeatureFileName(chunk)), new TypeReference<List<NCListItem>>() {});
        return list;
    }

    private String getLazyFeatureFileName(Long chunk) {
        return baseFolder + "/"+ String.format(lazyfeatureUrlTemplate,chunk.toString());
    }

    private int binarySearch(List<NCListItem> list,Long value) {
        int low = -1;
        int high = list.size();
        while (high  - low > 1) {
            int mid = (low + high) >>> 1;
            NCListItem item = list.get(mid);

            Long cmp = item.getEnd();
            if (cmp > value)
                high=mid;
            else if (cmp < value)
                low = mid;
        }
        NCListItem valueToCompare = new NCListItem();
        valueToCompare.setStart(value);
        return high;  // key not found
    }

    public static JBrowseChrTrackData create(File trackFile) throws IOException {
        return new JBrowseChrTrackData(om.readValue(trackFile,TrackData.class),trackFile.getParent());
    }

}
