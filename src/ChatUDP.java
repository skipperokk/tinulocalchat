import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class ChatUDP extends JFrame {
    private JTextArea taMain;
    private JTextField tfMessage;

    private final String FRM_TITLE = "Local chat";
    private final int FRM_LOC_X = 100;
    private final int FRM_LOC_Y = 100;
    private final int FRM_WIDTH = 400;
    private final int FRM_HEIGHT = 400;

    private final int PORT = 9876;
    private final String IP_BROADCAST = "192.168.0.255";

    public static void main(String[] args) {
        new ChatUDP().antistatic();
    }

    private void btnSendHandler() throws Exception {
        DatagramSocket sendSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(IP_BROADCAST);
        byte[] sendData;
        String sentence = tfMessage.getText();
        tfMessage.setText("");
        sendData = sentence.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
        sendSocket.send(sendPacket);
    }

    private void frameDraw(JFrame frame) {
        tfMessage = new JTextField();
        taMain = new JTextArea(FRM_HEIGHT / 19, 50);
        JScrollPane spMain = new JScrollPane(taMain);
        spMain.setLocation(0, 0);
        taMain.setLineWrap(true);
        taMain.setEditable(false);

        JButton btnSend = new JButton();
        btnSend.setText("Отправить");
        btnSend.setToolTipText("Отправить сообщение");
        JRootPane rootPane = frame.getRootPane();
        rootPane.setDefaultButton(btnSend);
        btnSend.addActionListener(e -> {
            try {
                btnSendHandler();
            } catch (Exception expt) {
                expt.printStackTrace();
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle(FRM_TITLE);
        frame.setLocation(FRM_LOC_X, FRM_LOC_Y);
        frame.setSize(FRM_WIDTH, FRM_HEIGHT);
        frame.setResizable(false);
        frame.getContentPane().add(BorderLayout.NORTH, spMain);
        frame.getContentPane().add(BorderLayout.CENTER, tfMessage);
        frame.getContentPane().add(BorderLayout.EAST, btnSend);
        frame.setVisible(true);
    }

    private void antistatic() {
        frameDraw(new ChatUDP());
        new thdReceiver().start();
    }

    private class thdReceiver extends Thread {
        @Override
        public void start() {
            super.start();
            try {
                customize();
            } catch (Exception expt) {
                expt.printStackTrace();
            }
        }

        private void customize() throws Exception {
            DatagramSocket receiveSocket = new DatagramSocket(PORT);
            Pattern regexp = Pattern.compile("[\u0020-\uFFFF]");
            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                receiveSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String sentence = new String(receivePacket.getData());
                Matcher matcher = regexp.matcher(sentence);

                taMain.append(IPAddress.toString() + ":" + port + ":> ");
                while (matcher.find()) {
                    taMain.append(sentence.substring(matcher.start(), matcher.end()));
                }
                taMain.append("\r\n");
                taMain.setCaretPosition(taMain.getText().length());
            }
        }
    }
}
