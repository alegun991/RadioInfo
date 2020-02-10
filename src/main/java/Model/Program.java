package Model;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * This class holds information about a Program
 */

public class Program {

    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String imageUrl;
    private int id;

    /**
     * Constructor
     * @param id id of program
     * @param title title of program
     * @param description description of program
     * @param imageUrl Url for image corresponding to program
     * @param startTime start time of program
     * @param endTime end time of program
     */
    public Program(int id, String title, String description, String imageUrl,
                   LocalDateTime startTime, LocalDateTime endTime){

        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.startTime = startTime;
        this.endTime = endTime;

    }

    public int getId() {
        return id;
    }

    /**
     *
     * @return program title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return program description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @return program start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     *
     * @return program end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Gets the image from an URL
     * @return ImageIcon holding the image for a program
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
