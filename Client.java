package odev44;

import java.awt.event.*;
import java.awt.*;
import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Client {

    private static final String SERVER_IP = "10.10.12.1";
    private static final int PORT = 12345;
    private static final String SECRET_KEY = "abcdefghijklmnopqrstuvwxyz123456";

    public static void main(String[] args) throws Exception {

        // Sayfa Başlığı 
        JFrame frame = new JFrame("CLIENT UYGULAMASI");

        // Label, TextField, Button, TextArea 
        JLabel label2 = new JLabel("Server IP ");
        label2.setBounds(30, 10, 100, 25);
        frame.add(label2, BorderLayout.NORTH);

        JTextField serverIP = new JTextField();
        serverIP.setBounds(90, 10, 130, 25);
        frame.add(serverIP);

        serverIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // kullanıcının girdiği veriyi alma
                String inputValue = serverIP.getText();

                // girdiği veriyi konsola yazdırma
                System.out.println(inputValue);
            }
        });

        JLabel label3 = new JLabel("Port ");
        label3.setBounds(230, 10, 50, 25);
        frame.add(label3);

        JTextField port = new JTextField();
        port.setBounds(260, 10, 50, 25);
        frame.add(port);

        port.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // kullanıcının girdiği veriyi alma
                String inputValue = port.getText();

                // girdiği veriyi konsola yazdırma
                System.out.println(inputValue);
            }
        });

        JButton button1 = new JButton("BAĞLAN");
        button1.setBounds(315, 10, 115, 25);
        frame.add(button1, BorderLayout.NORTH);

        JTextArea textArea1 = new JTextArea();
        textArea1.setBounds(30, 50, 400, 300);
        frame.add(textArea1);
        
        JTextArea textArea2 = new JTextArea();
        textArea2.setBounds(30, 360, 300, 75);
        frame.add(textArea2);

        JButton button2 = new JButton("Gönder");
        button2.setBounds(340, 360, 90, 75);
        frame.add(button2);

        // Sayfanın Büyüklüğü 
        frame.setSize(480, 500);
        frame.setLayout(null);

        // Çarpıya Basınca Kapatma Kodu
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Servera bağlanmak için bir soket oluşturuyoruz
        Socket socket = new Socket(SERVER_IP, PORT);

        // Veri göndermek ve almak için out ve in ayarladık
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        // Şifreleme ve şifre çözmek için secret key oluşturduk
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

        FileWriter fileWriter = new FileWriter("client_log.txt");
        
        // Performansı artırmak için FileWriter nesnesini BufferedWriter nesnesine aldık
        BufferedWriter logWriter = new BufferedWriter(fileWriter);

        // main döngü
        while (true) {
            // Girilen mesajı okuma
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Client: ");
            String message = reader.readLine();

            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                String string = input.readUTF();
                textArea1.setText(textArea1.getText() + "\n " + "Client: " + string);
            } catch (IOException ev) {
                textArea1.setText(textArea1.getText() + " \n" + "Network issues ");
                try {
                    Thread.sleep(2000);
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            button2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String message = textArea1.getText();
                    try {
                        // secretKey kullanarak mesajı şifreleyin
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                        byte[] encryptedMessage = cipher.doFinal(message.getBytes());
                        String encryptedMessageString = Base64.getEncoder().encodeToString(encryptedMessage);

                        // Şifreli mesajı servera gönder
                        out.writeObject(encryptedMessageString);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            
            // secretKey kullanarak mesajı şifreleme
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedMessage = cipher.doFinal(message.getBytes());
            String encryptedMessageString = Base64.getEncoder().encodeToString(encryptedMessage);

            // Şifreli mesajı servera gönderiyoruz
            out.writeObject(encryptedMessageString);
            out.flush();

            // şuanki tarih-saat bilgisini alıyoruz
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();

            // tarih-saati formatlı yazıyoruz
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String timeAndDate = dateFormat.format(date);

            // Mesajı ve saati/tarihi log dosyasına yazıyoruz
            logWriter.write("[" + timeAndDate + "]" + "[Client]" + "[" + message + "]");
            logWriter.newLine();
            logWriter.flush();

            // serverdan şifrelenmiş bir yanıtı okuyoruz
            String encryptedResponse = (String) in.readObject();

            // secretKey kullanarak yanıtın şifresini çözün
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedResponse = cipher.doFinal(Base64.getDecoder().decode(encryptedResponse));
            String response = new String(decryptedResponse);

            // Şifresi çözülmüş yanıtı yazdırıyoruz
            System.out.println("Server: " + response);
        }
    }
}
