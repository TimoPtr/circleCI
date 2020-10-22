/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core.avro;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kolibree.sdkws.test.BaseJobServiceTest;
import io.reactivex.Completable;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

/** Created by miguelaragues on 14/3/18. */
public class AvroUploaderJobServiceIntegrationTest extends BaseJobServiceTest {

  @Test
  public void userNotLoggedIn_doesntAttemptUploadAndDeletesCurrentFiles()
      throws InterruptedException, IOException {
    File file = File.createTempFile("test_generated", null);

    when(component().connector().hasConnectedAccount()).thenReturn(false);

    verify(component().avroFileUploader(), never()).deletePendingFiles();

    launchJobAndWait(AvroUploaderJobService.uploaderJobInfo(context(), file));

    verify(component().avroFileUploader()).deletePendingFiles();
    verify(component().avroFileUploader(), never()).uploadFileAndDeleteOnSuccess(any(File.class));
  }

  @Test
  public void userLoggedIn_attemptsToUploadFile() throws IOException, InterruptedException {
    File file = File.createTempFile("test_generated", null);

    when(component().connector().hasConnectedAccount()).thenReturn(true);

    when(component().avroFileUploader().uploadFileAndDeleteOnSuccess(file))
        .thenReturn(Completable.complete());

    launchJobAndWait(AvroUploaderJobService.uploaderJobInfo(context(), file));

    verify(component().avroFileUploader()).uploadFileAndDeleteOnSuccess(file);
  }
}
