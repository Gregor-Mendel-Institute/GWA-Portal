package com.gmi.nordborglab.browser.client.testutils.proxys;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.AuthorityProxy;

public class AppUserProxyHelper {

	public static AppUserProxy createProxy() {
		AppUserProxy proxy = mock(AppUserProxy.class);
		handleFirstName(proxy);
		handleLastName(proxy);
		handleEmail(proxy);
		handleAuthorities(proxy);
		return proxy;
	}
	
	private static void handleFirstName(AppUserProxy proxy) {
		PropertyStorage<String> storage = PropertyStorage.create();
		doAnswer(storage.createSetterAnswer()).when(proxy).setFirstname(anyString());
		when(proxy.getFirstname()).thenAnswer(storage.createGetterAnswer());
	}
	
	private static void handleLastName(AppUserProxy proxy) {
		PropertyStorage<String> storage = PropertyStorage.create();
		doAnswer(storage.createSetterAnswer()).when(proxy).setLastname(anyString());
		when(proxy.getLastname()).thenAnswer(storage.createGetterAnswer());
	}
	
	private static void handleEmail(AppUserProxy proxy) {
		PropertyStorage<String> storage = PropertyStorage.create();
		doAnswer(storage.createSetterAnswer()).when(proxy).setEmail(anyString());
		when(proxy.getEmail()).thenAnswer(storage.createGetterAnswer());
	}
	
	private static void handleAuthorities(AppUserProxy proxy) {
		PropertyStorage<String> storage = PropertyStorage.create();
		doAnswer(storage.createSetterAnswer()).when(proxy).setAuthorities(anyListOf(AuthorityProxy.class));
		when(proxy.getAuthorities()).thenAnswer(storage.createGetterAnswer());
	}
}
