package org.openengsb.core.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openengsb.core.api.security.BundleAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private static ExecutorService executor = Executors.newCachedThreadPool();

    static class RootCallable<V> extends ContextAwareCallable<V> {
        public RootCallable(Callable<V> original) {
            super(original);
        }

        @Override
        public V call() throws Exception {
            SecurityContextHolder.getContext().setAuthentication(new BundleAuthenticationToken("", ""));
            try {
                return super.call();
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    static class RootRunnable extends ContextAwareRunnable {

        public RootRunnable(Runnable original) {
            super(original);
        }

        @Override
        public void run() {
            SecurityContextHolder.getContext().setAuthentication(new BundleAuthenticationToken("", ""));
            try {
                super.run();
            } finally {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        }

    }

    public static <V> V executeWithSystemPermissions(Callable<V> task) throws ExecutionException {
        Future<V> future = executor.submit(new RootCallable<V>(task));
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void executeWithSystemPermissions(Runnable task) {
        executor.execute(new RootRunnable(task));
    }
}
