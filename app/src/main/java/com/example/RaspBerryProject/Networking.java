package com.example.RaspBerryProject;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;

public final class Networking extends AsyncTask {

    public Networking() {}

    //Runs runCommand in the background
    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            Log.d("runCommand", String.valueOf(runCommand()));
        }catch (JSchException ex){
            ex.printStackTrace();
        }
        return null;
    }

    //We SSH into our raspberry and run the appropriate python script to start the sensor
    private List<String> runCommand() throws JSchException {
        //we need a sessions
        Session session = getExec();
        session.connect();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        try{
            channel.setCommand("sudo python3 dataLogger.py");
            channel.setInputStream(null);
            InputStream output = channel.getInputStream();
            channel.connect();

            String result = output.toString();
            return asList(result.split("\n"));

        } catch (JSchException | IOException e) {
            closeConnection(channel, session);
            throw new RuntimeException(e);
        //we make sure the sessions and channel are closed
        } finally {
            closeConnection(channel, session);
            Log.d("Session", "Ended");
        }
    }

    //This is where we configure our ssh session
    private static Session getExec() throws JSchException {
        Session session = new JSch().getSession("pi", "192.168.20.40", 22);
        session.setPassword("");
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.setConfig("StrictHostKeyChecking", "no"); // disable check for RSA key
        return session;
    }

    //We disconnect the channel and session to release resources
    private static void closeConnection(ChannelExec channel, Session session) {
        try {
            channel.disconnect();
        } catch (Exception ignored) {
        }
        session.disconnect();
    }
}
