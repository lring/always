package edu.wpi.disco.rt.menu;

import java.util.List;

public class AdjacencyPairWrapper<C extends AdjacencyPair.Context> extends AdjacencyPairBase<C> {

   protected final AdjacencyPair inner;

   public AdjacencyPairWrapper (AdjacencyPair inner) {
      super(null, null);
      this.inner = inner;
   }

   @Override
   public void enter () { inner.enter(); } 

   @Override
   public boolean prematureEnd () { return inner.prematureEnd(); }

   @Override
   public AdjacencyPair nextState (String text) { return inner.nextState(text); }

   @Override
   public String getMessage () { return inner.getMessage(); }
   
   @Override
   public List<String> getChoices () { return inner.getChoices(); }

   @Override
   public double timeRemaining () { return inner.timeRemaining(); }

   @Override
   public boolean isTwoColumnMenu () { return inner.isTwoColumnMenu(); }
   
   @Override
   public C getContext () { return (C) inner.getContext(); }
   
   public static class Repeat extends AdjacencyPairWrapper<AdjacencyPair.Context> {
      
      public Repeat (AdjacencyPair inner) { super(inner); }
      
      @Override
      public void enter () {} // don't redo enter action
   }
}

