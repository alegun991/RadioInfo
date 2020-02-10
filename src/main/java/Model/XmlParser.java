package Model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * This class is responsible for parsing xml-data retrieved from Sveriges
 * Radio API. Two main parsing methods, one for channels and one for
 * corresponding programs to the channels
 */

public class XmlParser {

    /**
     * Empty constructor
     */
    public XmlParser() {

    }

    /**
     *
     * @return returns a list of channel objects.
     * @throws ParserConfigurationException exception for parsing
     * @throws SAXException
     * @throws IOException
     */
    public ArrayList<Channel> channelParser() throws ParserConfigurationException,
            SAXException, IOException {

        ArrayList<Channel> channels = new ArrayList<>();

        URL url = new URL("http://api.sr.se/api/v2/channels?pagination=false");
        Document doc = getDoc(url);

        doc.getDocumentElement().normalize();
        Element root = doc.getDocumentElement();

        NodeList channelList = root.getElementsByTagName("channel");

        for (int i = 0; i < channelList.getLength(); i++) {

            Element element = (Element) channelList.item(i);
            String imageUrl = null;

            var imageElement = element.getElementsByTagName(
                    "image").item(0);

            int id = Integer.parseInt(element.getAttribute("id"));
            String name = element.getAttribute("name");
            if (imageElement != null) {

                imageUrl = imageElement.getTextContent();
            }

            Channel channel = new Channel(id, name, imageUrl);
            channels.add(channel);

        }

        return channels;

    }

    /**
     *
     *
     * @param channelId id of channel
     * @return returns a list of Program objects
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public ArrayList<Program> channelEpisodes(int channelId)
            throws SAXException, ParserConfigurationException, IOException {

        ArrayList<Program> channelEpisodes = new ArrayList<>();
        LocalDateTime timeNow = LocalDateTime.now();
        LocalDateTime timeFrom = getTimeFrom(timeNow);
        LocalDateTime timeTo = getTimeTo(timeNow);
        URL url = new URL("http://api.sr.se/api/v2/scheduledepisodes?"
                + "pagination=false&channelid=" + channelId + "&fromdate=" +
                timeFrom + "&todate=" + timeTo);
        Document doc = getDoc(url);

        if (doc != null) {
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();


            NodeList episodeList = root.getElementsByTagName("scheduledepisode");

            for (int i = 0; i < episodeList.getLength(); i++) {

                Element element = (Element) episodeList.item(i);

                int id = 0;
                String title = null;
                String description = null;
                String imageUrl = null;
                LocalDateTime startTime = null;
                LocalDateTime endTime = null;

                var elem1 = (Element)element.getElementsByTagName(
                        "program").item(0);
                var elem2 = element.getElementsByTagName(
                        "description").item(0);
                var elem3 = element.getElementsByTagName(
                        "imageurl").item(0);
                var elem4 = element.getElementsByTagName(
                        "title").item(0);
                var elem5 = element.getElementsByTagName(
                        "starttimeutc").item(0);
                var elem6 = element.getElementsByTagName(
                        "endtimeutc").item(0);

                if (elem1 != null){
                    id = Integer.parseInt(elem1.getAttribute("id"));

                }

                if (elem2 != null) {

                    description = elem2.getTextContent();
                }
                if (elem3 != null) {

                    imageUrl = elem3.getTextContent();
                }
                if (elem4 != null) {

                    title = elem4.getTextContent();
                }
                if (elem5 != null) {

                    startTime = formatDateTime(elem5.getTextContent());
                }
                if (elem6 != null) {

                    endTime = formatDateTime(elem6.getTextContent());
                }


                Program program = new Program(id, title, description, imageUrl,
                        startTime, endTime);

                channelEpisodes.add(program);

            }
        }
        return channelEpisodes;
    }

    /**
     * We want program information from now to 12 hours ahead of now.
     * now and going 12 hours forward.
     *
     * @param timeNow current time
     * @return returns the end time
     */
    private LocalDateTime getTimeTo(LocalDateTime timeNow) {

        LocalDateTime tmp = timeNow.plusHours(12);

        if(tmp.getDayOfMonth() == timeNow.getDayOfMonth()){

            return timeNow;
        }

        else{
            return tmp;
        }
    }

    /**
     * We want program information starting from 12 hours before now.
     *
     * @param timeNow current time
     * @return returns the starting time
     */
    private LocalDateTime getTimeFrom(LocalDateTime timeNow) {

        LocalDateTime tmp = timeNow.minusHours(12);

        if(tmp.getDayOfMonth() == timeNow.getDayOfMonth()){

            return timeNow;
        }

        else{
            return tmp;
        }
    }

    /**
     * This method formats a time string retrieved from the API.
     *
     * @param time the time string to be formatted
     * @return returns a formatted LocalDateTime
     */
    private LocalDateTime formatDateTime(String time) {
        LocalDateTime localDateTime = null;

        if (time.contains("Z")) {

            String dateTime = time.substring(0, time.length() - 1);

            localDateTime = LocalDateTime.parse(dateTime);

            //correction for Sveriges Radios API, they seem to return data
            //for utc+0, hence adding 1 hour to get correct tableau data
            localDateTime = localDateTime.plusHours(1);

        }

        return localDateTime;
    }

    /**
     * helper method which is responsible for opening up a stream to
     * Sveriges Radio API and parsing that data.
     *
     * @param url url to api
     * @return a document of parsed data from API
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private Document getDoc(URL url) throws ParserConfigurationException
            , SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(url.openStream());
    }

}
