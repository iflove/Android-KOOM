/*
 * Copyright 2020 Kwai, Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * A jvm hprof dumper which use fork and don't block main process.
 *
 * @author Rui Li <lirui05@kuaishou.com>
 */

package com.kwai.koom.javaoom.hprof;

import java.io.IOException;

import android.os.Build;
import android.os.Debug;

import com.kwai.koom.base.MonitorLog;

public class ForkJvmHeapDumper extends HeapDumper {

  private static final String TAG = "OOMMonitor_ForkJvmHeapDumper";

  public ForkJvmHeapDumper() {
    super();
    if (soLoaded) {
      init();
    }
  }

  @Override
  public boolean dump(String path) {
    MonitorLog.i(TAG, "dump " + path);
    if (!soLoaded) {
      MonitorLog.e(TAG, "dump failed caused by so not loaded!");
      return false;
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
        || Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
      MonitorLog.e(TAG, "dump failed caused by version not supported!");
      return false;
    }

    boolean dumpRes = false;
    try {
      MonitorLog.i(TAG, "before suspend and fork.");
      int pid = suspendAndFork();
      if (pid == 0) {
        // Child process
        Debug.dumpHprofData(path);
        exitProcess();
      } else if (pid > 0) {
        // Parent process
        dumpRes = resumeAndWait(pid);
        MonitorLog.i(TAG, "notify from pid " + pid);
      }
    } catch (IOException e) {
      MonitorLog.e(TAG, "dump failed caused by " + e.toString());
      e.printStackTrace();
    }
    return dumpRes;
  }

  /**
   * Init before do dump.
   */
  private native void init();

  /**
   * Suspend the whole ART, and then fork a process for dumping hprof.
   *
   * @return return value of fork
   */
  private native int suspendAndFork();

  /**
   * Resume the whole ART, and then wait child process to notify.
   *
   * @param pid pid of child process.
   */
  private native boolean resumeAndWait(int pid);

  /**
   * Exit current process.
   */
  private native void exitProcess();
}
