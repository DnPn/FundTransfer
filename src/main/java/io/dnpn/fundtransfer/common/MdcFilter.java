package io.dnpn.fundtransfer.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter leveraging {@link MDC} so we can easily regroup the logs from a same request even if several requests are
 * executed concurrently. If the header `Request-Id` is set then we reuse this value, otherwise we set a new one. The
 * request ID can be retrieved in the response header `Request-Id`.
 */
@Slf4j
@Component
public class MdcFilter extends HttpFilter {

    public static final String REQUEST_ID_MDC_FIELD = "requestId";
    public static final String REQUEST_ID_HEADER = "Request-Id";

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String requestId = getOrCreateRequestId(request);
        MDC.put(REQUEST_ID_MDC_FIELD, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        log.debug("Incoming request {} {} from the IP {} with requestID set to {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                requestId);

        filterChain.doFilter(request, response);
    }

    private String getOrCreateRequestId(HttpServletRequest request) {
        var requestId = request.getHeader(REQUEST_ID_HEADER);

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }
}
