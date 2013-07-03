package wpi.edu.always.tictactoe;

import edu.wpi.always.*;
import edu.wpi.always.cm.CollaborationManager;
import edu.wpi.always.user.UserModel;

public class TicTacToePlugin extends Plugin {
   
   public TicTacToePlugin (UserModel userModel, CollaborationManager cm) {
      super("TicTacToe", userModel, cm);
      addActivity("PlayTicTacToe", 0, 0, 0, 0, TicTacToeSchema.class, TicTacToeClient.class); 
   }
   
   /**
    * For testing TicTacToe by itself
    */
   public static void main (String[] args) {
      new Always(true, TicTacToePlugin.class, "PlayTicTacToe").start();
   }
  
}
