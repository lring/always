package edu.wpi.always;

import java.io.File;
import java.util.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.picocontainer.*;
import org.picocontainer.behaviors.OptInCaching;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.LifecycleComponentMonitor;
import edu.wpi.always.client.*;
import edu.wpi.always.cm.CollaborationManager;
import edu.wpi.always.cm.perceptors.EngagementPerception.EngagementState;
import edu.wpi.always.cm.perceptors.*;
import edu.wpi.always.cm.perceptors.sensor.face.ShoreFacePerceptor;
import edu.wpi.always.cm.schemas.*;
import edu.wpi.always.user.*;
import edu.wpi.always.user.owl.*;
import edu.wpi.cetask.TaskClass;
import edu.wpi.disco.*;
import edu.wpi.disco.rt.*;
import edu.wpi.disco.rt.behavior.SpeechMarkupBehavior;
import edu.wpi.disco.rt.menu.AdjacencyPairBase;
import edu.wpi.disco.rt.schema.Schema;
import edu.wpi.disco.rt.util.*;

public class Always {

   /**
    * Main method for starting complete Always-On system. 
    * 
    * @param args [agentType closeness login model] NB: Case-sensitive!
    *  <p>
    *  agentType: UNITY (default), REETI or MIRROR<br> 
    *  closeness: STRANGER, ACQUAINTANCE, COMPANION or null (default, use value in user model)<br>
    *  login: true or false (default)<br>
    *  model: file in always/user (default most recent User.*.owl)
    */
   public static void main (String[] args) {
      Always always = make(args, null, null);
      always.start();
   }

   public enum AgentType { UNITY, REETI, MIRROR }
   
   private static AgentType agentType = AgentType.UNITY;
   
   public static AgentType getAgentType () { return agentType; }
   
   /**
    * Date that system started running (for logging purposes)
    * See {@link SessionSchema#DATE}
    */
   public final static Date DATE = new Date();

   private static boolean login;
   
   public static boolean isLogin () { return login; }
   
   public static File RELEASE;
   
   /**
    * Factory method for Always.  
    * 
    * @param args see {@link Always#main(String[])} 
    * @param plugin Start just this plugin 
    * @param activity Start just this activity (required if plugin is non-null)
    */
   public static Always make (String[] args, Class<? extends Plugin> plugin, String activity) {
      if ( args != null ) {
         if ( args.length > 0 ) agentType = AgentType.valueOf(args[0]);
         if ( args.length > 2 ) login = Boolean.parseBoolean(args[2]);
         if ( args.length > 3 ) UserUtils.USER_FILE = args[3];
      }
      if ( System.getenv("ALWAYS_RELEASE") != null ) {
         RELEASE = UserUtils.lastModified("/release", "_RELEASE", "");
         if ( RELEASE != null )
            Utils.lnprint(System.out, "Release = "+RELEASE);
      }
      if ( login ) Utils.lnprint(System.out, "Login condition!");
      Utils.lnprint(System.out, "Agent type = "+agentType);   
      Utils.lnprint(System.out, "Time of day = "+UserUtils.getTimeOfDay());
      Logger logger = Logger.THIS; // force logger creation printout now
      Always always = new Always(true, plugin == null);
      UserModel model = always.getUserModel();
      if ( args != null && args.length > 1 && !"null".equals(args[1])
            && !model.getUserName().isEmpty() )
         model.setCloseness(Closeness.valueOf(args[1]));
      Closeness closeness = model.getCloseness();
      Utils.lnprint(System.out, "Using closeness = "+closeness);
      logger.logId(model);
      always.plugin = plugin; 
      always.activity = activity;
      return always;
   }
   
   /**
    * Nested class with main method for testing Disco task models (and accessing
    * user model) without starting Always GUI.
    */
   public static class Disco {
      
      public static void main (String[] args) { 
         Interaction interaction = new DiscoRT.Interaction(
            new Agent("agent"), 
            new User("user"),
            args.length > 0 && args[0].length() > 0 ? args[0] : null);
         UserUtils.USER_FILE = "User.Diane.owl";  // no way to change for now
         // initialize duplicate interaction created above
         // before Activities so $always initialized
         new Always(true, false).init(interaction); 
         Plugin.loadAll( // to force loading of plugin classes used in JavaScript
               new edu.wpi.disco.Disco(interaction).load("/edu/wpi/always/resources/Activities.xml").getTaskClasses());
         interaction.start(true);  
      }
   }
   
   /**
    * Most recent instance of Always.  Useful for scripts.
    */
   public static Always THIS;
   
   public CollaborationManager getCM () {
      return container.getComponent(CollaborationManager.class);
   }
   
   public RelationshipManager getRM () {
      return container.getComponent(RelationshipManager.class);
   }
   
   public UserModel getUserModel () {
      return container.getComponent(UserModel.class);
   }
   
   public void printUserModel () {
      UserUtils.print(getUserModel(), System.out);
   }
   
   /**
    * To enabled tracing of Always implementation.  Note this variable can be conveniently
    * set using eval command in Disco console or in init script of a task model, such 
    * as Activities.xml.
    * 
    * @see DiscoRT#TRACE
    */
   public static boolean TRACE = true;
   
   /**
    * The container for holding all the components of the system
    */
   private final MutablePicoContainer container =
         new PicoBuilder().withBehaviors(new OptInCaching())
            .withLifecycle(new StartableLifecycleStrategy(new LifecycleComponentMonitor())) 
            .withConstructorInjection().build();
   
   public MutablePicoContainer getContainer () {
      return container;
   }
   
   public static boolean ALL_PLUGINS;
   
