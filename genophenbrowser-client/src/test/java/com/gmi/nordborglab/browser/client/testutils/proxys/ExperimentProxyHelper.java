package com.gmi.nordborglab.browser.client.testutils.proxys;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;

public class ExperimentProxyHelper {

	public static ExperimentProxy createProxy() {
		ExperimentProxy proxy = mock(ExperimentProxy.class);
		handleName(proxy);
		handleOriginator(proxy);
		handleDesign(proxy);
		handleComments(proxy);
		return proxy;
	}

	private static void handleName(ExperimentProxy proxy) {
		PropertyStorage<String> storage = PropertyStorage.create();
		doAnswer(storage.createSetterAnswer()).when(proxy).setName(anyString());
		when(proxy.getName()).thenAnswer(storage.createGetterAnswer());
	}

	private static void handleOriginator(ExperimentProxy proxy) {
		PropertyStorage<Double> storage = PropertyStorage.create();
		doAnswer(storage.createSetterAnswer()).when(proxy).setOriginator(
				anyString());
		when(proxy.getOriginator()).thenAnswer(storage.createGetterAnswer());
	}
	
	private static void handleDesign(ExperimentProxy proxy) {
		PropertyStorage<Double> storage = PropertyStorage.create();
		doAnswer(storage.createSetterAnswer()).when(proxy).setDesign(
				anyString());
		when(proxy.getDesign()).thenAnswer(storage.createGetterAnswer());
	}
	
	private static void handleComments(ExperimentProxy proxy) {
		PropertyStorage<Double> storage = PropertyStorage.create();
		doAnswer(storage.createSetterAnswer()).when(proxy).setComments(
				anyString());
		when(proxy.getComments()).thenAnswer(storage.createGetterAnswer());
	}
}
