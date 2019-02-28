package org.iblog.enhance.gateway.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.iblog.enhance.gateway.config.HttpClientConfiguration;
import org.iblog.enhance.gateway.core.Result;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.google.common.base.Strings;

/**
 * @author shaoxiao.xu
 * @date 2019/2/15 11:41
 */
@Component
public class HttpClientProvider {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientProvider.class);

    @Autowired
    private HttpClientConfiguration configuration;

    public Result request(HttpMethod method, String url, HttpHeaders httpHeaders, String requestBody) {
        try {
            switch (method) {
                case GET:
                    return get(url, httpHeaders);
                case POST:
                    return post(url, httpHeaders, requestBody);
                case PATCH:
                    return patch(url, httpHeaders, requestBody);
                case DELETE:
                    return delete(url, httpHeaders);
                case PUT:
                    return put(url, httpHeaders, requestBody);
                default:
                    return Result.failure("unknown http method type");
            }
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    private Result resultFrom(HttpResponse response) {
        if (response == null) {
            // exception
            return Result.failure("http request failed");
        }
        int status = response.getStatusLine().getStatusCode();
        String entity;
        try {
            entity = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            entity = null;
        }
        if (status >= 200 && status < 300) {
            return Result.success(entity);
        } else if (status >= 400 && status < 500) {
            return Result.failure("http request failed", String.valueOf(status));
        } else {
            return Result.failure("http request failed", String.valueOf(status));
        }
    }

    private Result post(String url, HttpHeaders httpHeaders, String body) throws Exception {
        HttpPost post = new HttpPost(url);
        if (httpHeaders != null) {
            post.setHeaders(headers(httpHeaders));
        }
        try {
            if (!Strings.isNullOrEmpty(body)) {
                StringEntity entity = new StringEntity(body);
                post.setEntity(entity);
            }
            CloseableHttpResponse response = configuration.getHttpClient().execute(post);
            return resultFrom(response);
        } catch (UnsupportedEncodingException uee) {
            logger.error("HttpClientProvider execute post: request body encoding", uee);
            throw uee;
        } catch (IOException ie) {
            logger.error("HttpClientProvider execute post: request", ie);
            throw ie;
        } finally {
            post.releaseConnection();
        }
    }

    private Result get(String url, HttpHeaders httpHeaders) throws IOException {
        HttpGet get = new HttpGet(url);
        if (httpHeaders != null) {
            get.setHeaders(headers(httpHeaders));
        }
        try {
            CloseableHttpResponse response = configuration.getHttpClient().execute(get);
            return resultFrom(response);
        } catch (IOException e) {
            logger.error("HttpClientProvider execute get: request", e);
            throw e;
        } finally {
            get.releaseConnection();
        }
    }

    private Result patch(String url, HttpHeaders httpHeaders, String body) throws Exception {
        HttpPatch patch = new HttpPatch(url);
        if (httpHeaders != null) {
            patch.setHeaders(headers(httpHeaders));
        }
        try {
            StringEntity entity = new StringEntity(body);
            patch.setEntity(entity);
            CloseableHttpResponse response = configuration.getHttpClient().execute(patch);
            return resultFrom(response);
        } catch (UnsupportedEncodingException uee) {
            logger.error("HttpClientProvider execute patch: request body encoding");
            throw uee;
        } catch (IOException ie) {
            logger.error("HttpClientProvider execute patch: request", ie);
            throw ie;
        } finally {
            patch.releaseConnection();
        }
    }

    private Result delete(String url, HttpHeaders httpHeaders) throws Exception {
        HttpDelete delete = new HttpDelete(url);
        if (httpHeaders != null) {
            delete.setHeaders(headers(httpHeaders));
        }
        try {
            CloseableHttpResponse response = configuration.getHttpClient().execute(delete);
            return resultFrom(response);
        } catch (IOException ie) {
            logger.error("HttpClientProvider execute patch: request");
            throw ie;
        } finally {
            delete.releaseConnection();
        }
    }

    private Result put(String url, HttpHeaders httpHeaders, String body) throws Exception {
        HttpPut put = new HttpPut(url);
        if (httpHeaders != null) {
            put.setHeaders(headers(httpHeaders));
        }
        try {
            StringEntity entity = new StringEntity(body);
            put.setEntity(entity);
            CloseableHttpResponse response = configuration.getHttpClient().execute(put);
            return resultFrom(response);
        } catch (UnsupportedEncodingException uee) {
            logger.error("HttpClientProvider execute put: request body encoding", uee);
            throw  uee;
        } catch (IOException ie) {
            logger.error("HttpClientProvider execute put: request", ie);
            throw ie;
        } finally {
            put.releaseConnection();
        }
    }

    private Header[] headers(HttpHeaders httpHeaders) {
        if (httpHeaders == null) {
            return null;
        }
        Set<String> names = httpHeaders.keySet().stream()
                .filter(name -> !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)
                        && !MarkRequestFilter.X_Task_Async.equalsIgnoreCase(name))
                .collect(Collectors.toSet());
        Header[] headers = new Header[names.size()];
        int vernier = 0;
        Iterator<String> iterator = names.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)
                    || MarkRequestFilter.X_Task_Async.equalsIgnoreCase(name)) {
                continue;
            }
            headers[vernier] = new BasicHeader(name, httpHeaders.getFirst(name));
            vernier ++;
        }
        return headers;
    }
}
