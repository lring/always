package edu.wpi.disco.rt.test;

import static org.junit.Assert.*;
import edu.wpi.disco.rt.*;
import edu.wpi.disco.rt.behavior.*;
import edu.wpi.disco.rt.realizer.*;
import org.junit.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PrimitiveBehaviorControlImplFixture {

   private static final int SOMETHING = 1;
   private IPrimitiveRealizerFactory factory;
   private PrimitiveBehaviorManager realizer;

   @Before
   public void setUp () {
      factory = new FakeRealizerFactory();
      realizer = new PrimitiveBehaviorManager(factory, new Resources());
   }

   @Test
   public void onePrimitive () {
      DummyPrimitive gazePrimitive = new DummyPrimitive(Resources.FOCUS,
            SOMETHING);
      realizer.realize(gazePrimitive);
      assertNotNull(realizer.currentRealizerFor(Resources.FOCUS));
      assertEquals(gazePrimitive, realizer.currentRealizerFor(Resources.FOCUS)
            .getParams());
      DummyPrimitiveRealizer primitiveRealizer = (DummyPrimitiveRealizer) realizer
            .currentRealizerFor(Resources.FOCUS);
      sleep(50);
      assertTrue(primitiveRealizer.runCallsCount > 0);
      assertNull(realizer.currentRealizerFor(Resources.SPEECH));
   }

   @Test
   public void primitiveRealizersAreScheduleOnSeparateThreads () {
      realizer.realize(new DummyPrimitive(Resources.MENU, SOMETHING));
      realizer.realize(new DummyPrimitive(Resources.SPEECH, SOMETHING));
      DummyPrimitiveRealizer gazeRealizer = (DummyPrimitiveRealizer) realizer
            .currentRealizerFor(Resources.MENU);
      DummyPrimitiveRealizer speechRealizer = (DummyPrimitiveRealizer) realizer
            .currentRealizerFor(Resources.SPEECH);
      // wait for their run() function to be called 3 times
      int n = 0;
      while (gazeRealizer.runCallsCount < 3 && speechRealizer.runCallsCount < 3) {
         sleep(100);
         assertFalse("realizer should not have been called on current thread",
               gazeRealizer.runThreadId == Thread.currentThread().getId());
         assertFalse("realizer should not have been called on current thread",
               speechRealizer.runThreadId == Thread.currentThread().getId());
         n++;
         if ( n > 10 )
            fail("test timed out without run() functions being called three times");
      }
   }

   @Test
   public void whenANewBehaviorForAResourceIsGiven_ShouldShutDownTheOneAlreadyRunningOnThatResource () {
      final int paramForFirstBehavior = 1;
      final int paramForSecondBehavior = 2;
      realizer
            .realize(new DummyPrimitive(Resources.FOCUS, paramForFirstBehavior));
      DummyPrimitiveRealizer r1 = (DummyPrimitiveRealizer) realizer
            .currentRealizerFor(Resources.FOCUS);
      int n = 0;
      while (r1.runCallsCount == 0) {
         sleep(20);
         n++;
         if ( n > 5 ) {
            fail("ASSUMPTION error that the behavior I just added would be called! There is something wrong somewhere else");
            return;
         }
      }
      realizer
            .realize(new DummyPrimitive(Resources.FOCUS, paramForSecondBehavior));
      DummyPrimitiveRealizer r2 = (DummyPrimitiveRealizer) realizer
            .currentRealizerFor(Resources.FOCUS);
      assertNotSame(r1, r2);
      assertFalse(r1.getParams().equals(r2.getParams()));
      // now making sure that r2 is running and r1 is stopped
      int r1CountSaved = r1.runCallsCount;
      n = 0;
      while (r2.runCallsCount <= 3) {
         sleep(DiscoRT.REALIZER_INTERVAL);
         n++;
         if ( n > 10 )
            fail("timeout in waiting for r2 to be called 4 times");
         assertTrue("looks like r1 is still running",
               r1.runCallsCount == r1CountSaved);
      }
   }

   @Test
   public void whenOnePrimitiveIsDone_ShouldFireDoneEvent () {
      FakeRealizerEventObserver observer = addObserverToRealizer();
      final int DOES_NOT_MATTER = 1;
      DummyPrimitive b1 = new DummyPrimitive(Resources.FOCUS, DOES_NOT_MATTER);
      realizer.realize(b1);
      sleep(10);
      assertFalse(observer.doneReceivedFor(b1));
      ((DummyPrimitiveRealizer) realizer.currentRealizerFor(Resources.FOCUS))
            .done();
      assertTrue(observer.doneReceivedFor(b1));
   }

   @Test
   public void testStopResource () {
      DummyPrimitive behavior = new DummyPrimitive(Resources.FOCUS, SOMETHING);
      realizer.realize(behavior);
      DummyPrimitiveRealizer r = (DummyPrimitiveRealizer) realizer
            .currentRealizerFor(Resources.FOCUS);
      sleep(10);
      realizer.stop(Resources.FOCUS);
      assertNotRunning(r);
   }

   @Test
   public void whenNullPrimitiveBehaviorIsSetForBehavior_ItShouldStopCurrentRealizerOnThatResource () {
      DummyPrimitive behavior = new DummyPrimitive(Resources.FOCUS, SOMETHING);
      realizer.realize(behavior);
      DummyPrimitiveRealizer r = (DummyPrimitiveRealizer) realizer
            .currentRealizerFor(Resources.FOCUS);
      realizer.realize(PrimitiveBehavior.nullBehavior(Resources.FOCUS));
      assertNotRunning(r);
   }

   @Test
   public void whenARealizerForTheSameBehaviorIsInEffect_ShouldNotStopIt () {
      DummyPrimitive b1 = new DummyPrimitive(Resources.FOCUS, SOMETHING);
      DummyPrimitive b2 = new DummyPrimitive(Resources.FOCUS, SOMETHING);
      realizer.realize(b1);
      PrimitiveRealizer<?> firstRealizer = realizer
            .currentRealizerFor(Resources.FOCUS);
      realizer.realize(b2);
      PrimitiveRealizer<?> secondRealizer = realizer
            .currentRealizerFor(Resources.FOCUS);
      assertSame(firstRealizer, secondRealizer);
   }

   private FakeRealizerEventObserver addObserverToRealizer () {
      FakeRealizerEventObserver observer = new FakeRealizerEventObserver();
      realizer.addObserver(observer);
      return observer;
   }

   /**
    * very simplistic, I know ;)
    * 
    * @param r
    */
   private void assertNotRunning (DummyPrimitiveRealizer r) {
      int savedCount = r.runCallsCount;
      sleep(DiscoRT.REALIZER_INTERVAL * 2);
      assertEquals(savedCount, r.runCallsCount);
   }

   private void sleep (int milliSeconds) {
      try {
         Thread.sleep(milliSeconds);
      } catch (InterruptedException ex) {
      }
   }

   static class FakeRealizerEventObserver implements
         PrimitiveBehaviorControlObserver {

      public List<PrimitiveBehavior> done = Collections
            .synchronizedList(new ArrayList<PrimitiveBehavior>());

      @Override
      public void primitiveDone (PrimitiveBehaviorControl sender,
            PrimitiveBehavior pb) {
         done.add(pb);
      }

      public boolean doneReceivedFor (PrimitiveBehavior pb) {
         return done.contains(pb);
      }

      @Override
      public void primitiveStopped (PrimitiveBehaviorControl sender,
            PrimitiveBehavior pb) {
      }
   }

   static class FakeRealizerFactory implements IPrimitiveRealizerFactory {

      @Override
      public PrimitiveRealizer<?> create (PrimitiveBehavior primitiveBehavior) {
         if ( DummyPrimitive.class.isAssignableFrom(primitiveBehavior
               .getClass()) ) {
            return new DummyPrimitiveRealizer(
                  (DummyPrimitive) primitiveBehavior);
         }
         return null;
      }
   }

   static class DummyPrimitiveRealizer implements
         PrimitiveRealizer<DummyPrimitive> {

      private final DummyPrimitive params;
      public int runCallsCount = 0;
      public long runThreadId = -1;
      List<PrimitiveRealizerObserver> observers = new CopyOnWriteArrayList<PrimitiveRealizerObserver>();

      public DummyPrimitiveRealizer (DummyPrimitive params) {
         this.params = params;
      }

      @Override
      public DummyPrimitive getParams () {
         return params;
      }

      @Override
      public void run () {
         runCallsCount++;
         runThreadId = Thread.currentThread().getId();
      }

      @Override
      public void addObserver (PrimitiveRealizerObserver observer) {
         observers.add(observer);
      }

      @Override
      public void removeObserver (PrimitiveRealizerObserver observer) {
         observers.remove(observer);
      }

      public void done () {
         for (PrimitiveRealizerObserver o : observers)
            o.prmitiveRealizerDone(this);
      }

      @Override
      public void shutdown () {
      }
   }
}
