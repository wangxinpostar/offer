package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String[] urls =
            {"/employee/login",
                    "/employee/logout",
                    "/backend/**",
                    "/front/**",
                    "/user/sendMsg",
                    "/user/login",
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources",
                    "/v2/api-docs"};

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse =
                (HttpServletResponse) servletResponse;

        String requestURI = httpServletRequest.getRequestURI();

        if (isMatch(requestURI)) {
            log.info("匹配到放行");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (httpServletRequest.getSession().getAttribute("employee") != null) {
            log.info("已登录，放行");
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        if (httpServletRequest.getSession().getAttribute("user") != null) {
            log.info("已登录，放行");
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        log.info("未登录，拦截");
        httpServletResponse.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    public boolean isMatch(String requestURI) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
