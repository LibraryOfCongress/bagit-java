package gov.loc.repository.bagit.transfer;

import static junit.framework.Assert.*;

import java.io.File;
import java.net.URI;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.utilities.ResourceHelper;

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
public class BagFetcherTest
{
	private static File tempDir = new File("target/unittestdata/BagFetcherTest");
	private Mockery context = new Mockery();
	private BagFactory bagFactory = new BagFactory();
	private BagFetcher unit;
	
	@Before
	public void setUp() throws Exception 
	{
		if (tempDir.exists())
			FileUtils.forceDelete(tempDir);
		
		tempDir.mkdirs();
		
		this.unit = new BagFetcher(this.bagFactory);
		this.unit.setFetchFailStrategy(new ThrowExceptionFailStrategy());

		FileUtils.copyDirectory(ResourceHelper.getFile("bags/v0_96/holey-bag"), tempDir);
		FileUtils.deleteDirectory(new File(tempDir, "data"));	
	}

	@After
	public void tearDown() throws Exception
	{
		if (tempDir.exists())
			FileUtils.deleteQuietly(tempDir);
	}

	@Test
	public void testFetchSingleThread() throws Exception
	{
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
			expectDest(this, mockDestinationFactory, "data/test1.txt");
			expectDest(this, mockDestinationFactory, "data/test2.txt");
						
			// Protocol
			one(mockProtocol).createFetcher(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"), null);	will(returnValue(mockFetcher));
			
			// Fetcher
			one(mockFetcher).initialize(); when(fetcherState.is("new")); then(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/dir3/test5.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/test4.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test1.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test2.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).close(); when(fetcherState.is("ready")); then(fetcherState.is("closed"));
		}});
		
		this.unit.registerProtocol("http", mockProtocol);
		
		Bag bag = this.bagFactory.createBag(tempDir);
		
		BagFetchResult result = this.unit.fetch(bag, mockDestinationFactory);
		
		assertTrue("Bag did not transfer successfully.", result.isSuccess());
	}
	
	@Test
	public void testFailsFast() throws Exception
	{
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
		
		BagFetchResult result = this.unit.fetch(bag, mockDestinationFactory);
		
		assertFalse("Bag transferred successfully when it shouldn't have.", result.isSuccess());
	}
	
	@Test
	public void testRetriesFile() throws Exception
	{
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
		
		BagFetchResult result = this.unit.fetch(bag, mockDestinationFactory);
		
		assertTrue("Bag failed transfer when it should have succeeded.", result.isSuccess());
	}

	@Test
	public void testRetriesNextFile() throws Exception
	{
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
		
		BagFetchResult result = this.unit.fetch(bag, mockDestinationFactory);
		
		assertTrue("Bag failed transfer when it should have succeeded.", result.isSuccess());
	}
	
	@Test
	public void attemptsToTransferMultipleLines() throws Exception
	{
		this.unit.setNumberOfThreads(1);
		
		// Fail fast, so that if both lines fail to transfer, we'll
		// bomb out with an exception.  We shouldn't get an exception, though
		// because the second fetch line should work.
		this.unit.setFetchFailStrategy(StandardFailStrategies.FAIL_FAST);

		final FetchedFileDestinationFactory mockDestinationFactory = this.context.mock(FetchedFileDestinationFactory.class);
		final FetchedFileDestination mockDestination = this.context.mock(FetchedFileDestination.class);
		final FetchProtocol mockProtocol = this.context.mock(FetchProtocol.class);
		final FileFetcher mockFetcher = this.context.mock(FileFetcher.class);
		final States fetcherState = this.context.states("fetcher").startsAs("new");
		
		context.checking(new Expectations() {{
			// Destination - first try.
			one(mockDestinationFactory).createDestination("data/test1.txt", null); will(returnValue(mockDestination));
			allowing(mockDestination).getFilepath(); will(returnValue("data/test1.txt"));
			one(mockDestination).abandon();
						
			// Destination - second try.
			expectDest(this, mockDestinationFactory, "data/test1.txt");
						
			// Protocol
			one(mockProtocol).createFetcher(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/does-not-exist"), null);	will(returnValue(mockFetcher));
			
			// Fetcher
			one(mockFetcher).initialize(); when(fetcherState.is("new")); then(fetcherState.is("ready"));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/does-not-exist"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready")); will(throwException(new BagTransferException("You got a 404!")));
			one(mockFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test1.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(fetcherState.is("ready"));
			one(mockFetcher).close(); when(fetcherState.is("ready")); then(fetcherState.is("closed"));
		}});
		
		this.unit.registerProtocol("http", mockProtocol);
		
		Bag bag = this.bagFactory.createBag(tempDir);
		
		// Clear out the fetch.txt, and then set it up so that we have
		// two lines for a file, but wtih different URIs.
		// The former should be a 404, the latter should work.
		// http://localhost:8989/bags/v0_96/holey-bag/data/does-not-exist - data/test1.txt
		// http://localhost:8989/bags/v0_96/holey-bag/data/test2.txt - data/test2.txt
		bag.getFetchTxt().clear();
		bag.getFetchTxt().add(new FetchTxt.FilenameSizeUrl("data/test1.txt", null, "http://localhost:8989/bags/v0_96/holey-bag/data/does-not-exist"));
		bag.getFetchTxt().add(new FetchTxt.FilenameSizeUrl("data/test1.txt", null, "http://localhost:8989/bags/v0_96/holey-bag/data/test1.txt"));
		
		BagFetchResult result = this.unit.fetch(bag, mockDestinationFactory);
		
		assertTrue("Bag failed transfer when it should have succeeded.", result.isSuccess());
	}

	@Test
	public void attemptsToTransferMultipleLinesWithDifferentSchemas() throws Exception
	{
		this.unit.setNumberOfThreads(1);
		
		// Fail fast, so that if both lines fail to transfer, we'll
		// bomb out with an exception.  We shouldn't get an exception, though
		// because the second fetch line should work.
		this.unit.setFetchFailStrategy(StandardFailStrategies.FAIL_FAST);

		final FetchedFileDestinationFactory mockDestinationFactory = this.context.mock(FetchedFileDestinationFactory.class);
		final FetchedFileDestination mockDestination = this.context.mock(FetchedFileDestination.class);
		final FetchProtocol mockHttpProtocol = this.context.mock(FetchProtocol.class, "HttpFetchProtocol");
		final FetchProtocol mockFtpProtocol = this.context.mock(FetchProtocol.class, "FtpFetchProtocol");
		final FileFetcher mockHttpFetcher = this.context.mock(FileFetcher.class, "HttpFetcher");
		final FileFetcher mockFtpFetcher = this.context.mock(FileFetcher.class, "FtpFetcher");
		final States httpFetcherState = this.context.states("http-fetcher").startsAs("new");
		final States ftpFetcherState = this.context.states("ftp-fetcher").startsAs("new");
		
		context.checking(new Expectations() {{
			// Destination - first try.
			one(mockDestinationFactory).createDestination("data/test1.txt", null); will(returnValue(mockDestination));
			allowing(mockDestination).getFilepath(); will(returnValue("data/test1.txt"));
			one(mockDestination).abandon();
						
			// Destination - second try.
			expectDest(this, mockDestinationFactory, "data/test1.txt");
						
			// Protocol
			one(mockFtpProtocol).createFetcher(new URI("ftp://localhost:8989/bags/v0_96/holey-bag/data/does-not-exist"), null);		will(returnValue(mockFtpFetcher));
			one(mockHttpProtocol).createFetcher(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test1.txt"), null);		will(returnValue(mockHttpFetcher));
			
			// FTP Fetcher
			one(mockFtpFetcher).initialize(); when(ftpFetcherState.is("new")); then(ftpFetcherState.is("ready"));
			one(mockFtpFetcher).fetchFile(with(equal(new URI("ftp://localhost:8989/bags/v0_96/holey-bag/data/does-not-exist"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(ftpFetcherState.is("ready")); will(throwException(new BagTransferException("Oh, man.  Don't use FTP!")));
			one(mockFtpFetcher).close(); when(ftpFetcherState.is("ready")); then(ftpFetcherState.is("closed"));

			// HTTP Fetcher
			one(mockHttpFetcher).initialize(); when(httpFetcherState.is("new")); then(httpFetcherState.is("ready"));
			one(mockHttpFetcher).fetchFile(with(equal(new URI("http://localhost:8989/bags/v0_96/holey-bag/data/test1.txt"))), with(any(Long.class)), with(aNonNull(FetchedFileDestination.class)), with(aNonNull(FetchContext.class))); when(httpFetcherState.is("ready"));
			one(mockHttpFetcher).close(); when(httpFetcherState.is("ready")); then(httpFetcherState.is("closed"));
		}});
		
		this.unit.registerProtocol("http", mockHttpProtocol);
		this.unit.registerProtocol("ftp", mockFtpProtocol);
		
		Bag bag = this.bagFactory.createBag(tempDir);
		
		// Clear out the fetch.txt, and then set it up so that we have
		// two lines for a file, but wtih different URIs with different schemes.
		// The former should be a 404, the latter should work.
		// ftp://localhost:8989/bags/v0_96/holey-bag/data/does-not-exist - data/test1.txt
		// http://localhost:8989/bags/v0_96/holey-bag/data/test2.txt - data/test2.txt
		bag.getFetchTxt().clear();
		bag.getFetchTxt().add(new FetchTxt.FilenameSizeUrl("data/test1.txt", null, "ftp://localhost:8989/bags/v0_96/holey-bag/data/does-not-exist"));
		bag.getFetchTxt().add(new FetchTxt.FilenameSizeUrl("data/test1.txt", null, "http://localhost:8989/bags/v0_96/holey-bag/data/test1.txt"));
		
		BagFetchResult result = this.unit.fetch(bag, mockDestinationFactory);
		
		assertTrue("Bag failed transfer when it should have succeeded.", result.isSuccess());
	}

	private void expectDest(Expectations e, FetchedFileDestinationFactory destFactory, String path) throws Exception
	{
		FetchedFileDestination mockDestination = this.context.mock(FetchedFileDestination.class, "FetchedFileDestination:" + path);
		
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
		
		// 4) Will not be abandoned.
		e.never(mockDestination).abandon();
	}
}
