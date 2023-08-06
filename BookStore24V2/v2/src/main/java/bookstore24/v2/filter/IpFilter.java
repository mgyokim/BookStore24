package bookstore24.v2.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class IpFilter extends GenericFilterBean {

    private final List<String> allowedIpAddresses = new ArrayList<>();

    public IpFilter() {
        // 허용할 IP 주소를 추가
        allowedIpAddresses.add("0:0:0:0:0:0:0:1");  //
        allowedIpAddresses.add("172.30.1.254");     // B
        allowedIpAddresses.add("61.79.215.100");    // K
        allowedIpAddresses.add("39.123.221.236");   // L
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String clientIpAddress = getClientIpAddress(httpServletRequest);

        if (!isIpAddressAllowed(clientIpAddress)) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }
        chain.doFilter(request, response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWAREDE-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private boolean isIpAddressAllowed(String ipAddress) {
        return allowedIpAddresses.contains(ipAddress);
    }
}
