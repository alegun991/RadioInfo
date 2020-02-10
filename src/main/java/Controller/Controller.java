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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller handles communication between model and views. The Controller is
 * responsible updating the GUI and executing workers to retrieve data from the
 * Model. The controller is also responsible for initiating listeners and
 * handling events triggered by those listeners.
 */

public class Controller {

    private Model model;
    private MainWindow view;
    private ChannelComboBox comboBox;
    private CopyOnWriteArrayList<Program> programs;
    private String currentChannel;
    private Timer timer;
    private String lastUpdated;
    private ImageIcon img;
    private AtomicBoolean isUpdating = new AtomicBoolean(false);

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
    public void initListeners() {
        view.refreshListener(actionEvent -> scheduledUpdate());
        view.aboutListener(actionEvent -> showAboutDialog());
        view.helpListener(actionEvent -> showHelpDialog());
        comboBox.comboBoxListener(this::showProgramData);
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
     * tableau is clicked. Gets both the tile and start time from the tableau
     * and checks for those values in the program list. When the program is
     * found it fetches the image and description for that program send it to
     * the view.
     */
    private void addProgramListener() {

        view.addTableListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                String title = view.getTable().getModel().getValueAt(
                        view.getTable().getSelectedRow(), 0).toString();

                String startTime = view.getTable().getModel().getValueAt(
                        view.getTable().getSelectedRow(), 1).toString();

                programs.forEach(p -> {
                    LocalDateTime ldt = p.getStartTime();
                    String formatTime = timeFormatter(ldt);

                    if (p.getTitle().equals(title) &&
                            formatTime.equals(startTime)) {

                        ImageIcon imageIcon = p.getImage();
                        String description = p.getDescription();

                        view.setOptionDialog(description, imageIcon);

                    }
                });


            }
        });

    }

    /**
     * Either called when refresh button is clicked or when channel is
     * chosen from combo box. Executes the program worker, cancels the current
     * timer and schedules an update.
     */
    public synchronized void scheduledUpdate() {

        updateData();

        timer.cancel();
        timer.purge();

        timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateTask(), 2000,
                2000);


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
     * Inner class, retrieves all radio channels on background thread, once
     * done, fills a combo box with channel names
     */
    class ChannelWorker extends SwingWorker<ArrayList<String>, Object> {

        @Override
        protected ArrayList<String> doInBackground() {
            model.loadChannels();

            return model.getChannelNames();

        }

        @Override
        protected void done() {

            try {
                var temp = get();

                comboBox = new ChannelComboBox(temp);
                view.addComboBox(comboBox);
                initListeners();


            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * inner class, fills table with programs based on the currently selected
     * channel. Retrieves programs
     */
    class ProgramWorker extends SwingWorker<String, TableData> {


        @Override
        protected String doInBackground() {

            programs = new CopyOnWriteArrayList<Program>(
                    model.getPrograms(getCurrentChannel()));

            setImg(model.getImageForName(getCurrentChannel()));

            LocalDateTime ldt = LocalDateTime.now();
            String status = "";

            for (Program p : programs) {

                var title = p.getTitle();
                var startTime = p.getStartTime();
                var formatTime1 = timeFormatter(startTime);
                var endTime = p.getEndTime();
                var formatTime2 = timeFormatter(endTime);

                if (startTime.isBefore(ldt) && endTime.isAfter(ldt)) {

                    status = "Running";
                }
                if (startTime.isBefore(ldt) && endTime.isBefore(ldt)) {

                    status = "Finished";
                }
                if (startTime.isAfter(ldt)) {

                    status = "Upcoming";
                }

                //publish TableData object for each program
                publish(new TableData(title, formatTime1,
                        formatTime2, status));


            }

            return null;
        }

        /**
         * Evoked on EDT.
         *
         * @param chunks list of table data
         */
        @Override
        protected void process(List<TableData> chunks) {


            view.updateTable(chunks);
        }

        @Override
        protected void done() {


            view.setChannelImage(getImg());
            lastUpdate(LocalDateTime.now());
            view.setLastUpdated(lastUpdated);

            isUpdating.set(false);

        }
    }

    /**
     * Responsible for executing a scheduled update. Its run method will be
     * called once
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
     * sets the image for a channel
     *
     * @param img channel image
     */
    private synchronized void setImg(ImageIcon img) {

        this.img = img;
    }

    /**
     * @return channel image
     */
    private synchronized ImageIcon getImg() {

        return img;
    }

    /**
     * @param ldt local date time
     * @return formatted String of pattern yyyy-MM-dd HH:mm:ss
     */
    private synchronized String timeFormatter(LocalDateTime ldt) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return ldt.format(formatter);
    }

    /**
     * Updates the time when last update occurred
     *
     * @param ldt local date time of last update
     */
    private synchronized void lastUpdate(LocalDateTime ldt) {

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

