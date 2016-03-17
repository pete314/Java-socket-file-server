#Java socket based file server
###Project General Description 
Multi-threaded TCP Server Application which allows multiple client applications to transfer files to and from the server. The client application can use command line input from the user to implement user functions. The service allows the users to:  

 1. Authenticate between the client application and the server application.  
 2. Copy a selected file from the server. (e.g. get file1.txt) 
 3. Move a selected file to the server. (e.g. put file1.txt)  
 4. List all the files in the current directory of the server.  
 5. Move to a different directory on the server.  
 6. Make a new directory on the server.  

###Repository description
The Project is divided into 2 part the server(FileServer) and the client(FileServerClient). The server side implementation support multiple client connections which is handled by two main component the ConnectionDispatcher and the ConnectionHandler which runs in connection specific thread. During the design process the key point with the server side implementation was to favor robustness and modularity. Currently almost no security features are implemented by the bases are created. Authentication sessions are generated for the connections which is handed to the client. This could be used in further development as identification for rebuilding connection as well as additional secure identification. 
The server uses a command validation mechanism to identify and validate requests. Each user (details) are kept in a “entity” which maintains user specific data, holding the current folder location as well as default with all credentials etc. 
The client uses similar design decisions regarding request and response validation. As a small addition the client uses GUI in some cases if available. 
###Execution instruction 
The Client and Server uses  a property exchange class which contains hard coded values required for running the project. This meant to handle reading from property files on a later stage. 
On local host jar files can be used for testing, from the dist folder:
```
java -jar /path/to/repo/dist/FileServerClient.jar
```
To run the project just run the the server first and the start the client and follow instructions on screen.  
```
java -jar /path/to/repo/dist/FileServer.jar
```
**The server should be started first :)**
###Notes 
This project can be a good starting point for junior developers or newcomers to java.

###Disclaimer
THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 