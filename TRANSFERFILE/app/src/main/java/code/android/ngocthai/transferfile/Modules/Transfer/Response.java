package code.android.ngocthai.transferfile.Modules.Transfer;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import code.android.ngocthai.transferfile.Common.Support.MySocket;
import code.android.ngocthai.transferfile.Common.Utils.ValuesConst;

/**
 * Created by Thaihn on 26/09/2016.
 */
public class Response {



    /**
     * Client connect to server
     */
    public static class ClientResponse extends AsyncTask<Void, Void, Void> {

        private String ip_address_server;
        private String response;
        private String msg_to_server = ValuesConst.pass_transfer + "," + response;
        private int port;

        /**
         * Constructor default to add values
         *
         * @param ip_server ip of server connect to
         * @param port      port for connect
         * @param response  response to client send file
         */
        public ClientResponse(String ip_server, String response, int port) {
            this.ip_address_server = ip_server;
            this.port = port;
            this.response = response;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Socket socket = null;
            DataOutputStream dataOutputStream = null;

            try {
                socket = new Socket(ip_address_server, port);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                if (!msg_to_server.isEmpty()) {
                    //---write message to server---
                    dataOutputStream.writeUTF(msg_to_server);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //---close connect---
                MySocket.closeSocket(socket);
                MySocket.closeDataOutput(dataOutputStream);
            }
            return null;
        }
    }

    /**
     * Class connect server with client using thread and always listen from client.
     */
    public static class ServerResponse extends Thread {

        private ServerSocket serverSocket;
        private Activity activity;
        private int port;

        /**
         * Constructor default to add values
         *
         * @param serverSocket
         * @param activity
         */
        public ServerResponse(ServerSocket serverSocket, Activity activity, int port) {
            this.serverSocket = serverSocket;
            this.activity = activity;
            this.port = port;
        }

        @Override
        public void run() {
            //---create socket and data input, output to send and receive data from client---
            Socket socket = null;
            DataInputStream dataInputStream = null;

            try {
                //---create server socket to listen from server---
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(port));

                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());

                    String msg_from_client = "";
                    //---if message from client is null program is break---
                    msg_from_client = dataInputStream.readUTF();

                    if (msg_from_client.equalsIgnoreCase("")) {
                        //---no msg--

                    } else {
                        String[] temp = msg_from_client.split(",");
                        String pass = temp[0];
                        final String result = temp[1];
                        if (pass.equalsIgnoreCase(ValuesConst.pass_transfer)) {
                            //---match pass---
                            if (!result.isEmpty()) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, "Transfer " + result, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //---close connect with client---
                MySocket.closeSocket(socket);
                MySocket.closeDataInput(dataInputStream);
            }
        }
    }

}
