package gov.loc.repository.bagit.utilities;

public interface Log extends org.apache.commons.logging.Log
{
    void trace(String msg, Object...args);
    void debug(String msg, Object...args);
    void warn(String msg, Object...args);
    void info(String msg, Object...args);
    void error(String msg, Object...args);
    void fatal(String msg, Object...args);
}
