package wrapper;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import controller.LogManager;
import controller.files.FileTunnel;
import model.Tunnel;

/**
 * Singleton class
 * @author RENATOS
 *
 */
@XmlRootElement(name = "Tunnels")
public class TunnelList {
	
	static LogManager log = new LogManager();
	
	private static TunnelList instance;
	private List<Tunnel> tunnelList;


	public TunnelList() {
		this.tunnelList = new ArrayList<Tunnel>();
	}

	public static TunnelList getInstance() {
		log.debug("TunnelList:getInstance()");
		if (instance == null) {
			log.debug("Instance is null, so updating instance...");
			instance = FileTunnel.getTunnels();
		}
		return instance;
	}

	public void add (Tunnel tunnel) {
		log.debug("TunnelList:add()");
		log.info("Processing tunnel with id " + tunnel.getId() + "...");
		if (tunnel.getParentServer() == 0 || !ServerList.exists(tunnel.getParentServer())) {
			throw new InvalidParameterException("Parent Server ID informed is not valid");
		}
		if (tunnelList.contains(tunnel)) {
			log.error("Tunnel "+tunnel.getId()+" already exists! Handling as Update...");
			tunnelList.remove(tunnel);
		}
		tunnelList.add(tunnel);
		this.save();
	}

	/**
	 * Save a Tunnel instance to the list
	 */
	public void save () {
		log.debug("TunnelList:save()");
		log.info("Saving to file");
		FileTunnel.saveTunnels(this);
	}

	/**
	 * Return Tunnel object of the given id, or null if none found
	 * @param id
	 * @return
	 */
	public Tunnel load (long id) {
		for (Tunnel tunnel : getTunnelList()) {
			if (tunnel.getId() == id)
				return tunnel;
		}
		return null;
	}

	public List<Tunnel> getServerTunnels(long id) {
		List<Tunnel> tnlList = new ArrayList<Tunnel>();
		for (Tunnel tunnel : getTunnelList()) {
			if (tunnel.getParentServer() == id) {
				tnlList.add(tunnel);
			}
		}
		return tnlList;
	}

	/**
	 * Remove a Server instance from the list
	 * @param server
	 */
	public void remove (Tunnel tunnel) {
		log.info("Removing tunnel with id "+ tunnel.getId());
		tunnelList.remove(tunnel);
		this.save();
	}
	
	public List<Tunnel> getTunnelList() {
		return tunnelList;
	}

	@XmlElement(name = "Tunnel")
	public void setTunnelList(List<Tunnel> tunnelList) {
		this.tunnelList = tunnelList;
	}

	public long getNextId() {
		Collection<Tunnel> col = tunnelList;
		if (col.isEmpty()) {
			return 1;
		}
		Tunnel highest = Collections.max(col, Tunnel.ID_COMPARATOR);
		return highest.getId()+1;
	}
}
