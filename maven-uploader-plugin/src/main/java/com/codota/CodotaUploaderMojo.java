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

package com.codota;

import com.codota.uploader.Uploader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"JavaDoc", "unused"})
@Mojo(name = "upload")
public class CodotaUploaderMojo
        extends AbstractMojo {

    /**
     * Location of the target files.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File buildDirectory;

    /**
     * Source directory.
     */
    @Parameter(defaultValue = "${project.build.sourceDirectory}", required =  true)
    private File sourceDirectory;

    @Parameter(defaultValue = "${project.artifactId}", readonly = true)
    private String artifactId;

    @Parameter(defaultValue = "${project.groupId}", readonly = true)
    private String groupId;

    @Parameter(defaultValue = "${project.version}", readonly = true)
    private String version;

    /**
     * Location of the result file.
     */
    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;


    /**
     * Codota name passed to the uploader
     */
    @Parameter(property = "token", defaultValue = "${codota.token}", required = true)
    private String token;


    /**
     * Codota endpoint passed to the uploader
     */
    @Parameter(property = "endpoint", defaultValue = "${codota.endpoint}", required = true)
    private String endpoint;



    /**
     * Optional project prefix passed to the uploader, will be prepended to the artifact name
     */
    @Parameter(property = "projectPrefix", defaultValue = "${codota.projectPrefix}", required = false)
    private String projectPrefix;

    /**
     * Repository GH  passed to the uploader, string, int
     */
    @Parameter(property = "stars", defaultValue = "${codota.stars}", required = false)
    private String stars;

    /**
     * Repository url, used to calculate source url, string
     */
    @Parameter(property = "repoUrl", defaultValue = "${codota.repoUrl}", required = false)
    private String repoUrl;

    /**
     * Base directory of the code, used to calculate source url, string
     */
    @Parameter(property = "baseDir", defaultValue = "${codota.baseDir}", required = false)
    private String baseDir;

    @Parameter(property = "reportFileName")
    private String reportFileName;


    public void execute()
            throws MojoExecutionException {

        if (version == null) {
            version = "NO_VERSION";
        }

        logParameters();
        ensureExists(buildDirectory);

        /**
         * @todo: add sanitization to the endpoint url string
         */
        final Uploader uploader;
        uploader = new Uploader(uploadUrl(), token, stars, getSrcDirUrl());

        final List<File> filesToUpload = new ArrayList<>();

        // Visit target directory and upload every jar file
        FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
            boolean root = true;
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (root) {
                    root = false;
                    return FileVisitResult.CONTINUE;
                }
                getLog().info("Not going into directory " + dir);
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                    throws IOException {
                if (shouldSendFile(path)) {
                    filesToUpload.add(path.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            Files.walkFileTree(buildDirectory.toPath(), fv);
            getLog().info("Uploading files " + filesToUpload);
            uploader.uploadFiles(filesToUpload);
            getLog().info("Done uploading");

        } catch (Exception e) {
            // print the task trace and give up on handling the exception
            e.printStackTrace();
        }
    }

    /**
     * Construct projectPrefix to append as the prefix of artifact name.
     * Returns "" if projectPrefix is null or empty and "prefixName:" else.
     */
    private String safeProjectPrefix() {
        return (this.projectPrefix == null || this.projectPrefix.trim().equals("")) ? "" :  this.projectPrefix+":";
    }

    /**
     * Construct artifact name from groupId and artifactId.
     * Can add version if you care to make this distinction when uploading artifacts
     */
    private String artifactName() {
        return safeProjectPrefix() + this.groupId + "." + this.artifactId;
    }

    private String uploadUrl() {
        return endpoint + "/" + artifactName() + "/" + version;
    }

    private void ensureExists(File f) {
        if (!f.exists()) {
            if (f.mkdirs()) {
                getLog().info("Created directory " + f);
            }
        }
    }

    private void logParameters() {
        getLog().info("Codota Uploader - Started " + artifactName() + " version " + version);
        getLog().info("Endpoint:" + endpoint);
        getLog().info("Build directory:" + buildDirectory);
        getLog().info("Source directory:" + sourceDirectory);
        getLog().info("Codota token exists:" + (token != null));
        getLog().info("Codota stars: " + stars);
        getLog().info("Codota repoUrl: " + repoUrl);
        getLog().info("Codota baseDir: " + baseDir);

    }

    private boolean shouldSendFile(Path path) {
        return path.toString().toLowerCase().endsWith(".jar") || path.toString().toLowerCase().endsWith(".war");
    }


    private  String getSrcDirUrl() {
        getLog().info("Codota getSrcDirUrl: " );

        String src = null;
        String srcDir = sourceDirectory.getPath();
        if (baseDir != null && repoUrl !=null && srcDir.startsWith(baseDir)) {

            src = repoUrl+ "/"+ srcDir.substring(baseDir.length())+"/";
            src = src.replaceAll("//", "/");

        }
        return src;

    }

}
