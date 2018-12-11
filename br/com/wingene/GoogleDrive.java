package br.com.wingene;

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

public class GoogleDrive extends GoogleApiBase {

	/**
	 * Insert a new permission.
	 *
	 * @param fileId ID of the file to insert permission for.
	 * @param value  User or group e-mail address, domain name or {@code null}
	 *               "default" type.
	 * @param type   The value "user", "group", "domain" or "default".
	 * @param role   The value "owner", "writer" or "reader".
	 * @return The inserted permission if successful, {@code null} otherwise.
	 */
	public void setPermission(String fileId, String email) throws IOException {

		JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
			@Override
			public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
				// Handle error
				System.err.println(e.getMessage());
			}

			@Override
			public void onSuccess(Permission permission, HttpHeaders responseHeaders) throws IOException {
				System.out.println("Permission ID: " + permission.getId());
			}
		};
		BatchRequest batch = getGoogleDrive().batch();
		Permission userPermission = new Permission().setType("user").setRole("writer").setEmailAddress(email);
		getGoogleDrive().permissions().create(fileId, userPermission).setFields("id").queue(batch, callback);

		/*
		 * Permission domainPermission = new
		 * Permission().setType("domain").setRole("reader").setDomain("example.com");
		 * driveService.permissions().create(fileId,
		 * domainPermission).setFields("id").queue(batch, callback);
		 */
		batch.execute();
	}

	public static Drive getGoogleDrive() throws IOException {
		Credential credential = authorize();
		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(credential))
				.setApplicationName(APPLICATION_NAME).build();
	}

}