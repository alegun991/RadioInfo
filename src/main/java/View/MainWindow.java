package View;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.List;

/**
 * This class is responsible for the main frame of the application.
 */

public class MainWindow extends JFrame {

    private JButton refreshButton;
    private JTable jTable;
    private ProgramTable tableModel;
    private JPanel imagePanel;
    private JLabel imageLabel;
    private JTextArea lastUpdate;
    private JTextArea noImageFound;
    private JMenuItem aboutItem;
    private JMenuItem helpItem;
    private JPanel comboPanel;

    /**
     * Constructor, initialises the components in the GUI
     */

    public MainWindow() {

        super("Radio Info");

        setLayout(new BorderLayout());
        imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());
        imageLabel = new JLabel();
        lastUpdate = new JTextArea();
        noImageFound = new JTextArea();

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.GRAY);

        refreshButton = new JButton("Update");
        bottomPanel.add(refreshButton, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        initMenu();
        initiateTable();


    }

    /**
     * Initializes the table and sets the dimensions, and style attributes
     * for it.
     */
    public void initiateTable(){

        tableModel = new ProgramTable();
        jTable = new JTable(tableModel) {

            @Override
            public JTableHeader getTableHeader() {
                JTableHeader tableHeader = super.getTableHeader();
                tableHeader.setFont(new Font("Serif", Font.BOLD, 12));
                tableHeader.setBackground(Color.DARK_GRAY);
                tableHeader.setForeground(Color.white);
                tableHeader.setPreferredSize(new Dimension(
                        getWidth(), 24));

                return tableHeader;
            }

            @Override
            public Color getGridColor() {
                return Color.BLACK;
            }
        };

        jTable.setRowHeight(20);
        jTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        jTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        jTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        jTable.getColumnModel().getColumn(3).setPreferredWidth(125);

        jTable.setFillsViewportHeight(true);
        JScrollPane tableScrollPane = new JScrollPane(jTable);
        JPanel tablePanel = new JPanel(new GridLayout());
        tablePanel.add(tableScrollPane);
        tablePanel.setPreferredSize(new Dimension(600, 400));
        add(tablePanel, BorderLayout.EAST);
    }

    /**
     * Adds a combo box to a jpanel, which is the added to the main fram.
     * @param comboBox A combo box filled with channel names.
     */
    public void addComboBox(JComboBox<String> comboBox) {

        comboPanel = new JPanel();
        comboPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        comboPanel.add(comboBox);

        add(comboPanel, BorderLayout.NORTH);

    }

    /**
     * Initialises the menubar with the menu and its menu items.
     */
    private void initMenu(){

        JMenuBar jMenuBar = new JMenuBar();
        JMenu jMenu = new JMenu("Options");
        aboutItem = new JMenuItem("About");
        jMenu.add(aboutItem);
        helpItem = new JMenuItem("Help");
        jMenu.add(helpItem);
        jMenuBar.add(jMenu);

        setJMenuBar(jMenuBar);

    }

    /**
     * Adds new data to the table model.
     * @param tableData list of table data. TableData holds data for each
     *                  column in the table.
     */
    public void updateTable(List<TableData> tableData) {

        clearModel();

        for (TableData td : tableData){

            tableModel.addTableData(td);

        }
        tableModel.fireTableDataChanged();
        pack();
    }

    public void clearModel(){

        tableModel.clearData();
    }


    /**
     * Adds an ActionListener to the refresh button.
     * @param listener ActionListener for refresh button
     */
    public void refreshListener(ActionListener listener) {

        refreshButton.addActionListener(listener);
    }

    /**
     * Adds mouse listener on the table
     * @param adapter mouse adapter
     */
    public void addTableListener(MouseAdapter adapter) {

        jTable.addMouseListener(adapter);

    }

    /**
     * Adds an ActionListener to the menu item "About"
     * @param listener ActionListener
     */
    public void aboutListener(ActionListener listener){

        aboutItem.addActionListener(listener);
    }

    /**
     * Adds an ActionListener to the menu item "Help"
     * @param listener ActionListener
     */
    public void helpListener(ActionListener listener){

        helpItem.addActionListener(listener);
    }

    /**
     * Messase dialog for menu item "About".
     */
    public void aboutDialog(){

        String aboutText = "About this application";
        String info = "This application is developed in the course \n" +
                "Applikationsutveckling i Java at Umea Univeristy. \n";

        JOptionPane.showMessageDialog(this, info, aboutText,
                JOptionPane.PLAIN_MESSAGE, null);
    }

    /**
     * Messase dialog for menu item "Help".
     */
    public void helpDialog(){

        String aboutText = "Help";
        String info = "This application uses Sveriges Radio API. Use the \n" +
                "drop down list to select a radio channel. By selecting a \n" +
                "channel, a table with programs corresponding to the channel \n" +
                "will be filled. By clicking on a specific program, an image \n" +
                "along with a description will appear in a pop-up window. ";

        JOptionPane.showMessageDialog(this, info, aboutText,
                JOptionPane.PLAIN_MESSAGE, null);

    }

    /**
     *
     * @return returns the JTable
     */
    public JTable getTable() {

        return jTable;
    }

    public ProgramTable getTableModel(){

        return tableModel;
    }

    /**
     * This method is used to show a dialog window when a user presses on
     * a specific program. Shows image and description for that program.
     * @param description Program description
     * @param image Program image
     */
    public void setOptionDialog(String description, ImageIcon image) {

        if (image != null) {
            Image tmp = image.getImage();
            tmp = tmp.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            image = new ImageIcon(tmp);
        }

        int middle = description.length() / 2;

        if(description.length() > 100){

            String firstHalf = description.substring(0, middle);
            String lastHalf = description.substring(middle);

            description = firstHalf + "\n" + lastHalf;
        }

        String programInfo = "Program Information";
        JOptionPane.showMessageDialog(this, description,
                programInfo, JOptionPane.PLAIN_MESSAGE, image);

    }

    /**
     * Sets the image for a specific channel
     * @param icon Image for a channel
     */
    public void setChannelImage(ImageIcon icon) {

        if (imagePanel != null) {
            imagePanel.remove(noImageFound);
            imagePanel.remove(imageLabel);

        }

        if(icon != null) {
            Image tmp = icon.getImage();
            tmp = tmp.getScaledInstance(350, 350, Image.SCALE_SMOOTH);
            icon = new ImageIcon(tmp);

            imageLabel = new JLabel(icon);
            imagePanel.add(imageLabel, BorderLayout.CENTER);
        }

        else{

            noImageFound = new JTextArea();
            noImageFound.append("No image found for chosen channel");
            imagePanel.add(noImageFound, BorderLayout.CENTER);
        }
        add(imagePanel);
        pack();

    }

    /**
     * Displays a text which shows when the program tableau was last updated
     * @param time time of last update
     */
    public void setLastUpdated(String time){

        lastUpdate.setText("");
        lastUpdate.append("Last updated: " + time);

        comboPanel.add(lastUpdate);
        pack();

    }
}
