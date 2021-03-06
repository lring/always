package edu.wpi.always;

import edu.wpi.always.cm.CollaborationManager;
import edu.wpi.always.user.UserModel;
import edu.wpi.disco.Interaction;
import edu.wpi.disco.rt.util.DiscoDocument;

/**
 * This is a reduced implementation of the relationship management algorithm described
 * in docs/CoonSidnerRich2012 in which three session plans are precomputed. 
 */
public class RelationshipManager {

   private final Interaction interaction;
   private final UserModel model;
   
   // see Activities.xml for closeness level rules
   
   public RelationshipManager (CollaborationManager cm) {
      interaction = cm.getContainer().getComponent(Interaction.class);
      model = cm.getContainer().getComponent(UserModel.class);
   }
 
   public DiscoDocument getSession () { 
      return new DiscoDocument(interaction.getDisco(), 
            "/edu/wpi/always/resources/"+model.getCloseness()+".xml"); 
   }

   public void afterInteraction (Interaction interaction, int closeness, int time) { }
}
