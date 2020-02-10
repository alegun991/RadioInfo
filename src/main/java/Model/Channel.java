package Model;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * This class holds all information about a channel
 */

public class Channel {

    private int id;
    private String name;
    private String imageUrl;
    private String tagLine;

    /**
     * Constructor
     * @param id id of channel
     * @param name name of channel
     * @param imageUrl url for the image
     */

    public Channel(int id, String name, String imageUrl) {

        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;

    }

    /**
     *
     * @return channel id
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return channel name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the image from an URL
     * @return ImageIcon holding the image for a channel
     */
    public ImageIcon getImage() {

        ImageIcon imageIcon = null;

        try {
            if(imageUrl != null) {
                URL url = new URL(imageUrl);
                BufferedImage image = ImageIO.read(url);
                imageIcon = new ImageIcon(image);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageIcon;
    }

}
