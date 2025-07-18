package com.mojang.realmsclient.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.gui.screens.UploadResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.User;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FileUpload {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_RETRIES = 5;
    private static final String UPLOAD_PATH = "/upload";
    private final File file;
    private final long realmId;
    private final int slotId;
    private final UploadInfo uploadInfo;
    private final String sessionId;
    private final String username;
    private final String clientVersion;
    private final String worldVersion;
    private final UploadStatus uploadStatus;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    @Nullable
    private CompletableFuture<UploadResult> uploadTask;
    private final RequestConfig requestConfig = RequestConfig.custom()
        .setSocketTimeout((int)TimeUnit.MINUTES.toMillis(10L))
        .setConnectTimeout((int)TimeUnit.SECONDS.toMillis(15L))
        .build();

    public FileUpload(File file, long realmId, int slotId, UploadInfo uploadInfo, User user, String clientVersiob, String worldVersion, UploadStatus uploadStatus) {
        this.file = file;
        this.realmId = realmId;
        this.slotId = slotId;
        this.uploadInfo = uploadInfo;
        this.sessionId = user.getSessionId();
        this.username = user.getName();
        this.clientVersion = clientVersiob;
        this.worldVersion = worldVersion;
        this.uploadStatus = uploadStatus;
    }

    public void upload(Consumer<UploadResult> resultConsumer) {
        if (this.uploadTask == null) {
            this.uploadTask = CompletableFuture.supplyAsync(() -> this.requestUpload(0));
            this.uploadTask.thenAccept(resultConsumer);
        }
    }

    public void cancel() {
        this.cancelled.set(true);
        if (this.uploadTask != null) {
            this.uploadTask.cancel(false);
            this.uploadTask = null;
        }
    }

    /**
     * @param retries The number of times this upload has already been attempted
     */
    private UploadResult requestUpload(int retries) {
        UploadResult.Builder uploadresult$builder = new UploadResult.Builder();
        if (this.cancelled.get()) {
            return uploadresult$builder.build();
        } else {
            this.uploadStatus.totalBytes = this.file.length();
            HttpPost httppost = new HttpPost(this.uploadInfo.getUploadEndpoint().resolve("/upload/" + this.realmId + "/" + this.slotId));
            CloseableHttpClient closeablehttpclient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();

            UploadResult uploadresult;
            try {
                this.setupRequest(httppost);
                HttpResponse httpresponse = closeablehttpclient.execute(httppost);
                long i = this.getRetryDelaySeconds(httpresponse);
                if (!this.shouldRetry(i, retries)) {
                    this.handleResponse(httpresponse, uploadresult$builder);
                    return uploadresult$builder.build();
                }

                uploadresult = this.retryUploadAfter(i, retries);
            } catch (Exception exception) {
                if (!this.cancelled.get()) {
                    LOGGER.error("Caught exception while uploading: ", (Throwable)exception);
                }

                return uploadresult$builder.build();
            } finally {
                this.cleanup(httppost, closeablehttpclient);
            }

            return uploadresult;
        }
    }

    private void cleanup(HttpPost post, @Nullable CloseableHttpClient httpClient) {
        post.releaseConnection();
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException ioexception) {
                LOGGER.error("Failed to close Realms upload client");
            }
        }
    }

    private void setupRequest(HttpPost post) throws FileNotFoundException {
        post.setHeader(
            "Cookie",
            "sid="
                + this.sessionId
                + ";token="
                + this.uploadInfo.getToken()
                + ";user="
                + this.username
                + ";version="
                + this.clientVersion
                + ";worldVersion="
                + this.worldVersion
        );
        FileUpload.CustomInputStreamEntity fileupload$custominputstreamentity = new FileUpload.CustomInputStreamEntity(
            new FileInputStream(this.file), this.file.length(), this.uploadStatus
        );
        fileupload$custominputstreamentity.setContentType("application/octet-stream");
        post.setEntity(fileupload$custominputstreamentity);
    }

    private void handleResponse(HttpResponse response, UploadResult.Builder uploadResult) throws IOException {
        int i = response.getStatusLine().getStatusCode();
        if (i == 401) {
            LOGGER.debug("Realms server returned 401: {}", response.getFirstHeader("WWW-Authenticate"));
        }

        uploadResult.withStatusCode(i);
        if (response.getEntity() != null) {
            String s = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (s != null) {
                try {
                    JsonParser jsonparser = new JsonParser();
                    JsonElement jsonelement = jsonparser.parse(s).getAsJsonObject().get("errorMsg");
                    Optional<String> optional = Optional.ofNullable(jsonelement).map(JsonElement::getAsString);
                    uploadResult.withErrorMessage(optional.orElse(null));
                } catch (Exception exception) {
                }
            }
        }
    }

    private boolean shouldRetry(long retryDelaySeconds, int retries) {
        return retryDelaySeconds > 0L && retries + 1 < 5;
    }

    private UploadResult retryUploadAfter(long seconds, int retries) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(seconds).toMillis());
        return this.requestUpload(retries + 1);
    }

    private long getRetryDelaySeconds(HttpResponse httpResponse) {
        return Optional.ofNullable(httpResponse.getFirstHeader("Retry-After")).map(NameValuePair::getValue).map(Long::valueOf).orElse(0L);
    }

    public boolean isFinished() {
        return this.uploadTask.isDone() || this.uploadTask.isCancelled();
    }

    @OnlyIn(Dist.CLIENT)
    static class CustomInputStreamEntity extends InputStreamEntity {
        private final long length;
        private final InputStream content;
        private final UploadStatus uploadStatus;

        public CustomInputStreamEntity(InputStream content, long length, UploadStatus uploadStatus) {
            super(content);
            this.content = content;
            this.length = length;
            this.uploadStatus = uploadStatus;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            Args.notNull(out, "Output stream");
            InputStream inputstream = this.content;

            try {
                byte[] abyte = new byte[4096];
                int j;
                if (this.length < 0L) {
                    while ((j = inputstream.read(abyte)) != -1) {
                        out.write(abyte, 0, j);
                        this.uploadStatus.bytesWritten += (long)j;
                    }
                } else {
                    long i = this.length;

                    while (i > 0L) {
                        j = inputstream.read(abyte, 0, (int)Math.min(4096L, i));
                        if (j == -1) {
                            break;
                        }

                        out.write(abyte, 0, j);
                        this.uploadStatus.bytesWritten += (long)j;
                        i -= (long)j;
                        out.flush();
                    }
                }
            } finally {
                inputstream.close();
            }
        }
    }
}
