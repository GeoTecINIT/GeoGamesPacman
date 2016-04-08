package com.example.diego.geogamespacman;

import java.util.Map;

/**
 * Created by Thomas on 2/20/2016.
 */
public class Node {

    private String osmId;
    private float lat;
    private float lon;
    private final Map<String, String> tags;
    private String version;

    public int getGraphicID() {
        return graphicID;
    }

    public void setGraphicID(int graphicID) {
        this.graphicID = graphicID;
    }

    private int graphicID;

    public Node(String id, String lat, String lon, String version, Map<String, String> tags) {
        this.osmId = id;
        this.lat = Float.parseFloat(lat);
        this.lon = Float.parseFloat(lon);
        this.tags = tags;
        this.version = version;

    }

    public String getOsmId() {
        return osmId;
    }

    public float getLat() { return lat; }

    public float getLon() {
        return lon;
    }

    public String getTagsText (){
        return this.tags.toString();
    }
}
