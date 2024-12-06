import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Server {
    private static volatile boolean serverRunning = true; // Controle do estado do servidor
    private ExecutorService executor;
    private Semaphore semaphore; // Controle de conexões simultâneas
    private int port;

    public Server(int port) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(10); // Pool com 10 threads
        this.semaphore = new Semaphore(10); // Limita a 10 conexões simultâneas
    }

    public void stopServer() {
        serverRunning = false; // Interrompe todas as threads
        executor.shutdown(); // Encerra o pool de threads
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName("localhost"))) {
            System.out.println("Servidor SMTP esperando por conexões na porta " + port);

            while (serverRunning) {
                try {
                    semaphore.acquire(); // Aguarda uma permissão para aceitar uma nova conexão
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(new ClientProcessor(clientSocket)); // Submete a tarefa para o pool de threads
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(2525);
        server.start();
    }
}
