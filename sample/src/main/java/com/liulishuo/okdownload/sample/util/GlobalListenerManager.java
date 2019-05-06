/*
 * Copyright (c) 2018 LingoChamp Inc.
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

package com.liulishuo.okdownload.sample.util;

import android.support.annotation.NonNull;

import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.UnifiedListenerManager;

public class GlobalListenerManager {
    private UnifiedListenerManager manager;

    private GlobalListenerManager() {
        manager = new UnifiedListenerManager();
    }

    private static class ClassHolder {
        private static final GlobalListenerManager INSTANCE = new GlobalListenerManager();
    }

    public static GlobalListenerManager getInstance() {
        return GlobalListenerManager.ClassHolder.INSTANCE;
    }

    public UnifiedListenerManager getManager() {
        return manager;
    }

    public void addAutoRemoveListenersWhenTaskEnd(int id) {
        manager.addAutoRemoveListenersWhenTaskEnd(id);
    }

    public void attachListener(@NonNull DownloadTask task, @NonNull DownloadListener listener) {
        manager.attachListener(task, listener);
    }

    public void enqueueTask(@NonNull DownloadTask task,
                     @NonNull DownloadListener listener) {
        manager.enqueueTaskWithUnifiedListener(task, listener);
    }

    public void enqueueTask(@NonNull DownloadTask task) {
        manager.enqueueTaskWithUnifiedListener(task);
    }
}
