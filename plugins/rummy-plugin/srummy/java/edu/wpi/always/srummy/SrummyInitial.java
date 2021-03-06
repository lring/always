package edu.wpi.always.srummy;

import java.util.ArrayList;
import java.util.List;
import edu.wpi.disco.rt.menu.AdjacencyPair;
import edu.wpi.disco.rt.menu.DialogStateTransition;

public class SrummyInitial extends SrummyAdjacencyPairImpl{

   private static List<String> 
   nextLevelButtonMarks = new ArrayList<String>(),
   prompts = new ArrayList<String>();
   private static String Level9Button1 = "That's good";
   private static String Level9Button2 = "I guess I'll trust you on that";
   private static String Level7Button1 = "That sounds pretty odd, but I'll try it";
   private static String Level7Button2 = "Okay, will do.";
   private static int explainingLine = 1;
   private boolean HasAlreadBeenHere = false;

   public SrummyInitial (SrummyStateContext context) {
      super("Let's play rummy", context);
      this.repeatOption = true;
      nextLevelButtonMarks.add("Ok");
      nextLevelButtonMarks.add("Uh-huh");
      nextLevelButtonMarks.add("Go on");
      prompts.add("To win in rummy, you must empty your hand of cards before I do.");
      prompts.add("To empty your hand, you place cards from your hand on the table, by making a group of, Melds.");
      prompts.add("A meld is 3 cards of the same type.  For example, three cards that are all nines makes a meld.  ");
      prompts.add("A meld is also 3 cards of the same suit, in sequence.  For example, the 9, 10 and 11 of hearts makes a meld.");
      prompts.add("Your turn starts when you take a card from the deck or from the discard pile.");
      prompts.add("If you want to meld, you meld during your turn before you discard.");
      prompts.add("During your turn you can also add to any meld on the table.  The meld can be your own or mine! ");
      prompts.add("And remember: aces are low, so they meld with a 2 and 3 of a suit.  Not with the king and queen! ");
      prompts.add("It's a bit tricky to get cards to sit on the table as a meld.  If it doesn't work the first time, "
            + "just move the card around a bit til it sticks.");
      prompts.add("After you discard a card, it's my turn.  ");
      prompts.add("When I take a card, it just appears in my hand.  It's hard to see me do it!  ");
      prompts.add("By the way, even though your cards are face up, I can't see them!");
      prompts.add("That's all there is to rummy!");
      choice("Ok", new DialogStateTransition() {
         @Override
         public AdjacencyPair run () {
            return new AskIfWantTutorial(getContext());
         }
      });
   }
   
   @Override
   public void enter(){
      if(!HasAlreadBeenHere){
         getContext().getSrummyUI().startPluginForTheFirstTime(this);
         getContext().getSrummyUI().setUpGame();
         getContext().getSrummyUI().makeBoardUnplayable();
         HasAlreadBeenHere = true;
      }
   }

   public class AskIfWantTutorial extends SrummyAdjacencyPairImpl{
      public AskIfWantTutorial (SrummyStateContext context) {
         super("Before we start, do you want a refresher on playing rummy?", context);
         this.repeatOption = true;
         choice("Yes, definitely", new DialogStateTransition() {
            @Override
            public AdjacencyPair run () {
               return new Tutorial(getContext());
            }
         });
         choice("No, I don't need it", new DialogStateTransition() {
            @Override
            public AdjacencyPair run () {
               return new StartGamingSequence(getContext());
            }
         });
      }
      @Override
      public void agentMoveOptionsReceived (String chosenMoveType) {
         if (chosenMoveType.equals("draw"))
            StartGamingSequence.receivedAgentDrawOptions = true;
         else if(chosenMoveType.equals("discard"))
            StartGamingSequence.receivedAgentDiscardOptions = true;
         else if(chosenMoveType.equals("meld"))
            StartGamingSequence.receivedAgentMeldOptions = true;
         else if(chosenMoveType.equals("layoff"))
            StartGamingSequence.receivedAgentLayoffOptions = true;
      }

   }
   
   @Override
   public void agentMoveOptionsReceived (String chosenMoveType) {
      if (chosenMoveType.equals("draw"))
         StartGamingSequence.receivedAgentDrawOptions = true;
      else if(chosenMoveType.equals("discard"))
         StartGamingSequence.receivedAgentDiscardOptions = true;
      else if(chosenMoveType.equals("meld"))
         StartGamingSequence.receivedAgentMeldOptions = true;
      else if(chosenMoveType.equals("layoff"))
         StartGamingSequence.receivedAgentLayoffOptions = true;
   }


   public class Tutorial extends SrummyAdjacencyPairImpl{
      public Tutorial (SrummyStateContext context) {
         super(prompts.get(explainingLine), context);
         this.repeatOption = true;
         if(explainingLine != 8 && explainingLine != 6){
            choice(nextLevelButtonMarks.get(SrummyClient.random.nextInt(
                  nextLevelButtonMarks.size())), new DialogStateTransition() {
               @Override
               public AdjacencyPair run () {
                  return new Tutorial(getContext());
               }
            });
         }
         if(explainingLine == 6){
            choice(Level7Button1, new DialogStateTransition() {
               @Override
               public AdjacencyPair run () {
                  return new Tutorial(getContext());
               }
            });
            choice(Level7Button2, new DialogStateTransition() {
               @Override
               public AdjacencyPair run () {
                  return new Tutorial(getContext());
               }
            });
         }
         if(explainingLine == 8){
            choice(Level9Button1, new DialogStateTransition() {
               @Override
               public AdjacencyPair run () {
                  return new LetsPlay(getContext());
               }
            });
            choice(Level9Button2, new DialogStateTransition() {
               @Override
               public AdjacencyPair run () {
                  return new LetsPlay(getContext());
               }
            });
         }
      }
      @Override
      public void enter () { explainingLine++; }
      
      @Override
      public void agentMoveOptionsReceived (String chosenMoveType) {
         if (chosenMoveType.equals("draw"))
            StartGamingSequence.receivedAgentDrawOptions = true;
         else if(chosenMoveType.equals("discard"))
            StartGamingSequence.receivedAgentDiscardOptions = true;
         else if(chosenMoveType.equals("meld"))
            StartGamingSequence.receivedAgentMeldOptions = true;
         else if(chosenMoveType.equals("layoff"))
            StartGamingSequence.receivedAgentLayoffOptions = true;
      }

   }

   public class LetsPlay extends SrummyAdjacencyPairImpl{
      public LetsPlay (SrummyStateContext context) {
         super("Ok let's play!", context);
         this.repeatOption = true;
         choice("Yes, great!", new DialogStateTransition() {
            @Override
            public AdjacencyPair run () {
               return new StartGamingSequence(getContext());
            }
         });
         choice("I want to hear the whole explanation one more time"
               , new DialogStateTransition() {
                  @Override
                  public AdjacencyPair run () {
                     explainingLine = 1;
                     return new Tutorial(getContext());
                  }
               });
      }
      
      @Override
      public void agentMoveOptionsReceived (String chosenMoveType) {
         if (chosenMoveType.equals("draw"))
            StartGamingSequence.receivedAgentDrawOptions = true;
         else if(chosenMoveType.equals("discard"))
            StartGamingSequence.receivedAgentDiscardOptions = true;
         else if(chosenMoveType.equals("meld"))
            StartGamingSequence.receivedAgentMeldOptions = true;
         else if(chosenMoveType.equals("layoff"))
            StartGamingSequence.receivedAgentLayoffOptions = true;
      }

   }

}
