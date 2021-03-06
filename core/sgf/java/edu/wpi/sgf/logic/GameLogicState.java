package edu.wpi.sgf.logic;

/** 
 * Classes for game edu.wpi.sgf.logic state, like maps of games, cards on the board, etc
 * are extended from this abstract.
 */

public abstract class GameLogicState {

	public boolean agentWins = false;
	public boolean userWins = false;
	public boolean tie = false;
	
	// for logging of games
   public enum Won { USER, AGENT, NEITHER }
}
