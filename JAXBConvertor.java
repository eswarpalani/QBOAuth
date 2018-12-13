package com.opentap.converter;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import com.opentap.tk.Log;

public class JAXBConvertor {

	private static HashMap<Class<?>, Marshaller>    marshallerMap = null; 
	private static HashMap<Class<?>, Unmarshaller>  unmarshallerMap = null; 

	private static final Object MARSHAL_LOCK = new Object() {};
	private static final Object UNMARSHAL_LOCK = new Object() {};	
	
	/**
	 * Jaxb XML to object
	 * @param decryptedData
	 * @param classType
	 * @return
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <E> E jaxbXMLToObject(String decryptedData, Class<E> classType) throws Exception {

		E object = null;

		try {
			Unmarshaller unmarshaller  = getUnmarshaller( classType );

			if ( unmarshaller == null )
				return object;
			
			JAXBElement root = unmarshaller.unmarshal(new StreamSource(new StringReader(decryptedData)), classType);
			object = (E) root.getValue();

		} catch( JAXBException ex ) {
			Log.error(JAXBConvertor.class,"Jaxb XML to object exception"+ex.getMessage());
			throw new Exception( ex );
			
		}
		return object;
	}

	/**
	 * Jaxb object to XML
	 * @param object
	 * @param classType
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E> String jaxbObjectToXML(Object object, Class<E> classType) throws Exception {

		//Variable for converting output Stream to String
		StringWriter stringWriter = new StringWriter();
		try {

			JAXBElement jaxbElement = new JAXBElement(new QName(classType.getSimpleName()), classType, object);
			Marshaller  marshaller  = getMarshaller( classType );
			
			if ( marshaller == null )
				return null;

			//for pretty-print XML in JAXB
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

			marshaller.marshal(jaxbElement, stringWriter);

		} catch ( JAXBException ex ) {
			Log.error(JAXBConvertor.class,"Jaxb object to XML exception"+ex.getMessage());
			throw new Exception(ex);
			
		}
		return stringWriter.toString();
	}
	
	private static Marshaller getMarshaller( Class<?> classType ) {
		Marshaller  marshall = null;
		
		if ( marshallerMap == null ) {
			synchronized( MARSHAL_LOCK ) {
				if ( marshallerMap == null ) {
					marshallerMap = new HashMap<Class<?>, Marshaller>();
				}
			}
		}
		
		marshall = marshallerMap.get( classType ); 
		
		if ( marshall != null )
			return marshall;
		
		synchronized( MARSHAL_LOCK ) {
			try {
				JAXBContext context   = JAXBContext.newInstance(classType);
				marshall = context.createMarshaller();

				marshallerMap.put( classType, marshall );
			} catch ( JAXBException ex ) {
				Log.error(JAXBConvertor.class,"Jaxb object to XML Marshalling exception" + ex.getMessage());
			}
		}
		
		return marshall;
	}
	
	private static  Unmarshaller getUnmarshaller( Class<?> classType ) {
		Unmarshaller  marshall = null;
		
		if ( unmarshallerMap == null ) {
			synchronized( UNMARSHAL_LOCK ) {
				if ( unmarshallerMap == null ) {
					unmarshallerMap = new HashMap<Class<?>, Unmarshaller>();
				}
			}
		}
		
		marshall = unmarshallerMap.get( classType ); 
		
		if ( marshall != null )
			return marshall;
		
		synchronized( UNMARSHAL_LOCK ) {
			try {
				JAXBContext context   = JAXBContext.newInstance(classType);
				marshall = context.createUnmarshaller();

				unmarshallerMap.put( classType, marshall );
			} catch ( JAXBException ex ) {
				Log.error(JAXBConvertor.class,"XML to JAXB Object Umarshalling exception" + ex.getMessage());
			}
		}
		
		return marshall;
	}
	
}