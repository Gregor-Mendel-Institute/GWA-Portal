package com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.list;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class CandidateGeneListModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(CandidateGeneListPresenter.class, CandidateGeneListPresenter.MyView.class, CandidateGeneListView.class, CandidateGeneListPresenter.MyProxy.class);
    }
}
