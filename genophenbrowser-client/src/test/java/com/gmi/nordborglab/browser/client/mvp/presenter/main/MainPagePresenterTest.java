package com.gmi.nordborglab.browser.client.mvp.presenter.main;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter.MENU;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestBase;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestModule;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class MainPagePresenterTest extends PresenterTestBase {
	
	public static class Module extends PresenterTestModule {

		@Override
		protected void configurePresenterTest() {
			AppUserFactory factory = AutoBeanFactorySource.create(AppUserFactory.class);
			bind(AppUserFactory.class).toInstance(factory);
		}
	}
	
	@Inject
	MainPagePresenter presenter;
	
	@Inject
	MainPagePresenter.MyView view;
	
	
	
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void showActiveNavigationItemWhenPlaceRequest() {
		PlaceRequest request = new PlaceRequest("home");
	    placeManager.revealPlace(request);
	    when(placeManager.getCurrentPlaceRequest()).thenReturn(request);
	    presenter.onReset();
	    verify(view).setActiveNavigationItem(MENU.HOME);
	}
	
	@Test
	public void retrieveUserInfoOnBind() {
		
		when(view.getUserData()).thenReturn("{\"authorities\":[{\"authority\":\"ROLE_USER\"}],\"email\":\"john.doe@test.com\",\"lastname\":\"Doe\",\"firstname\":\"John\"}");
		presenter.onBind();
		verify(view).getUserData();
		AppUserProxy appUser = presenter.currentUser.getAppUser();
		assertEquals(appUser.getEmail(),"john.doe@test.com");
		assertEquals(appUser.getLastname(),"Doe");
		assertEquals(appUser.getFirstname(),"John");
		assertEquals(appUser.getAuthorities().size(),1);
		assertEquals(appUser.getAuthorities().get(0).getAuthority(),"ROLE_USER");
	}
	
	@Test
	public void testNotificationDisplayEvent() {
		presenter.onBind();
		eventBus.fireEvent(new DisplayNotificationEvent("test", "test", true, DisplayNotificationEvent.LEVEL_MESSAGE, 0));
		verify(view).showNotification("test", "test", DisplayNotificationEvent.LEVEL_MESSAGE,0);
	}
	
}
