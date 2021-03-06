package edu.wpi.always.calendar.schema;

import edu.wpi.always.calendar.CalendarUI;
import edu.wpi.always.client.*;
import edu.wpi.always.user.UserModel;
import edu.wpi.always.user.calendar.Calendar;
import edu.wpi.always.user.people.PeopleManager;
import edu.wpi.always.user.places.PlaceManager;
import edu.wpi.disco.rt.menu.AdjacencyPair;

public class CalendarStateContext extends AdjacencyPair.Context {

   private final Keyboard keyboard;
   private final CalendarUI calendarUI;
   private final Calendar calendar;
   private final UIMessageDispatcher dispatcher;
   private final UserModel model;
   private final PlaceManager placeManager;
   private final PeopleManager peopleManager;


   public CalendarStateContext (Keyboard keyboard, CalendarUI calendarUI,
         Calendar calendar, UIMessageDispatcher dispatcher, UserModel model,
         PlaceManager placeManager, PeopleManager peopleManager) {
      this.keyboard = keyboard;
      this.calendarUI = calendarUI;
      this.calendar = calendar;
      this.dispatcher = dispatcher;
      this.model = model;
      this.placeManager = placeManager;
      this.peopleManager = peopleManager;
   }

   public Keyboard getKeyboard () {
      return keyboard;
   }

   public CalendarUI getCalendarUI () {
      return calendarUI;
   }

   public Calendar getCalendar () {
      return calendar;
   }

   public UIMessageDispatcher getDispatcher () {
      return dispatcher;
   }

   public UserModel getUserModel () { return model; }
   
   public PlaceManager getPlaceManager () {
      return placeManager;
   }

   public PeopleManager getPeopleManager () {
      return peopleManager;
   }
}
