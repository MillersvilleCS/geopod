package geopod.mission;

import geopod.utils.debug.Debug;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MissionEncrypter
{
	private static MissionEncrypter ENCRYPTER_INSTANCE;
	private static final String MISSION_ENCODING_KEY;
	private static final String ENCODING_ALGORITHM;
	private static final String CIPHER_TRANSFORMATION_SPEC;
	private static final String UTF8_CHARSET_SPEC;
	static
	{
		ENCRYPTER_INSTANCE = null;
		MISSION_ENCODING_KEY = "dOp!oEg*";
		ENCODING_ALGORITHM = "DES";
		CIPHER_TRANSFORMATION_SPEC = "DES/ECB/PKCS5Padding";
		UTF8_CHARSET_SPEC = "UTF8";
	}

	private Cipher m_encodingCipher;
	private Cipher m_decodingCipher;

	private MissionEncrypter ()
	{
		try
		{
			byte[] keyBytes = MISSION_ENCODING_KEY.getBytes ();
			SecretKey key = new SecretKeySpec (keyBytes, ENCODING_ALGORITHM);

			m_encodingCipher = Cipher.getInstance (CIPHER_TRANSFORMATION_SPEC);
			m_encodingCipher.init (Cipher.ENCRYPT_MODE, key);

			m_decodingCipher = Cipher.getInstance (CIPHER_TRANSFORMATION_SPEC);
			m_decodingCipher.init (Cipher.DECRYPT_MODE, key);
		}
		catch (Exception e)
		{
			if (Debug.isDebuggingOn ())
				e.printStackTrace ();
		}
	}

	public String encrypt (String plainText)
	{
		String encodedTextBase64 = null;
		try
		{
			byte[] utf8 = plainText.getBytes (UTF8_CHARSET_SPEC);
			byte[] encoded = m_encodingCipher.doFinal (utf8);
			encodedTextBase64 = new sun.misc.BASE64Encoder ().encode (encoded);
		}
		catch (Exception e)
		{
			if (Debug.isDebuggingOn ())
				e.printStackTrace ();
		}
		return (encodedTextBase64);
	}

	public String decrypt (String encodedText)
	{
		String plainText = null;
		try
		{
			byte[] decoded = new sun.misc.BASE64Decoder ().decodeBuffer (encodedText);
			byte[] utf8 = m_decodingCipher.doFinal (decoded);
			plainText = new String (utf8, UTF8_CHARSET_SPEC);
		}
		catch (Exception e)
		{
			if (Debug.isDebuggingOn ())
				e.printStackTrace ();
		}
		return (plainText);
	}

	public static MissionEncrypter getEncrypter ()
	{
		if (ENCRYPTER_INSTANCE == null)
		{
			ENCRYPTER_INSTANCE = new MissionEncrypter ();
		}
		return (ENCRYPTER_INSTANCE);
	}

}
