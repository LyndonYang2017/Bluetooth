
package simplesppserver;

/**
 *
 * @author lin
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
  
import javax.bluetooth.*;
import javax.microedition.io.*;

/**
* Class that implements an SPP Server which accepts single line of
* message from an SPP client and sends a single line of response to the client.
*/
public class SimpleSPPServer {
    
    //start server
    private void startServer() throws IOException{
  
        //Create a UUID for SPP
        UUID uuid = new UUID("1101", true);
        //Create the servicve url
        String connectionString = "btspp://localhost:" + uuid +";name=Sample SPP Server";
        
        //open server url
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier)Connector.open( connectionString );
        
        //Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect...");
        StreamConnection connection=streamConnNotifier.acceptAndOpen();
  
        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: "+dev.getBluetoothAddress());
        System.out.println("Remote device name: "+dev.getFriendlyName(true));
        
        
        
        //send response to spp client
        System.out.println("\nStart to write message to Client...");
        OutputStream outStream=connection.openOutputStream();
        PrintWriter pWriter=new PrintWriter(new OutputStreamWriter(outStream));
        
        int i = 0;
        while (i < 10)
        {
            pWriter.write("Hello, this information is from server!This is No." + i +" message.\r\n");
            pWriter.flush();
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleSPPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Messge has been sent!");
        
        //read string from spp client
        System.out.println("\nStart to read the message from client...");
        InputStream inStream=connection.openInputStream();
        BufferedReader bReader=new BufferedReader(new InputStreamReader(inStream));
        String lineRead=bReader.readLine();
        System.out.println(lineRead);
  
        pWriter.close();
        streamConnNotifier.close();
    }
  
  
    public static void main(String[] args) throws IOException {
        
        //display local device address and name
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: "+localDevice.getBluetoothAddress());
        System.out.println("Name: "+localDevice.getFriendlyName());
        
        SimpleSPPServer sampleSPPServer=new SimpleSPPServer();
        sampleSPPServer.startServer();
        
    }
}