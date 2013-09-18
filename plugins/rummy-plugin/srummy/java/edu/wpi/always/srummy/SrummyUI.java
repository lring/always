package edu.wpi.always.srummy;

import java.util.List;
import edu.wpi.always.srummy.SrummyUI;

public interface SrummyUI {

   public void prepareAgentCommentForAMoveBy(int player);
   public String getCurrentAgentComment();
   public void triggerAgentPlayTimer();
   public void cancelHumanCommentingTimer();
   public void triggerHumanCommentingTimer();
   public List<String> getCurrentHumanCommentOptionsForAMoveBy(int player);
   public void triggerNextStateTimer();
   public void startPluginForTheFirstTime(SrummyUIListener listener);
   public void updatePlugin(SrummyUIListener listener);
   public void sendBackAgentMove ();
   public void resetGame();
   //   public void prepareAgentMove();
   //   public void makeBoardPlayable();
   //   public void makeBoardUnplayable();
   //   public void playAgentMove (SrummyUI listener);

}