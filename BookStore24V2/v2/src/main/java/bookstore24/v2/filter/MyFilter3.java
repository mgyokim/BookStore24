package bookstore24.v2.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class MyFilter3 implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 토큰 : abc (이걸 만들어줘야함) - id, pw가 정상적으로 들어와서 로그인이 완료되면 토큰을 만들어주고, 그걸 응답을 해준다.
        // 요청할 때마다 header 에 Authorization 에 value 값으로 토큰을 가지고 올 것이다.
        // 그때 토큰이 넘어오면 이 토큰이 내가 만든 토큰이 맞는지만 검증하면 됨 (RSA, HS256 등으로)
        if (req.getMethod().equals("POST")) {
            log.info("필터3 POST 요청됨");
            String headerAuth = req.getHeader("Authorization");
            log.info(headerAuth);

            if (headerAuth.equals("abc")) {
                chain.doFilter(req, res);
            } else {
                PrintWriter printWriter = res.getWriter();
                printWriter.println("Reject Authorization");
            }
        }
    }
}
