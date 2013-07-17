package geopod.mission;

import geopod.utils.debug.Debug;

/**
 * Class for things that could potentially have encountered problems during
 * construction and not be initialized correctly, and need to let other classes
 * be aware of this fact.
 * 
 * @author geopod
 * 
 */
public class Failable
{
	protected boolean m_initializationFailed = false;

	/**
	 * Whether or not this {@link Failable} was successfully initialized.
	 * 
	 * @return true if there was an initialization problem, false if it was
	 *         initialized successfully.
	 */
	public boolean initializationFailed ()
	{
		return (m_initializationFailed);
	}

	/**
	 * Whether or not this {@link Failable} was successfully initialized.
	 * 
	 * @return true if it was initialized successfully, false if there was an
	 *         initialization problem
	 */
	public boolean initializationSuccessful ()
	{
		return (!m_initializationFailed);
	}

	/**
	 * Update whether or not this {@link Failable} has failed based on whether
	 * or not something else has failed. If <tt>otherFailure</tt> is true, then
	 * this {@link Failable} will fail as well.
	 * 
	 * @param otherFailure
	 */
	protected void checkForFailure (boolean otherFailure)
	{
		m_initializationFailed = (otherFailure) ? true : m_initializationFailed;
	}

	/**
	 * Update whether or not this {@link Failable} has failed based on whether
	 * or not <tt>otherFailable</tt> has failed. If <tt>otherFailable</tt> has
	 * failed, then this {@link Failable} will fail as well.
	 */
	protected void checkForFailure (Failable otherFailable)
	{
		boolean otherFailed = otherFailable.initializationFailed ();
		checkForFailure (otherFailed);
	}

	/**
	 * Record that a failure has occurred.
	 */
	protected void recordInitializationFailure ()
	{
		recordInitializationFailure ("");
	}

	/**
	 * Record that a failure has occurred and print the associated error
	 * message.
	 */
	protected void recordInitializationFailure (String failureMessage)
	{
		Debug.println (failureMessage);
		m_initializationFailed = true;
	}
}
