package io.dnpn.fundtransfer.common;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MdcFilterTest {

    private MdcFilter filter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void beforeEach() {
        this.filter = new MdcFilter();
    }

    @SneakyThrows
    @Test
    void WHEN_doFilter_THEN_chainToNextFilter() {
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @SneakyThrows
    @Test
    void GIVEN_requestIdHeaderIsSet_WHEN_doFilter_THEN_useIt() {
        String requestId = "abc";
        doReturn(requestId).when(request).getHeader(MdcFilter.REQUEST_ID_HEADER);

        filter.doFilter(request, response, filterChain);

        verify(response).setHeader(MdcFilter.REQUEST_ID_HEADER, requestId);
        assertEquals(requestId, MDC.get(MdcFilter.REQUEST_ID_MDC_FIELD));
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {""})
    @NullSource
    void GIVEN_noRequestIdHeader_WHEN_doFilter_THEN_createNewRequestId(String headerValue) {
        doReturn(headerValue).when(request).getHeader(MdcFilter.REQUEST_ID_HEADER);

        filter.doFilter(request, response, filterChain);

        String generatedRequestId = MDC.get(MdcFilter.REQUEST_ID_MDC_FIELD);
        assertNotNull(generatedRequestId);
        verify(response).setHeader(MdcFilter.REQUEST_ID_HEADER, generatedRequestId);
    }
}