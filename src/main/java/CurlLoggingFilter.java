import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;

@Component
public class CurlLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic, if needed.
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Wrap the request so that the body can be read multiple times
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);

        // Build the curl command from the request
        String curlCommand = buildCurlCommand(cachedRequest);

        // Log the curl command to the console
        System.out.println(curlCommand);

        // Continue the filter chain
        chain.doFilter(cachedRequest, response);
    }

    @Override
    public void destroy() {
        // Cleanup logic, if needed.
    }

    private String buildCurlCommand(HttpServletRequest request) throws IOException {
        StringBuilder curl = new StringBuilder("curl -X ");
        curl.append(request.getMethod()).append(" '").append(request.getRequestURL());

        String queryString = request.getQueryString();
        if (queryString != null) {
            curl.append('?').append(queryString);
        }
        curl.append("'");

        // Add headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            curl.append(" -H '").append(headerName).append(": ").append(headerValue).append("'");
        }

        // Add parameters
        Enumeration<String> parameterNames = request.getParameterNames();
        if (parameterNames.hasMoreElements()) {
            curl.append("?");
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = request.getParameter(paramName);
                curl.append(paramName).append("=").append(paramValue);
                if (parameterNames.hasMoreElements()) {
                    curl.append("&");
                }
            }
        }

        // Add body, if it’s a POST, PUT, or PATCH request
        if ("POST".equalsIgnoreCase(request.getMethod()) ||
                "PUT".equalsIgnoreCase(request.getMethod()) ||
                "PATCH".equalsIgnoreCase(request.getMethod())) {
            curl.append(" --data '").append(getRequestBody(request)).append("'");
        }

        return curl.toString();
    }

    // Helper method to read the body of the request
    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }
}
