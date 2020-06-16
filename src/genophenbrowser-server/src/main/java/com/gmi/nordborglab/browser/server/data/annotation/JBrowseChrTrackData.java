package com.gmi.nordborglab.browser.server.data.annotation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmi.nordborglab.browser.server.data.annotation.nclist.FeatureClasses;
import com.gmi.nordborglab.browser.server.data.annotation.nclist.HistogramMeta;
import com.gmi.nordborglab.browser.server.data.annotation.nclist.NCListItem;
import com.gmi.nordborglab.browser.server.data.annotation.nclist.TrackData;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class JBrowseChrTrackData {

    private static ObjectMapper om = new ObjectMapper();

    private List<HistogramMeta> histogramMetaList;
    private List<NCListItem> featureNcList;
    private List<FeatureClasses> featureClasses;

    private String lazyfeatureUrlTemplate;
    private String baseFolder;
    private Integer[] histogramData;

    public JBrowseChrTrackData(TrackData trackData, String baseFolder) {
        this.featureNcList = trackData.getFeatureNCList();
        this.lazyfeatureUrlTemplate = trackData.getLazyfeatureUrlTemplate().replace("{Chunk}", "%s");
        this.histogramMetaList = trackData.getHistogramMeta();
        this.baseFolder = baseFolder;
        this.featureClasses = trackData.getFeatureClasses();
    }

    public List<Gene> getGenes(Long start, Long end, boolean isFeatures) throws IOException {
        List<Gene> genes = Lists.newArrayList();
        getGenesFromNCList(featureNcList, start, end, isFeatures, genes);
        return genes;
    }

    private List<Gene> getGenesFromNCList(List<NCListItem> ncList, Long start, Long end, boolean isFeatures, List<Gene> genes) throws IOException {
        int listLength = ncList.size();
        int index = binarySearch(ncList, start);
        while ((index < listLength) && (index >= 0) && ncList.get(index).getStart() < end) {
            if (ncList.get(index).getChunk() != null) {
                List<NCListItem> lazyFeatures = loadLazyFeatures(ncList.get(index).getChunk());
                genes = getGenesFromNCList(lazyFeatures, start, end, isFeatures, genes);
            } else {
                genes.add(getGeneFromNCListItem(ncList.get(index), isFeatures));
            }
            if (ncList.get(index).getSubNCList() != null) {
                genes = getGenesFromNCList(ncList.get(index).getSubNCList(), start, end, isFeatures, genes);
            }
            index = index + 1;
        }
        return genes;
    }

    private Gene getGeneFromNCListItem(NCListItem item, boolean isFeature) {
        Gene gene = item.getGene(isFeature);
        return gene;
    }

    private List<NCListItem> loadLazyFeatures(Long chunk) throws IOException {
        List<NCListItem> list = new ArrayList<>();
        JsonNode root = om.readTree(new File(getLazyFeatureFileName(chunk)));
        return getNCListItems(root);
    }

    private List<NCListItem> getNCListItems(JsonNode node) {
        List<NCListItem> ncListItems = new ArrayList<>();
        for (JsonNode ncListItem : node) {
            NCListItem item = new NCListItem();
            FeatureClasses cls = featureClasses.get(ncListItem.get(0).asInt());
            item.setStart(ncListItem.get(cls.getStartIdx()).asLong());
            item.setEnd(ncListItem.get(cls.getEndIdx()).asLong());
            if (cls.getStrandIdx() != null) {
                item.setStrand(ncListItem.get(cls.getStrandIdx()).asInt());
            }
            if (cls.getNameIdx() != null) {
                item.setName(ncListItem.get(cls.getNameIdx()).asText());
            }
            if (cls.getSubfeatureIdx() != null) {
                item.setGeneFeatures(getGeneFeatures(ncListItem.get(cls.getSubfeatureIdx())));
            }
            // check if has subnclistitems
            int lastIdx = ncListItem.size() - 1;
            if (ncListItem.get(lastIdx).isObject()) {
                item.setSubNCList(getNCListItems(ncListItem.get(lastIdx).get("Sublist")));
            }

            ncListItems.add(item);
        }
        return ncListItems;
    }

    private List<GeneFeature> getGeneFeatures(JsonNode features) {
        List<GeneFeature> geneFeatures = new ArrayList<>();
        for (JsonNode featureJson : features) {
            FeatureClasses cls = featureClasses.get(featureJson.get(0).asInt());
            try {
                long start = featureJson.get(cls.getStartIdx()).asLong();
                long end = featureJson.get(cls.getEndIdx()).asLong();
                int strand = featureJson.get(cls.getStrandIdx()).asInt();
                String name = featureJson.get(cls.getTypeIdx()).asText();
                geneFeatures.add(new GeneFeature(start, end, strand, name));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return geneFeatures;
    }

    private String getLazyFeatureFileName(Long chunk) {
        return baseFolder + "/" + String.format(lazyfeatureUrlTemplate, chunk.toString());
    }

    private int binarySearch(List<NCListItem> list, Long value) {
        int low = -1;
        int high = list.size();
        while (high - low > 1) {
            int mid = (low + high) >>> 1;
            NCListItem item = list.get(mid);

            Long cmp = item.getEnd();
            if (cmp > value)
                high = mid;
            else if (cmp < value)
                low = mid;
        }
        NCListItem valueToCompare = new NCListItem();
        valueToCompare.setStart(value);
        return high;  // key not found
    }

    public static JBrowseChrTrackData create(File trackFile) throws IOException {
        TrackData trackData;
        JsonNode root = om.readTree(trackFile);
        List<HistogramMeta> histogramMetas = om.readValue(root.at("/histograms/meta").traverse(), new TypeReference<List<HistogramMeta>>() {
        });
        List<FeatureClasses> featureClasses = getFeatureClasses(root.at("/intervals/classes"));
        List<NCListItem> ncListItems = getRootNCListItems(root.at("/intervals/nclist"));
        trackData = new TrackData(ncListItems, root.at("/intervals/urlTemplate").asText(), histogramMetas, featureClasses);
        return new JBrowseChrTrackData(trackData, trackFile.getParent());
    }

    private static List<NCListItem> getRootNCListItems(JsonNode nclists) {
        List<NCListItem> nclistItems = new ArrayList<>();
        for (JsonNode nclist : nclists) {
            NCListItem item = new NCListItem();
            item.setStart(nclist.get(1).asLong());
            item.setEnd(nclist.get(2).asLong());
            item.setChunk(nclist.get(3).asLong());
            nclistItems.add(item);
        }
        return nclistItems;
    }

    private static List<FeatureClasses> getFeatureClasses(JsonNode classes) {
        List<FeatureClasses> featureClasses = new ArrayList<>();
        for (JsonNode cls : classes) {
            JsonNode attributes = cls.get("attributes");
            FeatureClasses.Builder builder = FeatureClasses.builder();
            for (int i = 1; i <= attributes.size(); i++) {
                String fieldName = attributes.get(i - 1).asText();

                switch (fieldName.toLowerCase()) {
                    case "start":
                        builder.setStartIdx(i);
                        break;
                    case "end":
                        builder.setEndIdx(i);
                        break;
                    case "strand":
                        builder.setStrandIdx(i);
                        break;
                    case "id":
                        builder.setNameIdx(i);
                        break;
                    case "type":
                        builder.setTypeIdx(i);
                        break;
                    case "subfeatures":
                        builder.setSubfeatureIdx(i);
                        break;
                }
            }
            featureClasses.add(builder.build());
        }
        return featureClasses;
    }

    public Integer[] getHistogramData() {
        if (histogramData == null) {
            try {
                histogramData = om.readValue(new File(baseFolder + "/" + histogramMetaList.get(0).getArrayParams().getUrlTemplate().replace("{Chunk}", "0")), Integer[].class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return histogramData;
    }

    public HistogramMeta getHistogramMeta() {
        return histogramMetaList.get(0);
    }

}
