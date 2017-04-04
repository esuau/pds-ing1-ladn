package fr.ladn.carsharingclub.ing1.sockets;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;
import java.io.*;
import java.net.*;

import fr.ladn.carsharingclub.ing1.model.Part;
import fr.ladn.carsharingclub.ing1.xml.ReadXMLFile;
import fr.ladn.carsharingclub.ing1.xml.WriteXMLFile;

/**
 * The class Client.
 */
public class Client {
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Client default constructor.
     * When starting, the client uses the file <tt>configClient.properties</tt> stored in src/main/resources/.
     *
     * @param p The part to be sent to the server.
     */
    public Client(Part p) {
        Properties properties = new Properties();

        try {
            FileInputStream input = new FileInputStream("configClient.properties");
            properties.load(input);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String serverAdress = properties.getProperty("serverAdress");
        int serverPort = Integer.parseInt(properties.getProperty("serverPort"));

        try {
            Socket socketClient = new Socket(serverAdress, serverPort);
            System.out.println("Connected to server : " + serverAdress + " on port : " + serverPort);
            in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            out = new PrintWriter(socketClient.getOutputStream(), true);
            sendData(p);
            socketClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts the text (XML) data sent by the server into an Part object.
     *
     * @return a Part object.
     * @see ReadXMLFile
     */
    public Part getData() {
        try {
            return ReadXMLFile.parserXML(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts an object into a XML formatted string and sends it to the server.
     *
     * @param p The part object to be turned into XML.
     * @see WriteXMLFile
     */
    private void sendData(Part p) {
        System.out.println(WriteXMLFile.factoryXML(p));
        out.println(WriteXMLFile.factoryXML(p));
    }
}
