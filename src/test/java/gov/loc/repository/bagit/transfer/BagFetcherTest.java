package gov.loc.repository.bagit.transfer;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class BagFetcherTest{
	private static File tempDir = new File("target/unittestdata/BagFetcherTest");
	private Mockery context = new Mockery();
	private BagFactory bagFactory = new BagFactory();
	private BagFetcher unit;
	
	@Before
	public void setUp() throws Exception {
		if (tempDir.exists()){
			FileUtils.forceDelete(tempDir);			
		}
		
		tempDir.mkdirs();
		
		this.unit = new BagFetcher(this.bagFactory);
		this.unit.setFetchFailStrategy(new ThrowExceptionFailStrategy());

		FileUtils.copyDirectory(ResourceHelper.getFile("bags/v0_96/holey-bag"), tempDir);
		FileUtils.deleteDirectory(new File(tempDir, "data"));	
	}

	@After
	public void tearDown() throws Exception {
		if (tempDir.exists()){
			FileUtils.deleteQuietly(tempDir);			
		}
	}

	@Test
	public void testFetchSingleThread() throws Exception{
		this.unit.setNumberOfThreads(1);

		final FetchedFileDestinationFactory mockDestinationFactory = this.context.mock(FetchedFileDestinationFactory.class);		
		final FetchProtocol mockProtocol = this.context.mock(FetchProtocol.class);
		final FileFetcher mockFetcher = this.context.mock(FileFetcher.class);
		final States fetcherState = this.context.states("fetcher").startsAs("new");
		
		context.checking(new Expectations() {{
			// Destination
			expectDest(this, mockDestinationFactory, "data/dir1/test3.txt");
			expectDest(this, mockDestinationFactory, "data/dir2/dir3/test5.txt");
			expectDest(this, mockDestinationFactory, "data/dir2/test4.txt");
			expectDest(this, mockDestinationFactory, "data/test 1.txt");
			expectDest(this, mockDestinationFactory, "data/test2.txt");
						
			// Protocol
			one(mockProtocol).createFetcher(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"), null);	will(returnValue(mockFetcher));
			
			// Fetcher
			one(mockFetcher).initialize(); when(fetcherState.is("new")); then(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/dir3/test5.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/test4.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test%201.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test2.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).close(); when(fetcherState.is("ready")); then(fetcherState.is("closed"));
		}});
		
		this.unit.registerProtocol("http", mockProtocol);
		
		Bag bag = this.bagFactory.createBag(tempDir);
		
		SimpleResult result = this.unit.fetch(bag, mockDestinationFactory, false);		
	}
	
	@Test
	public void testFailsFast() throws Exception{
		this.unit.setNumberOfThreads(1);
		this.unit.setFetchFailStrategy(StandardFailStrategies.FAIL_FAST);

		final FetchedFileDestinationFactory mockDestinationFactory = this.context.mock(FetchedFileDestinationFactory.class);
		final FetchedFileDestination mockDestination = this.context.mock(FetchedFileDestination.class);
		final FetchProtocol mockProtocol = this.context.mock(FetchProtocol.class);
		final FileFetcher mockFetcher = this.context.mock(FileFetcher.class);
		final States fetcherState = this.context.states("fetcher").startsAs("new");
		
		context.checking(new Expectations() {{
			// Destination
			one(mockDestinationFactory).createDestination("data/dir1/test3.txt", null); will(returnValue(mockDestination));
			allowing(mockDestination).getFilepath(); will(returnValue("data/dir1/test3.txt"));
			never(mockDestination).commit();
			one(mockDestination).abandon();
						
			// Protocol
			one(mockProtocol).createFetcher(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"), null);	will(returnValue(mockFetcher));
			
			// Fetcher
			one(mockFetcher).initialize(); when(fetcherState.is("new")); then(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready")); will(throwException(new BagTransferException("Unit test failure.")));
			one(mockFetcher).close(); when(fetcherState.is("ready")); then(fetcherState.is("closed"));
		}});
		
		this.unit.registerProtocol("http", mockProtocol);
		
		Bag bag = this.bagFactory.createBag(tempDir);
		
		SimpleResult result = this.unit.fetch(bag, mockDestinationFactory, false);
		
		assertFalse("Bag transferred successfully when it shouldn't have.", result.isSuccess());
	}
	
	@Test
	public void testRetriesFile() throws Exception{
		this.unit.setNumberOfThreads(1);
		this.unit.setFetchFailStrategy(StandardFailStrategies.ALWAYS_RETRY);

		final FetchedFileDestinationFactory mockDestinationFactory = this.context.mock(FetchedFileDestinationFactory.class);
		final FetchedFileDestination mockDestination = this.context.mock(FetchedFileDestination.class);
		final FetchProtocol mockProtocol = this.context.mock(FetchProtocol.class);
		final FileFetcher mockFetcher = this.context.mock(FileFetcher.class);
		final States fetcherState = this.context.states("fetcher").startsAs("new");
		
		context.checking(new Expectations() {{
			// Destination - first try
			one(mockDestinationFactory).createDestination("data/dir1/test3.txt", null); will(returnValue(mockDestination));
			allowing(mockDestination).getFilepath(); will(returnValue("data/dir1/test3.txt"));
			never(mockDestination).commit();
			one(mockDestination).abandon();
						
			// Destination - second try
			expectDest(this, mockDestinationFactory, "data/dir1/test3.txt");
						
			// Protocol
			one(mockProtocol).createFetcher(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"), null);	will(returnValue(mockFetcher));
			
			// Fetcher
			one(mockFetcher).initialize(); when(fetcherState.is("new")); then(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready")); will(throwException(new BagTransferException("Unit test failure.")));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready")); // Second one succeeds
			one(mockFetcher).close(); when(fetcherState.is("ready")); then(fetcherState.is("closed"));
		}});
		
		this.unit.registerProtocol("http", mockProtocol);
		
		Bag bag = this.bagFactory.createBag(tempDir);

		// Trim out all but one file from the fetch.txt, for simplicity.
		bag.getFetchTxt().remove(1);
		bag.getFetchTxt().remove(1);
		bag.getFetchTxt().remove(1);
		bag.getFetchTxt().remove(1);
		
		SimpleResult result = this.unit.fetch(bag, mockDestinationFactory, false);
	}

	@Test
	public void testRetriesNextFile() throws Exception{
		this.unit.setNumberOfThreads(1);
		this.unit.setFetchFailStrategy(StandardFailStrategies.ALWAYS_CONTINUE);

		final FetchedFileDestinationFactory mockDestinationFactory = this.context.mock(FetchedFileDestinationFactory.class);
		final FetchedFileDestination mockDestination = this.context.mock(FetchedFileDestination.class);
		final FetchProtocol mockProtocol = this.context.mock(FetchProtocol.class);
		final FileFetcher mockFetcher = this.context.mock(FileFetcher.class);
		final States fetcherState = this.context.states("fetcher").startsAs("new");
		
		context.checking(new Expectations() {{
			// Destination - first file
			one(mockDestinationFactory).createDestination("data/dir1/test3.txt", null); will(returnValue(mockDestination));
			allowing(mockDestination).getFilepath(); will(returnValue("data/dir1/test3.txt"));
			never(mockDestination).commit();
			one(mockDestination).abandon();
						
			// Destination - second file
			expectDest(this, mockDestinationFactory, "data/dir2/dir3/test5.txt");
						
			// Protocol
			one(mockProtocol).createFetcher(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"), null);	will(returnValue(mockFetcher));
			
			// Fetcher
			one(mockFetcher).initialize(); when(fetcherState.is("new")); then(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready")); will(throwException(new BagTransferException("Unit test failure.")));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/dir3/test5.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).close(); when(fetcherState.is("ready")); then(fetcherState.is("closed"));
		}});
		
		this.unit.registerProtocol("http", mockProtocol);
		
		Bag bag = this.bagFactory.createBag(tempDir);

		// Trim out all but two files from the fetch.txt, for simplicity.
		bag.getFetchTxt().remove(2);
		bag.getFetchTxt().remove(2);
		bag.getFetchTxt().remove(2);
		
		SimpleResult result = this.unit.fetch(bag, mockDestinationFactory, false);
		
		assertFalse("Bag failed transfer.", result.isSuccess());
	}

	@Test
	public void testResume() throws Exception{
		this.copyPartialBag();
		
		this.unit.setNumberOfThreads(1);
		this.unit.setFetchFailStrategy(StandardFailStrategies.ALWAYS_CONTINUE);

		final FetchedFileDestinationFactory mockDestinationFactory = this.context.mock(FetchedFileDestinationFactory.class);
		final FetchedFileDestination mockDestination = this.context.mock(FetchedFileDestination.class);
		final FetchProtocol mockProtocol = this.context.mock(FetchProtocol.class);
		final FileFetcher mockFetcher = this.context.mock(FileFetcher.class);
		
		final Bag bag = this.bagFactory.createBag(tempDir);

		context.checking(new Expectations() {{
			// Destination - first file
			one(mockDestinationFactory).createDestination("data/dir1/test3.txt", null); will(returnValue(mockDestination));
			allowing(mockDestination).getFilepath(); will(returnValue("data/dir1/test3.txt"));
			never(mockDestination).commit();
			one(mockDestination).abandon();
						
			// Destination - second file
			expectDest(this, mockDestinationFactory, "data/dir2/dir3/test5.txt");
			one(mockProtocol).createFetcher(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"), null);	will(returnValue(mockFetcher));
			
			allowing(mockFetcher).initialize(); 
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class)));  will(throwException(new BagTransferException("Unit test failure.")));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/dir3/test5.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); 
			never(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/test4.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class)));
			never(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test%201.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class)));
			never(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test2.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class)));
			allowing(mockFetcher).close(); 
			
		}});

		this.unit.registerProtocol("http", mockProtocol);
		SimpleResult result = this.unit.fetch(bag, mockDestinationFactory, true, false);
		assertFalse(result.isSuccess());
		
		this.deletePartialBag();
	}
	
	@Test
	public void testVerify() throws Exception
	{
		this.copyPartialBag();
		
		this.unit.setNumberOfThreads(1);
		this.unit.setFetchFailStrategy(StandardFailStrategies.ALWAYS_CONTINUE);

		final FetchedFileDestinationFactory mockDestinationFactory = this.context.mock(FetchedFileDestinationFactory.class);
		//final FetchedFileDestination mockDestination = this.context.mock(FetchedFileDestination.class);
		final FetchProtocol mockProtocol = this.context.mock(FetchProtocol.class);
		final FileFetcher mockFetcher = this.context.mock(FileFetcher.class);
		
		final Bag bag = this.bagFactory.createBag(tempDir);

		context.checking(new Expectations() {{
			// Destination - fourth file
			expectDest(this, mockDestinationFactory, "data/test 1.txt");
				
			// Destination - fifth file
			expectDest(this, mockDestinationFactory, "data/test2.txt");			
			
			one(mockProtocol).createFetcher(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test%201.txt"), null);	will(returnValue(mockFetcher));
			
			allowing(mockFetcher).initialize(); 
			//The first file is fetched and valid despite the status in fetch-progress.txt
			never(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class)));  will(throwException(new BagTransferException("Unit test failure.")));
			//The second file is fetched and valid despite the status in fetch-progress.txt
			never(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/dir3/test5.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); 
			never(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/test4.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class)));
			//The fourth file is missing in the partially fetched bag
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test%201.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class)));
			//The fifth file is corrupted
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test2.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class)));
			allowing(mockFetcher).close(); 
			
		}});

		this.unit.registerProtocol("http", mockProtocol);
		SimpleResult result = this.unit.fetch(bag, mockDestinationFactory, true, true);
		assertTrue(bag.getFetchProgressTxt() == null);

		this.deletePartialBag();		
	}
	
	@Test(expected=BagTransferException.class)
	public void testMissingFetchTxt() throws Exception{
		final FetchedFileDestinationFactory mockDestinationFactory = this.context.mock(FetchedFileDestinationFactory.class);
		File fetchTxtFile = new File(tempDir, this.bagFactory.getBagConstants().getFetchTxt());
		assertTrue(fetchTxtFile.exists());
		fetchTxtFile.delete();
		assertFalse(fetchTxtFile.exists());
		Bag bag = this.bagFactory.createBag(tempDir);
		
		this.unit.fetch(bag, mockDestinationFactory, false);
		
	}

	private void expectDest(Expectations e, FetchedFileDestinationFactory destFactory, String path) throws Exception{
		FetchedFileDestination mockDestination = this.context.mock(FetchedFileDestination.class, "FetchedFileDestination:" + path);
		BagFile mockCommittedFile = this.context.mock(BagFile.class, "BagFile:" + path);
		
		// Make the factory return a new destination.
		e.one(destFactory).createDestination(path, null);
		e.will(Expectations.returnValue(mockDestination));
		
		// That destination should:
		// 1) Always return the file path.
		e.allowing(mockDestination).getFilepath();
		e.will(Expectations.returnValue(path));
		
		// 2) Be opened once.
		// Except that the destination will never be opened because
		// it is being passed to a mock fetcher, which won't open it.  :-)
		// e.one(mockDestination).openOutputStream(false);
		// e.will(Expectations.returnValue(new NullOutputStream()));
		
		// 3) Be committed once.		
		e.one(mockDestination).commit(); 
		e.will(Expectations.returnValue(mockCommittedFile));

		e.one(mockCommittedFile).exists();
		e.will(Expectations.returnValue(true));

		e.one(mockCommittedFile).newInputStream();
		e.will(Expectations.returnValue(new FileInputStream(ResourceHelper.getFile("bags/v0_96/holey-bag/" + path))));
		
		// 4) Will not be abandoned.
		e.never(mockDestination).abandon();
	}
	
	private void copyPartialBag() throws Exception{
		if (tempDir.exists()){
			FileUtils.forceDelete(tempDir);			
		}
		
		tempDir.mkdirs();
		
		this.unit = new BagFetcher(this.bagFactory);
		this.unit.setFetchFailStrategy(new ThrowExceptionFailStrategy());

		FileUtils.copyDirectory(ResourceHelper.getFile("bags/v0_96/holey-bag-partially-fetched"), tempDir);
	}
	
	private void deletePartialBag() throws Exception{
		if (tempDir.exists()){
			FileUtils.deleteQuietly(tempDir);			
		}	
	}
}
