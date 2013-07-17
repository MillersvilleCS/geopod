package geopod.utils.debug;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvocationUtility
{
	private InvocationUtility ()
	{
		// Static class
	}

	/**
	 * Invoke a method on an invoking object with the specified arguments. I.e.,
	 * ((type)invokingObject).methodNameToInvoke (arguments);
	 * 
	 * @param type
	 *            allows method calls based on a supertype of invokingObject
	 * @param invokingObject
	 *            may be null if calling a static method
	 * @param methodNameToInvoke
	 * @param args
	 * @return the result from invoking the method or null if unsuccessful
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public static Object invokePrivateMethod (Class<?> type, Object invokingObject, String methodNameToInvoke,
			Object... args)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		Method[] methods = type.getDeclaredMethods ();
		for (Method method : methods)
		{
			String methodName = method.getName ();
			if (methodName.equals (methodNameToInvoke))
			{
				Object result = method.invoke (invokingObject, args);
				return (result);
			}
		}
		throw new NoSuchMethodException ("Method " + methodNameToInvoke + " not found.");
	}

	/**
	 * Invoke a method on an invoking object with the specified arguments. I.e.,
	 * invokingObject.methodNameToInvoke (arguments);
	 * 
	 * @param invokingObject
	 *            may be null if calling a static method
	 * @param methodNameToInvoke
	 * @param args
	 * @return the result from invoking the method or null if unsuccessful
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public static Object invokePrivateMethod (Object invokingObject, String methodNameToInvoke, Object... args)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		Class<?> type = invokingObject.getClass ();
		Object result = invokePrivateMethod (type, invokingObject, methodNameToInvoke, args);
		return (result);
	}
}
