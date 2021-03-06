package edu.wpi.disco.rt.perceptor;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PerceptorBufferManager<T extends Perception> {

   private final List<WeakReference<PerceptorBuffer<T>>> buffers = new CopyOnWriteArrayList<WeakReference<PerceptorBuffer<T>>>();

   public PerceptorBuffer<T> newBuffer () {
      PerceptorBuffer<T> buffer = new PerceptorBuffer<T>();
      buffers.add(new WeakReference<PerceptorBuffer<T>>(buffer));
      return buffer;
   }

   private void removeBuffer (WeakReference<PerceptorBuffer<T>> b) {
      buffers.remove(b);
   }

   public void pushPerception (T perception) {
      Iterator<WeakReference<PerceptorBuffer<T>>> bufferIterator = buffers
            .iterator();
      while (bufferIterator.hasNext()) {
         WeakReference<PerceptorBuffer<T>> bufferReference = bufferIterator
               .next();
         PerceptorBuffer<T> buffer = bufferReference.get();
         if ( buffer == null )
            removeBuffer(bufferReference);
         else
            buffer.push(perception);
      }
   }
}
