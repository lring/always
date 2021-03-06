package edu.wpi.disco.rt.menu;

import java.util.List;
import org.joda.time.DateTime;
import com.google.common.collect.Lists;
import edu.wpi.disco.rt.*;
import edu.wpi.disco.rt.behavior.*;
import edu.wpi.disco.rt.behavior.Constraint.Type;
import edu.wpi.disco.rt.realizer.petri.*;
import edu.wpi.disco.rt.util.*;

public class MenuTurnStateMachine implements BehaviorBuilder {

   public static final int TIMEOUT_DELAY = 30000; 
   public static final int MENU_DELAY = 1000;
   
   private final BehaviorHistory behaviorHistory;
   private final MenuPerceptor menuPerceptor;
   private final ResourceMonitor resourceMonitor;
   private final MenuTimeoutHandler timeoutHandler;
   private TimeStampedValue<Behavior> previousBehavior = new TimeStampedValue<Behavior>(Behavior.NULL);
   private DateTime waitingForResponseSince;
   private boolean needsToAddNull;
   private boolean extension, needsFocusResource;
   
   private AdjacencyPair state, previousState, timedOutState;
   
   public AdjacencyPair getState () { return state; }
   
   private Mode mode;
   
   public Mode getMode () { return mode; }

   public enum Mode { Speaking, Listening }  // mode of agent
   
   // for returning from interruptions
   public void resetTimeout () { waitingForResponseSince = DateTime.now(); } 
    
   public void setExtension (boolean extension) { this.extension = extension; }
   
   @Override
   public void setNeedsFocusResource (boolean focus) {
      this.needsFocusResource = focus;
   }

   public MenuTurnStateMachine (BehaviorHistory behaviorHistory,
         ResourceMonitor resourceMonitor, MenuPerceptor menuPerceptor,
         MenuTimeoutHandler timeoutHandler) {
      this.behaviorHistory = behaviorHistory;
      this.resourceMonitor = resourceMonitor;
      this.menuPerceptor = menuPerceptor;
      this.timeoutHandler = timeoutHandler;
      setMode(Mode.Speaking);
   }

