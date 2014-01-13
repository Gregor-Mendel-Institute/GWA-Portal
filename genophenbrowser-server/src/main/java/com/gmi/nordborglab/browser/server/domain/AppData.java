package com.gmi.nordborglab.browser.server.domain;

import com.gmi.nordborglab.browser.server.domain.cdv.StudyProtocol;
import com.gmi.nordborglab.browser.server.domain.cdv.Transformation;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.germplasm.Sampstat;
import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
import com.gmi.nordborglab.browser.server.domain.stats.AppStat;
import com.gmi.nordborglab.browser.server.domain.util.NewsItem;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;

import java.util.List;

public class AppData {

    protected List<UnitOfMeasure> unitOfMeasureList;
    protected List<StatisticType> statisticTypeList;
    protected List<AlleleAssay> alleleAssayList;
    protected List<StudyProtocol> studyProtocolList;
    protected List<Sampstat> sampStatList;
    protected List<Transformation> transformationList;

    protected List<UserNotification> userNotificationList;
    private List<AppStat> stats;
    private List<NewsItem> news;


    public AppData() {
    }

    public List<UnitOfMeasure> getUnitOfMeasureList() {
        return unitOfMeasureList;
    }

    public void setUnitOfMeasureList(List<UnitOfMeasure> unitOfMeasureList) {
        this.unitOfMeasureList = unitOfMeasureList;
    }

    public List<StatisticType> getStatisticTypeList() {
        return statisticTypeList;
    }

    public void setStatisticTypeList(List<StatisticType> statisticTypeList) {
        this.statisticTypeList = statisticTypeList;
    }

    public List<AlleleAssay> getAlleleAssayList() {
        return alleleAssayList;
    }

    public void setAlleleAssayList(List<AlleleAssay> alleleAssayList) {
        this.alleleAssayList = alleleAssayList;
    }

    public List<StudyProtocol> getStudyProtocolList() {
        return studyProtocolList;
    }

    public void setStudyProtocolList(List<StudyProtocol> studyProtocolList) {
        this.studyProtocolList = studyProtocolList;
    }

    public List<Sampstat> getSampStatList() {
        return sampStatList;
    }

    public void setSampStatList(List<Sampstat> sampStatList) {
        this.sampStatList = sampStatList;
    }

    public List<Transformation> getTransformationList() {
        return transformationList;
    }

    public void setTransformationList(List<Transformation> transformationList) {
        this.transformationList = transformationList;
    }

    public List<UserNotification> getUserNotificationList() {
        return userNotificationList;
    }

    public void setUserNotificationList(List<UserNotification> userNotificationList) {
        this.userNotificationList = userNotificationList;
    }

    public void setStats(List<AppStat> stats) {
        this.stats = stats;
    }

    public List<AppStat> getStats() {
        return stats;
    }

    public void setNews(List<NewsItem> news) {
        this.news = news;
    }

    public List<NewsItem> getNews() {
        return news;
    }
}
