package edu.wpi.always.cm.perceptors.sensor.pir;

import java.util.*;
import com.sun.jna.*;

public interface USBm2Library extends Library {

   public boolean findDevice ();

   public boolean initPorts ();

   public byte read ();

   static class USBmTypeMapper extends DefaultTypeMapper {

      {
         addToNativeConverter(CStringBuffer.class, new ToNativeConverter() {

            @Override
            public Class<CStringBuffer> nativeType () {
               return CStringBuffer.class;
            }

            @Override
            public Object toNative (Object stringBuffer, ToNativeContext context) {
               if ( stringBuffer instanceof CStringBuffer ) {
                  ((CStringBuffer) stringBuffer).clear();
                  return ((CStringBuffer) stringBuffer).getNativeBuffer();
               }
               return null;
            }
         });
         addToNativeConverter(SingleByteBuffer.class, new ToNativeConverter() {

            @Override
            public Class<SingleByteBuffer> nativeType () {
               return SingleByteBuffer.class;
            }

            @Override
            public Object toNative (Object byteBuffer, ToNativeContext context) {
               if ( byteBuffer instanceof SingleByteBuffer ) {
                  return ((SingleByteBuffer) byteBuffer).getNativeBuffer();
               }
               return null;
            }
         });
      }
   }

   @SuppressWarnings("serial")
   static Map<String, Object> OPTIONS = new HashMap<String, Object>() {

      {
         put(Library.OPTION_TYPE_MAPPER, new USBmTypeMapper());
      }
   };
   
   static final USBm2Library INSTANCE = (USBm2Library) Native.loadLibrary("USBm2-64", USBm2Library.class, OPTIONS);

}
