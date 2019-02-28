package org.iblog.enhance.gateway.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utilities that are commonly useful when using {@link Executors}, {@link ExecutorService},
 * {@link ThreadPoolExecutor} and the like.
 *
 * @author shaoxiao.xu
 * @date 2019/1/15 18:20
 */
public class ExecutorUtil {
    /**
     * The interface to handle an exception occurred during executing a task submitted to a thread pool.
     *
     * Note that caller can handle only Exception (including {@link RuntimeException}) but not Error.
     * In other words, Error will aways be thrown out.
     */
    public interface IExecutionExceptionHandler {
        /**
         * Exceptions that be passed to this method are CancellationException, InterruptedException,
         * and any Exception that can be thrown by the task executed by executed by the thread pool
         * (i.e., the exception unwrapped from an ExecutionException).
         *
         * @param r
         * @param e
         */
        void handle(Runnable r, Exception e);
    }

    /**
     * The interface to handle all exceptions and errors during executing a task submitted to
     * a thread pool.
     *
     * Note that all throwable, including {@link Error} would be passed to
     * the caller.
     */
    public interface IExecutionErrorHandler {
        /**
         * Exceptions that be passed to this method are CancellationException, InterruptedException,
         * and any Exception or Error that can be thrown by task executed by the thread pool (i.e.,
         * the unwrapped from an ExecutionException).
         *
         * @param r
         * @param e
         */
        void handle(Runnable r, Throwable e);
    }

    public abstract static class ExecutionExceptionHandler implements IExecutionExceptionHandler {
        @Override
        public void handle(Runnable r, Exception e) {
            if (e instanceof CancellationException || e instanceof InterruptedException) {
                return;
            }

        }

        /**
         * Exceptions passed th this method were thrown by the task executed by the thread pool
         * (i.e., the exception unwrapped from an ExecutionException).
         *
         * @param task
         * @param e
         */
        protected abstract void handleTaskException(Runnable task, Exception e);
    }

    /**
     * Usually used int {@link ThreadPoolExecutor#afterExecute(Runnable, Throwable)}
     * method of classes that extend {@link ThreadPoolExecutor}
     *
     * This method contains the boilerplate code of unwrapping {@link ExecutionException}
     * and allows the caller to specify the action of handing the actual exception.
     *
     * Note that if the ExecutionException thrown during the execution of <code>task</code>
     * wraps an Error (e.g., OOM), then the Error will always be thrown -- the <code>handler</code>
     * won't have a chance to catch or mask it.
     *
     * @param task @see {@link ThreadPoolExecutor#afterExecute(Runnable, Throwable)}
     * @param error @see {@link ThreadPoolExecutor#afterExecute(Runnable, Throwable)}
     * @param handler Action to handle exception.
     */
    public static void afterExecute(Runnable task, Throwable error, ExecutionExceptionHandler handler) {
        if (error == null && task instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) task;
                if (future.isDone()) {
                    future.get();
                }
            } catch (Exception e) {
                error = e;
            }
        }
        if (error instanceof ExecutionException) {
            error = error.getCause();
        }
        if (error instanceof Error) {
            throw (Error) error;
        } else if (error instanceof Exception) {
            handler.handle(task, (Exception) error);
        }
    }

    /**
     * Usually used in the {@link ThreadPoolExecutor#afterExecute(Runnable, Throwable)}
     * method of classes that extend {@link ThreadPoolExecutor}.
     *
     * The difference between this method and {@link #afterExecute(Runnable, Throwable, ExecutionExceptionHandler)}
     * is that this method catches {@link Error} for <code>handler</code> to handle.
     *
     * @param task
     * @param error
     * @param handler
     */
    public static void afterExecute2(Runnable task, Throwable error, IExecutionErrorHandler handler) {
        if (error == null && task instanceof Future<?> && ((Future<?>) task).isDone()) {
            try {
                ((Future<?>) task).get();
            } catch (CancellationException ce) {
                error = ce;
            } catch (ExecutionException ee) {
                error = ee.getCause();
            } catch (InterruptedException ie) {
                error = ie;
                Thread.currentThread().interrupt(); // ignore/reset
            } catch (Throwable t) {
                error = t;
            }
        }
        if (error != null && handler != null) {
            handler.handle(task, error);
        }
    }

    /**
     * Ensure the task is successful within the specified time.
     *
     * @param task
     * @param timeoutMillis
     * @return
     * @throws Exception
     */
    public static boolean waitUntilTrue(Callable<Boolean> task, long timeoutMillis) throws Exception {
        long start = System.nanoTime();
        long timeoutTicks = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (!task.call()) {
            if (start + timeoutTicks < System.nanoTime()) {
                return false;
            }
            Thread.sleep(50L);
        }
        return true;
    }
}
