import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private final ArrayList<Integer[]> listOfReservedVisits = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private Timetable timetable;
    private final static int TABLE_OFFSET = 1;
    private final static int MIN_AMOUNT_OF_DAYS = 1;
    private final static int MAX_AMOUNT_OF_DAYS = 5;
    private final static int MIN_AMOUNT_OF_HOURS = 1;
    private final static int MAX_AMOUNT_OF_HOURS = 9;
    private final static int FIRST_ELEMENT = 0;
    private final static int SECOND_ELEMENT = 1;
    private final static int RESERVATION_CHOSEN = 1;
    private final static int CANCEL_CHOSEN = 2;
    private final static int MIN_VISITS_POSSIBLE_TO_CANCEL = 0;



    public ClientHandler(Socket socket, Timetable timetable) throws IOException {
        try {
            this.socket = socket;
            this.timetable = timetable;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            displayTimetable(this.bufferedWriter, timetable.getTimetable());
            sendMessageToClient(bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");

        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    @Override
    public void run() {
        int choosedAction;
        while(socket.isConnected()){
            try {
                choosedAction = Integer.parseInt(bufferedReader.readLine());
                if(choosedAction == RESERVATION_CHOSEN){
                    synchronized (this) {
                        reserveVisit();
                    }
                } else if (choosedAction == CANCEL_CHOSEN) {
                    cancelVisit();
                } else {
                    sendMessageToClient(bufferedWriter, "Something went wrong, please try one else time");
                    sendMessageToClient(bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");
                }
            }
            catch (RuntimeException e){ //NumberFormatException | IndexOutOfBoundsException
                //System.out.println(e);
                System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
                try {
                    sendMessageToClient(bufferedWriter, "Choose action by typing 1 or 2, please try one else time");
                    sendMessageToClient(bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");
                } catch (IOException a) {
                    throw new RuntimeException(a);
                }
            }
            catch (Exception e){ //IOException
                //System.out.println(e);
                System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void reserveVisit() throws IOException, IndexOutOfBoundsException, NumberFormatException{ //IOException, IndexOutOfBoundsException, NumberFormatException
        int day_messageFromClient;
        int hour_messageFromClient;

        sendMessageToClient(bufferedWriter, "Which day you want reserve to? (1 - 5): ");

        day_messageFromClient = Integer.parseInt(bufferedReader.readLine());
        if(incorrectDayOrHour(day_messageFromClient, true))
            return;
        sendMessageToClient(bufferedWriter, "Which hour you want reserve to? (1 - 9): ");
        hour_messageFromClient = Integer.parseInt(bufferedReader.readLine());
        if(incorrectDayOrHour(day_messageFromClient, false))
            return;
        if(!validateInputedVisitToReserve(day_messageFromClient-TABLE_OFFSET, hour_messageFromClient-TABLE_OFFSET)){
            sendMessageToClient(bufferedWriter, "This date is currently occupied, please try to reserve free one");
            sendMessageToClient(bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");
            return;
        }
        Integer[] visit = {day_messageFromClient-TABLE_OFFSET, hour_messageFromClient-TABLE_OFFSET};
        listOfReservedVisits.add(visit);
        System.out.println(clientUsername + " has reserved visit on " + Timetable.days[visit[FIRST_ELEMENT]] + " " + Timetable.hours[visit[SECOND_ELEMENT]]);
        changeCalendar(day_messageFromClient-TABLE_OFFSET, hour_messageFromClient-TABLE_OFFSET, false); //this.timetable.getTimetable()
        refreshCalendar();
    }

    private boolean incorrectDayOrHour(int input, boolean isItDay) throws IOException, IndexOutOfBoundsException, NumberFormatException{
        if(isItDay && (input < MIN_AMOUNT_OF_DAYS || input > MAX_AMOUNT_OF_DAYS)){
            sendMessageToClient(bufferedWriter, "Please write a number from 1 to 5 to determine a day of week");
            sendMessageToClient(bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");
            return true;
        }else if (!isItDay && (input < MIN_AMOUNT_OF_HOURS || input > MAX_AMOUNT_OF_HOURS)){
            sendMessageToClient(bufferedWriter, "Please write a number from 1 to 9 to determine a hour of a chosen day");
            sendMessageToClient(bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");
            return true;
        }
        return false;
    }

    private void cancelVisit() throws Exception{
        if(listOfReservedVisits.isEmpty()) {
            sendMessageToClient(bufferedWriter, "You hasn't done any reservations yet");
            sendMessageToClient(bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");
            return;
        }
        int i = 0;
        int visit_to_cancel;
        Integer[][] visits = new Integer[listOfReservedVisits.toArray().length][];
        for(Integer[] visit : listOfReservedVisits) {
            sendMessageToClient(bufferedWriter, i + ". " + Timetable.days[visit[FIRST_ELEMENT]] + " " + Timetable.hours[visit[SECOND_ELEMENT]]); //Arrays.toString(visit)
            visits[i] = visit;
            i++;
        }
        sendMessageToClient(bufferedWriter, "Which from your reservations you want to cancel?: ");
        visit_to_cancel = Integer.parseInt(bufferedReader.readLine());
        if(visit_to_cancel < MIN_VISITS_POSSIBLE_TO_CANCEL || visit_to_cancel > visits.length){
            sendMessageToClient(bufferedWriter, "We can't recognize your choose, please, try again");
            sendMessageToClient(bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");
            return;
        }

        if(!listOfReservedVisits.contains(visits[visit_to_cancel])){
            sendMessageToClient(bufferedWriter, "You hasn't chosen proper visit to cancel, try one else time");
            sendMessageToClient(bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");
            return;
        }
        System.out.println(clientUsername + " has canceled visit on " + Timetable.days[visits[visit_to_cancel][FIRST_ELEMENT]] + " " + Timetable.hours[visits[visit_to_cancel][SECOND_ELEMENT]]);

        changeCalendar(visits[visit_to_cancel][FIRST_ELEMENT], visits[visit_to_cancel][SECOND_ELEMENT], true);
        refreshCalendar();
        listOfReservedVisits.remove(visits[visit_to_cancel]);
    }

    private void sendMessageToClient(BufferedWriter bufferedWriter, String mess) throws IOException{
        bufferedWriter.write(mess);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
    private boolean validateInputedVisitToReserve(Integer visit_day, Integer visit_hour){
        Integer[] wantedVisit = new Integer[2];
        wantedVisit[FIRST_ELEMENT] = visit_day;
        wantedVisit[SECOND_ELEMENT] = visit_hour;

        if(Objects.equals(timetable.timetable.get(visit_hour).get(visit_day), "xx"))
            return false;
        return !listOfReservedVisits.contains(wantedVisit);
    }
    public void changeCalendar(Integer visit_day, Integer visit_hour, Boolean remove) throws IndexOutOfBoundsException{
        if(!remove)
            timetable.timetable.get(visit_hour).set(visit_day, "xx");
        else
            timetable.timetable.get(visit_hour).set(visit_day, "oo");
    }
    public void refreshCalendar(){
        for(ClientHandler clientHandler : clientHandlers){
            try{
                //if(!clientHandler.clientUsername.equals(clientUsername)){
                displayTimetable(clientHandler.bufferedWriter, timetable.getTimetable());
                sendMessageToClient(clientHandler.bufferedWriter, "Server: Reserve visit - type 1 | Cancel visit - type 2: ");
                //}
            } catch( IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }
    public void displayTimetable(BufferedWriter bufferedWriter,  ArrayList<ArrayList<String>> timetable) throws IOException {
        sendMessageToClient(bufferedWriter, "Actualised timetable for current week\n");
        sendMessageToClient(bufferedWriter, "      Pn Wt Sr Cz Pt");
        String[] hours = {"10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
        int i = 0;
        for (ArrayList<String> row : timetable) {
            bufferedWriter.write(hours[i] + " ");
            for (String element : row) {
                bufferedWriter.write(element + " ");
            }
            bufferedWriter.newLine();
            i++;
        }
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        //refreshCalendar();
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
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
}
