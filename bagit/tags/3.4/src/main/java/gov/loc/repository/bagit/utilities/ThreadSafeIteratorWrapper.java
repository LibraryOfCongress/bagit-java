package gov.loc.repository.bagit.utilities;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ThreadSafeIteratorWrapper<E> implements Iterator<E>, Iterable<E> {
    private E nextItem;
    private Iterator<E> iterator;
    
	public ThreadSafeIteratorWrapper(Iterator<E> iterator)
    {
        this.iterator = iterator;
    }
    
    @Override
    public Iterator<E> iterator()
    {
        return this;
    }
    
    @Override
    public boolean hasNext()
    {
        synchronized (this)
        {
            synchronized (this.iterator)
            {
                boolean hasNext = this.iterator.hasNext();
                
                if (hasNext)
                    this.nextItem = this.iterator.next();
                else
                    this.nextItem = null;
                
                return hasNext;
            }
        }
    }

    @Override
    public E next()
    {
        synchronized (this)
        {
            if (this.nextItem == null)
                throw new NoSuchElementException();
            E tmp = this.nextItem;
            this.nextItem = null;
            return tmp;
        }
    }
    
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    
}
