import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

/**
 * Created by Michael2008 on 12/17/16.
 */
public class AmbryHttpClientUtil {
    /**
     * http connect timeout in ms
     */
    private static final int connectTimeout = 10 * 1000;
    /**
     * get connection timeout in ms
     */
    private static final int requestTimeout = 15 * 1000;
    /**
     * get data timeout in ms
     */
    private static final int socketTimeout = 30 * 1000;
    /**
     * max total connections to all hosts
     */
    private static final int maxTotalConnections = 300;
    /**
     * max default connections to one route
     */
    private static final int maxDefaultPerRouteConnections = 150;
    /**
     * if allow redirect
     */
    private static final boolean allowRedirect = true;
    /**
     * if allow self-signed certificates
     */
    private static final boolean allowSelfSignedSSL = true;
    private final static Object syncLock = new Object();
    /**
     * the httpclient instance for ambry requests
     */
    private static CloseableHttpClient httpClient = null;

    private AmbryHttpClientUtil() {
    }

    /**
     * Get default http client for ambry.
     * NOTE: Do not close the client, and make sure the response is closed or consumed fully.
     *
     * @return the default http client for ambry
     */
    public static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (syncLock) {
                if (httpClient == null) {
                    httpClient = createHttpClient();
                }
            }
        }
        return httpClient;
    }

    private static CloseableHttpClient createHttpClient() {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainsf).register("https", sslsf).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(maxTotalConnections);
        cm.setDefaultMaxPerRoute(maxDefaultPerRouteConnections);

        HttpRequestRetryHandler httpRequestRetryHandler = (exception, executionCount, context) -> {
            if (executionCount >= 3) {// give up on 3 times
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                return true;
            }
            if (exception instanceof SSLHandshakeException) {
                return false;
            }
            if (exception instanceof InterruptedIOException) {
                return false;
            }
            if (exception instanceof UnknownHostException) {
                return false;
            }
            if (exception instanceof SSLException) {
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        };

        // no need for browser headers
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(requestTimeout)
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .setRedirectsEnabled(allowRedirect)
                .build();
        // assemble all together
        HttpClientBuilder builder = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(httpRequestRetryHandler);
        // whether to verify ssl
        if (allowSelfSignedSSL) {
            SSLContextBuilder sslbuilder = new SSLContextBuilder();
            try {
                sslbuilder.loadTrustMaterial(null, (chain, authType) -> true);
                builder.setSSLSocketFactory(new SSLConnectionSocketFactory(sslbuilder.build(),
                        SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));
            } catch (Exception ignored) {
            }
        }
        return builder.build();
    }
}
