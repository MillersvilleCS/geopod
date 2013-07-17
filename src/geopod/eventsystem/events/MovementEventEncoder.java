package geopod.eventsystem.events;

import javax.vecmath.Quat4d;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MovementEventEncoder
		implements Converter
{

	@Override
	public boolean canConvert (@SuppressWarnings("rawtypes") Class testClass)
	{
		return testClass.equals (MovementEvent.class);
	}

	@Override
	public void marshal (Object value, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		MovementEvent event = (MovementEvent) value;
		StringBuilder s = new StringBuilder ();
		s.append (event.getTime ());
		s.append (',');
		s.append (event.getLatitude ());
		s.append (',');
		s.append (event.getLongitude ());
		s.append (',');
		s.append (event.getAltitude ());
		s.append (',');
		s.append (event.getRotation ().x);
		s.append (',');
		s.append (event.getRotation ().y);
		s.append (',');
		s.append (event.getRotation ().z);
		s.append (',');
		s.append (event.getRotation ().w);
		writer.setValue (s.toString ());
	}

	@Override
	public Object unmarshal (HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		String rawString = reader.getValue ();
		String[] valueArray = rawString.split (",");
		long time = Long.parseLong (valueArray[0]);
		double lat = Double.parseDouble (valueArray[1]);
		double lon = Double.parseDouble (valueArray[2]);
		double alt = Double.parseDouble (valueArray[3]);
		double x = Double.parseDouble (valueArray[4]);
		double y = Double.parseDouble (valueArray[5]);
		double z = Double.parseDouble (valueArray[6]);
		double w = Double.parseDouble (valueArray[7]);

		Quat4d quat = new Quat4d (x, y, z, w);

		return new MovementEvent (time, lat, lon, alt, quat);
	}

}
