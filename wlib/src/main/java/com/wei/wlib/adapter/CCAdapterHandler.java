package com.wei.wlib.adapter;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * A handler to control content. <br/>
 * Every modification of content will call {@link BaseAdapter#notifyDataSetChanged()} automatically.
 *
 * Created by yanhaifeng on 16-7-27.
 */

public class CCAdapterHandler<T> extends ArrayList<T> {

    private ContentObserver mContentObserver;

    private Transaction<T> mTransaction;

    public CCAdapterHandler() {
        super();
    }

    public CCAdapterHandler(Collection<T> collection) {
        super(collection);
    }

    void setContentObserver(ContentObserver contentObserver) {
        mContentObserver = contentObserver;
    }

    public Transaction<T> beginTransation() {
        mTransaction = new Transaction<T>(this);
        return mTransaction;
    }

    public void endTransaction() {
        mTransaction = null;
    }

    @Override
    public boolean add(T object) {
        try {
            return super.add(object);
        } finally {
            notifyObserver();
        }
    }

    @Override
    public void add(int index, T object) {
        try {
            super.add(index, object);
        } finally {
            notifyObserver();
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        try {
            return super.addAll(collection);
        } finally {
            notifyObserver();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> collection) {
        try {
            return super.addAll(index, collection);
        } finally {
            notifyObserver();
        }
    }

    @Override
    public T set(int index, T object) {
        try {
            return super.set(index, object);
        } finally {
            notifyObserver();
        }
    }

    public void set(T object) {
        try {
            super.clear();
            super.add(object);
        } finally {
            notifyObserver();
        }
    }

    public void set(Collection<T> collection) {
        try {
            super.clear();
            super.addAll(collection);
        } finally {
            notifyObserver();
        }
    }

    @Override
    public boolean remove(Object object) {
        try {
            return super.remove(object);
        } finally {
            notifyObserver();
        }
    }

    @Override
    public T remove(int index) {
        try {
            return super.remove(index);
        } finally {
            notifyObserver();
        }
    }

    @Override
    public void removeRange(int fromIndex, int toIndex) {
        try {
            super.removeRange(fromIndex, toIndex);
        } finally {
            notifyObserver();
        }
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        try {
            return super.removeAll(collection);
        } finally {
            notifyObserver();
        }
    }

    @Override
    public void clear() {
        try {
            super.clear();
        } finally {
            notifyObserver();
        }
    }

    private void notifyObserver() {
        if (mTransaction == null) {
            notifyObserverActually();
        }
    }

    private void notifyObserverActually() {
        if (mContentObserver != null) {
            try {
                mContentObserver.onContentChanged();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    interface ContentObserver {

        void onContentChanged();

    }

    public static class Transaction<T> {

        private CCAdapterHandler<T> mAdapterHandler;
        private LinkedList<Operation<T>> mOperations = new LinkedList<Operation<T>>();

        public Transaction(CCAdapterHandler<T> adapterHandler) {
            mAdapterHandler = adapterHandler;
        }

        public Transaction add(T object) {
            mOperations.add(new AddOperation<T>(object));
            return this;
        }

        public Transaction add(int index, T object) {
            mOperations.add(new AddOperation<T>(object, index));
            return this;
        }

        public Transaction addAll(Collection<T> collection) {
            mOperations.add(new AddAllOperation<T>(collection));
            return this;
        }

        public Transaction addAll(int index, Collection<T> collection) {
            mOperations.add(new AddAllOperation<T>(collection, index));
            return this;
        }

        public Transaction set(int index, T object) {
            mOperations.add(new SetOperation<T>(object, index));
            return this;
        }

        public Transaction set(Collection<T> collection) {
            mOperations.add(new SetAllOperation<T>(collection));
            return this;
        }

        public Transaction remove(T object) {
            mOperations.add(new RemoveOperation<T>(object));
            return this;
        }

        public Transaction remove(int index) {
            mOperations.add(new RemoveIndexOperation<T>(index));
            return this;
        }

        protected Transaction removeRange(int fromIndex, int toIndex) {
            mOperations.add(new RemoveRangeOperation<T>(fromIndex, toIndex));
            return this;
        }

        public Transaction removeAll(Collection<T> collection) {
            mOperations.add(new RemoveAllOperation<T>(collection));
            return this;
        }

        public Transaction clear() {
            mOperations.add(new ClearOperation<T>());
            return this;
        }

        public void commitAndEnd() {
            try {
                for (Operation<T> operation : mOperations) {
                    operation.doOperation(mAdapterHandler);
                }
                mAdapterHandler.notifyObserverActually();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mAdapterHandler.endTransaction();
            }
        }

        interface Operation<T> {

            void doOperation(CCAdapterHandler<T> handler);
        }

        static class AddOperation<T> implements Operation<T> {

            private T element;

            private int position = -1;

            public AddOperation(T element) {
                this.element = element;
            }

            public AddOperation(T element, int position) {
                this.element = element;
                this.position = position;
            }

            @Override
            public void doOperation(CCAdapterHandler<T> handler) {
                if (position == -1) {
                    handler.add(element);
                } else {
                    handler.add(position, element);
                }
            }
        }

        static class AddAllOperation<T> implements Operation<T> {

            private Collection<T> elements;

            private int position = -1;

            public AddAllOperation(Collection<T> elements) {
                this.elements = elements;
            }

            public AddAllOperation(Collection<T> elements, int position) {
                this.elements = elements;
                this.position = position;
            }

            @Override
            public void doOperation(CCAdapterHandler<T> handler) {
                if (position == -1) {
                    handler.addAll(elements);
                } else {
                    handler.addAll(position, elements);
                }
            }
        }

        static class SetOperation<T> implements Operation<T> {

            private T element;

            private int position = -1;

            public SetOperation(T element) {
                this.element = element;
            }

            public SetOperation(T element, int position) {
                this.element = element;
                this.position = position;
            }

            @Override
            public void doOperation(CCAdapterHandler<T> handler) {
                if (position == -1) {
                    handler.set(element);
                } else {
                    handler.set(position, element);
                }
            }
        }

        static class SetAllOperation<T> implements Operation<T> {

            private Collection<T> elements;

            public SetAllOperation(Collection<T> elements) {
                this.elements = elements;
            }

            @Override
            public void doOperation(CCAdapterHandler<T> handler) {
                handler.set(elements);
            }
        }

        static class RemoveOperation<T> implements Operation<T> {

            private T element;

            public RemoveOperation(T element) {
                this.element = element;
            }

            @Override
            public void doOperation(CCAdapterHandler<T> handler) {
                handler.remove(element);
            }
        }

        static class RemoveIndexOperation<T> implements Operation<T> {

            private int position = -1;

            public RemoveIndexOperation(int position) {
                this.position = position;
            }

            @Override
            public void doOperation(CCAdapterHandler<T> handler) {
                handler.remove(position);
            }
        }

        static class RemoveRangeOperation<T> implements Operation<T> {

            private int fromIndex;
            private int toIndex;

            public RemoveRangeOperation(int fromIndex, int toIndex) {
                this.fromIndex = fromIndex;
                this.toIndex = toIndex;
            }

            @Override
            public void doOperation(CCAdapterHandler<T> handler) {
                handler.removeRange(fromIndex, toIndex);
            }
        }

        static class RemoveAllOperation<T> implements Operation<T> {

            private Collection<T> elements;

            public RemoveAllOperation(Collection<T> elements) {
                this.elements = elements;
            }

            @Override
            public void doOperation(CCAdapterHandler<T> handler) {
                handler.removeAll(elements);
            }
        }

        static class ClearOperation<T> implements Operation<T> {

            @Override
            public void doOperation(CCAdapterHandler<T> handler) {
                handler.clear();
            }
        }

    }
}