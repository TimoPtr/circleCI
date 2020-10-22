/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core.avro;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import android.app.job.JobParameters;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.sdkws.core.IKolibreeConnector;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Spy;

/** Created by miguelaragues on 12/3/18. */
public class AvroUploaderJobServiceTest extends BaseUnitTest {

  @Spy private AvroUploaderJobService jobService;

  /*
  ON START JOB
   */

  @Test
  public void onStartJob_isLoggedInFalse_neverInvokesStartUploadingAvroFile() {
    AvroFileUploader avroFileUploader = mock(AvroFileUploader.class);
    jobService.avroFileUploader = avroFileUploader;

    doReturn(false).when(jobService).isUserLoggedIn();

    doNothing().when(jobService).jobCompletedAndDoesNotNeedReschedule(any(JobParameters.class));

    assertFalse(jobService.onStartJob(mock(JobParameters.class)));

    verify(jobService, never()).startUploadingAvroFile(any(JobParameters.class));
  }

  @Test
  public void onStartJob_isLoggedInFalse_invokesAvroFileUploaderDeletePendingFiles() {
    AvroFileUploader avroFileUploader = mock(AvroFileUploader.class);
    jobService.avroFileUploader = avroFileUploader;

    doReturn(false).when(jobService).isUserLoggedIn();

    doNothing().when(jobService).jobCompletedAndDoesNotNeedReschedule(any(JobParameters.class));

    jobService.onStartJob(mock(JobParameters.class));

    verify(avroFileUploader).deletePendingFiles();
  }

  @Test
  public void onStartJob_isLoggedInTrue_startUploadingAvroFileTrue_returnsTrue() {
    doReturn(true).when(jobService).isUserLoggedIn();

    JobParameters jobParameters = mock(JobParameters.class);

    doReturn(true).when(jobService).startUploadingAvroFile(jobParameters);

    assertTrue(jobService.onStartJob(jobParameters));
  }

  @Test
  public void onStartJob_isLoggedInTrue_startUploadingAvroFileFalse_returnsFalse() {
    doReturn(true).when(jobService).isUserLoggedIn();

    JobParameters jobParameters = mock(JobParameters.class);

    doReturn(false).when(jobService).startUploadingAvroFile(jobParameters);

    assertFalse(jobService.onStartJob(jobParameters));
  }

  /*
  IS USER LOGGED IN
   */
  @Test
  public void isUserLoggedIn_connectorGetAccountReturnsNull_returnsFalse() {
    IKolibreeConnector connector = mock(IKolibreeConnector.class);
    when(connector.hasConnectedAccount()).thenReturn(false);
    jobService.connector = connector;

    assertFalse(jobService.isUserLoggedIn());
  }

  @Test
  public void isUserLoggedIn_connectorGetAccountReturnsAccount_returnsTrue() {
    IKolibreeConnector connector = mock(IKolibreeConnector.class);
    when(connector.hasConnectedAccount()).thenReturn(true);
    jobService.connector = connector;

    assertTrue(jobService.isUserLoggedIn());
  }

  /*
  START UPLOADING AVRO FILE
   */
  @Test
  public void
      startUploadingAvroFile_extractAndValidateFileNull_invokesJobCompletedAndDoesNotNeedReschedule() {
    JobParameters jobParameters = mock(JobParameters.class);

    doNothing().when(jobService).jobCompletedAndDoesNotNeedReschedule(any(JobParameters.class));
    doReturn(null).when(jobService).extractAndValidateFile(jobParameters);

    jobService.startUploadingAvroFile(jobParameters);

    verify(jobService).jobCompletedAndDoesNotNeedReschedule(jobParameters);
  }

  @Test
  public void startUploadingAvroFile_extractAndValidateFileNull_returnsFalse() {
    JobParameters jobParameters = mock(JobParameters.class);

    doNothing().when(jobService).jobCompletedAndDoesNotNeedReschedule(any(JobParameters.class));
    doReturn(null).when(jobService).extractAndValidateFile(jobParameters);

    assertFalse(jobService.startUploadingAvroFile(jobParameters));
  }

  @Test
  public void startUploadingAvroFile_extractAndValidateFileNotNull_storesUploadFileDisposable()
      throws IOException {
    JobParameters params = mock(JobParameters.class);

    doReturn(mock(File.class)).when(jobService).extractAndValidateFile(params);

    assertNull(jobService.uploadFileDisposable);

    AvroFileUploader avroFileUploader = mock(AvroFileUploader.class);
    jobService.avroFileUploader = avroFileUploader;

    when(avroFileUploader.uploadFileAndDeleteOnSuccess(any(File.class)))
        .thenReturn(Completable.complete());
    doNothing().when(jobService).jobCompletedAndDoesNotNeedReschedule(any(JobParameters.class));

    jobService.startUploadingAvroFile(params);

    assertNotNull(jobService.uploadFileDisposable);
  }

