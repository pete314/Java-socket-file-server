
package ie.gmit.sw.fileserverclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Peter Nagy
 * @since 2015 December
 * @description Abstract print helper
 */
public class AbstractPrintHelper {
    
    private final String HELP_CONTENT = "The command could not be parse, please use --help\n" + 
                                                "--help || -h >> help content (this)\n" +
                                                "ls >> list current directory [use -l to list style]\n " +
                                                "cd >> Change directory [requires the directory name]\n" +
                                                "mkdir >> Create directory\n " +
                                                "rmdir >> Remove directory\n" +
                                                "put >> upload file\n"+
                                                "get >> download file\n"+
                                                "\n\nExamples: \n" +
                                                "ls -al {returns the current content in a list form}\n" + 
                                                "cd /myFolder {change to myFolder}\n";

    public AbstractPrintHelper() {
    }
    
    public void printMessage(String msg){
        System.out.println(msg);
    }
    
    public void printInLineMessage(String msg){
        System.out.print(msg);
    }
    
    public void printInLineReplaceMessage(String msg){
        try {
            msg = "\r" + msg;
            System.out.write(msg.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(AbstractPrintHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void printHelpMessage(){
        System.out.println(HELP_CONTENT);
    }
    
    public void logMessage(String msg){
        
    }
}
