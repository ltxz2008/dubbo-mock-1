package com.github.dumock.http.controller.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.github.dumock.constants.DuMockUrlConstants;
import com.github.dumock.enums.RespEnum;
import com.github.dumock.exception.DuMockRunTimeException;
import com.github.dumock.result.RequestResult;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by jetty on 18/7/10.
 */
public class ExceptionHandlerFilter implements Filter {

    private static Logger logger= LoggerFactory.getLogger(LoginStatusFilter.class);



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        try{
            filterChain.doFilter(servletRequest,servletResponse);
        }catch (Exception e){
            logger.error("业务逻辑处理出错",e);
            dealException((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, e);
        }
    }

    @Override
    public void destroy() {

    }

    private void dealException(HttpServletRequest servletRequest,HttpServletResponse response,Exception e){
        RequestResult requestResult=new RequestResult();
        if((isJSONInterface((HttpServletRequest)servletRequest))){
            if(e instanceof DuMockRunTimeException){
                DuMockRunTimeException duMockRunTimeException=(DuMockRunTimeException)e;
                requestResult= new RequestResult<Object>(duMockRunTimeException.getCode(),duMockRunTimeException.getMessage());
            }
            if(e.getCause()!=null && e.getCause() instanceof DuMockRunTimeException){
                DuMockRunTimeException duMockRunTimeException=(DuMockRunTimeException)e.getCause();
                requestResult=new RequestResult( duMockRunTimeException.getCode(),duMockRunTimeException.getMessage());
            }else{
                requestResult= RequestResult.fail();
            }
            LoggerHelper.put(requestResult);
            write(response, requestResult);
            return;
        }else{
            try{
                response.sendRedirect(servletRequest.getContextPath()+DuMockUrlConstants.ERROR_500);
                return;
            }catch (Exception e1){
                logger.error("返回信息写失败",e1);
            }
        }
    }

    private boolean isLoginException(Exception e){
        if(e instanceof DuMockRunTimeException){
            if(((DuMockRunTimeException) e).getCode().equals(RespEnum.LOGIN_STATUS_IS_LOSED.getCode())){
                return true;
            }
        }
        return false;
    }

    private String getContextPath(HttpServletRequest servletRequest){
        return servletRequest.getServletPath();
    }


    private Boolean isJSONInterface(HttpServletRequest servletRequest){
        return getContextPath(servletRequest).endsWith(".json");
    }

    private void write(HttpServletResponse response,RequestResult requestResult){
        try{
         //此处纪念一下当年json数据格式不对劲吃过的苦头，这块代码就不删了。
         /*   response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(requestResult));*/
           MappingJackson2HttpMessageConverter converter=new MappingJackson2HttpMessageConverter();
           converter.write(requestResult, requestResult.getClass(),MediaType.APPLICATION_JSON_UTF8,new ServletServerHttpResponse(response));
        }catch(Exception e1){
            logger.error("返回信息写失败",e1);
        }
    }




}

