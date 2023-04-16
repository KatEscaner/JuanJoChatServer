package service;

import model.Message;
import model.User;
import server.Server;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class MessageSender extends Thread{
    ObjectOutputStream objOut;
    User user;
    Message message;



    public MessageSender(ObjectOutputStream objOut, User user){
        this.objOut = objOut;
        this.user = user;
    }

    @Override
    public void run() {
        while(true) {
            try {
                // Check if user have a message
                if(Server.messageQueue.checkIfHaveMessages(user.getId())){
                    // Get the message
                    message = Server.messageQueue.getNextMessage(user.getId());
                    // Send the message
                    objOut.writeObject(message);
                    // Save the message in message file
                    if(message.getId() > 0)
                        addMessageToListAndWriteToFile(message, user.getId());
                    // Delete message from the queue
                    Server.messageQueue.removeMessage(user.getId());
                }
            } catch (SocketException se){
              break;
            } catch (Exception e) {
                e.printStackTrace();
            }

            try{
                Thread.sleep(1000);
            } catch (Exception e){}
        }
    }

    //Return file's message list
    public static List<Message> readMessagesFromList(Long userID) {
        List<Message> messages = new ArrayList<>();
        final String MESSAGE_STORAGE_PATH = "src/messageStorage/";
        File file = new File(MESSAGE_STORAGE_PATH + userID + ".txt");
        try{
            if(!file.exists()){
                file.createNewFile();
                try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
                    oos.writeObject(new ArrayList<Message>());
                }
            }
        } catch (Exception e){}

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            messages = (List<Message>) ois.readObject();
        } catch (EOFException e) {
            // End of file reached
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return messages;
    }

    //Read message list in the file, add message to list  and finally write the new message list in the file
    public void addMessageToListAndWriteToFile(Message message, Long userId) {
        List<Message> messages = readMessagesFromList(userId);
        messages.add(message);
        final String MESSAGE_STORAGE_PATH = "src/messageStorage/";
        File file = new File(MESSAGE_STORAGE_PATH + userId + ".txt");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Send all message of the file's message list
    public static void sendMessagesFromFile(Long userID, ObjectOutputStream objOut){
        List<Message> messages = readMessagesFromList(userID);
        try {
            for (Message message : messages) {
                objOut.writeObject(message);
            }
        } catch (Exception e){}
    }
}