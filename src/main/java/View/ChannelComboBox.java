package View;

import javax.swing.*;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for filling a JComboBox with Channel names.
 */

public class ChannelComboBox extends JComboBox<String> {

    List<String> channels;
    private static final String PLACE_HOLDER = "Choose a radio channel";

    /**
     * Constructor, initialises the combobox.
     * @param channels list of channel names
     */

    public ChannelComboBox(List<String> channels){
        this.channels = channels;
        addItem(PLACE_HOLDER);
        addChannels();
    }

    /**
     * Place holder text is only selected when program starts. Once another
     * channel is chosen, place holder text can't be chosen again.
     * @param anObject the channel name
     */
    @Override
    public void setSelectedItem(Object anObject) {

        if(!PLACE_HOLDER.equals(anObject)){

            super.setSelectedItem(anObject);
        }

    }

    /**
     * Goes through the list of channel names and adds them to the
     * JComboBox
     */
    public void addChannels(){

        for (String s : channels) {

            addItem(s);
        }

    }

    /**
     * Adds listeners for the items contained in the JComboBox
     * @param listener ItemListener for the JComboBox
     */
    public void comboBoxListener(ItemListener listener){

        addItemListener(listener);
    }

}