   @Override
   public Behavior build () {
      // note this method is coded as tail-recursive loops
      if ( state == null ) {
         if ( DiscoRT.TRACE) Utils.lnprint(System.out, "Nothing to say/do");
         return Behavior.NULL;
      }
      if ( !hasSomethingToSay() && !hasMenuChoices() ) {
         setState(state.nextState(null));
         return Behavior.NULL;
      }
      if ( state.prematureEnd() ) return nextState(null); // loop
      Behavior behavior = Behavior.NULL;
      MenuBehavior menuBehavior = hasMenuChoices() ? 
         new MenuBehavior(state.getChoices(), state.isTwoColumnMenu(), extension) :
         null;
      SpeechMarkupBehavior speechBehavior = hasSomethingToSay() ? 
         new SpeechMarkupBehavior(state.getMessage()) :
         null;
      if ( speechBehavior != null && menuBehavior != null ) {
         List<PrimitiveBehavior> primitives = speechBehavior.getPrimitives(true);
         primitives.add(menuBehavior);
         behavior = new Behavior(new CompoundBehaviorWithConstraints(primitives, 
               Lists.newArrayList(new Constraint(
               new SyncRef(SyncPoint.Start, speechBehavior.getSpeech()),
               new SyncRef(SyncPoint.Start, menuBehavior),
               Type.After, MENU_DELAY))));
      } else if ( mode == Mode.Speaking ) {
         if ( speechBehavior == null ) {
            setMode(Mode.Listening);
            return build(); // loop
         }
         behavior = new Behavior(speechBehavior);
      } else if ( mode == Mode.Listening ) {
         if ( menuBehavior == null ) return nextState(null); // loop
         behavior = Behavior.newInstance(menuBehavior);
         resetTimeout(); // reset now since nothing said
      }
      if ( needsFocusResource ) behavior = behavior.addFocusResource(); 
      if ( previousState != state ) {        
         if ( previousBehavior.getValue().equals(behavior) ) needsToAddNull = true; 
         update(Behavior.NULL);
      }
      if ( needsToAddNull )
         // this is a bit of a hack to fix problem that if two successive
         // states produce the same behavior, it won't be executed twice because
         // the behavior history thinks it is already done
         behavior = behavior.addNull();
      // check if already done and update behavior
      boolean alreadyDone = false;
      if ( previousBehavior.getValue().equals(behavior) ) {
         if ( behaviorHistory.isDone(behavior.getInner(), previousBehavior.getTimeStamp()) ) 
            alreadyDone = true;
      } else update(behavior);
      if ( alreadyDone && mode == Mode.Speaking ) setMode(Mode.Listening);
      if ( menuBehavior != null
           && resourceMonitor.isDone(menuBehavior, previousBehavior.getTimeStamp()) ) {
         String selected = checkMenuSelected(state.getChoices(), previousBehavior);
         if ( selected != null ) {
            // prevent infinite loop when same state, same menu and no message
            if ( state == previousState && speechBehavior == null ) update(Behavior.NULL);
            return nextState(selected); // loop
         }
      }
      // TODO Figure out why/if following causes speech to be repeated as soon as it is done
      /*
      if ( alreadyDone && mode == Mode.Hearing && !extension
           && speechBehavior != null && menuBehavior != null ) { 
         // while waiting for menu selection, release speech and other resources from speech markup, if any
         // (e.g., to allow robot to say "ouch" if you poke it)
         behavior = Behavior.newInstance(menuBehavior);
         if ( needsFocusResource ) behavior = behavior.addFocusResource();
      }
      */
      if ( mode == Mode.Listening && !extension && state != timedOutState // don't timeout twice
           && waitingForResponseSince.isBefore(DateTime.now().minusMillis(TIMEOUT_DELAY)) ) {
         timedOutState = state;
         AdjacencyPair newState = timeoutHandler.handle(state);
         if ( newState != null && newState != state ) {
            setState(newState);
            return build(); // loop
         }
      }
      return behavior;
   }

   public boolean hasSomethingToSay () {
      String s = state.getMessage();
      return s != null && s.length() > 0;
   }

   public boolean hasMenuChoices () {
      return state.getChoices() != null && !state.getChoices().isEmpty();
   }
   
   private String checkMenuSelected (List<String> userChoices, TimeStampedValue<Behavior> menuShownAt) {
      MenuPerception p = menuPerceptor.getLatest();
      // ignore selection that is not in choices, since could be from 
      // menu extension (or vice versa)
      if ( p != null && userChoices.contains(p.getSelected())
         && p.getTimeStamp() > menuShownAt.getTimeStamp() ) {
         return p.getSelected();
      }
      return null;
   }

   private void update (Behavior b) {
      previousState = state;
      previousBehavior = new TimeStampedValue<Behavior>(b);
   }

   private Behavior nextState (String text) {
      setState(state.nextState(text));
      return build(); // loop
   }
   
   public void setState (AdjacencyPair newState) {
      setMode(Mode.Speaking);
      needsToAddNull = false;
      if ( newState == null ) return;
      state = newState;
      newState.enter();
   }

   private double specificity;
   private boolean newActivity;

   @Override
   public BehaviorMetadata getMetadata () {
      if ( state == null )
         return new BehaviorMetadataBuilder().build();
      BehaviorMetadataBuilder builder = new BehaviorMetadataBuilder()
            .specificity(specificity)
            .timeRemaining(state.timeRemaining())
            .newActivity(newActivity);
      return builder.build();
   }

   public void setNewActivity (boolean n) {
      this.newActivity = n;
   }

   public void setSpecificityMetadata (double s) {
      this.specificity = s;
   }

   private void setMode (Mode newMode) {
      if ( newMode == Mode.Listening && mode == Mode.Speaking )
         resetTimeout();
      this.mode = newMode;
   }
}
