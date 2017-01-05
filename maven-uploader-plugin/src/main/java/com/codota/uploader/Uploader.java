/*
 * Copyright 2015 Codota
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codota.uploader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ContentType;
import java.io.File;
import java.io.IOException;

public class Uploader {

    private final String endpoint;
    private final CloseableHttpClient httpClient;
    private final String token;
    private final String repoName;
    private final String lastPushed;
    private final String stars;

    public Uploader(String codotaEndpoint, String token, String repoName, String lastPushed, String stars) {
        this.endpoint = codotaEndpoint;
        this.repoName = repoName;
        this.lastPushed = lastPushed;
        this.stars = stars;
        this.httpClient = HttpClientBuilder.create().build();
        this.token = token;
    }


    public void uploadFile(File file) throws IOException {
        uploadFile(file, endpoint);
    }

    private void uploadFile(File file, String uploadUrl) throws IOException {
            HttpPut putRequest = new HttpPut(uploadUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("code", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
            //builder.addPart("code", new FileBody(file));
            builder.addTextBody("repoName", repoName);
            builder.addTextBody("lastPushed", lastPushed);
            builder.addTextBody("stars", stars);

            final HttpEntity entity = builder.build();
            putRequest.setEntity(entity);

            putRequest.setHeader("enctype", "multipart/form-data");
            putRequest.setHeader("authorization", "bearer " + token);
            httpClient.execute(putRequest, new UploadResponseHandler(file));
    }


    private class UploadResponseHandler implements ResponseHandler<Object> {


        private File file;

        public UploadResponseHandler(File file) {
            this.file = file;
        }
        @Override
        public Object handleResponse(HttpResponse response)
                throws IOException {
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                System.out.println("Success! " + responseString);
                System.out.println("File uploaded to codota: " + file);
            } else {
                System.out.println("Request failed with status " + responseString + response.toString());
                 System.out.println("Failed to upload file to codota: " + file);
            }
            return null;
        }

    }


}
