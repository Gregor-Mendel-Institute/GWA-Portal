package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gwtplatform.mvp.client.UiHandlers;

public interface StudyListUiHandlers extends UiHandlers {

    public void onNewStudy();

    void selectFilter(ConstEnums.TABLE_FILTER filter);

    void updateSearchString(String value);
}
