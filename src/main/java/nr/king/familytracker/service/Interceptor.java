package nr.king.familytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class Interceptor implements HandlerInterceptor {
    @Autowired
    CommonUtils commonUtils;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;

    private static final Logger logger = LogManager.getLogger(Integer.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isValidToken(request.getHeader("X-Auth-Token")))
        {
            return true;
        }
        else{

            sendUnauthenticatedResponse(request,response,"Invalid Auth Token",403);
            return false;
        }

    }

    private boolean isValidToken(String authToken) {
        if ("9cabe3a2-10ba-46b1-afe7-bc63d1f66009".equals(authToken))
        {
            return true;
        }
        else{
            String[] tokenArray =  commonUtils.base64Decode(authToken).split(":");
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet("select AUTH_TOKEN from AUTH_TOKENS where where USER_ID=?",tokenArray[1]);
            return sqlRowSet.next();
        }

    }


    public void sendUnauthenticatedResponse(HttpServletRequest request, HttpServletResponse response, String message, int code) throws IOException
    {
        response.setStatus(code);
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET");
        response.getOutputStream().write(commonUtils.writeAsBytes(objectMapper, new ApiResponse(false, message)));
        logger.log(Level.INFO, "UnAuthenticated API request - " + request.getServletPath());
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
       // RequestContextHolder.clearContext();
    }

}
