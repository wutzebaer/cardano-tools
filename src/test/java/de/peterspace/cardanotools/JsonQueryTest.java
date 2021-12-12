package de.peterspace.cardanotools;

import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class JsonQueryTest {

	@Test
	void test1() throws Exception {
		DocumentContext jsonContext = JsonPath.parse("{ \"777\": { \"pct\": \"0.2\", \"addr\": [ \"addr1q8g3dv6ptkgsafh7k5muggrvfde2szzmc2mqkcxpxn7c63l9znc9e3xa82h\", \"pf39scc37tcu9ggy0l89gy2f9r2lf7husfvu8wh\" ] } }");
		jsonContext.read("$.*.*.*.image");
	}

	@Test
	void test2() throws Exception {
		DocumentContext jsonContext = JsonPath.parse("{\"721\": {\r\n"
				+ "  \"d2cd728ab017f411d4292f268dfd2ebce3097430f8b37f8821d904db\": {\r\n"
				+ "    \"benny\": {\r\n"
				+ "      \"name\": \"Caro nonno\",\r\n"
				+ "      \"files\": [\r\n"
				+ "        {\r\n"
				+ "          \"src\": \"ipfs://QmR2wpn7uT19LMzk4MCoFCXSwScemQ7zqVWz2nXBQdMBr1\",\r\n"
				+ "          \"name\": \"Foto insieme\",\r\n"
				+ "          \"mediaType\": \"image/jpg\"\r\n"
				+ "        },\r\n"
				+ "        {\r\n"
				+ "          \"src\": \"ipfs://Qmdn5rTiJhDtRobJeC4rGFcF1hb6uBz8cU7LPKxqrEZKuH\",\r\n"
				+ "          \"name\": \"Fai click su play\",\r\n"
				+ "          \"mediaType\": \"audio/mp3\"\r\n"
				+ "        }\r\n"
				+ "      ],\r\n"
				+ "      \"image\": \"ipfs://QmTUAGbb8Ty6hUTeCgJb4ampNu4mgjQ5Tt72NGVccWz28U\",\r\n"
				+ "      \"Pensiero\": [\r\n"
				+ "        \"Una risata e il dialetto barese sono il modo migliore\",\r\n"
				+ "        \"per averti di nuovo vicino. Ci manchi tanto,\",\r\n"
				+ "        \"la tua famiglia.\"\r\n"
				+ "      ]\r\n"
				+ "    }\r\n"
				+ "  }\r\n"
				+ "}}");
		jsonContext.read("$.721.*.*.image");
	}

}
