package de.peterspace.cardanotools.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient.TokenAmountByAddress;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/query")
public class QueryApi {

	private final CardanoDbSyncClient cardanoDbSyncClient;

	@GetMapping("/tokenAmountByAddress/{policyId}/json")
	public ResponseEntity<List<TokenAmountByAddress>> tokenAmountByAddress(@PathVariable("policyId") String policyId) throws Exception {
		List<TokenAmountByAddress> tokenAmountByAddress = cardanoDbSyncClient.getTokenAmountByAddress(policyId);
		return new ResponseEntity<>(tokenAmountByAddress, HttpStatus.OK);
	}

	@GetMapping("/tokenAmountByAddress/{policyId}/excel")
	public void tokenAmountByAddressExcel(@PathVariable("policyId") String policyId, HttpServletResponse response) throws Exception {
		List<TokenAmountByAddress> tokenAmountByAddressList = cardanoDbSyncClient.getTokenAmountByAddress(policyId);

		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=tokenAmountByAddress_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("tokenAmountByAddress");

		Row headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("address");
		headerRow.createCell(1).setCellValue("amount");

		int rowNum = 1;
		for (TokenAmountByAddress tokenAmountByAddress : tokenAmountByAddressList) {
			Row dataRow = sheet.createRow(rowNum);
			dataRow.createCell(0).setCellValue(tokenAmountByAddress.getAddress());
			dataRow.createCell(1).setCellValue(tokenAmountByAddress.getQty());
			rowNum++;
		}

		//slow
		//sheet.autoSizeColumn(0);
		//sheet.autoSizeColumn(1);

		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();
	}

}
