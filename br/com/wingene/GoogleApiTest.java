package br.com.wingene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleApiTest {

	public static void main(String[] args) {
		GoogleSheets gst = new GoogleSheets("10KgPHzhBteSzz9tTqFPtMuDKk6Zpa74AvF1Td_Kpjn8");
		try {
			//GoogleSheet gs = GoogleSheets.createSpreadSheet("Hello World");

			//(new GoogleDrive()).setPermission("10KgPHzhBteSzz9tTqFPtMuDKk6Zpa74AvF1Td_Kpjn8","jeugenio.ufmg@gmail.com");

			List<Object> row = new ArrayList<Object>();
			row.add("Aniversário");
			row.add("Nome");
			row.add("Idade");
			gst.InsertLine(row);
			row = new ArrayList<Object>();
			row.add("10/12/1942");
			row.add("Maria José");
			row.add("76");
			gst.InsertLine(row);
			row = new ArrayList<Object>();
			row.add("Vicente");
			row.add("85");
			gst.InsertLine(row, 2);

			ArrayList<Object> alterar = new ArrayList<Object>();
			alterar.add("Age");
			gst.UpdateLine(alterar, 1, 3);
			alterar = new ArrayList<Object>();
			alterar.add("Birthday");
			gst.UpdateLine(alterar, 1, 1);

			System.err.println("Tests completed sucessfuly");
			// gst.deleteLines(1, 1);
			/*
			 * ArrayList<Object> inserir = new ArrayList<Object>(); inserir.add("D");
			 * inserir.add("A"); inserir.add("D"); try { gsd.UpdateLine(inserir); } catch
			 * (com.google.api.client.googleapis.json.GoogleJsonResponseException ge) {
			 * System.err.println( ge.getDetails().getMessage()); } catch (Exception e) {
			 * e.printStackTrace(); }
			 */
			// gsd.ClearRange("A1:C1");
			// gsd.readByFilter("Teste", 7);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void preencheTabelas(HashMap<String, String> tabelas, String labKeyDir) throws IOException {
		tabelas.clear();
		Drive service = GoogleDrive.getGoogleDrive();
		try {
			FileList res = service.files().list()
					.setQ("name = '" + labKeyDir + "' and mimeType = 'application/vnd.google-apps.folder'")
					.setFields("nextPageToken, files(id)").execute();
			if (res.getFiles().size() == 0) {
				System.out.println("Directory " + labKeyDir + " is empty!");
				return;
			}
			String idFolder = res.getFiles().iterator().next().getId();
			FileList result = service.files().list()
					.setQ("'" + idFolder
							+ "' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
					.setFields("nextPageToken, files(id, name)").execute();
			List<File> files = result.getFiles();
			if (files == null || files.isEmpty()) {
				System.out.println("No files found.");
			} else {
				for (File file : files) {
					tabelas.put(file.getName(), file.getId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
