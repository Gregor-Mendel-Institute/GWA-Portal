package com.gmi.nordborglab.browser.client.mvp.view.main;

import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.jukito.All;
import org.junit.Test;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter.MENU;
import com.gmi.nordborglab.browser.client.mvp.view.main.MainPageView;
import com.gmi.nordborglab.browser.client.mvp.view.main.MainPageView.Binder;
import com.gmi.nordborglab.browser.client.testutils.ViewTestBase;
import com.gmi.nordborglab.browser.client.testutils.ViewTestModule;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.tester.MockFactory;
import com.gwtplatform.tester.MockingBinder;

public class MainPageViewTest extends ViewTestBase{
	
	public static class Module extends ViewTestModule {
		
		static class MyTestBinder extends MockingBinder<Widget, MainPageView> implements Binder {
			@Inject
			public MyTestBinder(final MockFactory mockitoMockFactory) {
				super(Widget.class, mockitoMockFactory);
			}
	    }

		@Override
		protected void configureViewTest() {
			bind(Binder.class).to(MyTestBinder.class);
			bindManyInstances(MENU.class, MENU.HOME,MENU.DIVERSITY,MENU.GERMPLASM,MENU.GENOTYPE);
		}
	}
	
	@Inject 
	MainPageView view;

	
	
	@Test
	public void testSetActiveNavigationItem(@All MENU activeItem) {
		String active_style_name = "ACTIVE_STYLE_NAME";
		given(view.style.current_page_item()).willReturn(active_style_name);
		given(view.homeLink.getTargetHistoryToken()).willReturn(NameTokens.home);
		given(view.diversityLink.getTargetHistoryToken()).willReturn(NameTokens.experiments);
		view.setActiveNavigationItem(activeItem);
		verify(view.homeLink).removeStyleName(active_style_name);
		verify(view.diversityLink).removeStyleName(active_style_name);
		
		if (activeItem.equals(NameTokens.home))
			verify(view.homeLink).addStyleName(active_style_name);
		else if (activeItem.equals(NameTokens.experiments))
			verify(view.diversityLink).addStyleName(active_style_name);
		else
			fail(activeItem+ " unknown");
	}
	
	
	
}
