package com.gmi.nordborglab.browser.client;


import java.util.ArrayList;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;

import com.gmi.nordborglab.browser.client.gin.DefaultPlace;
import com.gmi.nordborglab.browser.client.util.ParallelRunnable;
import com.gmi.nordborglab.browser.client.util.ParentCallback;
import com.gmi.nordborglab.browser.shared.proxy.AppDataProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.MotionChart;
import com.google.gwt.visualization.client.visualizations.OrgChart;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

public class ClientPlaceManager extends PlaceManagerImpl {
	
	private final PlaceRequest defaultPlaceRequest;
	private final CurrentUser currentUser;
	private final CustomRequestFactory rf;
	
	@Inject
	public ClientPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter, 
			@DefaultPlace String defaultNameToken,CurrentUser currentUser, CustomRequestFactory rf) {
		super(eventBus, tokenFormatter);
		this.defaultPlaceRequest = new PlaceRequest(defaultNameToken);
		this.currentUser = currentUser;
		this.rf = rf;
	}

	public void revealDefaultPlace() {
		revealPlace(defaultPlaceRequest);
	}

	@Override
	public void revealCurrentPlace() {
		
		final ParallelRunnable visualizationRunnable = new ParallelRunnable();
		final ParallelRunnable rfRunnalbe = new ParallelRunnable();
		final ParallelRunnable mapsRunnable = new ParallelRunnable();
		Receiver<AppDataProxy> receiver = new Receiver<AppDataProxy>() {

			@Override
			public void onSuccess(AppDataProxy response) {
				currentUser.setAppData(response);
				rfRunnalbe.run();
			}
		};
		
		ParentCallback parentCallback = new ParentCallback(visualizationRunnable,rfRunnalbe,mapsRunnable) {
			
			@Override
			protected void handleSuccess() {
				ClientPlaceManager.super.revealCurrentPlace();
			}
		};
		VisualizationUtils.loadVisualizationApi(visualizationRunnable,CoreChart.PACKAGE, MotionChart.PACKAGE,GeoChart.PACKAGE,OrgChart.PACKAGE);

		// load all the libs for use in the maps
		ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
		loadLibraries.add(LoadLibrary.ADSENSE);
		loadLibraries.add(LoadLibrary.DRAWING);
		loadLibraries.add(LoadLibrary.GEOMETRY);
		loadLibraries.add(LoadLibrary.PANORAMIO);
		loadLibraries.add(LoadLibrary.PLACES);
		loadLibraries.add(LoadLibrary.WEATHER);

		Runnable onLoad = new Runnable() {
			@Override
			public void run() {
				mapsRunnable.run();
			}
		};
		LoadApi.go(onLoad, loadLibraries, false);
		if (currentUser.getAppData() == null) {
			rf.helperRequest().getAppData().fire(receiver);
		}
		else { 
			rfRunnalbe.run();
		}
	}
}
