package de.peterspace.cardanominter.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class interfaceTest {

	@GetMapping("/rest/ok")
	public String ok() {
		return "ok";
	}

}