  @Test
  public void startUploadingAvroFile_extractAndValidateFileNotNull_returnsTrue()
      throws IOException {
    JobParameters params = mock(JobParameters.class);

    doReturn(mock(File.class)).when(jobService).extractAndValidateFile(params);

    AvroFileUploader avroFileUploader = mock(AvroFileUploader.class);
    jobService.avroFileUploader = avroFileUploader;

    when(avroFileUploader.uploadFileAndDeleteOnSuccess(any(File.class)))
        .thenReturn(Completable.complete());
    doNothing().when(jobService).jobCompletedAndDoesNotNeedReschedule(any(JobParameters.class));

    assertTrue(jobService.startUploadingAvroFile(params));
  }

  @Test
  public void
      startUploadingAvroFile_extractAndValidateFileNotNull_invokesUploadFileAndDeleteOnSuccess()
          throws IOException {
    JobParameters params = mock(JobParameters.class);

    File expectedFile = mock(File.class);
    doReturn(expectedFile).when(jobService).extractAndValidateFile(params);

    AvroFileUploader avroFileUploader = mock(AvroFileUploader.class);
    jobService.avroFileUploader = avroFileUploader;

    when(avroFileUploader.uploadFileAndDeleteOnSuccess(any(File.class)))
        .thenReturn(Completable.complete());
    doNothing().when(jobService).jobCompletedAndDoesNotNeedReschedule(any(JobParameters.class));

    jobService.startUploadingAvroFile(params);

    verify(avroFileUploader).uploadFileAndDeleteOnSuccess(expectedFile);
  }

  @Test
  public void
      startUploadingAvroFile_extractAndValidateFileNotNull_uploadFileSucceeds_invokesJobCompletedAndDoesNotNeedReschedule()
          throws IOException {
    JobParameters params = mock(JobParameters.class);

    File expectedFile = mock(File.class);
    doReturn(expectedFile).when(jobService).extractAndValidateFile(params);

    AvroFileUploader avroFileUploader = mock(AvroFileUploader.class);
    jobService.avroFileUploader = avroFileUploader;

    when(avroFileUploader.uploadFileAndDeleteOnSuccess(any(File.class)))
        .thenReturn(Completable.complete());
    doNothing().when(jobService).jobCompletedAndDoesNotNeedReschedule(any(JobParameters.class));

    jobService.startUploadingAvroFile(params);

    verify(jobService).jobCompletedAndDoesNotNeedReschedule(params);
  }

  @Test
  public void
      startUploadingAvroFile_extractAndValidateFileNotNull_uploadFileError_invokesJobFinishAndReschedule()
          throws IOException {
    JobParameters params = mock(JobParameters.class);

    File expectedFile = mock(File.class);
    doReturn(expectedFile).when(jobService).extractAndValidateFile(params);

    AvroFileUploader avroFileUploader = mock(AvroFileUploader.class);
    jobService.avroFileUploader = avroFileUploader;

    when(avroFileUploader.uploadFileAndDeleteOnSuccess(any(File.class)))
        .thenReturn(Completable.error(new IllegalStateException("Error forced in test")));
    doNothing().when(jobService).jobFinished(any(JobParameters.class), anyBoolean());

    jobService.startUploadingAvroFile(params);

    verify(jobService).jobFinished(params, true);
  }

  /*
  ON STOP JOB
   */
  @Test
  public void onStopJob_noDisposable_paramsContainValidFileNull_returnsTrueSoThatItsRescheduled() {
    assertTrue(jobService.onStopJob(mock(JobParameters.class)));
  }

  @Test
  public void onStopJob_noDisposable_paramsContainValidFileTrue_returnsTrue() {
    jobService.paramsContainValidFile = true;

    assertTrue(jobService.onStopJob(mock(JobParameters.class)));
  }

  @Test
  public void onStopJob_noDisposable_paramsContainValidFileFalse_returnsFalse() {
    jobService.paramsContainValidFile = false;

    assertFalse(jobService.onStopJob(mock(JobParameters.class)));
  }

  @Test
  public void onStopJob_withDisposable_invokesDispose() {
    jobService.uploadFileDisposable = mock(Disposable.class);

    jobService.onStopJob(mock(JobParameters.class));

    verify(jobService.uploadFileDisposable).dispose();
  }
}
