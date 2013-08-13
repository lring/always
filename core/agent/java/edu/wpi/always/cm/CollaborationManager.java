package edu.wpi.always.cm;

import java.io.File;

import org.picocontainer.BindKey;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;

import edu.wpi.always.Activity;
import edu.wpi.always.Plugin;
import edu.wpi.always.cm.perceptors.dummy.DummyEngagementPerceptor;
import edu.wpi.always.cm.perceptors.dummy.DummyMovementPerceptor;
import edu.wpi.always.cm.perceptors.sensor.face.ShoreFacePerceptor;
import edu.wpi.always.cm.primitives.AgentResources;
import edu.wpi.always.cm.primitives.PluginSpecificActionRealizer;
import edu.wpi.always.cm.schemas.SessionSchema;
import edu.wpi.always.user.UserModel;
import edu.wpi.always.user.UserUtils;
import edu.wpi.always.user.owl.OntologyUserModel;
import edu.wpi.cetask.TaskClass;
import edu.wpi.cetask.TaskModel;
import edu.wpi.disco.rt.DiscoRT;
import edu.wpi.disco.rt.Registry;
import edu.wpi.disco.rt.Resources;

public class CollaborationManager extends DiscoRT {

   private final MutablePicoContainer parent;
   private final TaskModel activities;
   
   public CollaborationManager (MutablePicoContainer parent) {
      super(parent);
      this.parent = parent;
      container.removeComponent(Resources.class);
      container.as(Characteristics.CACHE).addComponent(AgentResources.class);
      container.addComponent(PluginSpecificActionRealizer.class);
      activities = interaction.load("Activities.xml");
      // load user model after Activities.xml for initialization of USER_FOLDER
      parent.as(Characteristics.CACHE).addComponent(
            BindKey.bindKey(File.class,
                  OntologyUserModel.UserOntologyLocation.class),
                  new File(UserUtils.USER_DIR, UserUtils.USER_FILE));
      parent.getComponent(UserModel.class).load();
   }
 
   public void start (Class<? extends Plugin> plugin, String activity) {
      // FIXME Try to use real sensors
      container.as(Characteristics.CACHE).addComponent(DummyMovementPerceptor.class); 
      container.as(Characteristics.CACHE).addComponent(ShoreFacePerceptor.class);
      container.as(Characteristics.CACHE).addComponent(DummyEngagementPerceptor.class);
      if ( plugin != null ) {
         parent.as(Characteristics.CACHE).addComponent(plugin);
         Plugin instance = parent.getComponent(plugin);
         for (Registry r : instance.getRegistries(new Activity(plugin, activity, 0, 0, 0, 0)))
            addRegistry(r);
         System.out.println("Loaded plugin: "+instance);
         
      } else
         for (TaskClass top : activities.getTaskClasses()) {
            Plugin instance = Plugin.getPlugin(top, container);
            for (Activity a : instance.getActivities(0)) // not using closeness value
               for (Registry r : instance.getRegistries(a)) addRegistry(r);
            System.out.println("Loaded plugin: "+instance);
         }
      super.start(plugin == null ? "Session" : null);
      if ( plugin == null ) setSchema(null, SessionSchema.class);
   }
}