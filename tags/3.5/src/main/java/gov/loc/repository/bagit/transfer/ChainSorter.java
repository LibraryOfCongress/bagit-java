package gov.loc.repository.bagit.transfer;

import java.util.Comparator;

/**
 * Compares to objects by delegating to a chain of sub-comparisons.
 * This is useful for implementing secondary or tertiary sorting
 * of objects.  If no sub-comparisons are specified, the
 * {@link #compare(Object, Object) compare} method will default to
 * always returning 0.
 * 
 * @author Brian Vargas
 * @param <T> The type to be compared by the sub-comparisons.
 */
class ChainSorter<T> extends Object implements Comparator<T>
{
	private Comparator<T>[] comparators;
		
	@SuppressWarnings("unchecked")
	public ChainSorter(Comparator...sorters)
	{
		this.comparators = sorters;
	}
	
    @Override
    public int compare(T left, T right)
    {
    	int result = 0;
    	
    	for (int i = 0; i < this.comparators.length; i++)
    	{
    		result = this.comparators[i].compare(left, right);
    		
    		if (result != 0)
    			break;
    	}
    	
    	return result;
    }
}
