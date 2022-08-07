package nr.king.familytracker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.model.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Component
public class HttpUtils {

    private static final Logger logger = LogManager.getLogger(HttpUtils.class);

    @Autowired
    private CommonUtils commonUtils;
    @Autowired
    private ObjectMapper objectMapper;


    public HttpResponse doGetRequest(String url, Map<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder request = new Request.Builder().url(url);
        headers.forEach(request::addHeader);
        return getReponse(url, client, request);
    }

    private HttpResponse getReponse(String url, OkHttpClient client, Request.Builder request) throws IOException {
        Response response = client.newCall(request.build()).execute();
        HttpResponse httpResponse = new HttpResponse(response.code(), response.body() != null ? response.body().string() : "");
        logger.info(String.format("Requested Url is - %s - Response Code is - %s", url, httpResponse.getResponseCode()));
        return httpResponse;
    }

    public Response doRequest(String url, Map<String, String> headers, RequestMethod method, RequestBody body) throws IOException {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(30, TimeUnit.SECONDS);
        client.setConnectTimeout(20, TimeUnit.SECONDS);
        client.setWriteTimeout(30, TimeUnit.SECONDS);
        Request.Builder request = new Request.Builder().url(url);
        headers.forEach(request::addHeader);
        logger.info("\n#Requested Url is - " + url);
        addMethodType(method, request, body);
        return client.newCall(request.build()).execute();
    }

    private void addMethodType(RequestMethod method, Request.Builder request, RequestBody body) {
        switch (method) {
            case GET:
                request.get();
                return;
            case POST:
                request.post(body);
                return;
            case PUT:
                request.put(body);
                return;
        }
    }

    public <T> T processRequest(String url, Map<String, String> headers, RequestMethod method, RequestBody requestBody, Predicate<String> conditionToApply, Class<T> returnType) {
        try {
            Long startTime = System.currentTimeMillis();
            Response response = doRequest(url, headers, method, requestBody);
            String responsBody = response.body().string();
            logger.info("\n#Requested Url is - " + url + " \n#Response Body is - " + responsBody + " \n#Response Code is - " + response.code());
            if (response.code() != 200) {
                throw new FailedResponseException(String.format("Error connection to the server %s ", url));
            }
            if (conditionToApply.test(responsBody)) logger.info("No Records Available");
            return objectMapper.readValue(responsBody, returnType);
        } catch (Exception e) {
            logger.error("exception while getting response " + e.getMessage(), e);
            throw new RuntimeException(" Error while processing due to " + e.getMessage(), e);
        }
    }

    public HttpResponse doPostRequest(int userId, String url, Map<String, String> headers, String task, String body) throws IOException {
        Long startTime = System.currentTimeMillis();
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(30, TimeUnit.SECONDS);
        client.setConnectTimeout(20, TimeUnit.SECONDS);
        client.setWriteTimeout(30, TimeUnit.SECONDS);
        Request.Builder request = new Request.Builder().url(url);
        headers.forEach(request::addHeader);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, body);
        request.post(requestBody);
        HttpResponse httpResponse = GetReponse(url, client, request,body);
        return httpResponse;
    }

    private HttpResponse GetReponse(String url, OkHttpClient client, Request.Builder request, String requestBody) throws IOException {
        Response response = client.newCall(request.build()).execute();
        HttpResponse httpResponse = new HttpResponse(response.code(), response.body().string(), response.headers());
        // logger.info(String.format("\nRequested Url is - %s - Response Code is - %s -Request Body is -%s " , url, httpResponse.getResponseCode(),requestBody));
        return httpResponse;
    }
}
