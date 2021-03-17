package licenta.util;

import com.google.cloud.storage.Bucket;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import licenta.exception.ExceptionMessage;
import licenta.exception.definition.InternalServerErrorException;
import licenta.util.enumeration.Configuration;

import javax.ws.rs.core.Response;
import java.io.*;

public final class StorageUtil {

    public static Bucket getDefaultBucket() throws InternalServerErrorException {
        try {
            String bucketName = Util.getValueOfConfigVariable(Configuration.BUCKET_NAME);
            InputStream serviceAccount =
                    new ByteArrayInputStream(Util.getValueOfConfigVariable(Configuration.PERMISSIONS).getBytes());

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(bucketName)
                        .build();
                FirebaseApp.initializeApp(options);
            }

            return StorageClient.getInstance().bucket();
        } catch (IOException e) {
            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
