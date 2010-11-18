package gov.loc.repository.bagit.transfer;

/**
 * The action a fetch operation should take as a response
 * to failures.
 * 
 * @version $Id$
 * @see FetchFailStrategy
 */
public enum FetchFailureAction
{
	RETRY_CURRENT,
	CONTINUE_WITH_NEXT,
	STOP
}
