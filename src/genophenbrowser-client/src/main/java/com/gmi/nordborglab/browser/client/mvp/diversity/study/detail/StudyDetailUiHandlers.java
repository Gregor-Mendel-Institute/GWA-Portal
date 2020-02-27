package com.gmi.nordborglab.browser.client.mvp.diversity.study.detail;

import com.gwtplatform.mvp.client.UiHandlers;

public interface StudyDetailUiHandlers extends UiHandlers {

    public void onEdit();

    public void onSave();

    public void onCancel();

    public void onDelete();

    void onStartAnalysis();

    void onClickUpload();

    void onConfirmDelete();

    void onDeleteJob();

    void onReRunAnalysis();

}
