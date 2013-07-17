package geopod.utils.geocoding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import visad.georef.LatLonPoint;
import visad.georef.LatLonTuple;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Provides support for forward and reverse geocoding.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Geocoding">Geocoding</a>
 * @author Geopod Team
 * 
 */
public class GeocoderUtility
{
	private static final String GOOGLE_SERVICE;

	private static JsonParser m_jsonParser;

	static
	{
		GOOGLE_SERVICE = "http://maps.google.com/maps/api/geocode/";
		m_jsonParser = new JsonParser ();
	}

	private GeocoderUtility ()
	{
		// Static class, no constructor.
	}

	/**
	 * Inverse geocode a latitude/longitude to an address.
	 * 
	 * @param latitude
	 * @param longitude
	 * @return - the address of this location.
	 */
	public static String geocode (double latitude, double longitude)
	{
		String service = buildLatLonQuery ("json", latitude, longitude);
		String address = null;
		try
		{
			JsonArray results = performQuery (service);
			if (results.size () > 0)
			{
				JsonObject firstResult = (JsonObject) results.get (0);
				JsonArray addressComponents = firstResult.getAsJsonArray ("address_components");
				String state = "";
				String city = "";
				String country = "";
				for (Iterator<JsonElement> componentIter = addressComponents.iterator (); componentIter.hasNext ();)
				{
					JsonObject component = (JsonObject) componentIter.next ();
					JsonArray types = component.getAsJsonArray ("types");
					String type = "";
					if (types.size () > 0)
					{
						type = types.get (0).getAsString ();
						if (type.equals ("administrative_area_level_3"))
						{
							city = component.get ("long_name").getAsString ();
						}
						else if (type.equals ("country"))
						{
							country = component.get ("long_name").getAsString ();
						}
						else if (type.equals ("administrative_area_level_1"))
						{
							state = component.get ("long_name").getAsString ();
						}
					}
				}
				address = formatAddress (city, state, country);
			}

		}
		catch (IOException e)
		{
			address = "";
		}

		return (address);
	}

	private static String buildLatLonQuery (String format, double latitude, double longitude)
	{
		StringBuilder service = new StringBuilder (GOOGLE_SERVICE);
		service.append (format);
		service.append ("?latlng=");
		service.append (latitude);
		service.append (",");
		service.append (longitude);
		service.append ("&sensor=false");

		return (service.toString ());
	}

	private static String formatAddress (String city, String state, String country)
	{
		StringBuilder address = new StringBuilder ();
		address.append (!city.isEmpty () ? city : "?");
		address.append (", ");
		address.append (!state.isEmpty () ? state : "?");
		address.append (", ");
		address.append (!country.isEmpty () ? country : "?");

		return (address.toString ());
	}

	/**
	 * Geocode an address to a latitude/longitude pair.
	 * 
	 * @param address
	 * @return latitude/longitude pair or null if not found
	 */
	public static LatLonPoint geocode (String address)
	{
		address = address.replaceAll ("\\s", "%20");
		String service = buildAddressQuery ("json", address);
		LatLonPoint latLon = null;
		try
		{
			JsonArray results = performQuery (service);
			if (results.size () > 0)
			{
				JsonObject result = (JsonObject) results.get (0);
				JsonObject geometry = result.getAsJsonObject ("geometry");
				JsonObject location = geometry.getAsJsonObject ("location");
				double lat = location.get ("lat").getAsDouble ();
				double lon = location.get ("lng").getAsDouble ();
				latLon = new LatLonTuple (lat, lon);
			}
		}
		catch (Exception e)
		{
			// Catch all exceptions.
			latLon = null;
		}

		return (latLon);
	}

	private static String buildAddressQuery (String format, String address)
	{
		StringBuilder service = new StringBuilder (GOOGLE_SERVICE);
		service.append (format);
		service.append ("?address=");
		service.append (address);
		service.append ("&sensor=false");

		return (service.toString ());
	}

	private static JsonArray performQuery (String service)
			throws MalformedURLException, IOException
	{
		URL url = new URL (service);
		InputStreamReader inStream = new InputStreamReader (url.openStream ());
		BufferedReader reader = new BufferedReader (inStream);
		JsonObject response = (JsonObject) m_jsonParser.parse (reader);
		JsonArray results = response.getAsJsonArray ("results");

		return (results);
	}

}
