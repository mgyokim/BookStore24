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
    private final List<String> exemptedUrls = new ArrayList<>(); // IpFilter 적용을 제외 할 URI 추가

    public IpFilter() {
        // 허용할 IP 주소를 추가(배포시 해당 부분 수정해야함)
        allowedIpAddresses.add("127.0.0.1");        // IPv4
        allowedIpAddresses.add("0:0:0:0:0:0:0:1");  // IPv6
        allowedIpAddresses.add("172.30.1.254");     // B
        allowedIpAddresses.add("61.79.215.100");    // K
        allowedIpAddresses.add("39.123.221.236");   // L
        allowedIpAddresses.add("52.79.234.227");    // aws
        allowedIpAddresses.add("13.125.144.99");    // aws Elastic IP addresses
        allowedIpAddresses.add("185.199.111.153");  // React
        allowedIpAddresses.add("182.214.27.29");    // TwosomePlace
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String clientIpAddress = getClientIpAddress(httpServletRequest);

        String requestUri = httpServletRequest.getRequestURI(); // 요청 URI 가져오기

        // 예외 URL 패턴 리스트를 순회하면서 현재 요청 URI가 예외 패턴에 맞는지 확인
        boolean isExempted = false;
        for (String exemptedUrl : exemptedUrls) {
            if (requestUri.matches(exemptedUrl)) {
                isExempted = true;
                break;
            }
        }

        // 로그 메시지 출력
        System.out.println("Request from IP: " + clientIpAddress);
        System.out.println("Request URI: " + requestUri);
        System.out.println("Is Exempted: " + isExempted);

        if (!isExempted && !isIpAddressAllowed(clientIpAddress)) {
            System.out.println("Access Denied: IP not allowed");
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
