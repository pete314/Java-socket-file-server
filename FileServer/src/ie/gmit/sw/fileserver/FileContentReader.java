package ie.gmit.sw.fileserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Nagy
 * @since 2015 December
 * @description File content reader reads file contents
 */
public class FileContentReader {
    
    public FileContentReader(){}
    
    /**
     * Read file content into string buffer for additional processing
     * 
     * @param fileName
     * @return String buffer
     */
    public StringBuffer readFile(final String fileName){
        StringBuffer sb = new StringBuffer();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))))){
            String line;
            while((line = br.readLine()) != null){
                sb.append(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileContentReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return sb;
    }
    
    /**
     * User data specific file read
     * 
     * @param fileName 
     * @return Hasmap with username user entity
     */
    public Map<String, User> readUserData(final String fileName){
        Map<String, User> users = new HashMap<>();
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))))){
            String line;
            while((line = br.readLine()) != null){
                String[] words = line.split("\t");
                users.put(words[0], new User().setUsername(words[0]).setPassword(words[1].toCharArray()).setDefaultDirectory(words[2]));
            }
            return users;
        } catch (IOException ex) {
            Logger.getLogger(FileContentReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
