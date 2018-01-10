package com.example.daniel.assignmenttwo;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Stack;

public class RegistrationActivity extends AppCompatActivity {
    String[] s;
    String[] members;
    ListView lv;
    private LocationNotifier locationNotifier;
    private double latitude;
    private double longitude;
    Button btnGroupInfo;
    Button btnCreate;
    EditText editTextGroupName;
    String groupName = "";
    public static String currentID = "";
    public static boolean ableToUnregister = false;
    public static boolean recieveLocationMatrix = false;
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        res = getResources();
        btnGroupInfo = (Button) findViewById(R.id.btnGroupInfo);
        btnGroupInfo.setClickable(true);
        btnCreate = (Button) findViewById(R.id.btnCreate);
        editTextGroupName = (EditText) findViewById(R.id.editTextGroupName);
        Bundle bundle = this.getIntent().getExtras();
        s = bundle.getStringArray("listOfGroups");
        latitude = bundle.getDouble("latitude");
        longitude = bundle.getDouble("longitude");
        locationNotifier = new LocationNotifier(30000);
        if(s != null){
            lv = (ListView) findViewById(R.id.listViewGroups);
            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, s));
            lv.setOnItemClickListener(new ListViewerListener());
        }else{
            StringBuffer buffer = new StringBuffer();
            buffer.append(res.getString(R.string.tryRefresh));
            showMessage(res.getString(R.string.error), buffer.toString());
            Toast.makeText(getApplicationContext(), R.string.stringIsNull, Toast.LENGTH_SHORT).show();
        }
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editTextGroupName.getText().toString();
                if(text.length() > 0){
                    MainActivity.sc.sendMessage(MainActivity.mProtocol.registerToGroup(text));
                    Toast.makeText(getApplicationContext(), res.getString(R.string.theGroup) + " '" + text + "' " + res.getString(R.string.haBeenCreated), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), R.string.refreshPage, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.fillField, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerButtonListener(final boolean b) {
        btnGroupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                res = getResources();

                if(b == true){
                    StringBuffer buffer = new StringBuffer();
                    if(members != null){
                        buffer.append(res.getString(R.string.nameOfGroup) + groupName + "\n");
                        for(int i = 0; i < members.length; i++){
                            buffer.append(res.getString(R.string.memberNbr) + (i+1) + ": " + members[i] + "\n");
                        }
                        showMessage(res.getString(R.string.groupInfo), buffer.toString());
                    }else{
                        Toast.makeText(getApplicationContext(), R.string.memberArrayNull, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    showMessage(res.getString(R.string.error), res.getString(R.string.youAreNotRegistered));
                }

            }
        });
    }

    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    private class ListViewerListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            for(int i = 0; i < s.length; i++){
                if(pos == i){
                    Stack<String> stack = MainActivity.sc.getMyID();
                    if(stack.size()>=2){
                        res = getResources();
                        currentID = stack.get(stack.size()-1);
                        String prevID = stack.get(stack.size()-2);
                        MainActivity.sc.sendMessage(MainActivity.mProtocol.unregisterFromGroup(prevID));
                        MainActivity.sc.sendMessage(MainActivity.mProtocol.registerToGroup(""+s[i]));
                        ableToUnregister = true;
                        groupName = MainActivity.sc.getGroupName();
                        MainActivity.sc.sendMessage(MainActivity.mProtocol.getGroupMembers(groupName));
                        members = MainActivity.sc.getMembersInGroup();
                        registerButtonListener(true);
                        Toast.makeText(getApplicationContext(), res.getString(R.string.youHaveRegisteredTo) + " " + s[i], Toast.LENGTH_SHORT).show();
                        locationNotifier.setNotification(new Runnable() {
                            @Override
                            public void run() {
                                RegistrationActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.sc.sendMessage(MainActivity.mProtocol.sendMyPosition(currentID, ""+longitude, ""+latitude));
                                        Toast.makeText(getApplicationContext(), res.getString(R.string.latlon) + latitude + "," + longitude, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        locationNotifier.startNotification();
                        recieveLocationMatrix = true;
                    }else{
                        res = getResources();
                        MainActivity.sc.sendMessage(MainActivity.mProtocol.registerToGroup(""+s[i]));
                        ableToUnregister = true;
                        groupName = MainActivity.sc.getGroupName();
                        MainActivity.sc.sendMessage(MainActivity.mProtocol.getGroupMembers(groupName));
                        members = MainActivity.sc.getMembersInGroup();
                        registerButtonListener(true);
                        if(stack.size() == 1){
                            currentID = stack.get(stack.size()-1);
                            locationNotifier.setNotification(new Runnable() {
                                @Override
                                public void run() {
                                    RegistrationActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.sc.sendMessage(MainActivity.mProtocol.sendMyPosition(currentID, ""+longitude, ""+latitude));
                                            Toast.makeText(getApplicationContext(), res.getString(R.string.latlon) + latitude + "," + longitude, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            locationNotifier.startNotification();
                            recieveLocationMatrix = true;
                        }
                        Toast.makeText(getApplicationContext(), res.getString(R.string.youHaveRegisteredTo) + " " +s[i], Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    }
}
