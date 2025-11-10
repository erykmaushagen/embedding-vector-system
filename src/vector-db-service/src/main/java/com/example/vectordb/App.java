
import io.pinecone.clients.Pinecone;
import io.pinecone.configs.PineconeConfig;

import org.openapitools.db_control.client.model.*;

public class App {

    public static void main(String[] args) {
        System.out.println("--- Starte die Konfiguration des Vector Database Clients... ---");

        VectorDataBaseConfig dbConfig = null;
        try {
            // 1. Instanziierung des Konfigurationsobjekts.
            // Es wird angenommen, dass der API-Schlüssel aus 'secrets.json' geladen wird.
            // Der DUMMY_KEY dient nur als Platzhalter für den Konstruktor.
            dbConfig = new VectorDataBaseConfig();
            System.out.println("INFO: VectorDataBaseConfig-Instanz erstellt.");

            // 2. Verbindung testen
            System.out.println("INFO: Führe Verbindungstest durch...");
            boolean isConnected = dbConfig.testConnection();

            if (isConnected) {
                System.out.println("ERFOLG: Datenbankverbindung erfolgreich hergestellt. Client ist bereit.");

                // 3. Client abrufen und nutzen
                Pinecone client = dbConfig.getClient();
                System.out.println("ERFOLG: Pinecone Client erfolgreich abgerufen: " + client.getClass().getName());

                // Hier würden die eigentlichen Datenbankoperationen stattfinden.
                // client.listIndexes();

            } else {
                System.err.println(
                        "FEHLER: Datenbankverbindung konnte NICHT hergestellt werden. Bitte 'secrets.json' und Netzwerkeinstellungen prüfen.");
            }

        } catch (IllegalStateException e) {
            System.err.println("KRITISCHER FEHLER (API-Key): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("UNERWARTETER FEHLER: " + e.getMessage());
            // e.printStackTrace(); // Nur zur detaillierteren Fehlersuche
        } finally {
            // 4. Shutdown des Clients
            if (dbConfig != null) {
                dbConfig.shutdown();
            }
            System.out.println("INFO: Anwendung beendet.");
            System.out.println("-------------------------------------------------------------");
        }
    }
}