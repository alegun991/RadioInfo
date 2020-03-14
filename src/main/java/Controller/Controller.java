package Controller;

import Model.Model;
import Model.Program;
import View.ChannelComboBox;
import View.MainWindow;
import View.TableData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller handles communication between model and views. The Controller is
 * responsible updating the GUI and executing workers to retrieve data from the
 * Model. The controller is also responsible for initiating listeners and
 * handling events triggered by those listeners.
 */

public class Controller {

    private final Model model;
    private volatile MainWindow view;
    private final ChannelComboBox comboBox;
    private volatile ArrayList<Program> programs;
    private String currentChannel;
    private Timer timer;
    private String lastUpdated;
    private AtomicBoolean isUpdating = new AtomicBoolean(false);

    /**
     * Constructor
     * Initiates the GUI, executes the SwingWorker which retrieves channels and
     * initiates a timer.
     */
    public Controller() {
        model = new Model();
        comboBox = new ChannelComboBox();
        initView();
        new ChannelWorker().execute();
        timer = new Timer();
    }

    /**
     * d
     * Initiates the GUI on EDT.
     */
    private void initView() {

        SwingUtilities.invokeLater(() -> {
            view = new MainWindow();
            view.setMinimumSize(new Dimension(1080, 720));
            view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            view.setVisible(true);
            view.aboutListener(actionEvent -> showAboutDialog());
            view.helpListener(actionEvent -> showHelpDialog());
            view.addComboBox(comboBox);

        });

    }

    /**
     * Initialises the listeners for swing components in the view.
     */
    private void initListeners() {
        comboBox.comboBoxListener(this::showProgramData);
        view.refreshListener(actionEvent -> scheduledUpdate());

    }

    /**
     * Shows the message dialog for when the menu item "About" is clicked.
     */
    private void showAboutDialog() {

        view.aboutDialog();
    }

    /**
     * Shows the message dialog for when the menu item "Help" is clicked.
     */
    private void showHelpDialog() {

        view.helpDialog();
    }

    /**
     * When a new channel is selected from the combo box, gets the text from
     * the item which was clicked and then calls a scheduledUpdate which
     * updates the program data.
     *
     * @param itemEvent event when an item is click in the combo box.
     */
    private void showProgramData(ItemEvent itemEvent) {

        if (itemEvent.getStateChange() == ItemEvent.SELECTED) {

            setCurrentChannel(itemEvent.getItem().toString());
            scheduledUpdate();

        }
    }

    /**
     * This method is responsible for handling an event when an item in the
     * tableau is clicked. Gets both the id and start time from the tableau
     * and checks for those values in the program list. When the program is
     * found it fetches the image and description for that program send it to
     * the view.
     */
    private void addProgramListener() {

        view.addTableListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                var rows = view.getTableModel().getRowCount();

                if (rows > 0) {
                    int rowId = view.getTableModel().getRowId(
                            view.getTable().getSelectedRow());

                    String startTime = view.getTable().getModel().getValueAt(
                            view.getTable().getSelectedRow(), 1).toString();

                    for (Program p : programs) {
                        LocalDateTime ldt = p.getStartTime();
                        String formatTime = timeFormatter(ldt);

                        if (p.getId() == rowId && formatTime.equals(startTime)){

                            programImgRetriever(p);

                        }
                    }
                }
            }
        });
    }

    /**
     * Used to retrieve an image for a specific program
     *
     * @param p program
     */
    private void programImgRetriever(Program p) {

        SwingWorker<ImageIcon, Void> sw = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {

                return p.getImage();
            }

            @Override
            protected void done() {

                try {
                    var tmp = get();
                    view.setOptionDialog(p.getDescription(), tmp);
                    isUpdating.set(false);

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        sw.execute();

    }

    /**
     * Either called when refresh button is clicked or when channel is
     * chosen from combo box. Executes the program worker, cancels the current
     * timer and schedules an update.
     */
    public void scheduledUpdate() {

        if (isUpdating.compareAndSet(false, true)) {

            if (getCurrentChannel() != null) {

                updateData();
            }
        }

        timer.cancel();
        timer.purge();

        timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateTask(), 3600000,
                3600000);

    }

    /**
     * Executes worker for updating program tableau
     */
    public void updateData() {

        ProgramWorker pw = new ProgramWorker();
        pw.execute();

    }

    /**
     * Nested class, retrieves all radio channels on background thread, once
     * done, fills a combo box with channel names
     */
    class ChannelWorker extends SwingWorker<ArrayList<String>, String> {

        @Override
        protected ArrayList<String> doInBackground() {

            model.loadChannels();
            return model.getChannelNames();

        }

        @Override
        protected void done() {

            model.displayErrorMsg(view);
            try {
                var tmp = get();
                comboBox.addChannels(tmp);
                initListeners();
                addProgramListener();

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Nested class, fills table with programs based on the currently selected
     * channel. Retrieves programs
     */
    class ProgramWorker extends SwingWorker<ArrayList<Program>, Void> {

        @Override
        protected ArrayList<Program> doInBackground() {

            model.loadChannelImage(getCurrentChannel());
            return model.getPrograms(getCurrentChannel());
        }


        @Override
        protected void done() {
            //shows message dialog if any error occurred
            model.displayErrorMsg(view);

            var tableauItems = new ArrayList<TableData>();

            try {
                programs = get();

                LocalDateTime ldt = LocalDateTime.now();
                String status = "";

                for (Program p : programs) {

                    var id = p.getId();
                    var title = p.getTitle();
                    var startTime = p.getStartTime();
                    var formatTime1 = timeFormatter(startTime);
                    var endTime = p.getEndTime();
                    var formatTime2 = timeFormatter(endTime);

                    if (startTime.isBefore(ldt) && endTime.isAfter(ldt)) {

                        status = "Running";
                    }
                    if (endTime.isBefore(ldt)) {

                        status = "Finished";
                    }
                    if (startTime.isAfter(ldt)) {

                        status = "Upcoming";
                    }

                    tableauItems.add(new TableData(id, title, formatTime1,
                            formatTime2, status));

                }
                view.updateTable(tableauItems);
                view.setChannelImage(model.getChannelImg());

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            lastUpdate(LocalDateTime.now());
            view.setLastUpdated(lastUpdated);
            isUpdating.set(false);

        }

    }

    /**
     * Responsible for executing a scheduled update. will run 1 hour after
     * a new channel has been selected
     */
    class UpdateTask extends TimerTask {

        @Override
        public void run() {

            if (isUpdating.compareAndSet(false, true)) {
                updateData();
            }
        }
    }

    /**
     * @param ldt local date time
     * @return formatted String of pattern yyyy-MM-dd HH:mm:ss
     */
    private String timeFormatter(LocalDateTime ldt) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return ldt.format(formatter);
    }

    /**
     * Updates the time when last update occurred
     *
     * @param ldt local date time of last update
     */
    private void lastUpdate(LocalDateTime ldt) {

        lastUpdated = timeFormatter(ldt);
    }

    /**
     * Sets the current channel variable
     *
     * @param currentChannel the currently chosen channel
     */
    private synchronized void setCurrentChannel(String currentChannel) {

        this.currentChannel = currentChannel;
    }

    /**
     * @return currently chosen channel
     */
    private synchronized String getCurrentChannel() {

        return currentChannel;
    }

}

