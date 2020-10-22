package timber.log;

import android.os.Environment;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * This class writes log lines according to the csv format specified by Medicomp.
 *
 * <p>It's placed in the timber.log package so that we can override getTag as in DebugTree (from
 * which some code has been extracted). If at any given moment we want to force ourselves into
 * specifying the tag each time we call Timber.x, we should move it to a package of our own and
 * remove all the tag related code. Using current implementation we can still specify a tag by using
 *
 * <p>Timber.tag("mytag").x("message");
 *
 * <p>Also, when writing the log we should check that tag is not null to avoid writing null values
 * to the log.
 *
 * <p>It uses android-logback as logging system
 *
 * <p>Created by miguelaragues on 8/2/17.
 */
public class FileLoggingTree extends Timber.Tree {

  // private final Logger logger;
  private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");
  private static final int CALL_STACK_INDEX = 5;
  private static final int MAX_TAG_LENGTH = 23;
  private static final String LOG_SEPARATOR = ", ";

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private static final String LOG_NAME = "MessageLog";

  private String fileName;

  private static volatile File logFile;

  private final Object fileLock = new Object();

  @Override
  final String getTag() {
    String tag = super.getTag();
    if (tag != null) {
      return tag;
    }

    // DO NOT switch this to Thread.getCurrentThread().getStackTrace(). The test will pass
    // because Robolectric runs them on the JVM but on Android the elements are different.
    StackTraceElement[] stackTrace = new Throwable().getStackTrace();
    if (stackTrace.length <= CALL_STACK_INDEX) {
      throw new IllegalStateException(
          "Synthetic stacktrace didn't have enough elements: are you using proguard?");
    }
    return createStackElementTag(stackTrace[CALL_STACK_INDEX]);
  }

  /**
   * Extract the tag which should be used for the message from the {@code element}. By default this
   * will use the class name without any anonymous class suffixes (e.g., {@code Foo$1} becomes
   * {@code Foo}).
   *
   * <p>Note: This will not be called if a manual tag was specified.
   */
  private String createStackElementTag(StackTraceElement element) {
    String tag = element.getClassName();
    Matcher m = ANONYMOUS_CLASS.matcher(tag);
    if (m.find()) {
      tag = m.replaceAll("");
    }
    tag = tag.substring(tag.lastIndexOf('.') + 1);
    return tag.length() > MAX_TAG_LENGTH ? tag.substring(0, MAX_TAG_LENGTH) : tag;
  }

  private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

  @Override
  protected void log(int priority, String tag, String message, Throwable t) {
    if (getFile() == null) return;

    // see DataBlockProviderStorIO for an example of batched inserts
    EXECUTOR_SERVICE.execute(
        () -> {
          BufferedWriter buf = null;
          try {
            // BufferedWriter for performance, true to set append to logFile flag
            buf = new BufferedWriter(new FileWriter(getFile(), true));
            buf.append(ZonedDateTime.now().format(DATE_TIME_FORMATTER));
            buf.append(LOG_SEPARATOR);
            buf.append(extractPriority(priority));
            buf.append(LOG_SEPARATOR);
            buf.append(tag);
            buf.append(LOG_SEPARATOR);
            buf.append(message);
            buf.newLine();
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            if (buf != null) {
              try {
                buf.flush();
              } catch (IOException e) {
                e.printStackTrace();
              } finally {
                try {
                  buf.close();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            }
          }
        });
  }

  private String extractPriority(int priority) {
    switch (priority) {
      case Log.VERBOSE:
      case Log.DEBUG:
        return "DEBUG";
      case Log.WARN:
        return "WARNING";
      case Log.ERROR:
        return "ERROR";
      case Log.INFO:
      default:
        return "INFO";
    }
  }

  private File getFile() {
    if (logFile == null) {
      synchronized (fileLock) {
        if (logFile == null) {
          File directory = Environment.getExternalStorageDirectory();

          if (directory.exists() || directory.mkdirs()) {
            logFile = new File(directory, fileName());

            Timber.i("Creating log logFile at " + logFile.getAbsolutePath());
          } else {
            Timber.e("Couldn't create log file");
          }
        }
      }
    }

    return logFile;
  }

  @NonNull
  private String fileName() {
    if (fileName == null) {
      fileName =
          LOG_NAME
              + "_"
              + ZonedDateTime.now().format(DateTimeFormatter.ofPattern(FILE_DATETIME_PATTERN))
              + ".csv";
    }

    return fileName;
  }

  public static final String FILE_DATETIME_PATTERN = "yyMMdd_HHmmss";
}
