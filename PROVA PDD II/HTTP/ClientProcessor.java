import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.CountDownLatch;

public class ClientProcessor implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private CyclicBarrier barrier;
    private CountDownLatch latch;

    public ClientProcessor(Socket socket) {
        this.socket = socket;
        this.barrier = new CyclicBarrier(1); // Sincroniza as etapas do cliente
        this.latch = new CountDownLatch(1); // Espera até que o processamento esteja completo
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("220 SimpleSMTPServer Simple Mail Transfer Service Ready");

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("HELO")) {
                    out.println("250 SimpleSMTPServer Hello " + line.substring(5));
                } else if (line.startsWith("MAIL FROM:")) {
                    out.println("250 Sender <" + line.substring(10) + "> OK");
                } else if (line.startsWith("RCPT TO:")) {
                    out.println("250 Recipient <" + line.substring(9) + "> OK");
                } else if (line.equals("DATA")) {
                    out.println("354 End data with <CR><LF>.<CR><LF>");
                    String data;
                    while ((data = in.readLine()) != null) {
                        if (data.equals(".")) {
                            out.println("250 Message accepted for delivery");
                            break;
                        }
                    }
                } else if (line.equals("QUIT")) {
                    out.println("221 SimpleSMTPServer Service closing transmission channel");
                    break;
                } else {
                    out.println("500 Syntax error, command unrecognized");
                }

                // Chama yield para dar chance a outras threads
                Thread.yield();

                // Sincroniza a execução das etapas
                barrier.await();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                latch.countDown(); // Finaliza o processamento do cliente
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
