package geopod.utils.debug;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A class for timing code segments. The implementation uses System.nanoTime.
 * 
 * @author Geopod Team
 * 
 */
public class Stopwatch
{
	private static final NumberFormat ms_formatter;
	static
	{
		ms_formatter = new DecimalFormat ("#0.00");
	}

	private long m_startTime;
	private long m_stopTime;

	/**
	 * Create a stopwatch.
	 */
	public Stopwatch ()
	{
		reset ();
	}

	/**
	 * Reset the stopwatch time to zero.
	 */
	public void reset ()
	{
		m_startTime = m_stopTime = 0;
	}

	/**
	 * Start the clock.
	 */
	public void start ()
	{
		m_startTime = System.nanoTime ();
	}

	/**
	 * Stop the clock.
	 */
	public void stop ()
	{
		m_stopTime = System.nanoTime ();
	}

	/**
	 * @return the elapsed time in milliseconds.
	 */
	public double getElapsedTimeInMs ()
	{
		double delta = m_stopTime - m_startTime;
		double elapsed = delta * 1e-6;

		return (elapsed);
	}
	
	public double getCurrentElapsedTimeInMs ()
	{
		double delta = System.nanoTime () - m_startTime;
		double elapsed = delta * 1e-6;
		
		return (elapsed);
	}

	/**
	 * Outputs the elapsed time in milliseconds.
	 */
	public void printElapsedTimeInMs ()
	{
		double elapsedTime = this.getElapsedTimeInMs ();
		System.out.printf ("Elapsed time = %s ms.\n", ms_formatter.format (elapsedTime));
	}

	/**
	 * @return elapsed time in microseconds
	 */
	public double getElapsedTimeInUs ()
	{
		double delta = m_stopTime - m_startTime;
		double elapsed = delta * 1e-3;

		return (elapsed);
	}

	/**
	 * Outputs the elapsed time in microseconds.
	 */
	public void printElapsedTimeInUs ()
	{
		double elapsedTime = this.getElapsedTimeInUs ();
		System.out.printf ("Elapsed time = %s us.\n", ms_formatter.format (elapsedTime));
	}
}
