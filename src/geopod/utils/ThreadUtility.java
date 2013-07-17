package geopod.utils;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A utility class to handle running code on separate threads.
 * 
 * @author Geopod Team
 * 
 */
public class ThreadUtility
{
	private static ExecutorService ms_fixedExecutor;
	private static ScheduledExecutorService ms_scheduledExecutor;
	private static final int FIXED_THREAD_COUNT;
	private static final int SCHEDULED_THREAD_COUNT;

	static
	{
		FIXED_THREAD_COUNT = 5;
		SCHEDULED_THREAD_COUNT = 1;

		ms_fixedExecutor = null;
		ms_scheduledExecutor = null;
	}

	private ThreadUtility ()
	{
		// Static class, no constructor.
	}

	/**
	 * Startup the thread {@link ExecutorService executors} if they haven't
	 * already been started.
	 */
	public static void startupExecutors ()
	{
		if (ms_fixedExecutor == null)
		{
			ms_fixedExecutor = Executors.newFixedThreadPool (FIXED_THREAD_COUNT);
		}
		if (ms_scheduledExecutor == null)
		{
			ms_scheduledExecutor = Executors.newScheduledThreadPool (SCHEDULED_THREAD_COUNT);
		}
	}

	/**
	 * Spawn a task to run in a thread. Uses a {@link ExecutorService} to manage
	 * threading.
	 * 
	 * @param task
	 */
	public static void execute (Runnable task)
	{
		ms_fixedExecutor.execute (task);
	}

	/**
	 * Shutdown all thread {@link ExecutorService executors}.
	 */
	public static void shutdownExecutors ()
	{
		ms_fixedExecutor.shutdown ();
		ms_fixedExecutor = null;
		ms_scheduledExecutor.shutdown ();
		ms_scheduledExecutor = null;
	}

	/**
	 * Creates and executes a one-shot action that becomes enabled after the
	 * given delay.
	 * 
	 * @param task
	 *            - the task to execute
	 * @param delay
	 *            - the time from now to delay execution
	 * @param unit
	 *            - the time unit of the delay parameter
	 * @throws RejectedExecutionException
	 *             if the task cannot be scheduled for execution
	 * @throws NullPointerException
	 *             if command is null
	 */
	public static void execute (Runnable task, long delay, TimeUnit unit)
	{
		ms_scheduledExecutor.schedule (task, delay, unit);
	}

	/**
	 * Submits a Runnable task for execution and returns a Future representing
	 * that task. The Future's <tt>get</tt> method will return <tt>null</tt>
	 * upon <em>successful</em> completion.
	 * 
	 * @param task
	 *            the task to submit
	 * @return a Future representing pending completion of the task
	 * @throws RejectedExecutionException
	 *             if the task cannot be scheduled for execution
	 * @throws NullPointerException
	 *             if the task is null
	 */
	public static Future<?> submit (Runnable task)
	{
		Future<?> future = ms_fixedExecutor.submit (task);

		return (future);
	}

	/**
	 * Print diagnostic info about the current thread.
	 */
	public static void printThreadInfo ()
	{
		Thread thread = Thread.currentThread ();
		System.out.printf ("  Thread id: %d, name: %s, state: %s, daemon: %s, EDT: %s\n", thread.getId (),
				thread.getName (), thread.getState (), thread.isDaemon (), EventQueue.isDispatchThread ());
		ThreadGroup group = thread.getThreadGroup ();
		System.out.printf ("    priority: %d, group: %s, group count: %d\n", thread.getPriority (), group.getName (),
				group.activeCount ());
		StackTraceElement[] backtrace = thread.getStackTrace ();
		if (backtrace.length > 2)
		{
			System.out.printf ("    trace[2]: %s\n", backtrace[2]);
		}
	}

	/**
	 * Print a stack trace of the current thread.
	 */
	public static void printFullStackTrace ()
	{
		Thread thread = Thread.currentThread ();
		System.out.printf ("  Thread id: %d, name: %s, state: %s, daemon: %s, EDT: %s\n", thread.getId (),
				thread.getName (), thread.getState (), thread.isDaemon (), EventQueue.isDispatchThread ());
		ThreadGroup group = thread.getThreadGroup ();
		System.out.printf ("    priority: %d, group: %s, group count: %d\n", thread.getPriority (), group.getName (),
				group.activeCount ());
		StackTraceElement[] backtrace = thread.getStackTrace ();

		for (StackTraceElement e : backtrace)
		{
			System.out.printf ("    Stack Trace: %s\n", e);
		}
	}

	/**
	 * Print diagnostic info about the current thread along with a header
	 * message.
	 * 
	 * @param message
	 *            - a header message.
	 */
	public static void printThreadInfo (String message)
	{
		System.out.printf ("Note: %s\n", message);
		printThreadInfo ();
	}

	/**
	 * Causes <code>runnable</code> to have its <code>run</code> method called
	 * in the dispatch thread of {@link Toolkit#getSystemEventQueue the system
	 * EventQueue} if this method is not being called from it. This will happen
	 * after all pending events are processed.
	 * 
	 * @param runnable
	 *            the <code>Runnable</code> whose <code>run</code> method should
	 *            be executed synchronously on the <code>EventQueue</code>
	 */
	public static void invokeOnEdt (Runnable runnable)
	{
		boolean isEdt = EventQueue.isDispatchThread ();
		if (!isEdt)
		{
			EventQueue.invokeLater (runnable);
		}
		else
		{
			runnable.run ();
		}
	}

	/**
	 * Causes <code>runnable</code> to have its <code>run</code> method called
	 * in the dispatch thread of {@link Toolkit#getSystemEventQueue the system
	 * EventQueue} if this method is not being called from it. This will happen
	 * after all pending events are processed. The call blocks until this has
	 * happened. This method will throw an Error if called from the event
	 * dispatcher thread.
	 * 
	 * @param runnable
	 *            the <code>Runnable</code> whose <code>run</code> method should
	 *            be executed synchronously on the <code>EventQueue</code>
	 */
	public static void invokeOnEdtAndWait (Runnable runnable)
	{
		boolean isEdt = EventQueue.isDispatchThread ();
		if (!isEdt)
		{
			try
			{
				EventQueue.invokeAndWait (runnable);
			}
			catch (InterruptedException e)
			{
				System.err.println ("EDT task interrupted");
				e.printStackTrace ();
			}
			catch (InvocationTargetException e)
			{
				System.err.println ("EDT task invocation exception");
				e.printStackTrace ();
			}
		}
		else
		{
			runnable.run ();
		}
	}
}
