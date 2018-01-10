package com.example.daniel.assignmenttwo;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Daniel on 2017-10-04.
 */

public class ServerConnection extends Service {
    private RegistrationActivity ra;
    private Receive receive;
    private Send send;
    private Connect connect;
    private Socket socket;
    private Protocol protocol;
    InputStream is;
    DataInputStream dis;
    OutputStream os;
    DataOutputStream dos;
    private InetAddress address;
    private int connectionPort = 7117;
    private String ip = "195.178.227.53";
    private ArrayList<JSONObject> list = new ArrayList<JSONObject>();
    public static boolean connectedToServer = false;
    public static boolean newMarker = false;
    String[] arr;
    String[] members;
    String[][] locationsMatrix;
    String groupName = "";
    private int nbrOfGroups = 0;
    Stack<String> stackMyID = new Stack<String>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connect = new Connect();
        connect.execute();
        return super.onStartCommand(intent, flags, startId);
    }


    private class Connect extends AsyncTask<Void, String, Void>{

        @Override
        protected Void doInBackground(Void[] objects) {
            try {
                address = InetAddress.getByName(ip);
                socket = new Socket(address, connectionPort);
                is = socket.getInputStream();
                dis = new DataInputStream(is);
                os = socket.getOutputStream();
                dos = new DataOutputStream(os);
                dos.flush();
                publishProgress("Connected to server");
                connectedToServer = true;
                receive = new Receive();
                receive.start();

            } catch (Exception e) { // SocketException, UnknownHostException
                e.printStackTrace();
                connectedToServer = false;
                publishProgress("Failed to connect to server");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String[] values) {
            if(values[0].equals("Connected to server")){
                Toast.makeText(getApplicationContext(), R.string.connectedToServer, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), R.string.failedConnection, Toast.LENGTH_SHORT).show();
            }
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        //client receives the Binder and can use it to directly access public
        // methods available in either the Binder implementation or the Service.
        public ServerConnection getService(){
            return ServerConnection.this;
        }
    }

    private class Receive extends Thread {
        public void run() {
            String message;
            try {
                while (receive != null) {
                   // int read = dis.read(); //TODO Remove later after verifying that something is received from server
                   // Log.i("Read from server", read+"");
                    message = dis.readUTF();
                    list.add(new JSONObject(message));
                    Log.i("RECEIVED", message);
                    if(list.get(list.size()-1).getString("type").equals("register")){
                        stackMyID.add(list.get(list.size()-1).getString("id"));
                        groupName = list.get(list.size()-1).getString("group");
                        Log.i("My group id: ", stackMyID.get(stackMyID.size()-1));
                    }
                    if(list.get(list.size()-1).getString("type").equals("groups")){
                        Log.i("TEST 1", ""+list.get(list.size()-1).getString("groups"));
                        JSONArray jsonArray = list.get(list.size()-1).getJSONArray("groups");
                        arr = new String[jsonArray.length()];
                        JSONObject obj;
                        for(int i = 0; i < jsonArray.length(); i++){
                            obj = new JSONObject(jsonArray.get(i).toString());
                            String g = obj.get("group").toString();
                            arr[i] = g;
                        }
                        for(int i = 0; i < arr.length; i++){
                            Log.i("Group ", arr[i]);
                        }
                        nbrOfGroups = arr.length;
                    }
                    if(list.get(list.size()-1).getString("type").equals("members")){
                        Log.i("TEST 2", ""+list.get(list.size()-1).getString("members"));
                        JSONArray jsonArray = list.get(list.size()-1).getJSONArray("members");
                        members = new String[jsonArray.length()];
                        JSONObject obj;
                        for(int i = 0; i < jsonArray.length(); i++){
                            obj = new JSONObject(jsonArray.get(i).toString());
                            String g = obj.get("member").toString();
                            members[i] = g;
                        }
                        for(int i = 0; i < members.length; i++){
                            Log.i("Member ", members[i]);
                        }

                    }
                    if(list.get(list.size()-1).getString("type").equals("locations")){
                        JSONArray jsonArray = list.get(list.size()-1).getJSONArray("location");
                        locationsMatrix = new String[jsonArray.length()][3];
                        JSONObject objName, objLongitude, objLatitude;
                        for(int i = 0; i < locationsMatrix.length; i++){
                            for(int k = 0; k < locationsMatrix[i].length; k++){
                                if(k == 0){
                                    objName = new JSONObject(jsonArray.get(i).toString());
                                   locationsMatrix[i][k] = objName.get("member").toString();
                                }else if(k == 1){
                                    objLongitude = new JSONObject(jsonArray.get(i).toString());
                                    locationsMatrix[i][k] = objLongitude.get("longitude").toString();
                                }else if(k == 2){
                                    objLatitude = new JSONObject(jsonArray.get(i).toString());
                                    locationsMatrix[i][k] = objLatitude.get("latitude").toString();
                                }
                            }
                        }
                        newMarker = true;
                        for(int i = 0; i < locationsMatrix.length; i++) {
                            for (int k = 0; k < locationsMatrix[i].length; k++) {
                                Log.i("Element ", locationsMatrix[i][k]);
                            }
                        }
                    }
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                receive = null;
            }
        }
    }

    public void sendMessage(String msg){
        new Send(msg).start();
    }

    private class Send extends Thread {
        String mMsg;

        public Send(String msg) {
            mMsg = msg;
        }

        public void run(){
            try {
                dos.writeUTF(mMsg);
                Log.i("SENT", mMsg);
            } catch (Exception e) { // IOException, ClassNotFoundException
                e.printStackTrace();
            }
        }
    }

    public String[] getListOfGroups(){
        return arr;
    }

    public int getNbrOfGroups(){
        return nbrOfGroups;
    }

    public Stack<String> getMyID(){
        return stackMyID;
    }

    public String[] getMembersInGroup(){
        return members;
    }

    public String getGroupName(){
        return groupName;
    }

    public String[][] getLocationsMatrix(){
        return locationsMatrix;
    }


}
