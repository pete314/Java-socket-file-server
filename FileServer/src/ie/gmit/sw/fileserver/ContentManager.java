package ie.gmit.sw.fileserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Nagy
 * @since 2015 December
 */
public class ContentManager {

    private final User user;
    private String requestRootPath;
    private final String USER_DATA_DIR = "userData";

    public ContentManager(User user) {
        this.user = user;
        requestRootPath = buildRootPath();
    }

    private String buildRootPath() {
        return new File(System.getProperty("user.dir")
                + File.separator 
                + USER_DATA_DIR 
                + File.separator
                + user.getDefaultDirectory() 
                + File.separator
                + user.getCurrentDirectory()).toString();
    }

    /**
     * Build a list of the content 
     * 
     * @param style The modifier parameters
     * @return StringBiffer with the current list content 
     */
    public StringBuffer getFolderContents(String style) {
        StringBuffer result = new StringBuffer();
        File f = new File(requestRootPath);
        ArrayList<File> files = new ArrayList<>(Arrays.asList(f.listFiles()));

        files.stream().forEach((file) -> {
            if (style.isEmpty()) {
                result.append(file.getName()).append(" ");
            }else if (style.contains("a") && !style.contains("l")){
                result.append(file.getName()).append(" ").append(file.isDirectory() ? "dir" : "file" + " " + file.length()).append(" ");
            }else if (style.contains("l")){
                result.append(file.getName()).append(" ").append(file.isDirectory() ? "dir" : "file" + " " + file.length()).append(" ").append(getFileAttributes(file)[2]).append("\n");
            }
        });
        
        return result;
    }
    
    /**
     * 
     * 
     * @param style The modifier of the listing
     * @param subPath The folder path of interest
     * @return StringBuffer with the content list
     */
    public StringBuffer getFolderContents(String style, String subPath) {
        //shoudl check if contains trailing path separator 
        requestRootPath = subPath.charAt(0) == '.' ? subPath :  new File(
                                                                    System.getProperty("user.dir") 
                                                                    + File.separator
                                                                    + user.getDefaultDirectory()
                                                                    + File.separator
                                                                    + subPath).toString();
        return getFolderContents(style);
    }
    
    public boolean changeDirectory(String path, boolean isAbsolute){
        File f = new File((isAbsolute ? buildRootPath() : user.getDefaultDirectory()) + File.separator + path);
        return f.exists() && f.isDirectory();
    }
    
    public boolean removeDirectory(String path, boolean isAbsolute){
        File f = new File((isAbsolute ? buildRootPath() : user.getDefaultDirectory()) + File.separator + path);
        if(f.exists() && f.isDirectory()){
            try{
                f.delete();
                return true;
            } catch (SecurityException se) {
                System.out.println(se);
            }
        }
        return false;
    }
    
    public boolean createDirectory(String dirName, boolean isAbsolute){
        File f = new File((isAbsolute ? buildRootPath() : user.getDefaultDirectory()) + File.separator + dirName);
        if(!f.exists()){
            try{
                f.mkdir();
                return true;
            } catch (SecurityException se) {
                System.out.println(se);
            }
        }
        return false;
    }
    
    public File getFilePointer(String filePath, boolean isAbsolute){
        return new File((isAbsolute ? buildRootPath() : user.getDefaultDirectory()) + File.separator + filePath);
    }
    
    private String[] getFileAttributes(File file){
        try {
            String[] fileInfo = new String[3];
            BasicFileAttributes fa = Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class);
            
            fileInfo[0] = fa.lastAccessTime().toString();
            fileInfo[1] = fa.lastModifiedTime().toString();
            fileInfo[2] = fa.creationTime().toString();
            
            return fileInfo;
        } catch (IOException ex) {
            Logger.getLogger(ContentManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
