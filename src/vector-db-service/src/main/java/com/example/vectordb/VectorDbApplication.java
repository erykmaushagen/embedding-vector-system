import com.example.vectordb.client.PineconeClientFactory;

import io.pinecone.clients.Pinecone;
import io.pinecone.configs.PineconeConfig;

import org.openapitools.db_control.client.model.*;

public class VectorDbApplication {

    public static void main(String[] args) {
        System.out.println("***************** TESTING *****************");

        VectorDataBaseConfig myConfig = new VectorDataBaseConfig();

        Pinecone myClient = myConfig.getClient();

        myConfig.testConnection(myClient);

        myConfig.shutdown(myClient);
        System.out.println("Client heruntergefahren.");

        System.out.println("***************** TESTING ENDE *****************");

    }
}