   public Always (boolean logToConsole, boolean allPlugins) {
      THIS = this;
      ALL_PLUGINS = allPlugins;
      new Logger(allPlugins);
      if ( logToConsole )
         BasicConfigurator.configure();
      else
         BasicConfigurator.configure(new NullAppender());
      container.as(Characteristics.CACHE).addComponent(this);
      container.as(Characteristics.CACHE).addComponent(RelationshipManager.class);  
      addRegistry(new OntologyUserRegistry()); 
      register();
      addCMRegistry(new ClientRegistry());
      addCMRegistry(new StartupSchemas());
      addCMRegistry(new EngagementRegistry());
      SpeechMarkupBehavior.ANALYZER = new AgentSpeechMarkupAnalyzer();
      Scheduler.THREADS = 1;  // for EngagementSchema only
      CollaborationManager cm = new CollaborationManager(container);
      container.as(Characteristics.CACHE).addComponent(cm);
      init(cm.getInteraction());
   }

   public void init (Interaction interaction) {
      edu.wpi.disco.Disco disco = interaction.getDisco();
      // for convenient use in Disco scripts
      disco.setGlobal("$always", this);
      disco.eval("edu.wpi.always = Packages.edu.wpi.always;", "Always.init");
      edu.wpi.disco.Disco.RANDOM_ALTERNATIVES = true;
   }

   private final List<OntologyRegistry> ontologyRegistries = new ArrayList<OntologyRegistry>();
   private final List<ComponentRegistry> registries = new ArrayList<ComponentRegistry>();
   private final List<Registry> cmRegistries = new ArrayList<Registry>();
   
   public List<Registry> getCMRegistries () { return cmRegistries; }
   
   public void addRegistry (Registry registry) {
      if ( registry instanceof ComponentRegistry )
         registries.add((ComponentRegistry) registry);
      // NB could be instance of both
      if ( registry instanceof OntologyRegistry )
         ontologyRegistries.add((OntologyRegistry) registry);
      if ( !(registry instanceof ComponentRegistry || registry instanceof OntologyRegistry) )
            throw new IllegalArgumentException("Unknown registry type: "+registry);
   }

   public void addCMRegistry (Registry registry) {
      cmRegistries.add(registry);
   }

   private Class<? extends Plugin> plugin; 
   private String activity;
   
   public void start () {
      // start container first, since cm has own start method
      container.start(); 
      CollaborationManager cm = container.getComponent(CollaborationManager.class);
      for (Registry registry : cmRegistries) cm.addRegistry(registry);      
      Utils.lnprint(System.out, "Starting Collaboration Manager...");
      cm.start(plugin, activity); 
      Utils.lnprint(System.out, "Always running...");
      if ( plugin != null ) {
         Schema schema =  container.getComponent(plugin).startActivity(activity);
         cm.setSchema(null, schema.getClass());
         cm.getInteraction().setSchema(schema);
      }
   }

   public void stop () { 
      container.stop();
      Utils.lnprint(System.out, "Always stopped.");
   }
   
   private void register () {
      for (ComponentRegistry registry : registries)
         registry.register(container);
      OntologyRuleHelper helper = container.getComponent(OntologyRuleHelper.class);
      for (OntologyRegistry registry : ontologyRegistries)
         registry.register(helper);
   }
   
   public static boolean THROW; // for debugging
   
   // Assumes java being called inside a restart loop
   public static void restart (Exception e, String message) {
      Utils.lnprint(System.out, (e == null ? "" : e)+message);
      if ( THROW && e != null ) edu.wpi.cetask.Utils.rethrow(e);  
      else { e.printStackTrace(); exit(1); }
   }
   
   private enum Disengagement { GOODBYE, TIMEOUT, ERROR } // for logging

   private static volatile boolean exiting;
   private static final Object lock = new Object();
   
   public static void exit (int code) {
      EngagementSchema.EXIT = true;
      synchronized (lock) {
         if ( exiting ) return;
         exiting = true;
      }
      if ( SessionSchema.getCurrentLoggerName() != Logger.Activity.SESSION ) {
         Logger.logEvent(Logger.Event.END);
         SessionSchema.setCurrentLoggerName(Logger.Activity.SESSION);
      }
      MutablePicoContainer container = THIS.getCM().getContainer();
      ClientProxy proxy = container.getComponent(ClientProxy.class);
      proxy.hidePlugin(true);
      if ( code != 0 ) {
         Thread.dumpStack();
         proxy.say("Oops. There seems to be an error in my programming. I am going to restart and be back in a few minutes. Bye!");
         Logger.logEngagement(container.getComponent(EngagementPerceptor.class).getLatest().getState(), 
               EngagementState.IDLE);
      }
      // clear menus before logging end
      proxy.showMenu(null, false, true); // must be before regular menu
      proxy.showMenu(null, false, false);
      Logger.logEvent(Logger.Event.END,
            code == 0 ? (EngagementSchema.EXIT ? Disengagement.GOODBYE : Disengagement.TIMEOUT ) : Disengagement.ERROR,
            AdjacencyPairBase.REPEAT_COUNT,    
            SessionSchema.DATE == null ? 0 :   
               (int) ((System.currentTimeMillis() - SessionSchema.DATE.getTime())/1000L),
            (int) (Logger.TOTAL_ENGAGED_TIME/1000L));
      Utils.lnprint(System.out,  "EXITING WITH CODE "+code+" ...");
      // need to free resources held by Shore engine which block exit
      FacePerceptor face = container.getComponent(FacePerceptor.class);
      if ( face != null ) face.stop();
      // give menu clearing messages and final agent utterance time to be sent
      try { Thread.sleep(5000); } catch (InterruptedException e) {}
      // black screen last so agent visible while talking
      proxy.setScreenVisible(false);     
      proxy.setAgentVisible(false);
      System.exit(code);
   }
}

