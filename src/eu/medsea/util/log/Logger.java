package eu.medsea.util.log;

public abstract class Logger
{
	public static Logger getLogger(String name)
	{
		try {
			return new Log4jLoggerImpl(name);
		} catch (Throwable t) {
			return new DummyLoggerImpl();
		}
	}
	public static Logger getLogger(Class clazz)
	{
		return getLogger(clazz.getName());
	}

	public abstract void warn(Object message);
	public abstract void warn(Object message, Throwable t);
	public abstract void trace(Object message);
	public abstract void trace(Object message, Throwable t);
	public abstract boolean isTraceEnabled();
	public abstract boolean isInfoEnabled();
	public abstract boolean isDebugEnabled();
	public abstract void info(Object message);
	public abstract void info(Object message, Throwable t);
	public abstract void fatal(Object message);
	public abstract void fatal(Object message, Throwable t);
	public abstract void error(Object message);
	public abstract void error(Object message, Throwable t);
	public abstract void debug(Object message);
	public abstract void debug(Object message, Throwable t);
}
