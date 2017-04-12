import java.io.InputStream;

public class AmbryClient {

    private final boolean useSSL;
    private final String hostname;
    private final int port;
    private final String endpoint;

    /**
     * Create an Ambry client.
     *
     * @param hostname the Ambry host to connect to
     * @param port     the port to connect to
     */
    public AmbryClient(String hostname, int port) {
        this(hostname, port, false);
    }

    /**
     * Create an Ambry client.
     *
     * @param hostname the Ambry host to connect to
     * @param port     the port to connect to
     * @param useSSL   whether to use SSL connection
     */
    public AmbryClient(String hostname, int port, boolean useSSL) {
        this.useSSL = useSSL;
        if (!hostname.contains("://")) {
            this.hostname = (useSSL ? "https://" : "http://") + hostname;
        } else {
            this.hostname = hostname;
        }
        this.port = port;
        this.endpoint = this.hostname + ":" + this.port;
    }

    public AmbryPostFileResponse postFile(InputStream file, AmbryFileMeta fileMeta) {
        return AmbryHelper.postFile(endpoint + "/", fileMeta.toHeaders(), file);
    }
}
