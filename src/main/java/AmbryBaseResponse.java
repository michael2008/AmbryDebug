import org.apache.http.Header;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base Ambry response.
 */
public class AmbryBaseResponse implements AutoCloseable {
    public static final String DEFAULT_SUCCESS_MSG = "success";
    public static final String DEFAULT_FAILURE_MSG = "failed";

    /**
     * http status code and message
     */
    protected final int code;
    protected final String status;
    protected final InputStream content;
    protected final Map<String, String> headers = new HashMap<>();
    /**
     * ambry status indicator
     */
    protected boolean succeeded;
    protected String message;

    public AmbryBaseResponse(Integer code, String status, InputStream content, List<Header> headers) {
        this.code = code;
        this.status = status;
        this.content = content;
        this.succeeded = (code == HttpStatus.SC_OK);
        this.message = this.succeeded ? DEFAULT_SUCCESS_MSG : DEFAULT_FAILURE_MSG;
        if (!this.succeeded && this.headers.containsKey(Headers.FAILURE_REASON)) {
            this.message = this.headers.get(Headers.FAILURE_REASON);
        }
        headers.forEach(h -> this.headers.put(h.getName(), h.getValue()));
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public InputStream getContent() {
        return content;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Make sure you close the stream.
     * See: https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fundamentals.html#d5e217
     * for the difference of closing the stream and closing the http response.
     */
    @Override
    public void close() {
        try {
            if (content != null) {
                content.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final class Headers {
        // general headers
        /**
         * {@code "Cache-Control"}
         */
        public static final String CACHE_CONTROL = "Cache-Control";
        /**
         * {@code "Content-Length"}
         */
        public static final String CONTENT_LENGTH = "Content-Length";
        /**
         * {@code "Content-Type"}
         */
        public static final String CONTENT_TYPE = "Content-Type";
        /**
         * {@code "Date"}
         */
        public static final String DATE = "Date";
        /**
         * {@code "Expires"}
         */
        public static final String EXPIRES = "Expires";
        /**
         * {@code "Last-Modified"}
         */
        public static final String LAST_MODIFIED = "Last-Modified";
        /**
         * {@code "Location"}
         */
        public static final String LOCATION = "Location";
        /**
         * {@code "Pragma"}
         */
        public static final String PRAGMA = "Pragma";

        // ambry specific headers
        /**
         * mandatory in request; long; size of blob in bytes
         */
        public final static String BLOB_SIZE = "x-ambry-blob-size";
        /**
         * mandatory in request; string; name of service
         */
        public final static String SERVICE_ID = "x-ambry-service-id";
        /**
         * optional in request; date string; default unset ("infinite ttl")
         */
        public final static String TTL = "x-ambry-ttl";
        /**
         * optional in request; 'true' or 'false' case insensitive; default 'false'; indicates private content
         */
        public final static String PRIVATE = "x-ambry-private";
        /**
         * mandatory in request; string; default unset; content type of blob
         */
        public final static String AMBRY_CONTENT_TYPE = "x-ambry-content-type";
        /**
         * optional in request; string; default unset; member id.
         * <p/>
         * Expected usage is to set to member id of content owner.
         */
        public final static String OWNER_ID = "x-ambry-owner-id";
        /**
         * not allowed  in request. Allowed in response only; string; time at which blob was created.
         */
        public final static String CREATION_TIME = "x-ambry-creation-time";
        /**
         * prefix for any header to be set as user metadata for the given blob
         */
        public final static String USER_META_DATA_HEADER_PREFIX = "x-ambry-um-";

        /**
         * Header to contain the Cookies
         */
        public final static String COOKIE = "Cookie";
        /**
         * Header to be set by the clients during a Get blob call to denote, that blob should be served only if the blob
         * has been modified after the value set for this header.
         */
        public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
        /**
         * The possible failure reason of the operation
         */
        public static final String FAILURE_REASON = "x-ambry-failure-reason";
    }
}
