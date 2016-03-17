package ie.gmit.sw.fileserver;

import java.security.Security;
import java.util.Map;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author Peter Nagy
 * @since 2015 December
 * @decription Server initializer handles initialization of dependencies on startup
 */
public class ServerInitializer {
    
    Map<String, User> users;
    
    public ServerInitializer(){
        Security.addProvider(new BouncyCastleProvider());
    }
    
    public boolean createUsers(){
        users = new FileContentReader().readUserData("./userCred/users.dat");
        return users.size() > 0;
    }
}
