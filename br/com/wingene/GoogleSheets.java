package br.com.wingene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Response;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

public class GoogleSheets extends GoogleApiBase {
	

	private String spreadsheetId;

	public GoogleSheets(String id) {
		setSpreadsheetId(id);
	}

	public String setSpreadsheetId(String id) {
		spreadsheetId = id;

		return id;
	}

	public String getSpreadsheetId() {
		return spreadsheetId;
	}

	/**
	 * Build and return an authorized Sheets API client service.
	 * 
	 * @return an authorized Sheets API client service
	 * @throws IOException
	 */
	public static Sheets getGoogleSheets() throws IOException {
		Credential credential = authorize();
		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(credential))
				.setApplicationName(APPLICATION_NAME).build();
	}

	
	
	public static GoogleSheets createSpreadSheet(String name) throws IOException {
		Spreadsheet requestBody = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(name));

		Sheets sheetsService = getGoogleSheets();
		Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody)
				.setFields("spreadsheetId");

		Spreadsheet response = request.execute();

		return new GoogleSheets(response.getSpreadsheetId());
	}

	public void InsertLine(List<Object> row) throws Exception {
		InsertLine(row, 1);
	}

	public void InsertLine(List<Object> row, int startColumn) throws Exception {
		String range = getA1(1, 1, row.size() + startColumn);
		ValueRange response = getGoogleSheets().spreadsheets().values().get(getSpreadsheetId(), range).execute();
		List<List<Object>> values = response.getValues();
		int rows = 1;
		if (values != null) {
			rows = values.size();
		} else {
			rows = 1;
		}
		List<Object> rowShift = new ArrayList<Object>();
		for (int i = 0; i < startColumn - 1; i++)
			rowShift.add("");
		rowShift.addAll(row);
		List<List<Object>> valuesInsert = new ArrayList<List<Object>>();
		valuesInsert.add(rowShift);

		ValueRange body = new ValueRange().setValues(valuesInsert);
		getGoogleSheets().spreadsheets().values()
				.append(getSpreadsheetId(), getA1(rows, 1, row.size() + startColumn), body)
				.setValueInputOption("USER_ENTERED").execute();
	}

	public void deleteLines(Integer startLine, Integer endLine) {
		Spreadsheet spreadsheet = null;
		try {
			spreadsheet = getGoogleSheets().spreadsheets().get(getSpreadsheetId()).execute();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
		Request request = new Request();
		DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();
		DimensionRange dimensionRange = new DimensionRange();
		dimensionRange.setDimension("ROWS");
		dimensionRange.setStartIndex(startLine-1);
		dimensionRange.setEndIndex(endLine);

		dimensionRange.setSheetId(spreadsheet.getSheets().get(0).getProperties().getSheetId());
		deleteDimensionRequest.setRange(dimensionRange);

		request.setDeleteDimension(deleteDimensionRequest);

		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		content.setRequests(requests);

		try {
			BatchUpdateSpreadsheetResponse resp = getGoogleSheets().spreadsheets()
					.batchUpdate(getSpreadsheetId(), content).execute();
			for (Response reply : resp.getReplies()) {
				System.out.println(reply);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * public Map<String, List<PlainTriple> readByFilter(String query, int cols)
	 * throws Exception { Map< String, List<PlainTriple> > mapRow = new HashMap<
	 * String, List<PlainTriple> >(); BatchGetValuesByDataFilterRequest
	 * dataFilterRequest = new BatchGetValuesByDataFilterRequest(); List<DataFilter>
	 * dataFilters = new ArrayList<DataFilter>(); DataFilter df = new DataFilter();
	 * df.setA1Range("A:Z"); df.put("metadataKey", "JOSE"); //df.set("filter",
	 * "JOSE"); dataFilters.add(df); dataFilterRequest.setDataFilters(dataFilters);
	 * BatchGetValuesByDataFilterResponse response =
	 * getSheetsService().spreadsheets().values()
	 * .batchGetByDataFilter(getSpreadsheetId(), dataFilterRequest) .execute();
	 * List<Object> values = (List<Object>) response.values();
	 * 
	 * for (Object o : values) { System.err.println(o); }
	 * 
	 * 
	 * 
	 * //return mapRow;
	 * 
	 * }
	 */
	private String getA1(int rowStart, int colStart, int colEnd) {
		return getA1(rowStart, colStart, -1, colEnd);
	}

	private String getA1(int rowStart, int colStart, int rowEnd, int colEnd) {
		String sEnd = String.valueOf(rowEnd);
		if (rowEnd == -1)
			sEnd = "";
		sEnd = columnToLetter(colEnd) + sEnd;
		String ret = sEnd;
		if (rowStart != rowEnd || colStart != colEnd)
			ret = columnToLetter(colStart) + rowStart + ":" + sEnd;
		return ret;
	}

	private String columnToLetter(int column) {
		String letter = "";
		while (column > 0) {
			int temp = (column - 1) % 26;
			letter = ((char) ('A' + temp)) + letter;
			column = (column - temp - 1) / 26;
		}
		return letter;
	}

	public void UpdateLine(List<Object> values, int row) throws Exception {
		UpdateLine(values, row, 1);
	}

	public void UpdateLine(List<Object> values, int row, int col) throws Exception {
		try {
			ArrayList<List<Object>> update = new ArrayList<List<Object>>();
			update.add(values);
			ValueRange body = new ValueRange().setValues(update);
			getGoogleSheets().spreadsheets().values()
					.update(getSpreadsheetId(), getA1(row, col, row, (values.size() - 1 + col)), body)
					.setValueInputOption("USER_ENTERED").execute();

		} catch (java.net.SocketTimeoutException e) {
			throw new Exception("Timeout Exception Google");
		} catch (Exception e) {
			throw e;
		}
	}

	public List<List<Object>> read() throws Exception {
		return read(-1, -1);
	}

	public List<List<Object>> read(int firstLine, int lastLine) throws Exception {
		int li, le;
		String range;
		if (lastLine == -1 && firstLine == -1) {
			range = "A1:ZZ50";
			li = 1;
			le = 50;
		} else {
			li = firstLine;
			le = ((lastLine - firstLine) < 50 ? lastLine : 50);
			range = "A" + firstLine + ":ZZ" + le;
		}
		int inc = le;
		int i = 5;
		ValueRange response = null;
		List<List<Object>> values = new ArrayList<List<Object>>();
		// Thread currentThread = Thread.currentThread();
		// synchronized (currentThread) {
		while (response == null) {
			// Executor executor = Executors.newSingleThreadExecutor();
			try {
				response = getGoogleSheets().spreadsheets().values().get(getSpreadsheetId(), range).execute();
			} catch (IOException e) {
				// System.out.println("Waiting Google quota " +i);
				// executor.execute(new Runnable() { public void run() {
				try {
					// long start = Calendar.getInstance().getTimeInMillis();
					Thread.sleep((long) java.lang.Math.pow(2, (i > 12 ? 12 : i)));
					// long total = Calendar.getInstance().getTimeInMillis() - start;
					// System.out.println("Dormiu por ====> " + total + "ms, esperado [" +
					// (long)java.lang.Math.pow(2,(i>12?12:i)) +"]");
				} catch (InterruptedException e1) {
					throw new Exception("Google quota exceded!");
					// System.out.println("Wait interrupted");
				}
				// } });

				if (i == 50)
					throw new Exception("Google quota exceded!");
				i++;
				continue;
			}

			if (response != null && response.getValues() != null) {
				boolean temDados = false;
				for (List<Object> l : response.getValues()) {
					for (Object o : l) {
						if (o != null && !o.toString().trim().equals("")) {
							temDados = true;
						} else {
							temDados = false;
						}
					}
					if (temDados)
						values.add(l);
				}
				if (!temDados || response.getValues().size() < inc)
					break;
				response = null;
				if (lastLine == -1 && firstLine == -1) {
					li += inc;
					le += inc;
					range = "A" + li + ":ZZ" + le;
				} else {
					li += inc;
					if (li > lastLine)
						break;
					le += inc;
					if (le > lastLine)
						le = lastLine;
					range = "A" + li + ":ZZ" + le;
				}
			}
		}
		// }

		return values;
	}

	public void ClearRange(String range) throws Exception {

		ClearValuesResponse result = getGoogleSheets().spreadsheets().values()
				.clear(getSpreadsheetId(), range, new ClearValuesRequest()).execute();
		System.out.printf("%s", result.getClearedRange());

	}

	
}