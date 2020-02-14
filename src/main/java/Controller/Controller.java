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
import java.util.List;
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
    private ChannelComboBox comboBox;
    private volatile ArrayList<Program> programs;
    private String currentChannel;
    private Timer timer;
    private String lastUpdated;
    private AtomicBoolean isUpdating = new AtomicBoolean(false);
    private boolean mouseListenerIsActive = false;

    /**
     * Constructor
     * Initiates the GUI, executes the SwingWorker which retrieves channels and
     * initiates a timer.
     */
    public Controller() {
        model = new Model();
        initView();
        new ChannelWorker().execute();
        timer = new Timer();
    }

    /**
     * Initiates the GUI on EDT.
     */
    public void initView() {

        SwingUtilities.invokeLater(() -> {
            view = new MainWindow();
            view.setMinimumSize(new Dimension(1080, 720));
            view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            view.setVisible(true);

        });

    }

    /**
     * Initialises the listeners for swing components in the view.
     */
    private void initListeners() {
        view.aboutListener(actionEvent -> showAboutDialog());
        view.helpListener(actionEvent -> showHelpDialog());
        comboBox.comboBoxListener(this::showProgramData);

    }

    private void initTableListener() {
        view.refreshListener(actionEvent -> scheduledUpdate());
        addProgramListener();
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

                if (mouseListenerIsActive && !isUpdating.get()) {

                    int rowId = view.getTableModel().getRowId(
                            view.getTable().getSelectedRow());

                    String startTime = view.getTable().getModel().getValueAt(
                            view.getTable().getSelectedRow(), 1).toString();

                    for (Program p : programs) {
                        LocalDateTime ldt = p.getStartTime();
                        String formatTime = timeFormatter(ldt);

                        if (p.getId() == rowId && formatTime.equals(startTime)) {

                            programImgRetriever(p);

                        }
                    }
                }
            }
        });

    }

    private void programImgRetriever(Program p) {

        SwingWorker<Void, ImageIcon> sw = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {

                publish(p.getImage());

                return null;
            }

            @Override
            protected void process(List<ImageIcon> chunks) {
                view.setOptionDialog(p.getDescription(), chunks.get(0));
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

        if (!isUpdating.get()) {
            updateData();
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
        isUpdating.set(true);
        ProgramWorker pw = new ProgramWorker();
        pw.execute();
    }

    /**
     * Nested class, retrieves all radio channels on background thread, once
     * done, fills a combo box with channel names
     */
    class ChannelWorker extends SwingWorker<Void, String> {

        @Override
        protected Void doInBackground() {
            model.loadChannels();

            var tmp = model.getChannelNames();

            for (String s : tmp) {
                publish(s);
            }

            return null;

        }

        @Override
        protected void process(List<String> chunks) {
            model.displayErrorMsg(view);
            comboBox = new ChannelComboBox(chunks);
            view.addComboBox(comboBox);

        }

        @Override
        protected void done() {
            initListeners();

        }
    }

    /**
     * Nested class, fills table with programs based on the currently selected
     * channel. Retrieves programs
     */
    class ProgramWorker extends SwingWorker<ImageIcon, TableData> {


        @Override
        protected ImageIcon doInBackground() {

            programs = new ArrayList<Program>(
                    model.getPrograms(getCurrentChannel()));

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

                publish(new TableData(id, title, formatTime1,
                        formatTime2, status));

            }

            return model.getImageForName(getCurrentChannel());
        }

        /**
         * Evoked on EDT. Updates the table model.
         *
         * @param chunks list of table data
         */
        @Override
        protected void process(List<TableData> chunks) {

            view.updateTable(chunks);
        }

        @Override
        protected void done() {

            model.displayErrorMsg(view);
            try {
                var img = get();

                //shows message dialog if any error occurred
                view.setChannelImage(img);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            //only "activate" mouse listener if any data was received.
            //If not, "deactivate" listener
            if (programs.size() == 0 && mouseListenerIsActive) {
                mouseListenerIsActive = false;
                view.clearModel();

            } else if (programs.size() > 0 && !mouseListenerIsActive) {
                initTableListener();
                mouseListenerIsActive = true;
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

            if (!isUpdating.get()) {

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

