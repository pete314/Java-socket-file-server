package ie.gmit.sw.fileserver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Nagy
 */
public class ConnectionServer extends Thread {

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private User currentUser;
    private ContentManager cm;
    private StringBuffer responseBuffer;
    private final Socket socket;
    private final String clientSessionId;
    private final StringBuffer incomingCommand;
    private final ServerInitializer si;

    public ConnectionServer(Socket socket, String clientSessionId, ServerInitializer si) {
        this.socket = socket;
        this.clientSessionId = clientSessionId;
        this.si = si;
        responseBuffer = new StringBuffer();
        incomingCommand = new StringBuffer();

        log("New connection with client# " + clientSessionId + " at " + socket);
    }
   
    
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            log("Accepted Client : ID - " + clientSessionId + " : Address - "
                    + socket.getInetAddress().getHostName());

            responseBuffer.append("Connection successful");
            sendResponse();

            do {
                try {
                    incomingCommand.append((String) in.readObject());
                    log(incomingCommand.toString());
                    validateCommand();
                    sendResponse();
                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            } while (!incomingCommand.toString().equals("exit"));

        } catch (IOException e) {
            log("Error handling client# " + clientSessionId + ": " + e);
        } finally {
            try {
                socket.close();
                out.close();
                in.close();
            } catch (IOException e) {
                log("Couldn't close a socket.");
            }
            log("Connection with client# " + clientSessionId + " closed");
        }
    }

    /**
     * Send response from responseBuffer(stringBuilder) as String
     */
    private void sendResponse() {
        try {
            out.writeObject(responseBuffer.toString());
            out.flush();

            log("server> {RESPONSE}" + responseBuffer.toString());
            responseBuffer.delete(0, responseBuffer.length());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    
    /**
     * Send response to client in byte format
     * 
     * @param bytes The response data in byte format
     */
    private void sendResponse(byte[] bytes) {
        try {
            out.write(bytes, 0, bytes.length);
            out.flush();

            log("server> {RESPONSE} file content sent with lenght " + bytes.length);
            responseBuffer.delete(0, responseBuffer.length());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    
    /**
     * Request Command validation and method resolver
     * 
     * @todo    Break and re-factor this to external class(es) 
     * @param   request The request body sent to the 
     * @return  true if command call is valid and method call is success 
     */
    private boolean validateCommand(String... request) {
        List<String> commandBits = Arrays.asList(request.length > 0 ? request[0].split(" ") : incomingCommand.toString().split(" "));
        incomingCommand.delete(0, incomingCommand.length());

        switch (commandBits.get(0)) {
            case "AUTH":
                if (commandBits.contains("-u") && commandBits.contains("-p")) {
                    User tmpUser = si.users.get(commandBits.get(commandBits.indexOf("-u") + 1));
                    if (tmpUser != null && Arrays.equals(tmpUser.getPassword(), commandBits.get(commandBits.indexOf("-p") + 1).toCharArray())) {
                        responseBuffer.append("SESSION_ID ").append(this.clientSessionId);
                        currentUser = tmpUser;
                        return true;
                    } else {
                        responseBuffer.append("Wrong username or password");
                        return false;
                    }
                }
                responseBuffer.append("Not enough information for Authentication");
                return false;
            case "cd":
            case "CD":
                cm = new ContentManager(currentUser);
                if (commandBits.size() == 1) {
                    if (cm.changeDirectory("", true)) {
                        responseBuffer.append("");
                        currentUser.setCurrentDirectory("");
                    }
                } else if (commandBits.get(1).charAt(0) == '.') {
                    if (cm.changeDirectory(commandBits.get(1), false)) {
                        responseBuffer.append(commandBits.get(1));
                        currentUser.setCurrentDirectory(commandBits.get(1));
                    }
                } else {
                    if (cm.changeDirectory(commandBits.get(1), true)) {
                        responseBuffer.append(commandBits.get(1));
                        currentUser.setCurrentDirectory(commandBits.get(1));
                    }
                }
                return true;
            case "ls":
                if (commandBits.size() < 2) {
                    return false;
                }
                cm = new ContentManager(currentUser);
                responseBuffer = commandBits.size() == 2 ? cm.getFolderContents(commandBits.get(1)) : cm.getFolderContents(commandBits.get(1), commandBits.get(2));
                return true;
            case "mkdir":
                if (commandBits.size() < 2) {
                    return false;
                }
                cm = new ContentManager(currentUser);
                if (cm.createDirectory(commandBits.get(1), commandBits.get(1).contains("."))) {
                    responseBuffer.append(commandBits.get(1)).append(" created");
                    return true;
                }
                responseBuffer.append("Couldn't create ").append(commandBits.get(1));
                return false;
            case "rmdir":
                if (commandBits.size() < 2) {
                    return false;
                }
                cm = new ContentManager(currentUser);
                if (cm.removeDirectory(commandBits.get(1), commandBits.get(1).contains("."))) {
                    responseBuffer.append(commandBits.get(1)).append(" deleted");
                    return true;
                }
                responseBuffer.append("Couldn't delete ").append(commandBits.get(1));
                return false;
            case "put":
                //validate the request param count and file existance
                if (commandBits.size() < 3) {
                    return false;
                }
                int parts = Integer.parseInt(commandBits.get(2));//@todo: should write a try parse int 

                cm = new ContentManager(currentUser);
                File file = cm.getFilePointer(commandBits.get(1), true);
                boolean result = handleFileUpload(file, parts);
                responseBuffer = new StringBuffer("File Trasfer success");

                return result;

            case "get":
                if (commandBits.size() < 2) {
                    return false;
                }
                cm = new ContentManager(currentUser);
                File filePointer = cm.getFilePointer(commandBits.get(1), true);
                return handleFileDownload(filePointer);
            default:
                responseBuffer.append("Not valid command");
        }

        return false;
    }
    
    /**
     * Handle download request
     * 
     * @param file The file to download data from
     * @return 
     */
    private boolean handleFileDownload(File file) {
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {

                byte[] data = new byte[4096];
                int chunks = (int) (file.length() / 4096);
                responseBuffer.append(chunks);
                sendResponse();
                int read;
                while (true) {
                    read = fis.read(data);
                    if(read < 0)
                        break;
                    sendResponse(data);
                }

            } catch (IOException ex) {
                Logger.getLogger(ConnectionServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Handle the file upload process 
     * 
     * @param file The file pointer to save data into
     * @param chunks The number of arts the data will arrive
     * @return 
     */
    private boolean handleFileUpload(File file, int chunks) {
        responseBuffer.append("OK");
        sendResponse();
        int partCnt = 0;
        boolean run = true;
        try (BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(file))) {
            do {
                byte[] byteBuffer = new byte[4096];
                int read = in.read(byteBuffer, 0, byteBuffer.length);
                String eofParse = new String(byteBuffer);//@todo: unnecessary overhead
                if (read < 0 || eofParse.contains("EOF")) {
                    run = false;
                } else {
                    fileWriter.write(byteBuffer, 0, read);
                    fileWriter.flush();
                    partCnt++;
                    responseBuffer.append("OK - part ").append(partCnt);
                    sendResponse();
                }
            } while (run);

        } catch (IOException ex) {
            Logger.getLogger(ConnectionServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;//@todo: no point
    }

    /**
     * Convenient method to print a new line
     * @param message The log body 
     */
    private void log(String message) {
        System.out.println(message);
    }
}
