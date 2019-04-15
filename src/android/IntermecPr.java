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

        int selection = 4;
        switch(selection) {
          case 1:
            Log.i(TAG,"PREPARING DPL Document");

            docDPL = new DocumentDPL();
            paramDPL = new ParametersDPL();

            docDPL.setEnableAdvanceFormatAttribute(true);
            docDPL.writeTextInternalBitmapped("Hello World", 0, 100, 5, paramDPL);

            paramDPL.setIsUnicode(false);
            paramDPL.setWideBarWidth(4);
            paramDPL.setNarrowBarWidth(4);
            paramDPL.setSymbolHeight(0);
            //AutoFormatting
            docDPL.writeBarCodeQRCode("This is the data portion", true, 0, "", "", "", "", 920, 0, paramDPL);
            docDPL.writeTextInternalBitmapped("QR Barcode w/ Auto Formatting", 1, 1030, 0);

            printData = docDPL.getDocumentData();

            break;
          case 2:

            docExPCL_LP = new DocumentExPCL_LP(3);
            paramExPCL_LP = new ParametersExPCL_LP();

            docExPCL_LP.writeText("Barcode Sample");

            docExPCL_LP.writeBarCode(BarcodeExPCL_LP.Code39, "DMITRIY", true, (byte) 100);
            docExPCL_LP.writeBarCode(BarcodeExPCL_LP.Code39, "DMITRIY", false, (byte) 50);

            docExPCL_LP.writeText("QR Sample");

            paramExPCL_LP.setFontIndex(10);
            docExPCL_LP.writeBarCodeQRCode("www.datamax-oneil.com", true, 2, (byte) 'L', 3, paramExPCL_LP);
            printData = docExPCL_LP.getDocumentData();

            break;
          case 3:

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
            docLP.writeText("  ");
            docLP.writeText("  ");
            printData = docLP.getDocumentData();

            break;
          case 4:
            Log.i(TAG,"EZ MODE");
            docEZ = new DocumentEZ("MF204");
            paramEZ = new ParametersEZ();

            /*paramEZ.setHorizontalMultiplier(2);
            paramEZ.setVerticalMultiplier(10);

            docEZ.writeText("Code 39 Barcodes", 2530, 1);
            docEZ.writeBarCode("BC39N", "0123456789", 2560, 1, paramEZ);
            docEZ.writeBarCode("BC39W", "0123456789", 2660, 1, paramEZ);*/

            paramEZ.setHorizontalMultiplier(4);
            paramEZ.setVerticalMultiplier(1);

            //QR
            docEZ.writeText("QR Barcode Manual Formating", 3650, 1);
            docEZ.writeBarCodeQRCode("N0123456789,B0004(&#),QR//BARCODE", 2, 9, 1, 3680, 1, paramEZ);

            docEZ.writeText("QR Barcode Auto Formatting 1", 3950, 1);
            docEZ.writeBarCodeQRCode("0123456789012345678901234567890123456789", 2, 9, 0, 3980, 1, paramEZ);

            paramEZ.setHorizontalMultiplier(8);
            docEZ.writeText("QR Barcode Auto Formatting 2", 4250, 1);
            docEZ.writeBarCodeQRCode("0123456789ABCDE", 2, 9, 0, 4280, 1, paramEZ);

            printData = docEZ.getDocumentData();

            break;
          case 5:

            Log.i(TAG,"DPL MODE");

            docDPL = new DocumentDPL();
            paramDPL = new ParametersDPL();

            //QRCODE
            paramDPL.setIsUnicode(false);
            paramDPL.setWideBarWidth(4);
            paramDPL.setNarrowBarWidth(4);
            paramDPL.setSymbolHeight(0);
            //AutoFormatting
            docDPL.writeBarCodeQRCode("This is the data portion", true, 0, "", "", "", "", 920, 0, paramDPL);
            docDPL.writeTextInternalBitmapped("QR Barcode w/ Auto Formatting", 1, 1030, 0);

            //Manual Formatting
            docDPL.writeBarCodeQRCode("1234This is the data portion", false, 2, "H", "4", "M", "A", 1070, 0, paramDPL);
            docDPL.writeTextInternalBitmapped("QR Barcode w/ Manual formatting", 1, 1200, 0);

            printData = docDPL.getDocumentData();

            break;
          default:
            Log.i(TAG,"Default");
        }

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
                  Thread.sleep(100);
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
