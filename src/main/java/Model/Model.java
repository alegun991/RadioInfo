package Model;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * This class acts as the "main" Model class.
 */
public class Model {

    private ArrayList<Channel> channels;
    private XmlParser xmlParser;
    private String errorMSg = null;

    /**
     * Constructor, initializes the xmlreader and channel list
     */
    public Model() {
        channels = new ArrayList<>();
        xmlParser = new XmlParser();
    }

    /**
     * Calls the parser to retrieve all the channels from the API.
     */
    public void loadChannels() {

        try {
            channels = xmlParser.channelParser();

        } catch (ParserConfigurationException | IOException | SAXException e) {

            errorMSg = "Radio channels could not be loaded. Caused by: "
                    + e.getClass().getSimpleName() + "\n" + e.getMessage();
            System.err.println("Error loading channels " + e.toString());

        }
    }

    /**
     * Goes through the list of channels, if the name matches, retrieves the
     * corresponding image to that channel
     * @param channelName the name of a specific channel
     * @return image for that channel
     */
    public ImageIcon getImageForName(String channelName) {

        ImageIcon image;
        for (Channel c : channels) {

            if (c.getName().equals(channelName)) {

                image = c.getImage();
                return image;
            }
        }
        return null;
    }

    /**
     * Goes through a list of channels and fills another list with just
     * the channel names
     * @return a list of channel names
     */
    public ArrayList<String> getChannelNames() {

        ArrayList<String> channelNames = new ArrayList<>();

        for (Channel c : channels) {

            channelNames.add(c.getName());
        }
        return channelNames;

    }

    /**
     * Helper method used to get the id for a channel based on the channel
     * name
     * @param name channel name
     * @return the id for the channel
     */
    private int getChannelId(String name) {
        int id = 0;

        for (Channel c : channels) {

            if (name.equals(c.getName())) {

                id = c.getId();
            }
        }
        return id;

    }

    /**
     * This method calls the parser to get programs for a specific channel id.
     * Then checks that the airing time of a program is before 12 hours in
     * to the future and after 12 hours past from now. Then adds that to a new
     * list which is then returned.
     * @param name channel name
     * @return a list of programs
     */
    public ArrayList<Program> getPrograms(String name) {
        int id = getChannelId(name);

        var timeValidPrograms = new ArrayList<Program>();

        try {
            LocalDateTime pastDateTime = LocalDateTime.now().minusHours(12);
            LocalDateTime futureDateTime = LocalDateTime.now().plusHours(12);

            if (id != 0) {
                var programs = new ArrayList<>(xmlParser.channelEpisodes(id));

                for (Program p : programs) {

                    LocalDateTime startTime = p.getStartTime();

                    if (startTime.isBefore(futureDateTime) &&
                            startTime.isAfter(pastDateTime)) {

                        timeValidPrograms.add(p);
                    }
                }
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {

            errorMSg = "Programs could not be loaded. " +
                    "Caused by: " + e.getClass().getSimpleName();

            System.err.println("Error loading programs " + e.toString());


        }

        return timeValidPrograms;
    }

    /**
     * Displays error message if any exceptions occurred.
     * @param component Parent component.
     */
    public void displayErrorMsg(Component component){

        if (errorMSg != null){
            JOptionPane.showMessageDialog(component, errorMSg, "Alert",
                    JOptionPane.WARNING_MESSAGE, null);
        }
    }
}
