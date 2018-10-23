package com.psliusar.layers.subscription;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import androidx.annotation.NonNull;

public class Subscriptions implements Iterable<Subscription> {

    private HashSet<Subscription> subs;

    @NonNull
    public synchronized Subscriptions manage(@NonNull Subscription... subscriptions) {
        if (subs == null) {
            subs = new HashSet<>();
        }

        for (Subscription s : subscriptions) {
            final Wrapper wrapper = new Wrapper(s);
            subs.add(wrapper);
            wrapper.subscribe(this);
        }

        return this;
    }

    @NonNull
    @Override
    public Iterator<Subscription> iterator() {
        return subs == null ? new EmptyIterator<Subscription>() : subs.iterator();
    }

    public synchronized void unSubscribeAll() {
        if (subs == null) {
            return;
        }
        for (Subscription s : subs) {
            if (s.isSubscribed()) {
                s.unSubscribe();
            }
        }
        subs.clear();
    }

    public synchronized boolean hasSubscriptions() {
        if (subs == null || subs.size() == 0) {
            return false;
        }
        for (Subscription s : subs) {
            if (s.isSubscribed()) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return subs == null ? 0 : subs.size();
    }

    void remove(@NonNull Subscription subscription) {
        subs.remove(subscription);
    }

    private static class Wrapper extends Subscription {

        private final Subscription original;

        public Wrapper(@NonNull Subscription original) {
            this.original = original;
        }

        @Override
        public boolean isSubscribed() {
            return super.isSubscribed() || original.isSubscribed();
        }

        @Override
        public void unSubscribe() {
            original.unSubscribe();
            super.unSubscribe();
        }

        @NonNull
        @Override
        public Subscription getOriginal() {
            return original.getOriginal();
        }
    }

    private static class EmptyIterator<E> implements Iterator<E> {

        public boolean hasNext() {
            return false;
        }

        public E next() {
            throw new NoSuchElementException();
        }
    }
}
