package edu.wpi.disco.rt.schema;

import edu.wpi.disco.rt.*;
import edu.wpi.disco.rt.behavior.*;
import edu.wpi.disco.rt.behavior.Behavior;
import edu.wpi.disco.rt.util.Utils;
import org.picocontainer.*;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class SchemaManager {

   private final MutablePicoContainer container;
   private final Scheduler scheduler;
   private final List<Class<? extends Schema>> toRunAtStartUp = new ArrayList<Class<? extends Schema>>();
   private final Map<Class<? extends Schema>, SchemaFactory> factories = new HashMap<Class<? extends Schema>, SchemaFactory>();
   private boolean startUpDone = false;

   public SchemaManager (MutablePicoContainer container) {
      this.container = container;
      this.scheduler = container.getComponent(Scheduler.class);
   }

   public void startUp () {
      assert !startUpDone : "SchemaManager.startUp() was already called";
      for (Class<? extends Schema> s : toRunAtStartUp) {
         start(s);
      }
      startUpDone = true;
   }

   public <T extends Schema> T start (Class<T> type) {
      if ( DiscoRT.TRACE ) Utils.lnprint(System.out, "Starting: "+type);
      long interval = DiscoRT.SCHEMA_INTERVAL;
      boolean daemon = false;
      T instance;
      if ( factories.containsKey(type) ) {
         SchemaFactory factory = factories.get(type);
         instance = (T) factory.create(container);
         interval = factory.getUpdateDelay();
         daemon = factory.isDaemon();
      } else instance = container.getComponent(type);
      ScheduledFuture<?> future = scheduler.schedule(instance, interval, daemon);
      instance.setFuture(future);
      return instance;
   }

   public void registerSchema (Class<? extends Schema> type, boolean runOnStartup, boolean daemon) {
      registerSchema(type, DiscoRT.SCHEMA_INTERVAL, runOnStartup, daemon);
   }

   public void registerSchema (Class<? extends Schema> type, long updateDelay,
         boolean runOnStartup, boolean daemon) {
      registerSchema(new SchemaConfig(type, updateDelay, runOnStartup, daemon));
   }

   public void registerSchema (SchemaConfig config) {
      registerSchema(new ConfigSchemaFactory(config));
   }

   public void registerSchema (SchemaFactory factory) {
      Class<? extends Schema> type = factory.getSchemaType();
      factories.put(type, factory);
      if ( factory.isRunOnStartup() ) {
         assert !startUpDone : "SchemaManager.RegisterSchema() called with a startup schema, after startUp was called";
         toRunAtStartUp.add(type);
      }
   }

   private class ConfigSchemaFactory implements SchemaFactory {

      private SchemaConfig config;

      public ConfigSchemaFactory (SchemaConfig config) {
         container.addComponent(config.type);
         this.config = config;
      }

      @Override
      public long getUpdateDelay () {
         return config.updateDelay;
      }
      
      @Override
      public boolean isRunOnStartup () {      
         return config.runOnStartup;
      }

      @Override
      public boolean isDaemon () {      
         return config.daemon;
      }
      
      @Override
      public Class<? extends Schema> getSchemaType () {
         return config.type;
      }

      @Override
      public Schema create (PicoContainer container) {
         return container.getComponent(getSchemaType());
      }

   }
}
