package server;

import protocol.KnockKnockProtocol;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * @author garden
 * @create 8/11/18
 */
public class SSLServer {
    public static void main(String[] args) {
        try {
            // Get the keystore
            System.setProperty("javax.net.debug", "all");
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            String password = "abcdefg";
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("server/certificate.p12");
            keyStore.load(inputStream, password.toCharArray());

            // TrustManagerFactory
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
            InputStream inputStream1 = ClassLoader.getSystemClassLoader().getResourceAsStream("client/cert-and-key.p12");
            trustStore.load(inputStream1, password.toCharArray());
            trustManagerFactory.init(trustStore);
            X509TrustManager x509TrustManager = null;
            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager) {
                    x509TrustManager = (X509TrustManager) trustManager;
                    break;
                }
            }

            if (x509TrustManager == null) throw new NullPointerException();


            // KeyManagerFactory ()
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
            keyManagerFactory.init(keyStore, password.toCharArray());
            X509KeyManager x509KeyManager = null;
            for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
                if (keyManager instanceof X509KeyManager) {
                    x509KeyManager = (X509KeyManager) keyManager;
                    break;
                }
            }
            if (x509KeyManager == null) throw new NullPointerException();

            // set up the SSL Context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{x509KeyManager}, new TrustManager[]{x509TrustManager}, null);

            SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(8333);
            serverSocket.setNeedClientAuth(false);
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
            SSLSocket socket = (SSLSocket) serverSocket.accept();


            // InputStream and OutputStream Stuff
            PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String inputLine, outputLine;

            // Initiate conversation with client
            KnockKnockProtocol kkp = new KnockKnockProtocol();
            outputLine = kkp.processInput(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = kkp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
        }catch (IOException e){
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
