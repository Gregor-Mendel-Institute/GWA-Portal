package com.gmi.nordborglab.browser.client.testutils;

import com.gwtplatform.tester.MockFactory;
import org.mockito.Mockito;

public class MockitoMockFactory implements MockFactory {

    @Override
    public <T> T mock(Class<T> classToMock) {
        return Mockito.mock(classToMock);
    }

}
