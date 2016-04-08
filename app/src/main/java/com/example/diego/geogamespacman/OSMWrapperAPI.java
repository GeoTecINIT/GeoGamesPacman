package com.example.diego.geogamespacman;

import android.os.StrictMode;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Thomas on 2/19/2016.
 */
public class OSMWrapperAPI {


    private static final String OVERPASS_API = "http://www.overpass-api.de/api/interpreter";
//    private static final String OPENSTREETMAP_API_06 = "http://www.openstreetmap.org/api/0.6/";

//    public static Node getNode(String nodeId) throws IOException, ParserConfigurationException, SAXException {
//        String string = "http://www.openstreetmap.org/api/0.6/node/" + nodeId;
//        URL osm = new URL(string);
//        HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
//
//        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
//        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
//        Document document = docBuilder.parse(connection.getInputStream());
//        List<Node> nodes = getNodes(document);
//        if (!nodes.isEmpty()) {
//            return nodes.iterator().next();
//        }
//        return null;
//    }

//    /**
//     *
//     * @param lon the longitude
//     * @param lat the latitude
//     * @param vicinityRange bounding box in this range
//     * @return the xml document containing the queries nodes
//     * @throws IOException
//     * @throws SAXException
//     * @throws ParserConfigurationException
//     */
//    @SuppressWarnings("nls")
//    private static Document getXML(double lon, double lat, double vicinityRange) throws IOException, SAXException,
//            ParserConfigurationException {
//
//        DecimalFormat format = new DecimalFormat("##0.0000000", DecimalFormatSymbols.getInstance(Locale.ENGLISH)); //$NON-NLS-1$
//        String left = format.format(lat - vicinityRange);
//        String bottom = format.format(lon - vicinityRange);
//        String right = format.format(lat + vicinityRange);
//        String top = format.format(lon + vicinityRange);
//
//        String string = OPENSTREETMAP_API_06 + "map?bbox=" + left + "," + bottom + "," + right + ","
//                + top;
//        URL osm = new URL(string);
//        HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
//
//        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
//        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
//        return docBuilder.parse(connection.getInputStream());
//    }
//
//    public static Document getXMLFile(String location) throws ParserConfigurationException, SAXException, IOException {
//        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
//        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
//        return docBuilder.parse(location);
//    }

    /**
     *
     * @param xmlDocument
     * @return a list of openseamap nodes extracted from xml
     */
    @SuppressWarnings("nls")
    public static List<Node> getNodes(Document xmlDocument) {
        List<Node> nodes = new ArrayList<Node>();

        // Document xml = getXML(8.32, 49.001);
        org.w3c.dom.Node osmRoot = xmlDocument.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            org.w3c.dom.Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("node")) {
                NamedNodeMap attributes = item.getAttributes();
                NodeList tagXMLNodes = item.getChildNodes();
                Map<String, String> tags = new HashMap<String, String>();
                for (int j = 1; j < tagXMLNodes.getLength(); j++) {
                    org.w3c.dom.Node tagItem = tagXMLNodes.item(j);
                    NamedNodeMap tagAttributes = tagItem.getAttributes();
                    if (tagAttributes != null) {
                        tags.put(tagAttributes.getNamedItem("k").getNodeValue(), tagAttributes.getNamedItem("v")
                                .getNodeValue());
                    }
                }
                org.w3c.dom.Node namedItemID = attributes.getNamedItem("id");
                org.w3c.dom.Node namedItemLat = attributes.getNamedItem("lat");
                org.w3c.dom.Node namedItemLon = attributes.getNamedItem("lon");
                org.w3c.dom.Node namedItemVersion = attributes.getNamedItem("version");

                String id = namedItemID.getNodeValue();
                String latitude = namedItemLat.getNodeValue();
                String longitude = namedItemLon.getNodeValue();
                String version = "0";
                if (namedItemVersion != null) {
                    version = namedItemVersion.getNodeValue();
                }

                nodes.add(new Node(id, latitude, longitude, version, tags));
            }

        }
        return nodes;
    }

//    public static List<Node> getOSMNodesInVicinity(double lat, double lon, double vicinityRange) throws IOException,
//            SAXException, ParserConfigurationException {
//        return OSMWrapperAPI.getNodes(getXML(lon, lat, vicinityRange));
//    }

    /**
     *
     * @param query the overpass query
     * @return the nodes in the formulated query
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document getNodesViaOverpass(String query) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String hostname = OVERPASS_API;
            String queryString = query;
            URL osm = new URL(hostname);
            HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
            printout.writeBytes("data=" + URLEncoder.encode(queryString, "utf-8"));
            printout.flush();
            printout.close();
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            return docBuilder.parse(connection.getInputStream());
        } catch (Exception e) {
            Log.e("OSMWrapper API", "Failed to retrieve OSM Nodes. Check query.");
        }
        return null;
    }



}

