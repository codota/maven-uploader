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

import com.codota.uploader.Uploader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * To use this test:
 * 1. obtain a valid toke from Codota.
 * 2. update the filename variable to contain a valid file name.
 */
public class TestUploader {

    // constants
    final String endpoint = "https://upload.codota.com/artifacts";
    // STEP 1: obtain a valid token.
    //         Without a valid token your upload request would be rejected.
    final String token = "<TEST_TOKEN>";
    final String stars = "100";
    final String srcUrl = "https://github.com/translation-cards/translation-cards/tree/master";

    // optional: can change these as you wish
    final String groupId = "testGroup";
    final String artifactId = "testArtifactId";
    final String version = "1.0";

    @Test
    public void testFileUpload() throws IOException, URISyntaxException {
        // STEP 2: update <FILENAME> to point to a valid filename on your system.
        String filename = "<FILENAME>";
        String uploadUrl = endpoint + "/" + groupId + "." + artifactId + "/" + version;

        Uploader uploader = new Uploader(uploadUrl, token, stars, "");
        uploader.uploadFiles(Collections.singleton(new File(filename)));
    }
}
