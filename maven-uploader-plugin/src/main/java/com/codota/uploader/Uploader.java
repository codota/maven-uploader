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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class Uploader {

    private final String endpoint;
    private final CloseableHttpClient httpClient;
    private final String token;
    private final String stars;
    private final String srcDirUrl;

    public Uploader(String codotaEndpoint, String token, String stars, String srcDirUrl) {
        this.endpoint = codotaEndpoint;
        this.stars = stars;
        this.httpClient = HttpClientBuilder.create().build();
        this.token = token;
        this.srcDirUrl = srcDirUrl;
    }


    public void uploadFiles(Collection<File> files) throws IOException, URISyntaxException {
        uploadFiles(files, endpoint);
    }

    private void uploadFiles(Collection<File> files, String uploadUrl) throws IOException, URISyntaxException {


            URIBuilder uriBuilder = new URIBuilder(uploadUrl);

            if (stars !=null) {
                uriBuilder.setParameter("stars", stars);

            }
            if (srcDirUrl !=null) {
                uriBuilder.setParameter("srcDirUrl", srcDirUrl);

            }

            URI uri = uriBuilder.build();
            System.out.println("uri: "+uri);
            HttpPut putRequest = new HttpPut(uri);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (File file : files) {
                builder.addPart("code", new FileBody(file));
            }


            final HttpEntity entity = builder.build();
            putRequest.setEntity(entity);

            putRequest.setHeader("enctype", "multipart/form-data");
            putRequest.setHeader("authorization", "bearer " + token);
            httpClient.execute(putRequest, new UploadResponseHandler(files));


    }


    private class UploadResponseHandler implements ResponseHandler<Object> {


        private Collection<File> files;

        public UploadResponseHandler(Collection<File> files) {
            this.files = files;
        }
        @Override
        public Object handleResponse(HttpResponse response)
                throws IOException {
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                System.out.println("Success! " + responseString);
                System.out.println("File uploaded to codota: " + files);
            } else {
                System.out.println("Request failed with status " + responseString + response.toString());
                 System.out.println("Failed to upload file to codota: " + files);
            }
            return null;
        }

    }


}
