package fr.ladn.carsharingclub.ing1.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import fr.ladn.carsharingclub.ing1.utils.CRUD;
import org.apache.log4j.Logger;

import fr.ladn.carsharingclub.ing1.db.ConnectionPool;
import fr.ladn.carsharingclub.ing1.db.PartDAO;
import fr.ladn.carsharingclub.ing1.db.OperationDAO;
import fr.ladn.carsharingclub.ing1.model.Part;
import fr.ladn.carsharingclub.ing1.model.Reparation;
import fr.ladn.carsharingclub.ing1.utils.Container;
import fr.ladn.carsharingclub.ing1.utils.XML;

/**
 * The ConnectionThread class.
 * This class provides the listening part of the server.
 * It manages the connection attempts to the server.
 *
 * @see Server
 */
public class ConnectionThread extends Thread {

    /** The logger. */
    private final static Logger logger = Logger.getLogger(ConnectionThread.class.getName());

    /** The client socket. */
    private Socket clientSocket;

    /** The Data Access Object for parts */
    private PartDAO partDAO;
    private OperationDAO repDAO;

    /**
     * Gets the socket initialized by the server.
     *
     * @param clientSocket the socket provided by the server
     * @see Server
     */
    ConnectionThread(Socket clientSocket, ConnectionPool connectionPool) {
        logger.info("Initializing connection.");
        this.clientSocket = clientSocket;
        this.partDAO = new PartDAO(connectionPool);
        this.repDAO = new OperationDAO(connectionPool);
    }

    /**
     * Listens for connection attempts to the server.
     */
    public void run() {
        try {
            logger.info("Client " + clientSocket.getInetAddress() + " connected.");
            getData();
            clientSocket.close();
        } catch (IOException e) {
            logger.error("Server error: " + e.getMessage());
        }
    }

    /**
     * Turns the XML sent by the client into a 
     * @see Part
     */
    private void getData() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String str = in.readLine();
            logger.info("Received " + str);
            Container container = XML.parse(str);
            logger.info("Operations: " + container.getCRUD());
            switch (container.getCRUD()) {
                case PING:
                    logger.info("The server was successfully pinged from the client " + clientSocket.getInetAddress() + ".");
                    break;
                case CREATE:
                    logger.info("Attempt to create part in database.");
                    partDAO.create((Part) container.getObject());
                    break;
                case READ:
                    logger.info("Attempt to read part from database.");
                    int id = ((Part) container.getObject()).getId();
                    if (id > 0) sendData(new Container<>(CRUD.PING, partDAO.read(id)));
                    else sendData(new Container<>(CRUD.PING, partDAO.readAll()));
                    break;
                case UPDATE:
                    logger.info("Attempt to update part in database.");
                    partDAO.update((Part) container.getObject());
                    break;
                case DELETE:
                    logger.info("Attempt to delete part in database.");
                    partDAO.delete((Part) container.getObject());
                    break;
                case READ_OPERATION_S:
                    logger.info("Attempt to read operation in database.");
                    ArrayList<String> statuts = ((Reparation)container.getObject()).getList();
                    //int iden = ((Reparation) container.getObject()).getId_reparation();
                   /* if (iden < 0)*/ sendData(new Container<>(CRUD.PING, repDAO.displayVehicleByStatus(statuts)));
                    break;
                case READ_PARTS_FAILURE:
                    logger.info("Attenmt to read assoc_reparation_pannes.");
                    int idPart = ((Part) container.getObject()).getId();
                    sendData(new Container<>(CRUD.PING, partDAO.failurePartsReadAll(idPart)));
                    break;
                default:
                    logger.info("Sorry. This operation is not covered yet.");
            }
            logger.info("Finished operation.");
        } catch (IOException e) {
            logger.error("Failed to get data from client: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to execute database operation: " + e.getMessage());
        }
    }

    /**
     * Sends object data to a client via the socket connection.
     *
     * @param container the Container object to be sent.
     * @see XML
     */
    private void sendData(Container container) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(XML.stringify(container));
            logger.info("Data has been sent back to the client.");
            logger.info(XML.stringify(container));
        } catch (IOException e) {
            logger.error("Failed to send data to client: " + e.getMessage());
        }
    }
}