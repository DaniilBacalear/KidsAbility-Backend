package com.kidsability.automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
//@RestController
//@RequestMapping("/api/token")
public class AutomationApplication {
//	@Autowired
//	private AzureCredentials azureCredentials;
//
//	@Autowired
//	private Practitioner practitioner;

	public static void main(String[] args) {
		SpringApplication.run(AutomationApplication.class, args);
	}

//	@GetMapping
//	public Drive getToken() throws IOException {
//		var graphClient = GraphApiUtil.getGraphClient(azureCredentials);
//		Drive drive = graphClient.sites("kidsabilitycnd.sharepoint.com,22e50cef-a99b-4737-bad1-4db70df77e5a,d5100f4e-e475-4809-a53d-7132adee1d31")
//				.drive()
//				.buildRequest()
//				.get();
//		Practitioner practitioner = new Practitioner();
//		practitioner.setEmail("abc");
//		practitioner.setId(123l);
//		return drive;

//		return GraphApiUtil.getToken(azureCredentials);
//	}

}
