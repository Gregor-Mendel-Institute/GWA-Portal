package com.gmi.nordborglab.browser.client.testutils.proxys;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class PropertyStorage<T> {
	private T property;

	public static <T> PropertyStorage<T> create() {
		return new PropertyStorage<T>();
	}

	public Answer<Void> createSetterAnswer() {
		return new Answer<Void>() {
			@SuppressWarnings("unchecked")
			public Void answer(InvocationOnMock invocation) throws Throwable {
				property = (T) invocation.getArguments()[0];
				return null;
			}
		};
	}

	public Answer<T> createGetterAnswer() {
		return new Answer<T>() {
			public T answer(InvocationOnMock invocation) throws Throwable {
				return property;
			}
		};
	}
}
