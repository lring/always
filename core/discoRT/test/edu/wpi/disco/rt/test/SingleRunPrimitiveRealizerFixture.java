package edu.wpi.disco.rt.test;

import static org.junit.Assert.assertEquals;
import edu.wpi.disco.rt.behavior.SpeechBehavior;
import edu.wpi.disco.rt.realizer.*;
import org.junit.Test;

public class SingleRunPrimitiveRealizerFixture {

   @Test
   public void whenItIsDone_ShouldFireDoneMessageOnEachRun () {
      Subject subject = new Subject(new SpeechBehavior("Good"));
      MockObserver observer = new MockObserver();
      subject.addObserver(observer);
      subject.run();
      subject.run();
      subject.run();
      assertEquals(0, observer.receivedDoneCount);
      subject.done();
      assertEquals(1, observer.receivedDoneCount);
      subject.run();
      assertEquals(2, observer.receivedDoneCount);
      for (int i = 0; i < 10; i++)
         subject.run();
      assertEquals(12, observer.receivedDoneCount);
   }

   public static class Subject extends
         SingleRunPrimitiveRealizer<SpeechBehavior> {

      public Subject (SpeechBehavior params) {
         super(params);
      }

      @Override
      protected void singleRun () {
      }

      public void done () {
         fireDoneMessage();
      }
   }

   public static class MockObserver implements PrimitiveRealizerObserver {

      public int receivedDoneCount = 0;

      @Override
      public void prmitiveRealizerDone (PrimitiveRealizer<?> realizer) {
         receivedDoneCount++;
      }
   }
}
