package com.kepler.avisala;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.json.*;
import java.util.stream.*;
import org.apache.commons.io.IOUtils;
import com.notnoop.apns.*;
import java.io.*;
import java.net.*;

public class AvisalaServer {

    private static final int fNumberOfThreads = 100;
    private static final Executor fThreadPool = Executors.newFixedThreadPool(fNumberOfThreads);

    static String token = "f081eb073ead496b47015b9b6d5ccc7379014eb92ae26a1c6d9ebfd2bac3c8c5";


    public static void main(String[] args) throws IOException {
        ServerSocket socket = new ServerSocket(20081);
        while (true)
        {
            final Socket connection = socket.accept();
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    HandleRequest(connection);
                }
            };
            fThreadPool.execute(task);
        }
    }

    private static void HandleRequest(Socket s) {
        BufferedReader in;
        PrintWriter out;
        String request;

        try {
            String webServerAddress = s.getInetAddress().toString();
            System.out.println("New Connection:" + webServerAddress);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            request = new String(IOUtils.toByteArray(in));

            System.out.print("\n\nClient request: " + request);
            JSONObject obj = new JSONObject(request);
            String device = obj.getString("device");
            String model = obj.getString("model");
            String event = obj.getString("event");
            String trainLoss = obj.getString("train_loss");

            System.out.print("\ndevice " + device);
            System.out.print("\nmodel " + model);
            System.out.print("\nevent " + event);
            System.out.print("\ntrainLoss " + trainLoss);

            ApnsService service = APNS.newService().withCert("cert/dev/Certificates_APNS.p12", "******").withSandboxDestination().build();
            String payload = APNS.newPayload().alertBody(model + ' ' + event + ' '  + trainLoss).build();
    	      service.push(token, payload);


            out = new PrintWriter(s.getOutputStream(), true);
            out.println("HTTP/1.0 200");
            out.println("Content-type: application/json");
            out.println("Server-name: AvisalaServer");
            String response = "{\"mensagem\": \"OK\"}";
            out.println("Content-length: " + response.length());
            out.println("");
            out.println(response);
            out.flush();
            out.close();
            s.close();
        }
        catch (IOException e) {
            System.out.println("Failed respond to client request: " + e.getMessage());
        }
        finally {
            if (s != null) {
                try {
                    s.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }
}
