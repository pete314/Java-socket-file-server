package ie.gmit.sw.fileserverclient;

/**
 *
 * @author Peter Nagy
 */
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class ClientConnectionHandler {

    private BufferedReader clid;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket requestSocket;
    private volatile boolean listen = true;
    private String sessionId;
    private final StringBuffer requestBuffer;
    private final StringBuffer responseBuffer;
    private final AbstractPrintHelper printHelper;

    public ClientConnectionHandler() {
        requestBuffer = new StringBuffer();
        responseBuffer = new StringBuffer();
        clid = new BufferedReader(new InputStreamReader(System.in));
        printHelper = new AbstractPrintHelper();
        connectToServer();
    }

    /**
     * Maintain the current connection to server and handle requests/responses
     * 
     * @todo has to be broken up into connector, request, validation, resolver 
     */
    private void connectToServer() {
        try {
            requestSocket = new Socket(PropertieExchange.serverAddress, PropertieExchange.serverPort);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            do {
                try {

                    if (sessionId == null) {
                        String response = (String) in.readObject();
                        String[] responseBits = response.split(" ");
                        if (responseBits.length == 2 && responseBits[0].equals("SESSION_ID") && responseBits[1].length() > 42) {
                            sessionId = responseBits[1];
                            requestBuffer.append("welcome");
                            sendRequest();
                        } else {
                            getAuthCredentials();
                            sendRequest();
                        }
                        continue;
                    }

                    printHelper.printMessage((String) in.readObject());
                    printHelper.printMessage("Please enter a command to execute");
                    printHelper.printInLineMessage(PropertieExchange.CLI_APP_ID);

                    requestBuffer.append(clid.readLine());
                    switch (requestBuffer.toString()) {
                        case "exit":
                            listen = false;
                            break;
                        case "put":
                            uploadFile();
                            break;
                        case "get":
                            requestBuffer.delete(0, requestBuffer.length());
                            downloadFile();
                            break;
                        case "help":
                            printHelper.printHelpMessage();
                            break;
                        default:
                            sendRequest();
                    }
                } catch (ClassNotFoundException classNot) {
                    printHelper.logMessage("data received in unknown format");
                }
            } while (listen);
        } catch (IOException ex) {
            Logger.getLogger(ClientConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                requestSocket.close();
                out.close();
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        printHelper.printMessage("bye now");
    }

    /**
     * Send request from the current requestBuffer
     */
    private void sendRequest() {
        try {
            out.writeObject(requestBuffer.toString());
            out.flush();

            requestBuffer.delete(0, requestBuffer.length());
        } catch (IOException ex) {
            printHelper.logMessage(sessionId);
        }
    }

    /**
     * Send Byte based data to server
     * 
     * @param content data in Byte[] format
     * @param contLen The length of the current used data
     */
    private void sendRequest(byte[] content, int contLen) {
        try {
            out.write(content, 0, contLen);
            out.flush();

            requestBuffer.delete(0, requestBuffer.length());
        } catch (IOException ex) {
            printHelper.logMessage(sessionId);
        }
    }

    /**
     * Authentication handler method
     * @todo re-factor this into command validation external class
     */
    private void getAuthCredentials() {
        requestBuffer.delete(0, requestBuffer.length());

        String username = null;
        char[] password = null;//just to avoid string pool based hack
        do {
            try {
                printHelper.printInLineMessage("Please enter your username: ");
                username = clid.readLine();

                printHelper.printInLineMessage("Please enter your password: ");
                password = clid.readLine().toCharArray();
            } catch (IOException ex) {
                printHelper.logMessage(ex.toString());
            }
        } while (username == null && password == null);

        //remplace this with bcrypt
        String hashPass = new String(password);
        requestBuffer.append("AUTH -u ").append(username)
                .append(" -p ").append(hashPass);
    }
    
    /**
     * Convenient method to print response messages
     * @param responseBody 
     */
    private void printResponse(String responseBody) {
        printHelper.printMessage(responseBody);
    }

    /**
     * @todo Move this into external class from here
     */
    private void uploadFile() {
        File file = fileSelector();

        if (file.exists() && file.isFile()) {
            transferFile(file);
        } else {
            printHelper.printMessage("Selected file is does not exist or is not file! Please try again.");
        }
    }

    /**
     * Upload transfer handler
     * 
     * @param f File pointer to the uploaded file data
     * @return 
     */
    private boolean transferFile(File f) {
        try (InputStream fis = new FileInputStream(f);) {
            int partCnt = 0;
            int parts = (int) (f.length() / 4096);
            int read;
            byte[] cBuffer = new byte[4096];

            requestBuffer.append(" ").append(f.getName().replaceAll(" ", "_")).append(" ").append(parts);
            sendRequest();

            //@todo: refactor this into responseParser
            responseBuffer.append((String) in.readObject());
            if (!responseBuffer.toString().equals("OK")) {
                printHelper.printMessage("File transfer error, aborted transfer! please try again later.");
                return false;
            }

            while ((read = fis.read(cBuffer)) > 0) {
                //@todo: pipe into base64
                sendRequest(cBuffer, read);

                responseBuffer.delete(0, responseBuffer.length());//empty response buffer
                responseBuffer.append((String) in.readObject());
                if (!responseBuffer.toString().contains("OK")) {
                    printHelper.printMessage("File transfer error, aborted transfer! please try again later.");
                    break;
                }
                printHelper.printInLineReplaceMessage("Part " + ++partCnt + " of " + parts + " transfered");
            }

            requestBuffer.delete(0, requestBuffer.length());
            requestBuffer.append("EOF");
            sendRequest();

            printHelper.printInLineReplaceMessage("SUCCESSFUL UPLOAD - Part " + partCnt + " of " + parts + " transfered ");

            return true;
        } catch (FileNotFoundException | ClassNotFoundException ex) {
            Logger.getLogger(ClientConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {

        }

        return false;
    }

    /**
     * Selector handles file selection with gui if possible
     * 
     * @return File the selected file
     */
    private File fileSelector() {
        File f = new File("");
        if (GraphicsEnvironment.isHeadless()) {
            try {
                printHelper.printInLineMessage("Please enter valid filename (with correct path): ");
                String fileName = clid.readLine();
                f = new File(fileName);
            } catch (IOException ex) {
                Logger.getLogger(ClientConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            printHelper.printInLineMessage("Please choose a file with the pop up dialog");
            JFrame frame = new JFrame();
            JFileChooser fc = new JFileChooser();

            frame.toFront();
            frame.repaint();

            if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(frame)) {
                frame.setVisible(false);
                f = fc.getSelectedFile();

            } else {
            }
        }

        return f;
    }
    
    private void downloadFile() {
        try {
            printHelper.printInLineMessage("Please give the file name: ");
            String fileName = clid.readLine();
            requestBuffer.append("get ").append(fileName);
            sendRequest();
            
            int chunks = Integer.parseInt((String) in.readObject());
            
            byte[] contentBuffer = new byte[4096];

            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                int read;
                while (true) {
                    read = in.read(contentBuffer, 0, contentBuffer.length);
                    if(read == -1)
                        break;
                    fos.write(contentBuffer, 0, read);
                }
            }
            
            //could be removed
            requestBuffer.append("ls -al");
        } catch (IOException ex) {
            Logger.getLogger(ClientConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
