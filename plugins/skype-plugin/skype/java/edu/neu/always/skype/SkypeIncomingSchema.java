package edu.neu.always.skype;

import java.util.*;
import edu.wpi.always.Always;
import edu.wpi.always.Always.AgentType;
import edu.wpi.always.client.*;
import edu.wpi.always.cm.perceptors.*;
import edu.wpi.always.cm.perceptors.sensor.face.ShoreFacePerceptor;
import edu.wpi.disco.rt.ResourceMonitor;
import edu.wpi.disco.rt.behavior.*;
import edu.wpi.disco.rt.menu.*;

public class SkypeIncomingSchema extends SkypeSchema {

   protected final FacePerceptor shore;
   protected final UIMessageDispatcher dispatcher;
   protected final ClientProxy proxy;
   
   public SkypeIncomingSchema (BehaviorProposalReceiver behaviorReceiver,
         BehaviorHistory behaviorHistory, ResourceMonitor resourceMonitor,
         MenuPerceptor menuPerceptor, FacePerceptor shore, UIMessageDispatcher dispatcher, 
         Always always, SkypeClient client, ClientProxy proxy) {
      super(new IncomingSkype(shore, dispatcher, proxy),
            behaviorReceiver, behaviorHistory, resourceMonitor, menuPerceptor, always);
      this.shore = shore instanceof ShoreFacePerceptor.Reeti ? null : shore;
      this.dispatcher = dispatcher;
      this.proxy = proxy;
      // note client not used, but must be in argument list for creation
      interruptible = false;
   }
   
   // TODO  ***DANGER** After sending endCall message below
   //       system will wait indefinitely until it receives
   //       callEnded from client (so that Shore can restart camera)
   //       If client crashes at this point, system is permanently
   //       hung.  Solution???
   
   static volatile boolean EXIT; // set from SkypeClient thread
   
   @Override
   public void runActivity () {
      if ( EXIT ) stop();
      else super.runActivity();
   }
   
   public static final long SHORE_START_DELAY_SECONDS = 90;
   
   @Override
   public void dispose () { 
      super.dispose();      
      // this code is here so it is run even if schema throws an error
      if ( shore != null ) 
         // need to wait until certain that camera is released
         new Timer().schedule(new TimerTask() {
            @Override
            public void run () { shore.start(); }}, 
            SHORE_START_DELAY_SECONDS*1000L); 
      EngagementPerception.setRecoveringEnabled(true);
   }
   
   private static class IncomingSkype extends MultithreadAdjacencyPair<AdjacencyPair.Context> {
   
      private final FacePerceptor shore;
      private final UIMessageDispatcher dispatcher;
      private final ClientProxy proxy;

      private IncomingSkype (FacePerceptor shore, final UIMessageDispatcher dispatcher,
            final ClientProxy proxy) {
         super("", new AdjacencyPair.Context());
         this.shore = shore;
         this.dispatcher = dispatcher;
         this.proxy = proxy;
         this.repeatOption = false;
         choice("Please end this call", new DialogStateTransition() {
             @Override
             public AdjacencyPair run () { 
                proxy.setAgentVisible(false);
                dispatcher.send(new Message("endCall"));
                return null;
             }
         });
      }
            
      @Override
      public void enter () {
         log(Direction.INCOMING, SkypeInterruptHandler.CALLER_NAME);
         if ( shore != null ) shore.stop();
         EXIT = false;
         dispatcher.send(new Message("acceptCall"));
         // see SkypeClient
         if ( Always.getAgentType() == AgentType.REETI )
            proxy.setAgentVisibleReeti(true);
         // NB: Safer to move following to someplace where it is only called 
         // once the video connection is successfully established!!!
         EngagementPerception.setRecoveringEnabled(false);
      }
   }
}
