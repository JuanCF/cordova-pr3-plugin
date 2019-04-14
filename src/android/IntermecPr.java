package com.honeywell.intermec;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import honeywell.connection.ConnectionBase;
import honeywell.connection.Connection_Bluetooth;
import honeywell.connection.Connection_TCP;
import honeywell.printer.DocumentDPL;
import honeywell.printer.DocumentDPL.*;
import honeywell.printer.DocumentEZ;
import honeywell.printer.DocumentLP;
import honeywell.printer.DocumentExPCL_LP;
import honeywell.printer.DocumentExPCL_PP;
import honeywell.printer.DocumentExPCL_PP.*;
import honeywell.printer.ParametersDPL;
import honeywell.printer.ParametersDPL.*;
import honeywell.printer.ParametersEZ;
import honeywell.printer.ParametersExPCL_LP;
import honeywell.printer.ParametersExPCL_LP.*;
import honeywell.printer.ParametersExPCL_PP;
import honeywell.printer.ParametersExPCL_PP.*;
import honeywell.printer.UPSMessage;
import honeywell.printer.configuration.dpl.*;
import honeywell.printer.configuration.ez.*;
import honeywell.printer.configuration.expcl.*;


/**
 * This class echoes a string called from JavaScript.
 */
public class IntermecPr extends CordovaPlugin {

    private static final String TAG = "IntermecPR3";
    byte[] printData = {0};
    ConnectionBase conn = null;
    private String m_printerMAC = "00:07:80:AB:B1:C7";

    //Document and Parameter Objects
    private DocumentEZ docEZ;
    private DocumentLP docLP;
    private DocumentDPL docDPL;
    private DocumentExPCL_LP docExPCL_LP;
    private DocumentExPCL_PP docExPCL_PP;

    private ParametersEZ paramEZ;
    private ParametersDPL paramDPL;
    private ParametersExPCL_LP paramExPCL_LP;
    private ParametersExPCL_PP paramExPCL_PP;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            this.createDocumentToPrint();
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
  
    private void createDocumentToPrint(){
      try{

        docLP = new DocumentLP("!");

        docLP.writeText("                   For Delivery");
        docLP.writeText(" ");
        docLP.writeText("Customer Code: 00146");
        docLP.writeText("Address: Manila");
        docLP.writeText("Tin No.: 27987641");
        docLP.writeText("Area Code: PN1-0004");
        docLP.writeText("Business Style: SUPERMARKET A");
        docLP.writeText(" ");
        docLP.writeText("PRODUCT CODE   PRODUCT DESCRIPTION          QTY.  Delivr.");
        docLP.writeText("------------   --------------------------   ----  -------");
        docLP.writeText("    111        Wht Bread Classic 400g        51     51   ");
        docLP.writeText("    112        Clsc Wht Bread 600g           77     77   ");
        docLP.writeText("    113        Wht Bread Clsc 600g           153    25   ");
        docLP.writeText("    121        H Fiber Wheat Bread 600g      144    77   ");
        docLP.writeText("    122        H Fiber Wheat Bread 400g      112    36   ");
        docLP.writeText("    123        H Calcium Loaf 400g           81     44   ");
        docLP.writeText("    211        California Raisin Loaf        107    44   ");
        docLP.writeText("    212        Chocolate Chip Loaf           159    102  ");
        docLP.writeText("    213        Dbl Delights(Ube & Chse)      99     80   ");
        docLP.writeText("    214        Dbl Delights(Choco & Mocha)   167    130  ");
        docLP.writeText("    215        Mini Wonder Ube Cheese        171    79   ");
        docLP.writeText("    216        Mini Wonder Ube Mocha         179    100  ");
        docLP.writeText("  ");
        docLP.writeText("  ");
        printData = docLP.getDocumentData();

        this.runPrintingTask();

      }catch(Exception e) {
          Log.e(TAG,e.toString());
      }
    }

    private void runPrintingTask(){
      cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            try{
              conn = Connection_Bluetooth.createClient(m_printerMAC, false);

              if(!conn.getIsOpen()) { conn.open(); }

              int bytesWritten = 0;
              int bytesToWrite = 1024;
              int totalBytes = printData.length;
              int remainingBytes = totalBytes;
              while (bytesWritten < totalBytes)
              {
                  if (remainingBytes < bytesToWrite)
                      bytesToWrite = remainingBytes;

                  //Send data, 1024 bytes at a time until all data sent
                  conn.write(printData, bytesWritten, bytesToWrite);
                  bytesWritten += bytesToWrite;
                  remainingBytes = remainingBytes - bytesToWrite;
                 //Thread.sleep(100);
              }

              //signals to close connection
              conn.close();

            }catch(Exception e) {
              Log.e(TAG,e.toString());
            }
          }
      });
    }
}
