package ie.gmit.sw.fileserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.encoders.Base64;
import sun.security.provider.SecureRandom;

/**
 *
 * @author Peter Nagy
 * 
 */

public class ConnectionDispatcher {
    
    public static void main(String[] args) throws Exception {
        ServerInitializer si = new ServerInitializer();
        if(si.createUsers()) createSocketPool(si);
        else closeServer();
    }
    
    private static void createSocketPool(ServerInitializer si){
        System.out.println("The dispatcher is running.");
        try (ServerSocket soketConnListener = new ServerSocket(2004)) {
            while (true) {
                new ConnectionServer(soketConnListener.accept(), getSessionId(), si).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionDispatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void closeServer(){
        System.err.println("Error while initilizing server, closing down!");
        System.exit(1);
    }
    
    /**
     * Create new session id with a 4096bit Initialization Vector
     * 
     * @return String session Id
     */
    private static String getSessionId(){
        try {
            SecureRandom randomBase = new SecureRandom();
            randomBase.engineSetSeed(randomBase.engineGenerateSeed(256));// :)
            
            MessageDigest mda = MessageDigest.getInstance("Whirlpool", "BC");
            return new String(Base64.encode(mda.digest(randomBase.engineGenerateSeed(512))));
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            System.err.println("Could not generate secure session id, fall back to default!");
        }
        
        return null;
    }
}