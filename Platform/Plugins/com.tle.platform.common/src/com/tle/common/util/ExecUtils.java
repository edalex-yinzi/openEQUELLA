/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.util;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.tle.common.Triple;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExecUtils {
  public static final String PLATFORM_SOLARIS_X86 = "solaris-x86"; // $NON-NLS-1$
  public static final String PLATFORM_SOLARIS_SPARC = "solaris-sparc"; // $NON-NLS-1$
  public static final String PLATFORM_SOLARIS64 = "solaris64"; // $NON-NLS-1$
  public static final String PLATFORM_LINUX = "linux"; // $NON-NLS-1$
  public static final String PLATFORM_LINUX64 = "linux64"; // $NON-NLS-1$
  public static final String PLATFORM_WIN = "win"; // $NON-NLS-1$
  public static final String PLATFORM_WIN32 = "win32"; // $NON-NLS-1$
  public static final String PLATFORM_WIN64 = "win64"; // $NON-NLS-1$
  public static final String PLATFORM_MAC = "mac"; // $NON-NLS-1$

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecUtils.class);
  private static final String[] EXE_TYPES = {
    "", ".exe", ".bat"
  }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  /**
   * For a given Process ID, kill any child processes and then kill the process. Works on Unix only,
   * as it leverages pgrep and kill commands.
   *
   * @param pid The Process for which to terminate including it's direct children.
   */
  public static void killLinuxProcessTree(int pid) {
    if (pid < 1) {
      throw new IllegalArgumentException("Process ID should be greater than 0");
    }
    // get child process PIDs and kill them
    getChildUnixProcessPids(pid).ifPresent(ExecUtils::sendSigKill);

    // kill process itself, after all the child processes have been terminated
    sendSigKill(pid);
  }

  /**
   * Runs pgrep -P for a given Process ID, to get a list of child processes as Process IDs.
   *
   * @param pid The parent process ID to check for child processes.
   * @return Process IDs for the children of {@code pid}.
   */
  public static Optional<int[]> getChildUnixProcessPids(int pid) {
    Optional<int[]> pids = Optional.empty();
    StringBuilder childPid = new StringBuilder();
    try {
      Process getChildPid = Runtime.getRuntime().exec("pgrep -P " + pid);
      getChildPid.waitFor();

      if (getChildPid.exitValue() == 0) {
        CharStreams.copy(new InputStreamReader(getChildPid.getInputStream()), childPid);
      } else {
        StringBuilder errorOutput = new StringBuilder();
        CharStreams.copy(new InputStreamReader(getChildPid.getErrorStream()), errorOutput);
        LOGGER.warn("getChildPid function did not run properly.\n" + errorOutput);
      }
      getChildPid.destroy();

      String childPidInfo = childPid.toString();
      if (!childPidInfo.isEmpty()) {
        // convert string to array of ints
        pids =
            Optional.of(
                Arrays.stream(childPidInfo.replaceAll("\n", " ").split(" "))
                    .mapToInt(Integer::parseInt)
                    .toArray());
      }
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Error getting child processes for: " + pid, e);
    } catch (NumberFormatException e) {
      LOGGER.error("Unsupported output from 'pgrep -P'. Unable to terminate child processes.");
      LOGGER.error("'pgrep' output: " + childPid.toString());
      LOGGER.error("Parsing exception.", e);
    }
    return pids;
  }

  /**
   * Creates a process which then sends a SIGKILL signal to an array of given processes.
   *
   * @param pids the Process IDs of the processes to kill.
   * @return the exitValue of the SIGKILL process (not the process being killed)
   */
  public static int sendSigKill(int[] pids) {
    try {
      StringBuilder command = new StringBuilder("kill -9 ");
      for (int pid : pids) {
        command.append(pid).append(" ");
      }
      LOGGER.debug("Running command: " + command);
      Process sigKill = Runtime.getRuntime().exec(String.valueOf(command));
      sigKill.waitFor();
      int returnValue = sigKill.exitValue();
      if (sigKill.exitValue() == 0) {
        StringBuilder successOutput = new StringBuilder();
        CharStreams.copy(new InputStreamReader(sigKill.getInputStream()), successOutput);
        LOGGER.debug("Output of kill function: " + successOutput);
      } else {
        StringBuilder errorOutput = new StringBuilder();
        CharStreams.copy(new InputStreamReader(sigKill.getErrorStream()), errorOutput);
        LOGGER.warn("Attempt to terminate processes failed (" + command + "):\n" + errorOutput);
      }
      sigKill.destroy();
      return returnValue;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("killing processes " + Arrays.toString(pids) + " failed.", e);
    }
  }

  /**
   * Overload of above for a single Process ID. Simply calls the int[] version of this method and
   * returns the exitValue.
   *
   * @param pid The Process ID of the process to kill.
   * @return the exitValue of the SIGKILL process (not the process being killed)
   */
  public static int sendSigKill(int pid) {
    return sendSigKill(new int[] {pid});
  }

  /**
   * Gets the process ID (PID) of a given *nix process.
   *
   * @param p The Process of which to get the PID.
   * @return An Optional int. If not on Linux, or if the PID declared field is not available, the
   *     value will be empty.
   */
  public static synchronized Optional<Integer> getPidOfProcess(Process p) {
    Optional<Integer> pid = Optional.empty();

    try {
      if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
        Field f = p.getClass().getDeclaredField("pid");
        f.setAccessible(true);
        pid = Optional.of(f.getInt(p));
        f.setAccessible(false);
      }
    } catch (NoSuchFieldException e) {
      LOGGER.error("The field pid does not exist on the process. Cannot return pid", e);
    } catch (IllegalAccessException | IllegalArgumentException e) {
      LOGGER.error("pid field is inaccessible, or cannot be converted to an integer.", e);
    }
    return pid;
  }

  public static File findExe(File file) {
    return findExe(file.getParentFile(), file.getName());
  }

  public static File findExe(File path, String exe) {
    for (String exeType : EXE_TYPES) {
      File exeFile = new File(path, exe + exeType);
      if (exeFile.exists() && exeFile.canExecute()) {
        return exeFile;
      }
    }
    return null;
  }

  public static ExecResult exec(List<String> options) {
    return exec(options.toArray(new String[options.size()]));
  }

  public static ExecResult exec(String... command) {
    return exec(command, null, null);
  }

  public static ExecResult exec(String command, String[] env, File dir) {
    Map<String, String> envMap = new HashMap<String, String>();
    for (String e : env) {
      StringTokenizer st2 = new StringTokenizer(e, "=");
      if (st2.hasMoreTokens()) {
        String key = st2.nextToken();
        if (st2.hasMoreTokens()) {
          envMap.put(key, st2.nextToken());
        }
      }
    }
    return exec(splitCommand(command), envMap, dir);
  }

  public static ExecResult exec(String[] cmdarray, Map<String, String> additionalEnv, File dir) {
    try {
      final Triple<Process, StreamReader, StreamReader> cp =
          createProcess(cmdarray, additionalEnv, dir);

      final Process proc = cp.getFirst();
      final StreamReader stdOut = cp.getSecond();
      final StreamReader stdErr = cp.getThird();

      int exitStatus;
      while (true) {
        synchronized (proc) {
          exitStatus = proc.waitFor();
          if (stdOut.isFinished() && stdErr.isFinished()) {
            break;
          }
          proc.wait();
        }
      }

      LOGGER.debug("Exec finished");
      return new ExecResult(exitStatus, stdOut.getResult(), stdErr.getResult());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static ExecResult execWithTimeLimit(long maxDurationInSeconds, String[] options) {
    if (maxDurationInSeconds < 1L) {
      LOGGER.debug("maxDurationInSeconds not set. Using a regular non-timed process.");
      return exec(options, null, null);
    }
    return execWithTimeLimit(options, null, null, maxDurationInSeconds);
  }

  public static ExecResult execWithTimeLimit(
      String[] cmdarray, Map<String, String> additionalEnv, File dir, long durationInSeconds) {
    try {
      final Triple<Process, StreamReader, StreamReader> cp =
          createProcess(cmdarray, additionalEnv, dir);
      LOGGER.debug("Started timed process");
      final Process proc = cp.getFirst();
      int pid = getPidOfProcess(proc).orElse(0);
      final StreamReader stdOut = cp.getSecond();
      final StreamReader stdErr = cp.getThird();
      final boolean timeout = !proc.waitFor(durationInSeconds, TimeUnit.SECONDS);
      if (timeout) {
        // If the process is not terminated before the given timeout is reached,
        // kill the process and throw an InterruptedException.
        String platform = determinePlatform();
        if (platform.equals(PLATFORM_LINUX) || platform.equals(PLATFORM_LINUX64)) {
          killLinuxProcessTree(pid);
        } else {
          LOGGER.debug(
              "Platform ("
                  + platform
                  + ") does not support process tree kill. Processes may be left hanging");
        }
        throw new InterruptedException();
      }
      LOGGER.debug("Timed process finished");
      return new ExecResult(proc.exitValue(), stdOut.getResult(), stdErr.getResult());
    } catch (Exception e) {
      if (e instanceof InterruptedException) {
        throw new RuntimeException(
            "Timer of " + durationInSeconds + " seconds on this operation was exceeded.");
      }
      throw new RuntimeException(e);
    }
  }

  public static void execAsync(String cmd) {
    execAsync(splitCommand(cmd), null, null);
  }

  public static void execAsync(String[] cmdarray, Map<String, String> additionalEnv, File dir) {
    try {
      createProcess(cmdarray, additionalEnv, dir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Triple<Process, StreamReader, StreamReader> createProcess(
      String[] cmdarray, Map<String, String> additionalEnv, File dir) throws IOException {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Executing cmd: " + String.join(" ", cmdarray));
      LOGGER.debug(
          "Working directory: "
              + Optional.ofNullable(dir).map(File::getAbsolutePath).orElse("<unspecified>"));
    }

    ProcessBuilder pbuilder = new ProcessBuilder(cmdarray);
    if (additionalEnv != null) {
      Map<String, String> env = pbuilder.environment();
      env.putAll(additionalEnv);
    }
    pbuilder.directory(dir);
    Process proc = pbuilder.start();
    proc.getOutputStream().close();

    final InputStreamReader inErr = new InputStreamReader(proc.getErrorStream());
    final InputStreamReader inStd = new InputStreamReader(proc.getInputStream());

    StreamReader errReader = new StreamReader(inErr, proc);
    StreamReader outReader = new StreamReader(inStd, proc);
    errReader.start();
    outReader.start();

    return new Triple<Process, StreamReader, StreamReader>(proc, outReader, errReader);
  }

  public static String[] splitCommand(String cmd) {
    StringTokenizer st = new StringTokenizer(cmd);
    String[] cmdarray = new String[st.countTokens()];
    for (int i = 0; st.hasMoreTokens(); i++) {
      cmdarray[i] = st.nextToken();
    }
    return cmdarray;
  }

  public static class StreamReader extends Thread {
    private final StringBuilder writer = new StringBuilder();

    private final Reader reader;
    private final Process proc;

    private boolean finished;

    public StreamReader(Reader reader, Process proc) {
      this.reader = reader;
      this.proc = proc;
    }

    @Override
    public void run() {
      try {
        CharStreams.copy(reader, writer);
      } catch (Exception ex) {
        LOGGER.error("Error reading from stream", ex); // $NON-NLS-1$
        // nothing really you can do about this is there?
      } finally {
        finished = true;
        synchronized (proc) {
          proc.notify();
        }

        try {
          Closeables.close(reader, true);
        } catch (IOException ex) {
          // Ignore
        }
      }
    }

    public boolean isFinished() {
      return finished;
    }

    public String getResult() {
      return writer.toString();
    }
  }

  public static class ExecResult {
    private final int exitStatus;
    private final String stdout;
    private final String stderr;

    public ExecResult(final int exitStatus, final String stdout, final String stderr) {
      this.exitStatus = exitStatus;
      this.stdout = stdout;
      this.stderr = stderr;
    }

    public void ensureOk() {
      if (exitStatus != 0) {
        throw new RuntimeException(
            "Exec process returned "
                + exitStatus
                + ".  StdOut:\n" //$NON-NLS-1$ //$NON-NLS-2$
                + stdout
                + "\nStdErr:\n"
                + stderr); //$NON-NLS-1$
      }
    }

    public int getExitStatus() {
      return exitStatus;
    }

    public String getStderr() {
      return stderr;
    }

    public String getStdout() {
      return stdout;
    }
  }

  public static boolean isPlatformUnix(String platform) {
    return !platform.startsWith("win"); // $NON-NLS-1$
  }

  @SuppressWarnings("nls")
  public static String determinePlatform() {
    String name = System.getProperty("os.name").toLowerCase();
    boolean is64bit = is64Bit(name);
    if (name.startsWith("windows")) {
      return is64bit ? PLATFORM_WIN64 : PLATFORM_WIN32;
    } else if (name.startsWith(PLATFORM_LINUX)) {
      return is64bit ? PLATFORM_LINUX64 : PLATFORM_LINUX;
    } else if (name.startsWith("solaris") || name.startsWith("sunos")) {
      if (System.getProperty("os.arch").startsWith("sparc")) {
        return PLATFORM_SOLARIS_SPARC;
      } else {
        return is64bit ? PLATFORM_SOLARIS64 : PLATFORM_SOLARIS_X86;
      }
    } else if (name.startsWith("mac os x")) {
      return "mac";
    } else {
      return "unsupported";
    }
  }

  @SuppressWarnings("nls")
  public static boolean is64Bit(String name) {
    boolean is64bit = false;
    if (name.contains("windows")) {
      is64bit = (System.getenv("ProgramFiles(x86)") != null);
    } else {
      is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
    }
    return is64bit;
  }

  private ExecUtils() {
    throw new Error();
  }

  @SuppressWarnings("nls")
  public static boolean isRunningInJar(Class<?> clazz) {
    String classFile = clazz.getName().replace('.', '/');
    URL res = clazz.getClassLoader().getResource(classFile + ".class");
    return res.getProtocol().equals("jar");
  }

  @SuppressWarnings("nls")
  public static File findJarFolder(Class<?> clazz) {
    String classFile = clazz.getName().replace('.', '/');
    URL res = clazz.getClassLoader().getResource(classFile + ".class");
    if (!res.getProtocol().equals("jar")) {
      throw new RuntimeException("Not running from a jar file: '" + res + "'");
    }
    try {
      String file = res.getFile();
      URL fileUrl = new URL(file.substring(0, file.lastIndexOf('!')));
      return new File(URLDecoder.decode(fileUrl.getFile(), "UTF-8")).getParentFile();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
