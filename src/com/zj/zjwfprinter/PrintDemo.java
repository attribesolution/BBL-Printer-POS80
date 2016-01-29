package com.zj.zjwfprinter;

import android.app.Activity;  
import android.os.Bundle;  
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.zj.wfsdk.*;

public class PrintDemo extends Activity {	
    Button btnConn = null;
	Button btnPrint = null;
	Button btn_test = null;
	Button btnClose = null;
	EditText edtContext = null;
	WifiCommunication wfComm = null;
	EditText txt_ip = null;
	int  connFlag = 0;
	@Override  
	public void onCreate(Bundle savedInstanceState){  
	    super.onCreate(savedInstanceState);  
	    setContentView(R.layout.main);
	    btnConn = (Button) this.findViewById(R.id.btn_conn); 
	    btnConn.setOnClickListener(new ClickEvent());
	    btnPrint = (Button) this.findViewById(R.id.btnSend);
	    btnPrint.setOnClickListener(new ClickEvent());
	    btn_test = (Button) this.findViewById(R.id.btn_test);
	    btn_test.setOnClickListener(new ClickEvent());
	    btnClose = (Button) this.findViewById(R.id.btnClose);
	    btnClose.setOnClickListener(new ClickEvent());
	    edtContext = (EditText) this.findViewById(R.id.txt_content);
	    txt_ip = (EditText)this.findViewById(R.id.txt_ip);
	    wfComm = new WifiCommunication(mHandler);

	    btnConn.setEnabled(true);
	    btnPrint.setEnabled(false);
	    btn_test.setEnabled(false);
	    btnClose.setEnabled(false);
	}   
	  
	@Override
	protected void onDestroy() {
		super.onDestroy();
		wfComm.close();
	}
	  
	class ClickEvent implements View.OnClickListener {
		public void onClick(View v) {
			if (v == btnConn) {	
				if( connFlag == 0 ){   //避免连续点击此按钮创建多个连接线程
					connFlag = 1;
				    Log.d("wifi调试","点击\"连接\"");
				    String strAddressIp = txt_ip.getText().toString();
				    wfComm.initSocket(strAddressIp,9100);
				}
			} else if (v == btnPrint) {
                String msg = edtContext.getText().toString();
                if( msg.length() > 0 ){
                	wfComm.sendMsg(msg,"gbk");
                    byte[] tail = new byte[3];
                    tail[0] = 0x0A;
                    tail[1] = 0x0D;
                    wfComm.sndByte(tail);
                }
			} else if (v == btnClose) {
				wfComm.close();
			} else if (v == btn_test) {
                String msg = "";
                String lang = getString(R.string.strLang);
				printImage();
				
            	byte[] cmd = new byte[3];
        	    cmd[0] = 0x1b;
        	    cmd[1] = 0x21;
            	if((lang.compareTo("en")) == 0){	
            		cmd[2] |= 0x10;
            		wfComm.sndByte(cmd);          //set double height and double width mode
            		wfComm.sendMsg("Congratulations! \n\n", "GBK");
            		cmd[2] &= 0xEF;        
            		wfComm.sndByte(cmd);          //cancel double height and double width mode
            		msg = "  You have sucessfully created communications between your device and our WIFI printer.\n\n"
                            +"  Our company is a high-tech enterprise which specializes" +
                            " in R&D,manufacturing,marketing of thermal printers and barcode scanners.\n\n";

            		wfComm.sendMsg(msg, "GBK");
            	}else if((lang.compareTo("ch")) == 0){
            		cmd[2] |= 0x10;
            		wfComm.sndByte(cmd);             //set double height and double width mode
            		wfComm.sendMsg("恭喜您! \n\n", "GBK");  //send data to the printer By gbk encoding
            		cmd[2] &= 0xEF;                 
            		wfComm.sndByte(cmd);            //cancel double height and double width mode
            		msg = "  您已经成功的连接上了我们的WIFI打印机！\n\n"
            		+ "  我们公司是一家专业从事研发，生产，销售商用票据打印机和条码扫描设备于一体的高科技企业.\n\n";
            		wfComm.sendMsg(msg, "GBK");
            	}
			}
		}
	}  
	
    private final  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case WifiCommunication.WFPRINTER_CONNECTED:
            	connFlag = 0;
            	Toast.makeText(getApplicationContext(), "Connect the WIFI-printer successful",
                        Toast.LENGTH_SHORT).show();
        	    btnPrint.setEnabled(true);
        	    btn_test.setEnabled(true);
        	    btnClose.setEnabled(true);
        	    btnConn.setEnabled(false);
            	break;
            case WifiCommunication.WFPRINTER_DISCONNECTED:
            	Toast.makeText(getApplicationContext(), "Disconnect the WIFI-printer successful",
                        Toast.LENGTH_SHORT).show();
    		    btnConn.setEnabled(true);
			    btnPrint.setEnabled(false);
			    btn_test.setEnabled(false);
			    btnClose.setEnabled(false);
            	break;
            case WifiCommunication.SEND_FAILED:
            	Toast.makeText(getApplicationContext(), "Send Data Failed,please reconnect",
                        Toast.LENGTH_SHORT).show();
            	break;
            case WifiCommunication.WFPRINTER_CONNECTEDERR:
            	connFlag = 0;
            	Toast.makeText(getApplicationContext(), "Connect the WIFI-printer error",
                        Toast.LENGTH_SHORT).show();
            	break;
            case WifiCommunication.CONNECTION_LOST:
            	connFlag = 0; 
            	Toast.makeText(getApplicationContext(), "Connection lost,please reconnect",
                        Toast.LENGTH_SHORT).show();
    		    btnConn.setEnabled(true);
			    btnPrint.setEnabled(false);
			    btn_test.setEnabled(false);
			    btnClose.setEnabled(false);
            	break;
            default:
                break;
            }
        }
    };
    
    //打印图形
	private void printImage() {
    	byte[] sendData = null;
    	PrintPic pg = new PrintPic();
    	pg.initCanvas(384);     
    	pg.initPaint();
    	pg.drawImage(0, 0, "/mnt/sdcard/icon.jpg");
    	sendData = pg.printDraw();
    	wfComm.sndByte(sendData);  //打印byte流数据
    }	
}
