package edu.wpi.always.client.reeti;

//a Java socket client

import java.io.*;
import edu.wpi.always.client.RemoteConnection;
import edu.wpi.disco.rt.util.Utils;

public class ReetiCommandSocketConnection extends RemoteConnection {

   private PrintWriter writer;
   
   public ReetiCommandSocketConnection (String host) {
      super(host, 12045);
      connect(host, 12045);
   }

   @Override
   protected void connect (String host, int port) {
      super.connect(host, port);
      try { writer = new PrintWriter(socket.getOutputStream(), true); }
      catch (Exception e) { restart(e); }
   }
   
   public void send (String message) {
      try {
         writer.println(message); 
         if ( writer.checkError() ) 
//          ignore transient error?         
//          throw new IOException("ReetiCommandSocket println error:"+message);
            Utils.lnprint(System.out, "ReetiCommandSocket println error: "+message);
      } catch (Exception e) { restart(e); }
   }

   public void close () {
      writer.close();
      try { socket.close(); }
      catch (IOException e) { e.printStackTrace(); }
   }
   
   public void wiggleEars () {
      send("Object.wiggleEars();");
   }
   
   public void reboot () {
      send("var p = new Process(\"reboot\",[]); wall(p.status); wall(p.run);");
   }
}