package io.cucumber.pro.results;

import gherkin.deps.com.google.gson.Gson;
import io.cucumber.pro.Keys;
import io.cucumber.pro.Logger;
import io.cucumber.pro.StackTrace;
import io.cucumber.pro.config.Config;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HTTPResultsPublisher implements ResultsPublisher {
    private static final Gson GSON = new Gson();
    private static final String CONTENT_TYPE_CUCUMBER_JAVA_RESULTS_JSON = "application/x.cucumber.java.results+json";
    private final String url;
    private final String authToken;
    private final Config config;
    private final Logger logger;

    /**
     * @param url    where to send results
     * @param logger where to print errors and warnings
     */
    HTTPResultsPublisher(String url, Config config, Logger logger) {
        this.url = url;
        this.config = config;
        this.authToken = config.getString(Keys.CUCUMBERPRO_TOKEN);
        this.logger = logger;
    }

    @Override
    public void publish(File resultsJsonFile, final Map<String, String> env, String profileName, String revision, String branch, String tag) {
        HttpClient client = buildHttpClient();

        HttpPost post = new HttpPost(URI.create(url));

        try {
            Map<String, String> git = new HashMap<>();
            git.put("revision", revision);
            git.put("branch", branch);
            git.put("tag", tag);

            Map<String, Object> body = new HashMap<>();
            body.put("environment", env);
            body.put("cucumberJson", GSON.fromJson(new InputStreamReader(new FileInputStream(resultsJsonFile), "UTF-8"), List.class));
            body.put("profileName", profileName);
            body.put("git", git);

            String json = GSON.toJson(body);
            post.setEntity(new StringEntity(json, ContentType.create(CONTENT_TYPE_CUCUMBER_JAVA_RESULTS_JSON, "UTF-8")));

            HttpResponse response = client.execute(post);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode >= 200 && statusCode < 400) {
                logger.log(Logger.Level.INFO, "Published results to Jam: " + url);
            } else {
                // Read the HTTP response and throw an error
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                response.getEntity().writeTo(baos);
                String responseBody = new String(baos.toByteArray(), "UTF-8");

                String suggestion = "";
                if (statusCode == 401)
                    suggestion = String.format("You need to define %s", Keys.CUCUMBERPRO_TOKEN);
                if (statusCode == 403)
                    suggestion = String.format("You need to change the value of %s", Keys.CUCUMBERPRO_TOKEN);

                logger.log(Logger.Level.ERROR, "Failed to publish results to : %s\nHTTP Status: %s\n%s\n%s",
                        url,
                        statusLine,
                        responseBody,
                        suggestion);
            }
        } catch (Throwable e) {
            logger.log(Logger.Level.ERROR, "Failed to publish results to %s\n%s", url, StackTrace.get(e));
        }
    }

    private HttpClient buildHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        int timeout = config.getInteger(Keys.CUCUMBERPRO_CONNECTION_TIMEOUT);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        if (authToken != null) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(authToken, "");
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(provider);
        }
        return httpClientBuilder.build();
    }
}
