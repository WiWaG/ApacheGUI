package ca.apachegui.virtualhosts;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import ca.apachegui.db.SettingsDao;
import ca.apachegui.global.Constants;
import ca.apachegui.modules.SharedModuleHandler;
import ca.apachegui.modules.StaticModuleHandler;
import apache.conf.parser.Directive;
import apache.conf.parser.Enclosure;
import apache.conf.parser.EnclosureParser;

public class VirtualHosts {

	private static Logger logger = Logger.getLogger(VirtualHost.class);
	
	public static VirtualHost[] getAllVirtualHosts() throws Exception {
		
		EnclosureParser parser = new EnclosureParser(SettingsDao.getInstance().getSetting(Constants.confFile), SettingsDao.getInstance().getSetting(Constants.serverRoot), StaticModuleHandler.getStaticModules(), SharedModuleHandler.getSharedModules());
		
		Enclosure virtualHostEnclosures[] = parser.getEnclosure(Constants.virtualHostDirectiveString);
		
		ArrayList<VirtualHost> virtualHosts = new ArrayList<VirtualHost>(); 
		
		VirtualHost virtualHost;
		ArrayList <NetworkInfo> networkInfo;
		Directive directives[];
		for(Enclosure virtualHostEnclosure : virtualHostEnclosures) {
			
			virtualHost = new VirtualHost();
			
			virtualHost.setFile(virtualHostEnclosure.getFile());

			networkInfo = new ArrayList<NetworkInfo>();
			String values[] = virtualHostEnclosure.getValue().split(" ");
			for(String value : values) {
				networkInfo.add(extractNetworkInfo(value));
			}
			
			virtualHost.setNetworkInfo(networkInfo.toArray(new NetworkInfo[networkInfo.size()]));
			
			directives = virtualHostEnclosure.getDirectives();
			for(Directive directive : directives) {
				if(directive.getType().equals(Constants.serverNameDirectiveString)) {
					virtualHost.setServerName(directive.getValues()[0]);
				}
				if(directive.getType().equals(Constants.documentRootDirectiveString)) {
					virtualHost.setDocumentRoot(directive.getValues()[0]);
				}
			}
			
			logger.info(virtualHost.toString());
			
			virtualHosts.add(virtualHost);
		}
		
		return virtualHosts.toArray(new VirtualHost[virtualHosts.size()]);
	}
	
	private static NetworkInfo extractNetworkInfo(String value) {
		
		NetworkInfo networkInfo = new NetworkInfo();
		
		//There is no port number
		if(!value.contains(":") || (value.contains("[") && value.contains("]") && !value.contains("]:"))) {
			
			String address;
			
			if(value.contains("[") && value.contains("]")) {
				address = value.substring(value.indexOf("[") + 1, value.indexOf("]"));
			} else {
				address = value;
			}
			
			networkInfo.setAddress(address);
			
		} else {
			
			String address;
			int port;
			
			if(value.contains("[") && value.contains("]")) {
				address = value.substring(value.indexOf("[") + 1, value.indexOf("]"));
				port = Integer.parseInt(value.substring(value.lastIndexOf(":") + 1));
				
			} else {
				String addressPort[] = value.split(":");
				address = addressPort[0];
				port = Integer.parseInt(addressPort[1]);
			}
						
			networkInfo.setAddress(address);
			networkInfo.setPort(port);
		}
		
		return networkInfo;
	}
}