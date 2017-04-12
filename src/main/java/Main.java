import java.io.ByteArrayInputStream;

public class Main {

    // 4 * 1024 * 1024 is problematic on newer versions, and other sizes are ok on all versions.
    private static final int HIT_BLOCK_SIZE = 4 * 1024 * 1024;

    private static final String AMBRY_ENDPOINT = "http://192.168.2.34";

    public static void main(String[] args) {
        byte[] block = new byte[HIT_BLOCK_SIZE];
        AmbryFileMeta meta = new AmbryFileMeta(block.length, "HoloCloud", "application/octet-stream");
        AmbryClient ambryClient = new AmbryClient(AMBRY_ENDPOINT, 1174);
        try (AmbryPostFileResponse response = ambryClient.postFile(new ByteArrayInputStream(block), meta)) {
            if (!response.isSucceeded()) {
                throw new RuntimeException("OSS save failed + " + response.getMessage());
            }
            System.out.println("Post file succeeded, ambry Id: " + response.getAmbryId());
        } catch (Exception e) {
            throw new RuntimeException("Post file failed.", e);
        }
    }
}


