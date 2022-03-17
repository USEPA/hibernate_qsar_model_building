package gov.epa.web_services;

public class WebService {
	
	public String address;
	
	public WebService(String server, int port) {
		this.address = server + ":" + port;
	}
	
	public WebService(String url) {
		this.address = url;
	}

}
