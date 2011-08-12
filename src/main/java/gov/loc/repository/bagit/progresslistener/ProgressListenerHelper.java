package gov.loc.repository.bagit.progresslistener;

import java.text.MessageFormat;

public class ProgressListenerHelper {
		
	public static String format(String activity, Object item, Long count, Long total) {
		String msg;
		
		if (count != null)
		{
			if (total != null)
			{
				msg = MessageFormat.format("{0} {1} ({2} of {3})", activity, item, count, total);
			}
			else
			{
				msg = MessageFormat.format("{0} {1} ({2})", activity, item, count);
			}
		}
		else
		{
			msg = MessageFormat.format("{0} {1}", activity, item);
		}
		return msg;
	}
}
