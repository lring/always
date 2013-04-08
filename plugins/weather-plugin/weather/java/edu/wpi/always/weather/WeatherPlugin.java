package edu.wpi.always.weather;

import edu.wpi.always.*;
import edu.wpi.always.user.*;
import edu.wpi.always.weather.wunderground.WundergroundWeatherProvider;

// TODO Make this work with live data
//      using generateNewReport(), loadRecentDataFromFile();

public class WeatherPlugin extends Plugin {
   
   public WeatherPlugin (UserModel userModel) {
      super("Weather", userModel);
      addActivity("DiscussWeather", 0, 0, 0, 0, WeatherSchema.class, WundergroundWeatherProvider.class); 
   }
   
   // Property names must be constants and start with plugin name
   public static final String FAVORITE = "WeatherFavorite"; 
         
   /**
    * For testing Weather by itself
    */
   public static void main (String[] args) {
      Always always = new Always(true, WeatherPlugin.class, "DiscussWeather");
      always.start();
      Plugin plugin = always.getContainer().getComponent(WeatherPlugin.class);
      // testing new user property extension (see Weather.owl)
      plugin.setProperty(FAVORITE, "hot and humid");
      System.out.println("My favorite weather is "+plugin.getProperty(FAVORITE));
   }
  

  
}
