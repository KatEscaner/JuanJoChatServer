import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final int PORT = 7000;
    private static final int THREAD_POOL_SIZE = 10;
    // Object where we manage messages of the users
    public static final MessageQueue messageQueue = new MessageQueue();
    // List of online sessions
    public static SessionManager sessionManager = new SessionManager();

    public static void main(String[] args) {
        // object which can manage threads
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try(ServerSocket server = new ServerSocket(PORT)){
            while(true){
                Socket service = server.accept();
                System.out.println("Connection established");
                // create a ClientHandler Thread with a socket which is connected with client
                executor.execute(new ClientHandler(service));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
