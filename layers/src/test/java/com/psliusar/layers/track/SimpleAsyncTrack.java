package com.psliusar.layers.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleAsyncTrack extends AsyncTrack<String, Integer> {

    private final String[] array;

    public SimpleAsyncTrack(@NonNull String[] array) {
        this.array = array;
    }

    @Override
    protected String doInBackground() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(array[i]);
        }
        return builder.toString();
    }

    @Override
    protected WorkerTask<String, Integer> createWorkerTask() {
        return new MockedWorkerTask();
    }

    private static class MockedWorkerTask extends WorkerTask<String, Integer> {

        private OnCompletionListener<String, Integer> listener;

        @Override
        protected void execute(@NonNull final AsyncTrack<String, Integer> parent) {
            final Semaphore semaphore = new Semaphore(1);
            final AtomicReference<String> valueRef = new AtomicReference<>();
            final AtomicReference<Throwable> throwableRef = new AtomicReference<>();
            new Thread() {
                @Override
                public void run() {
                    try {
                        semaphore.acquire();
                        sleep(40);
                        valueRef.set(parent.doInBackground());
                    } catch (Throwable throwable) {
                        throwableRef.set(throwable);
                    }
                    semaphore.release();
                }
            }.start();

            try {
                Thread.sleep(20);
                semaphore.acquire();
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            done(valueRef.get(), throwableRef.get());
        }

        private void done(@Nullable String value, @Nullable Throwable throwable) {
            if (listener != null) {
                if (throwable == null) {
                    listener.onWorkCompleted(value);
                } else {
                    listener.onError(throwable);
                }
            }
        }

        @Override
        protected void cancel() {
            listener = null;
        }

        @Override
        protected boolean isCancelled() {
            // XXX
            return false;
        }

        @Override
        protected void setListener(@Nullable OnCompletionListener<String, Integer> listener) {
            this.listener = listener;
        }
    }
}
