/*
 * Copyright (c) 2017 LingoChamp Inc.
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

package com.liulishuo.okdownload.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.liulishuo.okdownload.sample.base.BaseSampleActivity;
import com.liulishuo.okdownload.sample.util.DemoUtil;
import com.liulishuo.okdownload.sample.util.GlobalListenerManager;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * On this demo you can see the simplest way to download a task.
 */
public class SingleActivity extends BaseSampleActivity {

    private static final String TAG = "SingleActivity";
    private DownloadTask task;

    private DownloadListener4WithSpeed listener;

    private TextView statusTv;
    private ProgressBar progressBar;
    private CardView actionView;
    private TextView actionTv;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        statusTv = findViewById(R.id.statusTv);
        progressBar = findViewById(R.id.progressBar);
        actionView = findViewById(R.id.actionView);
        actionTv = findViewById(R.id.actionTv);
        initSingleDownload(statusTv,progressBar,actionView,actionTv);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initAction(actionView, actionTv, statusTv, progressBar);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override public int titleRes() {
        return R.string.single_download_title;
    }

    @Override protected void onDestroy() {
        super.onDestroy();
//        if (task != null) task.cancel();
        Log.d("qqqqqqqq", "onDestroy");
        GlobalListenerManager.getInstance().getManager().detachListener(task.getId());
    }

    private void initSingleDownload(TextView statusTv, ProgressBar progressBar, View actionView,
                                    TextView actionTv) {
        initTask();
        initStatus(statusTv, progressBar);
    }

    private void initTask() {
        final String filename = "single-test";
        final String url =
                "https://cdn.llscdn.com/yy/files/xs8qmxn8-lls-LLS-5.8-800-20171207-111607.apk";
        final File parentFile = DemoUtil.getParentFile(this);
        task = new DownloadTask.Builder(url, parentFile)
                .setFilename(filename)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(1000)
                // ignore the same task has already completed in the past.
                .setPassIfAlreadyCompleted(false)
                .build();
    }

    private void initStatus(TextView statusTv, ProgressBar progressBar) {
        final StatusUtil.Status status = StatusUtil.getStatus(task);
        if (status == StatusUtil.Status.COMPLETED) {
            progressBar.setProgress(progressBar.getMax());
        }

        statusTv.setText(status.toString());
        final BreakpointInfo info = StatusUtil.getCurrentInfo(task);
        if (info != null) {
            Log.d(TAG, "init status with: " + info.toString());

            DemoUtil.calcProgressToView(progressBar, info.getTotalOffset(), info.getTotalLength());
        }
    }


    private void initListener(final TextView statusTv, final ProgressBar progressBar,
                              final TextView actionTv) {
        listener = new DownloadListener4WithSpeed() {
            private long totalLength;
            private String readableTotalLength;

            @Override public void taskStart(@NonNull DownloadTask task) {
                statusTv.setText(R.string.task_start);
            }

            @Override
            public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info,
                                  boolean fromBreakpoint,
                                  @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
                statusTv.setText(R.string.info_ready);

                totalLength = info.getTotalLength();
                readableTotalLength = Util.humanReadableBytes(totalLength, true);
                DemoUtil.calcProgressToView(progressBar, info.getTotalOffset(), totalLength);
            }

            @Override public void connectStart(@NonNull DownloadTask task, int blockIndex,
                                               @NonNull Map<String, List<String>> requestHeaders) {
                final String status = "Connect Start " + blockIndex;
                statusTv.setText(status);
            }

            @Override
            public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode,
                                   @NonNull Map<String, List<String>> responseHeaders) {
                final String status = "Connect End " + blockIndex;
                statusTv.setText(status);
            }

            @Override
            public void progressBlock(@NonNull DownloadTask task, int blockIndex,
                                      long currentBlockOffset,
                                      @NonNull SpeedCalculator blockSpeed) {
            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, long total, @NonNull SpeedCalculator taskSpeed) {
                Log.d("qqqqqqqq", this.toString() + "progress");
                final String readableOffset = Util.humanReadableBytes(currentOffset, true);
                final String readableTotal = Util.humanReadableBytes(total, true);
                final String progressStatus = readableOffset + "/" + readableTotal;
                final String speed = taskSpeed.speed();
                final String progressStatusWithSpeed = progressStatus + "(" + speed + ")";

                statusTv.setText(progressStatusWithSpeed);
                totalLength = total;
                Log.d("qqqqqqqq", "progress currentOffset=" + currentOffset);
                DemoUtil.calcProgressToView(progressBar, currentOffset, total);
            }

            @Override
            public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info,
                                 @NonNull SpeedCalculator blockSpeed) {
            }

            @Override public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                                          @Nullable Exception realCause,
                                          @NonNull SpeedCalculator taskSpeed) {
                final String statusWithSpeed = cause.toString() + " " + taskSpeed.averageSpeed();
                statusTv.setText(statusWithSpeed);

                actionTv.setText(R.string.start);
                // mark
                task.setTag(null);
                if (cause == EndCause.COMPLETED) {
                    final String realMd5 = fileToMD5(task.getFile().getAbsolutePath());
                    if (!realMd5.equalsIgnoreCase("f836a37a5eee5dec0611ce15a76e8fd5")) {
                        Log.e(TAG, "file is wrong because of md5 is wrong " + realMd5);
                    }
                }
            }
        };
        GlobalListenerManager.getInstance().attachListener(task, listener);
        GlobalListenerManager.getInstance().addAutoRemoveListenersWhenTaskEnd(task.getId());
    }

    private void initAction(final View actionView, final TextView actionTv, final TextView statusTv,
                            final ProgressBar progressBar) {
        actionTv.setText(R.string.start);
        initListener(statusTv, progressBar, actionTv);
        if (task.getListener() != null) {
            Log.d("SingleActivity", "listener不为null");
        }
//        task.replaceListener(listener);
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final boolean started = task.getTag() != null;

                if (started) {
                    // to cancel
                    task.cancel();
//                    task.replaceListener(listener);
                } else {
                    actionTv.setText(R.string.cancel);
                    GlobalListenerManager.getInstance().enqueueTask(task,listener);
                    // to start
//                    startTask();
                    // mark
                    task.setTag("mark-task-started");
                }
            }
        });
    }

    private void startTask() {
        task.enqueue(listener);
    }

    @SuppressFBWarnings(value = "REC")
    public static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "file to md5 failed", e);
                }
            }
        }
    }

    private static String convertHashToString(byte[] md5Bytes) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            buf.append(Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return buf.toString().toUpperCase();
    }


    private boolean isTaskRunning() {
        final StatusUtil.Status status = StatusUtil.getStatus(task);
        return status == StatusUtil.Status.PENDING || status == StatusUtil.Status.RUNNING;
    }
}
