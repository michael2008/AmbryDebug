import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Michael2008 on 4/12/2017.
 */
public class AmbryHelper {


    /**
     * post file to ambry endpoint
     */
    public static AmbryPostFileResponse postFile(String endpoint, Map<String, String> headers, InputStream content) {
        HttpPost httpPost = new HttpPost(endpoint + "/");
        InputStreamEntity entity = new InputStreamEntity(content);
        httpPost.setEntity(entity);
        setHeaders(httpPost, headers);
        return getResponse(AmbryPostFileResponse.class, httpPost);
    }


    /**
     * Get the ambry http response.
     *
     * @param httpRequest the http request to execute
     * @return the ambry http response
     */
    private static <T extends AmbryBaseResponse> T getResponse(Class<T> c, HttpRequestBase httpRequest) {
        CloseableHttpClient httpClient = AmbryHttpClientUtil.getHttpClient();
        try {
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new RuntimeException("Http request error.");
            }
            return c.getDeclaredConstructor(Integer.class, String.class, InputStream.class, List.class)
                    .newInstance(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(),
                            entity.getContent(), Arrays.asList(response.getAllHeaders()));
        } catch (Exception e) {
            throw new RuntimeException("get ambry response failed.", e);
        }
    }

    private static void setHeaders(HttpMessage httpMessage, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpMessage.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }
}
