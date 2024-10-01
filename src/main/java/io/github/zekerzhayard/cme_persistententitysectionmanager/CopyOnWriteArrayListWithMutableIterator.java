package io.github.zekerzhayard.cme_persistententitysectionmanager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import sun.misc.Unsafe;

public class CopyOnWriteArrayListWithMutableIterator<T> extends CopyOnWriteArrayList<T> {
    static MethodHandle getArrayMethod = getMethod();

    public static <T> List<T> create() {
        return new CopyOnWriteArrayListWithMutableIterator<>();
    }

    private static MethodHandle getMethod() {
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Unsafe theUnsafe = (Unsafe) theUnsafeField.get(null);

            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            MethodHandles.Lookup implLookup = (MethodHandles.Lookup) theUnsafe.getObject(theUnsafe.staticFieldBase(implLookupField), theUnsafe.staticFieldOffset(implLookupField));
            return implLookup.findVirtual(CopyOnWriteArrayList.class, "getArray", MethodType.methodType(Object[].class));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected Object[] getArray() {
        try {
            return (Object[]) getArrayMethod.invokeWithArguments((Object) this);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>The returned iterator provides a snapshot of the state of the list
     * when the iterator was constructed. No synchronization is needed while
     * traversing the iterator. The iterator does <em>NOT</em> support the
     * {@code remove} method.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    public Iterator<T> iterator() {
        return new COWIterator<T>(getArray(), 0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The returned iterator provides a snapshot of the state of the list
     * when the iterator was constructed. No synchronization is needed while
     * traversing the iterator. The iterator does <em>NOT</em> support the
     * {@code remove}, {@code set} or {@code add} methods.
     */
    public ListIterator<T> listIterator() {
        return new COWIterator<T>(getArray(), 0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The returned iterator provides a snapshot of the state of the list
     * when the iterator was constructed. No synchronization is needed while
     * traversing the iterator. The iterator does <em>NOT</em> support the
     * {@code remove}, {@code set} or {@code add} methods.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<T> listIterator(int index) {
        Object[] elements = getArray();
        int len = elements.length;
        if (index < 0 || index > len)
            throw new IndexOutOfBoundsException("Index: "+index);

        return new COWIterator<T>(elements, index);
    }

    final class COWIterator<E> implements ListIterator<E> {
        /** Snapshot of the array */
        private final Object[] snapshot;
        /** Index of element to be returned by subsequent call to next.  */
        private int cursor;

        private COWIterator(Object[] elements, int initialCursor) {
            cursor = initialCursor;
            snapshot = elements;
        }

        public boolean hasNext() {
            return cursor < snapshot.length;
        }

        public boolean hasPrevious() {
            return cursor > 0;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (! hasNext())
                throw new NoSuchElementException();
            return (E) snapshot[cursor++];
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            if (! hasPrevious())
                throw new NoSuchElementException();
            return (E) snapshot[--cursor];
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor-1;
        }

        public void remove() {
            CopyOnWriteArrayListWithMutableIterator.this.remove(snapshot[cursor - 1]);
        }

        @SuppressWarnings("unchecked")
        public void set(E e) {
            CopyOnWriteArrayListWithMutableIterator.this.set(cursor - 1, (T) e);
        }

        @SuppressWarnings("unchecked")
        public void add(E e) {
            CopyOnWriteArrayListWithMutableIterator.this.add(cursor - 1, (T) e);
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] elements = snapshot;
            final int size = elements.length;
            for (int i = cursor; i < size; i++) {
                @SuppressWarnings("unchecked") E e = (E) elements[i];
                action.accept(e);
            }
            cursor = size;
        }
    }
}
