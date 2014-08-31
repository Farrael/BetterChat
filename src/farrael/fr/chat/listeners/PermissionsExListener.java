package farrael.fr.chat.listeners;

import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ru.tehkode.permissions.PermissionEntity.Type;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEntityEvent.Action;
import farrael.fr.chat.Chat;

public class PermissionsExListener implements Listener{
	Chat chat = Chat.instance;

	@EventHandler
	public void onPermissionsChanged(PermissionEntityEvent event){
		if(event.getAction() == Action.INFO_CHANGED){
			if(event.getEntity().getType() == Type.USER){
				PermissionUser user = (PermissionUser) event.getEntity();
				if(user.getPlayer() != null)
					PlayerListener.setChatName(user.getPlayer());
			} else {
				PermissionGroup group = (PermissionGroup) event.getEntity();
				updateGroup(group.getActiveUsers());
			}
		} else if(event.getAction() == Action.INHERITANCE_CHANGED){
			if(event.getEntity().getType() == Type.USER){
				PermissionUser user = (PermissionUser) event.getEntity();
				if(user.getPlayer() != null)
					PlayerListener.setChatName(user.getPlayer());
			}	
		}
	}

	public void updateGroup(Set<PermissionUser> list){
		for(PermissionUser user : list){
			if(user.getPlayer() != null)
				PlayerListener.setChatName(user.getPlayer());
		}
	}
}
