import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private final static int PORT = 2137;
    private Client(Socket socket, String clientUsername) {
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = clientUsername;

        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    private void sendMessage(){
        try{
            writeToBufferedWriter(bufferedWriter, clientUsername);
            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String messageToSend = scanner.nextLine();
                writeToBufferedWriter(bufferedWriter, messageToSend);
            }
        } catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    private void writeToBufferedWriter(BufferedWriter bufferedWriter, String mess) throws IOException{
        bufferedWriter.write(mess);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void listenForMessage(){
        new Thread(() -> {
            String refreshedTimetableFromServer;
            while(socket.isConnected()){
                try{
                    refreshedTimetableFromServer = bufferedReader.readLine();
                    System.out.println(refreshedTimetableFromServer);

                } catch (IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();

    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username: ");
        String clientUsername = scanner.nextLine();
        Socket socket = new Socket("localhost", PORT);
        Client client = new Client(socket, clientUsername);

        client.listenForMessage();
        client.sendMessage();
    }
}
