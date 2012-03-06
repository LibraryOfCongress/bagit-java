package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.FetchTxt;
import java.util.Arrays;
import junit.framework.Assert;
import org.junit.Test;

public class ChainSorterTests
{
	private ChainSorter<FetchTxt.FilenameSizeUrl> unit;
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSortsByFirstThenSecond()
	{
		this.unit = new ChainSorter<FetchTxt.FilenameSizeUrl>(
				new FetchFilenameSorter(),
				new FetchSizeSorter());
		
		FetchTxt.FilenameSizeUrl[] items = new FetchTxt.FilenameSizeUrl[] {
				new FetchTxt.FilenameSizeUrl("/ddd", 300L, "http://www.example.org/foo"),
				new FetchTxt.FilenameSizeUrl("/ccc", 100L, "http://www.example.org/foo"),
				new FetchTxt.FilenameSizeUrl("/ddd", 200L, "http://www.example.org/foo"),
				new FetchTxt.FilenameSizeUrl("/bbb", 800L, "http://www.example.org/foo"),
				new FetchTxt.FilenameSizeUrl("/ccc", 200L, "http://www.example.org/foo"),
				new FetchTxt.FilenameSizeUrl("/aaa", 500L, "http://www.example.org/foo"),
				new FetchTxt.FilenameSizeUrl("/ddd", 100L, "http://www.example.org/foo")
		};
		
		Arrays.sort(items, this.unit);
		
		Assert.assertEquals(7, items.length);
		assertEquals("/aaa", 500L, items[0]);
		assertEquals("/bbb", 800L, items[1]);
		assertEquals("/ccc", 100L, items[2]);
		assertEquals("/ccc", 200L, items[3]);
		assertEquals("/ddd", 100L, items[4]);
		assertEquals("/ddd", 200L, items[5]);
		assertEquals("/ddd", 300L, items[6]);
	}
	
	public static void assertEquals(String path, Long length, FetchTxt.FilenameSizeUrl actual)
	{
		Assert.assertEquals(path, actual.getFilename());
		Assert.assertEquals(length, actual.getSize());
	}
}
