import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // Inicia o servidor SMTP na porta 2525
        Server server = new Server(2525);
        server.start();
    }
}

