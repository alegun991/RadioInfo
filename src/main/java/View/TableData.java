package View;

/**
 * This class represents a data object which holds information about data
 * to be presented in the table.
 */


public class TableData {

    private String title;
    private String startTime;
    private String endTime;
    private String status;
    private int id;

    public TableData(int id, String title, String startTime, String endTime,
                     String status){

        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;

    }

    /**
     *
     * @return id of program
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return Program title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return Program start time
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     *
     * @return Program end time
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Indicates if a program has aired, is running or is upcoming.
     * @return returns a string indicating status
     */
    public String getStatus() {
        return status;
    }
}
