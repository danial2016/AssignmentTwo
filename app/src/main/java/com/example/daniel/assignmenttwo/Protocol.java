package com.example.daniel.assignmenttwo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Daniel on 2017-10-06.
 */

public class Protocol {
    JSONObject obj;

    public String unregisterFromGroup(String groupID) {
        obj = new JSONObject();
        try {
            obj.put("type", "unregister");
            obj.put("id", groupID);
        } catch (JSONException e) { }
        return obj.toString();
    }

    public String getCurrentGroups() {
        obj = new JSONObject();
        try {
            obj.put("type", "groups");
        } catch (JSONException e) { }
        return obj.toString();
    }

    public String getGroupMembers(String groupName) {
        obj = new JSONObject();
        try {
            obj.put("type", "members");
            obj.put("group", groupName);
        } catch (JSONException e) { }
        return obj.toString();
    }

    public String sendMyPosition(String id, String longitude, String latitude){
        obj = new JSONObject();
        try {
            obj.put("type", "location");
            obj.put("id", id);
            obj.put("longitude", longitude);
            obj.put("latitude", latitude);
        } catch (JSONException e) { }
        return obj.toString();
    }


    public String registerToGroup(String groupName) {
        obj = new JSONObject();
        try {
            obj.put("type", "register");
            obj.put("group", groupName);
            obj.put("member", "Daniel");
        } catch (JSONException e) { }
        return obj.toString();
    }
}
