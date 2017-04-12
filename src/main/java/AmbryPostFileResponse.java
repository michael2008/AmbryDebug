import org.apache.http.Header;
import org.apache.http.HttpStatus;

import java.io.InputStream;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class AmbryPostFileResponse extends AmbryBaseResponse {
    /**
     * The resource id returned after save on Ambry.
     */
    private String ambryId;

    public AmbryPostFileResponse(Integer code, String status, InputStream content, List<Header> headers) {
        super(code, status, content, headers);
        if (this.code != HttpStatus.SC_CREATED) {
            this.setSucceeded(false);
            this.setMessage(this.headers.getOrDefault(Headers.FAILURE_REASON, "unknown failure reason"));
            return;
        }
        this.setSucceeded(true);
        this.setMessage(DEFAULT_SUCCESS_MSG);
        // validate
        checkArgument(this.headers.containsKey(Headers.LOCATION), "malformed response.");
        this.ambryId = this.headers.get(Headers.LOCATION).replace("/", "");
    }

    public String getAmbryId() {
        return ambryId;
    }
}
