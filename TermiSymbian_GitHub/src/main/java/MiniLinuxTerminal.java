import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class MiniLinuxTerminal extends MIDlet implements CommandListener {
    private Display display;
    private Form form;
    private TextField inputField;
    private StringItem outputArea;
    private Command runCmd;

    public MiniLinuxTerminal() {
        display = Display.getDisplay(this);
        form = new Form("TermiSymbian v1.0");
        
        outputArea = new StringItem("", "Linux-like Shell for J2ME\nType 'help' or 'ls'\n> ");
        inputField = new TextField("Input:", "", 50, TextField.ANY);
        runCmd = new Command("Gönder", Command.OK, 1);

        form.append(outputArea);
        form.append(inputField);
        form.addCommand(runCmd);
        form.setCommandListener(this);
    }

    protected void startApp() {
        display.setCurrent(form);
    }

    protected void pauseApp() {}

    protected void destroyApp(boolean unconditional) {}

    public void commandAction(Command c, Displayable d) {
        if (c == runCmd) {
            String command = inputField.getString().trim();
            inputField.setString("");
            processCommand(command);
        }
    }

    private void processCommand(String cmd) {
        String log = "\n$ " + cmd + "\n";
        
        if (cmd.equalsIgnoreCase("help")) {
            log += "Desteklenen komutlar: help, clear, echo, uname, ls, whoami";
        } else if (cmd.equalsIgnoreCase("uname -a")) {
            log += "Symbian-Linux " + System.getProperty("microedition.platform") + " armv6l";
        } else if (cmd.equalsIgnoreCase("whoami")) {
            log += "root";
        } else if (cmd.equalsIgnoreCase("clear")) {
            outputArea.setText("> ");
            return;
        } else if (cmd.startsWith("echo ")) {
            log += cmd.substring(5);
        } else if (cmd.equalsIgnoreCase("ls")) {
            log += listFiles();
        } else {
            log += "sh: command not found: " + cmd;
        }

        outputArea.setText(outputArea.getText() + log + "\n> ");
    }

    private String listFiles() {
        return "bin/   etc/   home/   root/   sdcard/   sys/";
    }
}