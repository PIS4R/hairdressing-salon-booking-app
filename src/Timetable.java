import java.util.ArrayList;

public class Timetable {

    public ArrayList<ArrayList<String>> timetable;
    private final static int AMOUNT_OF_DAYS = 5;
    private final static int AMOUNT_OF_HOURS = 9;

    public final static String[] days = {"Pn", "Wt", "Sr", "Cz", "Pt"};
    public final static String[] hours = {"10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};


    public Timetable() {
        this.timetable = initTimetable();
    }
    public final ArrayList<ArrayList<String>> getTimetable() {
        return this.timetable;
    }
    public final void setTimetable(ArrayList<ArrayList<String>> newTimetable) {
        this.timetable = newTimetable;
    }
    public final ArrayList<ArrayList<String>> initTimetable() {
        ArrayList<ArrayList<String>> timetable = new ArrayList<>();

        for (int i = 0; i < AMOUNT_OF_HOURS; i++) {
            ArrayList<String> row = new ArrayList<>();
            for (int j = 0; j < AMOUNT_OF_DAYS; j++) {
                row.add("oo");
            }
            timetable.add(row);
        }

        return timetable;
    }

}
