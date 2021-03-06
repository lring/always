package edu.wpi.always.cm.schemas;

import edu.wpi.always.Logger;
import edu.wpi.disco.rt.ResourceMonitor;
import edu.wpi.disco.rt.behavior.*;
import edu.wpi.disco.rt.menu.*;

public abstract class ActivityStateMachineSchema<C extends AdjacencyPair.Context> extends ActivitySchema {

   protected final MenuTurnStateMachine stateMachine;

   public ActivityStateMachineSchema (AdjacencyPair initial, 
         BehaviorProposalReceiver behaviorReceiver,
         BehaviorHistory behaviorHistory, ResourceMonitor resourceMonitor,
         MenuPerceptor menuPerceptor, Logger.Activity loggerName) {
      super(behaviorReceiver, behaviorHistory, loggerName);
      initial.getContext().setSchema(this);
      stateMachine = new MenuTurnStateMachine(behaviorHistory, resourceMonitor,
            menuPerceptor, new RepeatMenuTimeoutHandler<C>());
      stateMachine.setSpecificityMetadata(SPECIFICITY);
      stateMachine.setState(initial);
      stateMachine.setNeedsFocusResource(true); // default value
   }

   @Override
   public void runActivity () {
      propose(stateMachine);
   }
   
   @Override
   public void setNeedsFocusResource (boolean focus) {
      if ( stateMachine != null ) stateMachine.setNeedsFocusResource(focus);
   }
   
   public void resetTimeout () {
      if (stateMachine != null ) stateMachine.resetTimeout(); 
   }
}
