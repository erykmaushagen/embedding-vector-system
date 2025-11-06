
import io.pinecone.clients.Pinecone;
import io.pinecone.configs.PineconeConfig;

import org.openapitools.db_control.client.model.*;

class TestServiceConnection {
    private String apiKey;

    public TestServiceConnection(String API_KEY) {
        this.apiKey = API_KEY;
    }

    public Pinecone setConnection(String[] args) {
        Pinecone clientInstance = new Pinecone.Builder(this.apiKey).build();
        return clientInstance;
    }

    public Object getApiKey() {
        return this.apiKey;
    }
}

public class App {

    public static void main(String[] args) {
        String meinKey = "xy";

        Pinecone testInstance = new Pinecone.Builder(meinKey).build();

        // PineconeConfig config = testInstance.getConfig();

        // System.out.println("API-KEY:" + config.getApiKey());
        // System.out.println("HOST:" + config.getHost());
        // System.out.println("HOST:" + config.getHost());
        // if (config.isTLSEnabled()) {
        // System.out.println("TLS is enabled");
        // } else {
        // System.out.println("TLS is not enabled");
        // }

    }
}
