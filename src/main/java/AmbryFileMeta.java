import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michael2008 on 12/18/16.
 */
public class AmbryFileMeta {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    //x-ambry-um- (if supplied at upload), Zero or more headers with this prefix that represent user metadata
    private final Map<String, String> umMeta = new HashMap<>();
    // required for upload and fetch response
    private long blobSize;
    private String serviceId;
    private String contentType;
    // required for fetch response
    private Date creationTime;
    private boolean isPrivate = false;
    // optional
    private Long ttl = -1L; // in seconds, default infinite
    private String ownerId = "";

    public AmbryFileMeta(long blobSize, String serviceId, String contentType) {
        this.blobSize = blobSize;
        this.serviceId = serviceId;
        this.contentType = contentType;
    }

    public AmbryFileMeta(long blobSize, String serviceId, String contentType, Date creationTime, boolean isPrivate) {
        this(blobSize, serviceId, contentType);
        this.creationTime = creationTime;
        this.isPrivate = isPrivate;
    }

    public long getBlobSize() {
        return blobSize;
    }

    public String getServiceId() {
        return serviceId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void addUserMeta(String name, String value) {
        if (name == null || value == null || !name.startsWith(AmbryBaseResponse.Headers.USER_META_DATA_HEADER_PREFIX)) {
            throw new RuntimeException(String.format("bad user meta: [%s:%s]", name, value));
        }
        this.umMeta.put(name, value);
    }

    public Map<String, String> getUmMeta() {
        return umMeta;
    }

    public Map<String, String> toHeaders() {
        // generate headers
        Map<String, String> headers = new HashMap<>();
        // required
        headers.put(AmbryBaseResponse.Headers.BLOB_SIZE, String.valueOf(this.blobSize));
        headers.put(AmbryBaseResponse.Headers.SERVICE_ID, this.serviceId);
        headers.put(AmbryBaseResponse.Headers.AMBRY_CONTENT_TYPE, StringUtils.isBlank(this.contentType) ? DEFAULT_CONTENT_TYPE : this.contentType);
        // optional
        headers.put(AmbryBaseResponse.Headers.TTL, String.valueOf(this.ttl));
        headers.put(AmbryBaseResponse.Headers.PRIVATE, String.valueOf(this.isPrivate));
        if (!StringUtils.isBlank(this.ownerId)) {
            headers.put(AmbryBaseResponse.Headers.OWNER_ID, this.ownerId);
        }
        this.umMeta.forEach(headers::put);
        return headers;
    }
}
