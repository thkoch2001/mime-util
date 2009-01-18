package eu.medsea.util.log;


public class Log4jLoggerImpl
extends Logger
{
	private org.apache.log4j.Logger log4jLogger;

	public Log4jLoggerImpl(String name) {
		log4jLogger = org.apache.log4j.Logger.getLogger(name);
	}

	public void debug(Object message, Throwable t) {
		log4jLogger.debug(message, t);
	}
	public void debug(Object message) {
		log4jLogger.debug(message);
	}
	public void error(Object message, Throwable t) {
		log4jLogger.error(message, t);
	}
	public void error(Object message) {
		log4jLogger.error(message);
	}
	public void fatal(Object message, Throwable t) {
		log4jLogger.fatal(message, t);
	}
	public void fatal(Object message) {
		log4jLogger.fatal(message);
	}
	public void info(Object message, Throwable t) {
		log4jLogger.info(message, t);
	}
	public void info(Object message) {
		log4jLogger.info(message);
	}
	public boolean isDebugEnabled() {
		return log4jLogger.isDebugEnabled();
	}
	public boolean isInfoEnabled() {
		return log4jLogger.isInfoEnabled();
	}
	public boolean isTraceEnabled() {
		return log4jLogger.isTraceEnabled();
	}
	public void trace(Object message, Throwable t) {
		log4jLogger.trace(message, t);
	}
	public void trace(Object message) {
		log4jLogger.trace(message);
	}
	public void warn(Object message, Throwable t) {
		log4jLogger.warn(message, t);
	}
	public void warn(Object message) {
		log4jLogger.warn(message);
	}
}
