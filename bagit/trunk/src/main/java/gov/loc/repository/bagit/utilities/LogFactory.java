package gov.loc.repository.bagit.utilities;

import java.text.MessageFormat;

public final class LogFactory
{
    private LogFactory() {}
    
    public static final Log getLog(Class<?> clazz)
    {
        org.apache.commons.logging.Log realLog = org.apache.commons.logging.LogFactory.getLog(clazz);
        return new NicerLog(realLog);
    }
    
    private static class NicerLog implements Log
    {
        private org.apache.commons.logging.Log realLog;
        
        public NicerLog(org.apache.commons.logging.Log realLog)
        {
            this.realLog = realLog;
        }
        
        @Override
        public void trace(String msg, Object... args)
        {
            if (this.isTraceEnabled())
            {
                this.trace(MessageFormat.format(msg, args));
            }
        }
        
        @Override
        public void debug(String msg, Object... args)
        {
            if (this.isDebugEnabled())
            {
                this.debug(MessageFormat.format(msg, args));
            }
        }

        @Override
        public void error(String msg, Object... args)
        {
            if (this.isErrorEnabled())
            {
                this.error(MessageFormat.format(msg, args));
            }
        }

        @Override
        public void fatal(String msg, Object... args)
        {
            if (this.isFatalEnabled())
            {
                this.fatal(MessageFormat.format(msg, args));
            }
        }

        @Override
        public void info(String msg, Object... args)
        {
            if (this.isInfoEnabled())
            {
                this.info(MessageFormat.format(msg, args));
            }
        }

        @Override
        public void warn(String msg, Object... args)
        {
            if (this.isWarnEnabled())
            {
                this.warn(MessageFormat.format(msg, args));
            }
        }

        public void debug(Object msg, Throwable e)
        {
            this.realLog.debug(msg, e);
        }

        public void debug(Object msg)
        {
            this.realLog.debug(msg);
        }

        public void error(Object msg, Throwable e)
        {
            this.realLog.error(msg, e);
        }

        public void error(Object msg)
        {
            this.realLog.error(msg);
        }

        public void fatal(Object msg, Throwable e)
        {
            this.realLog.fatal(msg, e);
        }

        public void fatal(Object msg)
        {
            this.realLog.fatal(msg);
        }

        public void info(Object msg, Throwable e)
        {
            this.realLog.info(msg, e);
        }

        public void info(Object msg)
        {
            this.realLog.info(msg);
        }

        public boolean isDebugEnabled()
        {
            return this.realLog.isDebugEnabled();
        }

        public boolean isErrorEnabled()
        {
            return this.realLog.isErrorEnabled();
        }

        public boolean isFatalEnabled()
        {
            return this.realLog.isFatalEnabled();
        }

        public boolean isInfoEnabled()
        {
            return this.realLog.isInfoEnabled();
        }

        public boolean isTraceEnabled()
        {
            return this.realLog.isTraceEnabled();
        }

        public boolean isWarnEnabled()
        {
            return this.realLog.isWarnEnabled();
        }

        public void trace(Object msg, Throwable e)
        {
            this.realLog.trace(msg, e);
        }

        public void trace(Object msg)
        {
            this.realLog.trace(msg);
        }

        public void warn(Object msg, Throwable e)
        {
            this.realLog.warn(msg, e);
        }

        public void warn(Object msg)
        {
            this.realLog.warn(msg);
        }
    }
}
