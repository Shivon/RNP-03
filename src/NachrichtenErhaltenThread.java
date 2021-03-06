import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Jana on 15.11.2015.
 */
public class NachrichtenErhaltenThread extends  Thread {

    private Werkzeug _ui;
    private Socket _socket;
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private String sentence;

    public NachrichtenErhaltenThread(Werkzeug ui, Socket socket) {
        super();
        this._ui = ui;
        this._socket = socket;
    }

    @Override
    public void run() {

        try {
            /* Socket-Basisstreams durch spezielle Streams filtern */
            outToServer = new DataOutputStream(_socket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(
                    _socket.getInputStream(), "UTF-8"));

                while (!this.isInterrupted() && !_ui.isLoggedIn()) {
                    sentence = readFromServer();
                    if (sentence.startsWith(".auth")) {
                        _ui.writeInWritingField("Bitte Usernamen angeben: ");
                        System.out.println("TCP Client got from Server: " + sentence);
                    }
                    else if(sentence.startsWith(".authfail")){
                        _ui.writeInWritingField("Vergeben, bitte anderen Usernamen angeben: ");
                        _ui.throwUserNameNotAvailableException();
                    }
                    else if (sentence.startsWith(".ack")){
                        _ui.setLoggedIn(true);
                        _ui.writeInWritingField("");
                        System.out.println("HALLO TCP Client got from Server: " + sentence);
                         sentence = sentence.replaceFirst(Pattern.quote(".ack"), "");
                    }
                }
                while (!this.isInterrupted() && _ui.isLoggedIn()) {
                    sentence = readFromServer();
                    if (sentence == null) {
                        this.interrupt();
                    }
                    else if (sentence.startsWith(".clients:")) {
                        _ui.writeInMemberField("");
                        sentence = sentence.replaceFirst(Pattern.quote(".clients:"), "");
                        _ui.writeInMemberField(sentence);
                        System.out.println("MEMBERS   TCP Client got from Server: " + sentence);
                    }
                    else if (sentence.startsWith(".msg:")){
                        sentence = sentence.replaceFirst(Pattern.quote(".msg:"), "");
                        _ui.writeInChatArea(sentence);
                        System.out.println("TCP Client got from Server: " + sentence);
                    }

            /* Socket-Streams schliessen --> Verbindungsabbau */
                }
            _socket.close();
            }catch(IOException e){
                System.err.println("Connection aborted by server!");
            }
            _ui.shutDown();
            System.out.println("TCP Client stopped!");
        }

    private String readFromServer() throws IOException {
         /* Lies die Antwort (reply) vom Server */
        String reply = inFromServer.readLine();

        return reply;
    }
}
