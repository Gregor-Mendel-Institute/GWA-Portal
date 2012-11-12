package com.gmi.nordborglab.browser.client.testutils.proxys;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gmi.nordborglab.browser.shared.proxy.AuthorityProxy;

public class AuthorityProxyHelper {

	public static AuthorityProxy createProxy() {
		AuthorityProxy proxy = mock(AuthorityProxy.class);
		handleAuthority(proxy);
		return proxy;
	}
	
	private static void handleAuthority(AuthorityProxy proxy) {
		PropertyStorage<String> storage = PropertyStorage.create();
		doAnswer(storage.createSetterAnswer()).when(proxy).setAuthority(anyString());
		when(proxy.getAuthority()).thenAnswer(storage.createGetterAnswer());
	}
}
