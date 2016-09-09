package com.app.services;

/**
 * Created by VIJAY on 03-09-2016.
 */
/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2015 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Encapsulates a {@link Handler} running in a background.
 *
 * The {@link Thread} is started on demand, whenever one of post or send calls took place.
 * You can call {@link #start()} externally if you want to create a {@link Handler} faster.
 *
 * All messages that can't be ran when {@link Handler} is not yet created are stored in
 * mRunAfterCreated queue.
 */
public class BackgroundHandler extends HandlerThread implements Handler.Callback {

    private final Object mHandlerCreatedLock = new Object();

    private final Queue<RunTask> mRunAfterCreated = new LinkedList<>();

    private Handler mHandler;

    private volatile boolean mStarted;

    public BackgroundHandler() {
        super("BackgroundHandler");
    }

    public BackgroundHandler(final String name) {
        super(name);
    }

    public BackgroundHandler(final String name, final int priority) {
        super(name, priority);
    }

    private synchronized void startIfNotStarted() {
        if (!mStarted) {
            start();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public synchronized final void start() {
        mStarted = true;
        super.start();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public final void interrupt() {
        super.interrupt();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @SuppressWarnings("deprecation")
    @Override
    public final int countStackFrames() {
        return super.countStackFrames();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @SuppressWarnings("deprecation")
    @Override
    public final void destroy() {
        super.destroy();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public final ClassLoader getContextClassLoader() {
        return super.getContextClassLoader();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public final long getId() {
        return super.getId();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public final StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public final State getState() {
        return super.getState();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public final UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return super.getUncaughtExceptionHandler();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public final boolean isInterrupted() {
        return super.isInterrupted();
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public final void setContextClassLoader(final ClassLoader cl) {
        super.setContextClassLoader(cl);
    }

    /**
     * {@inheritDoc}
     *
     * Is final to avoid accidental overriding
     */
    @Override
    public final void setUncaughtExceptionHandler(final UncaughtExceptionHandler handler) {
        super.setUncaughtExceptionHandler(handler);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        synchronized (mHandlerCreatedLock) {
            mHandler = new Handler(BackgroundHandler.this);
            if (!mRunAfterCreated.isEmpty()) {
                RunTask task;
                while ((task = mRunAfterCreated.poll()) != null) {
                    executeTask(task);
                }
            }
        }
    }

    /**
     * Since {@link BackgroundHandler} does not extend {@link Handler}, it implements {@link
     * Handler.Callback}
     *
     * @param msg A {@link android.os.Message Message} object
     * @return true if no further handling is desired
     */
    @Override
    public boolean handleMessage(final Message msg) {
        return false;
    }

    private void executeTask(@NonNull final RunTask runTask) {
        if (runTask.mMessage != null) {
            if (runTask.mAtFrontOfQueue) {
                mHandler.sendMessageAtFrontOfQueue(runTask.mMessage);
            } else if (runTask.mAtTime != 0) {
                mHandler.sendMessageAtTime(runTask.mMessage, runTask.mAtTime);
            } else {
                mHandler.sendMessage(runTask.mMessage);
            }
        } else if (runTask.mRunnable != null) {
            if (runTask.mAtFrontOfQueue) {
                mHandler.postAtFrontOfQueue(runTask.mRunnable);
            } else if (runTask.mAtTime != 0) {
                mHandler.postAtTime(runTask.mRunnable, runTask.mToken, runTask.mAtTime);
            } else {
                mHandler.post(runTask.mRunnable);
            }
            mHandler.postDelayed(runTask.mRunnable, runTask.mAtTime);
        }
    }


    /**
     * Causes the Runnable r to be added to the message queue.
     * The runnable will be run on the thread to which this handler is
     * attached.
     *
     * @param r The Runnable that will be executed.
     * @return Returns true if the Runnable was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.
     */
    public final boolean post(final Runnable r) {
        return postDelayed(r, 0);
    }

    /**
     * Causes the Runnable r to be added to the message queue, to be run
     * at a specific time given by <var>uptimeMillis</var>.
     * <b>The time-base is {@link android.os.SystemClock#uptimeMillis}.</b>
     * Time spent in deep sleep will add an additional delay to execution.
     * The runnable will be run on the thread to which this handler is attached.
     *
     * @param r            The Runnable that will be executed.
     * @param uptimeMillis The absolute time at which the callback should run,
     *                     using the {@link android.os.SystemClock#uptimeMillis} time-base.
     * @return Returns true if the Runnable was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.  Note that a
     * result of true does not mean the Runnable will be processed -- if
     * the looper is quit before the delivery time of the message
     * occurs then the message will be dropped.
     */
    public final boolean postAtTime(final Runnable r, final long uptimeMillis) {
        return postAtTime(r, null, uptimeMillis);
    }

    /**
     * Causes the Runnable r to be added to the message queue, to be run
     * at a specific time given by <var>uptimeMillis</var>.
     * <b>The time-base is {@link android.os.SystemClock#uptimeMillis}.</b>
     * Time spent in deep sleep will add an additional delay to execution.
     * The runnable will be run on the thread to which this handler is attached.
     *
     * @param r            The Runnable that will be executed.
     * @param uptimeMillis The absolute time at which the callback should run,
     *                     using the {@link android.os.SystemClock#uptimeMillis} time-base.
     * @return Returns true if the Runnable was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.  Note that a
     * result of true does not mean the Runnable will be processed -- if
     * the looper is quit before the delivery time of the message
     * occurs then the message will be dropped.
     * @see android.os.SystemClock#uptimeMillis
     */
    public final boolean postAtTime(final Runnable r, @Nullable final Object token,
                                    final long uptimeMillis) {
        if (r == null) {
            throw new NullPointerException("Runnable is null");
        }
        startIfNotStarted();
        synchronized (mHandlerCreatedLock) {
            //noinspection IfMayBeConditional
            if (mHandler != null) {
                return mHandler.postAtTime(r, token, uptimeMillis);
            } else {
                return mRunAfterCreated.add(new RunTask(r, token, uptimeMillis, false));
            }
        }
    }

    /**
     * Causes the Runnable r to be added to the message queue, to be run
     * after the specified amount of time elapses.
     * The runnable will be run on the thread to which this handler
     * is attached.
     * <b>The time-base is {@link android.os.SystemClock#uptimeMillis}.</b>
     * Time spent in deep sleep will add an additional delay to execution.
     *
     * @param r           The Runnable that will be executed.
     * @param delayMillis The delay (in milliseconds) until the Runnable
     *                    will be executed.
     * @return Returns true if the Runnable was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.  Note that a
     * result of true does not mean the Runnable will be processed --
     * if the looper is quit before the delivery time of the message
     * occurs then the message will be dropped.
     */
    public final boolean postDelayed(final Runnable r, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return postAtTime(r, SystemClock.uptimeMillis() + delayMillis);
    }

    /**
     * Posts a message to an object that implements Runnable.
     * Causes the Runnable r to executed on the next iteration through the
     * message queue. The runnable will be run on the thread to which this
     * handler is attached.
     * <b>This method is only for use in very special circumstances -- it
     * can easily starve the message queue, cause ordering problems, or have
     * other unexpected side-effects.</b>
     *
     * @param r The Runnable that will be executed.
     * @return Returns true if the message was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.
     */
    public final boolean postAtFrontOfQueue(final Runnable r) {
        if (r == null) {
            throw new NullPointerException("Runnable is null");
        }
        startIfNotStarted();
        synchronized (mHandlerCreatedLock) {
            //noinspection IfMayBeConditional
            if (mHandler != null) {
                return mHandler.postAtFrontOfQueue(r);
            } else {
                return mRunAfterCreated.add(new RunTask(r, null, 0, true));
            }
        }
    }

    /**
     * Remove any pending posts of Runnable r that are in the message queue.
     */
    public final void removeCallbacks(final Runnable r) {
        if (r == null) {
            return;
        }
        synchronized (mHandlerCreatedLock) {
            if (!mRunAfterCreated.isEmpty()) {
                RunTask toRemove = null;
                for (final RunTask runTask : mRunAfterCreated) {
                    if (runTask.mRunnable == r) {
                        toRemove = runTask;
                        break;
                    }
                }
                if (toRemove != null) {
                    mRunAfterCreated.remove(toRemove);
                }
            }
            if (mHandler != null) {
                mHandler.removeCallbacks(r);
            }
        }
    }

    /**
     * Remove any pending posts of Runnable <var>r</var> with Object
     * <var>token</var> that are in the message queue.  If <var>token</var> is null,
     * all callbacks will be removed.
     */
    public final void removeCallbacks(final Runnable r, final Object token) {
        if (r == null) {
            return;
        }
        synchronized (mHandlerCreatedLock) {
            if (!mRunAfterCreated.isEmpty()) {
                final Set<RunTask> toRemove = new HashSet<>(mRunAfterCreated.size());
                for (final RunTask runTask : mRunAfterCreated) {
                    if (runTask != null && runTask.mRunnable != null && runTask.mRunnable == r && (
                            token == null || runTask.mToken == token)) {
                        toRemove.add(runTask);
                    }
                }
                if (!toRemove.isEmpty()) {
                    mRunAfterCreated.removeAll(toRemove);
                }
            }
            if (mHandler != null) {
                mHandler.removeCallbacks(r, token);
            }
        }
    }

    /**
     * Pushes a message onto the end of the message queue after all pending messages
     * before the current time. It will be received in {@link Handler#handleMessage},
     * in the thread attached to this handler.
     *
     * @return Returns true if the message was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.
     */
    public final boolean sendMessage(Message msg) {
        return sendMessageDelayed(msg, 0);
    }

    /**
     * Sends a Message containing only the what value.
     *
     * @return Returns true if the message was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.
     */
    public final boolean sendEmptyMessage(int what) {
        return sendEmptyMessageDelayed(what, 0);
    }

    /**
     * Sends a Message containing only the what value, to be delivered
     * after the specified amount of time elapses.
     *
     * @return Returns true if the message was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.
     * @see #sendMessageDelayed(android.os.Message, long)
     */
    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageDelayed(msg, delayMillis);
    }

    /**
     * Sends a Message containing only the what value, to be delivered
     * at a specific time.
     *
     * @return Returns true if the message was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.
     * @see #sendMessageAtTime(android.os.Message, long)
     */

    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageAtTime(msg, uptimeMillis);
    }

    /**
     * Enqueue a message into the message queue after all pending messages
     * before (current time + delayMillis). You will receive it in
     * {@link Handler#handleMessage}, in the thread attached to this handler.
     *
     * @return Returns true if the message was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.  Note that a
     * result of true does not mean the message will be processed -- if
     * the looper is quit before the delivery time of the message
     * occurs then the message will be dropped.
     */
    public final boolean sendMessageDelayed(final Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
    }

    /**
     * Enqueue a message into the message queue after all pending messages
     * before the absolute time (in milliseconds) <var>uptimeMillis</var>.
     * <b>The time-base is {@link android.os.SystemClock#uptimeMillis}.</b>
     * Time spent in deep sleep will add an additional delay to execution.
     * You will receive it in {@link Handler#handleMessage}, in the thread attached
     * to this handler.
     *
     * @param uptimeMillis The absolute time at which the message should be
     *                     delivered, using the
     *                     {@link android.os.SystemClock#uptimeMillis} time-base.
     * @return Returns true if the message was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.  Note that a
     * result of true does not mean the message will be processed -- if
     * the looper is quit before the delivery time of the message
     * occurs then the message will be dropped.
     */
    public boolean sendMessageAtTime(final Message msg, final long uptimeMillis) {
        if (msg == null) {
            throw new NullPointerException("Message is null");
        }
        startIfNotStarted();
        synchronized (mHandlerCreatedLock) {
            //noinspection IfMayBeConditional
            if (mHandler != null) {
                return mHandler.sendMessageAtTime(msg, uptimeMillis);
            } else {
                return mRunAfterCreated.add(new RunTask(msg, uptimeMillis, false));
            }
        }
    }

    /**
     * Enqueue a message at the front of the message queue, to be processed on
     * the next iteration of the message loop.  You will receive it in
     * {@link Handler#handleMessage}, in the thread attached to this handler.
     * <b>This method is only for use in very special circumstances -- it
     * can easily starve the message queue, cause ordering problems, or have
     * other unexpected side-effects.</b>
     *
     * @return Returns true if the message was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.
     */
    public final boolean sendMessageAtFrontOfQueue(final Message msg) {
        if (msg == null) {
            throw new NullPointerException("Message is null");
        }
        startIfNotStarted();
        synchronized (mHandlerCreatedLock) {
            //noinspection IfMayBeConditional
            if (mHandler != null) {
                return mHandler.sendMessageAtFrontOfQueue(msg);
            } else {
                return mRunAfterCreated.add(new RunTask(msg, 0, true));
            }
        }
    }

    /**
     * Remove any pending posts of messages with code 'what' that are in the
     * message queue.
     */
    public final void removeMessages(int what) {
        removeMessages(what, null);
    }

    /**
     * Remove any pending posts of messages with code 'what' and whose obj is
     * 'object' that are in the message queue.  If <var>object</var> is null,
     * all messages will be removed.
     */
    public final void removeMessages(final int what, @Nullable final Object object) {
        synchronized (mHandlerCreatedLock) {
            if (!mRunAfterCreated.isEmpty()) {
                final Set<RunTask> toRemove = new HashSet<>(mRunAfterCreated.size());
                for (final RunTask runTask : mRunAfterCreated) {
                    if (runTask.mMessage != null && runTask.mMessage.what == what && (object == null
                            || runTask.mMessage.obj == object)) {
                        toRemove.add(runTask);
                    }
                }
                if (!toRemove.isEmpty()) {
                    mRunAfterCreated.removeAll(toRemove);
                }
            }
            if (mHandler != null) {
                mHandler.removeMessages(what, object);
            }
        }
    }

    /**
     * Remove any pending posts of callbacks and sent messages whose
     * <var>obj</var> is <var>token</var>.  If <var>token</var> is null,
     * all callbacks and messages will be removed.
     */
    public final void removeCallbacksAndMessages(@Nullable final Object token) {
        synchronized (mHandlerCreatedLock) {
            if (!mRunAfterCreated.isEmpty()) {
                if (token == null) {
                    mRunAfterCreated.clear();
                } else {
                    final Set<RunTask> toRemove = new HashSet<>(mRunAfterCreated.size());
                    for (final RunTask runTask : mRunAfterCreated) {
                        if (token.equals(runTask.mToken)) {
                            toRemove.add(runTask);
                        }
                    }
                    if (!toRemove.isEmpty()) {
                        mRunAfterCreated.removeAll(toRemove);
                    }
                }
            }
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(token);
            }
        }
    }

    /**
     * Check if there are any pending posts of messages with code 'what' in
     * the message queue.
     */
    public final boolean hasMessages(final int what) {
        return hasMessages(what, null);
    }

    /**
     * Check if there are any pending posts of messages with code 'what' and
     * whose obj is 'object' in the message queue.
     */
    public final boolean hasMessages(final int what, @Nullable final Object object) {
        synchronized (mHandlerCreatedLock) {
            if (!mRunAfterCreated.isEmpty()) {
                return true;
            }
            //noinspection SimplifiableIfStatement
            if (mHandler != null) {
                return mHandler.hasMessages(what, object);
            }
        }
        return false;
    }

    /**
     * Returns current Thread's {@link Handler}.
     * May be null if the Thread is not yet started or did not yet created the {@link Handler}
     *
     * @return Current Thread's {@link Handler}
     */
    @Nullable
    public Handler getHandler() {
        synchronized (mHandlerCreatedLock) {
            return mHandler;
        }
    }


    private static final class RunTask {

        final Message mMessage;
        final Runnable mRunnable;
        final Object mToken;
        final long mAtTime;
        final boolean mAtFrontOfQueue;

        private RunTask(@NonNull final Message message,
                        final long atTime,
                        final boolean atFrontOfQueue) {
            mMessage = message;
            mRunnable = null;
            mToken = null;
            mAtTime = atTime;
            mAtFrontOfQueue = atFrontOfQueue;
        }

        private RunTask(@NonNull final Runnable runnable,
                        @Nullable final Object token,
                        final long atTime,
                        final boolean atFrontOfQueue) {
            mMessage = null;
            mRunnable = runnable;
            mToken = token;
            mAtTime = atTime;
            mAtFrontOfQueue = atFrontOfQueue;
        }
    }
}