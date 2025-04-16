import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class Server {
    private final ServerSocket serverSocket;
    private static final Timetable timetable = new Timetable();
    private final static int PORT = 2137;
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    private void startServer() {
        try{
            while(!serverSocket.isClosed()){

                    Socket socket = serverSocket.accept();
                    System.out.println("A new Client has connected");
                    ClientHandler clientHandler = new ClientHandler(socket, timetable);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
            }

        } catch (IOException e){
            closeServerSocket();
        }
    }


    private void closeServerSocket(){
        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{

        ServerSocket serverSocket = new ServerSocket(PORT);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}
