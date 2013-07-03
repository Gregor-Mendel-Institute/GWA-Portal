package com.gmi.nordborglab.browser.shared.proxy;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.AppData")
public interface AppDataProxy extends ValueProxy {

    public List<UnitOfMeasureProxy> getUnitOfMeasureList();

    public void setUnitOfMeasureList(List<UnitOfMeasureProxy> unitOfMeasures);

    public List<StatisticTypeProxy> getStatisticTypeList();

    public void setStatisticTypeList(List<StatisticTypeProxy> statisticTypeList);


    public List<StudyProtocolProxy> getStudyProtocolList();

    public void setStudyProtocolList(List<StudyProtocolProxy> studyProtocolList);

    public List<AlleleAssayProxy> getAlleleAssayList();

    public void setAlleleAssayList(List<AlleleAssayProxy> alleleAssayList);

    public List<SampStatProxy> getSampStatList();

    public void setSampStatList(List<SampStatProxy> sampStatList);

    public List<TransformationProxy> getTransformationList();

    public void setTransformationList(List<TransformationProxy> transformationList);

    public List<UserNotificationProxy> getUserNotificationList();

    public List<AppStatProxy> getStats();

    public List<NewsItemProxy> getNews();
}
