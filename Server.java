package odev44;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.crypto.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Server {

    private static final int PORT = 12345;
    private static final String SECRET_KEY = "abcdefghijklmnopqrstuvwxyz123456";

    public static void main(String[] args) throws Exception {

        // Sayfa Başlığı 
        JFrame frame = new JFrame("SERVER UYGULAMASI");

        //  Label, TextField, Button, TextArea 
        JLabel label2 = new JLabel("Dinlenecek Port Numarası ");
        label2.setBounds(30, 10, 200, 25);
        frame.add(label2, BorderLayout.NORTH);

        JTextField port = new JTextField();
        port.setBounds(200, 10, 75, 25);
        frame.add(port);

        JButton button1 = new JButton("Serverı Başlat");
        button1.setBounds(305, 10, 125, 25);
        frame.add(button1, BorderLayout.SOUTH);

        // buton1 action listener
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }

        });

        JTextArea textArea1 = new JTextArea();
        textArea1.setBounds(30, 50, 400, 300);
        frame.add(textArea1);

        JTextArea textArea2 = new JTextArea();
        textArea2.setBounds(30, 360, 300, 75);
        frame.add(textArea2);

        JButton button2 = new JButton("Gönder");
        button2.setBounds(340, 360, 90, 75);
        frame.add(button2);
        
        // buton2 action listener
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textArea2.getText();
                textArea2.setText("");
            }
        });

        // Sayfanın Büyüklüğü 
        frame.setSize(480, 500);
        frame.setLayout(null);

        // Çarpıya Basınca Kapatma 
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server portu dinliyor " + PORT + "...");

        Socket socket = serverSocket.accept();
        //System.out.println("Clientla bağlantı sağlandı " + socket.getInetAddress());

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

        FileWriter fileWriter = new FileWriter("server_log.txt");
        BufferedWriter logWriter = new BufferedWriter(fileWriter);

        // main döngü
        while (true) {
            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                String string = input.readUTF();
                textArea1.setText(textArea1.getText() + "\n " + "Client: " + string);
            } catch (Exception ev) {
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
                        // secret key kullanarak mesajı şifreleyin
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                        byte[] encryptedMessage = cipher.doFinal(message.getBytes());
                        String encryptedMessageString = Base64.getEncoder().encodeToString(encryptedMessage);

                        // şifreli mesajı sunucuya gönder
                        out.writeObject(encryptedMessageString);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            String encryptedMessage = (String) in.readObject();

            // secret key kullanarak mesajın şifresini çözme
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedMessage = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            String message = new String(decryptedMessage);

            // şifresi çözülmüş mesajı ekrana
            System.out.println("Client: " + message);

            // şuanki tarih-saat verilerini aldık
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();

            // formatli şekilde tarih-saat yazdırma
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String timeAndDate = dateFormat.format(date);

            // mesajı log dosyasına yazar
            logWriter.write("[" + timeAndDate + "]" + "[Client]" + "[" + message + "]");
            logWriter.newLine();
            logWriter.flush();

            // kullanıcıdan response oku
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Server: ");
            String response = reader.readLine();

            // secret key kullanarak yanıtı şifreledik
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedResponse = cipher.doFinal(response.getBytes());
            String encryptedResponseString = Base64.getEncoder().encodeToString(encryptedResponse);

            // clienta şifreli yanıt gönder
            out.writeObject(encryptedResponseString);
            out.flush();
        }
    }
}
