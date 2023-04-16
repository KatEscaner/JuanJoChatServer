package service;

import dao.GroupDAO;
import dao.UserDAO;
import model.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;

public class ClientHandler implements Runnable{
    Socket service;
    Connection con = null;
    boolean verified = false;
    UserCredentials userCredentials = null;
    final UserDAO userDAO = new UserDAO();
    final GroupDAO groupDAO = new GroupDAO();
    User user = null;
    boolean exit = false;

    // get a socket since the socketServer
    public ClientHandler(Socket s){
        service = s;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream objIn = new ObjectInputStream(service.getInputStream());
                ObjectOutputStream objOut = new ObjectOutputStream(service.getOutputStream());
                DataOutputStream dataOut = new DataOutputStream(service.getOutputStream())
        ) {
            while(!verified){
                userCredentials = (UserCredentials) objIn.readObject();
                try{
                    connect();
                    // Check if the client user exists
                    if (userDAO.checkUser(userCredentials, con)) {
                        verified = true;
                        // Get user information
                        user = userDAO.getUser(userCredentials, con);
                        // Check if user is already connected
                        if(Server.sessionManager.isUserConnected(user.getId())) {
                            //Disconnect old session
                            Server.sessionManager.closeSession(user.getId());
                        }
                    }
                    con.close();
                    dataOut.writeBoolean(verified);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            //Send groups which user is member
            try {
                connect();
                sendGroups(objOut, con);
                con.close();
            } catch (Exception e){
                e.printStackTrace();
            }

            //Send file with the messages of the user
            MessageSender.sendMessagesFromFile(user.getId(), objOut);

            //Create a Thread which look if a message is in the queue
            MessageSender messageSender = new MessageSender(objOut, user);
            messageSender.start();

            // Add user to connected users list
            Server.sessionManager.addSession(new Session(user.getId(), objOut));

            while(!service.isClosed()){
                try{
                    Message message = (Message) objIn.readObject();
                    if(message != null){
                        message.setSender(user.getId());
                        connect();
                        if(groupDAO.checkIfUserIsMember(message, con)){
                            Server.messageQueue.addMessage(message, groupDAO.getMembersOfAGroup(message.getGroup(), con));
                            con.close();
                        }
                    }
                } catch (IOException | ClassNotFoundException e){
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                service.close();
            } catch (Exception e){}
        }
    }

    // Get a connection since Service.ConnectionPool
    public void connect(){
        try{
            con = ConnectionPool.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void sendGroups(ObjectOutputStream objOut, Connection con){
        try {
            userDAO.getUserGroupsID(user.getId(), con)
                    .forEach(e -> {
                        try {
                            objOut.writeObject(groupDAO.getGroup(e, con));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}