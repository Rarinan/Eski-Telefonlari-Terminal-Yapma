import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class MiniLinuxTerminal extends MIDlet implements CommandListener {
    private Display display;
    private Form form;
    private TextField inputField;
    private StringItem outputArea;
    private Command runCommand, exitCommand;
    private Random random = new Random();

    private Vector pkgs = new Vector();
    private Vector files = new Vector();
    private long startTime;

    public MiniLinuxTerminal() {
        startTime = System.currentTimeMillis();
        display = Display.getDisplay(this);
        form = new Form("TermiSymbian Arch-Linux");

        files.addElement("welcome.txt");
        files.addElement("projects/");

        // cmatrix varsayılan yüklü gelsin
        pkgs.addElement("cmatrix");

        outputArea = new StringItem("", "TermiSymbian Arch-Linux v1.0\nType 'help'\n---------------------------\n");
        inputField = new TextField("archuser@c5-03:~$ ", "", 40, TextField.ANY | TextField.NON_PREDICTIVE);

        runCommand = new Command("Run", Command.OK, 1);
        exitCommand = new Command("Exit", Command.EXIT, 2);

        form.append(outputArea);
        form.append(inputField);
        form.addCommand(runCommand);
        form.addCommand(exitCommand);
        form.setCommandListener(this);
    }

    protected void startApp() { display.setCurrent(form); }
    protected void pauseApp() {}
    protected void destroyApp(boolean u) {}

    public void commandAction(Command c, Displayable d) {
        if (c == runCommand) {
            String cmd = inputField.getString().trim();
            inputField.setString("");
            processCommand(cmd);
        } else if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }

    private String getUptime() {
        long elapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        long mins = elapsedSec / 60;
        long secs = elapsedSec % 60;
        return (mins > 0) ? (mins + "m " + secs + "s") : (secs + "s");
    }

    private void processCommand(String fullCmd) {
        if (fullCmd.length() == 0) return;
        String cur = outputArea.getText();
        String res = "";
        String cmd = fullCmd.toLowerCase().trim();

        // --- CMATRIX ANIMASYONU ---
        if (cmd.equals("cmatrix")) {
            if (pkgs.contains("cmatrix")) {
                MatrixCanvas matrix = new MatrixCanvas(display, form);
                display.setCurrent(matrix);
                matrix.start();
                return;
            } else {
                res = "\ncommand not found. Try: pacman -S cmatrix\n";
            }
        }
        // --- PACMAN ---
        else if (cmd.startsWith("pacman -s ")) {
            String p = cmd.substring(10).trim();
            if (p.equals("cmatrix") || p.equals("sl") || p.equals("cowsay")) {
                if (!pkgs.contains(p)) {
                    pkgs.addElement(p);
                    res = "\ndownloading " + p + "... [100%]\ninstalling " + p + "... [100%]\n:: " + p + " installed!\n";
                } else {
                    res = "\nwarning: " + p + " is up to date\n";
                }
            } else {
                res = "\nerror: target not found: " + p + "\n";
            }
        } 
        // --- PAKETLER ---
        else if (cmd.equals("sl")) {
            res = pkgs.contains("sl") ? "\n  ==== ____====\n _|__|| C5-03  |\n(o)(o)(o)----(o)\nChoo Choo!\n" : "\ncommand not found. Try: pacman -S sl\n";
        }
        else if (cmd.equals("cowsay")) {
            res = pkgs.contains("cowsay") ? "\n < Arch King >\n  \\   ^__^\n   \\  (oo)\n" : "\ncommand not found. Try: pacman -S cowsay\n";
        }
        // --- DOSYA SİSTEMİ ---
        else if (cmd.equals("ls")) {
            res = "\n";
            for (int i = 0; i < files.size(); i++) res += files.elementAt(i) + "  ";
            res += "\n";
        }
        else if (cmd.startsWith("mkdir ")) {
            files.addElement(fullCmd.substring(6).trim() + "/");
            res = "\n";
        }
        else if (cmd.startsWith("touch ")) {
            files.addElement(fullCmd.substring(6).trim());
            res = "\n";
        }
        else if (cmd.startsWith("cat ")) {
            String f = fullCmd.substring(4).trim();
            res = f.equals("welcome.txt") ? "\nWelcome to Nokia C5-03 Arch Linux!\n" : "\ncat: " + f + ": No such file\n";
        }
        // --- ARCH FASTFETCH ---
        else if (cmd.equals("fastfetch") || cmd.equals("neofetch")) {
            res = "\n  /\\       archuser@c5-03\n" +
                  " /  \\      --------------\n" +
                  "/ /\\ \\     OS: Arch Linux ARM (Symbian S60v5)\n" +
                  "\\ \\/ /     Host: Nokia C5-03\n" +
                  "  \\/       Kernel: 2.6.28-ARCH\n" +
                  "           Uptime: " + getUptime() + "\n" +
                  "           Packages: " + (pkgs.size() + 14) + " (pacman)\n" +
                  "           Shell: TermiSymbian 1.0\n" +
                  "           Display: 360x640 3.2\" nHD\n" +
                  "           WM: KDE Plasma 6.1\n" +
                  "           Terminal: JavaME-KVM\n" +
                  "           CPU: ARM1136JF-S @ 600 MHz\n" +
                  "           GPU: PowerVR SGX530\n" +
                  "           Memory: 48MB / 128MB\n\n" +
                  "           [#][#][#][#][#][#][#][#]\n";
        }
        else if (cmd.equals("htop")) {
            long tot = Runtime.getRuntime().totalMemory() / 1024;
            long free = Runtime.getRuntime().freeMemory() / 1024;
            long used = tot - free;
            int cpu = 15 + random.nextInt(30);
            res = "\nCPU [" + getBar(cpu) + " " + cpu + "%]\nMem[" + getBar((int)(used * 100 / tot)) + " " + used + "K/" + tot + "K]\nPID USER CPU% COMMAND\n 1  arch 15%  JavaJVM\n";
        }
        else if (cmd.startsWith("ping ")) {
            res = "\nPING " + fullCmd.substring(5).trim() + ": 64 bytes time=38ms\n";
        }
        else if (cmd.equals("clear")) {
            outputArea.setText("TermiSymbian Arch-Linux\n---------------------------\n");
            return;
        }
        else if (cmd.equals("help")) {
            res = "\nCmds: cmatrix, ls, mkdir, touch, cat, pacman -S, fastfetch, htop, ping, clear, whoami, uname\n";
        }
        else if (cmd.equals("whoami")) { res = "\narchuser\n"; }
        else if (cmd.startsWith("uname")) { res = "\nLinux c5-03 2.6.28-ARCH armv6l\n"; }
        else {
            res = "\nbash: " + fullCmd + ": command not found\n";
        }

        outputArea.setText(cur + "archuser@c5-03:~$ " + fullCmd + res + "\n");
    }

    private String getBar(int p) {
        int b = p / 10;
        String r = "";
        for (int i = 0; i < 10; i++) r += (i < b) ? "|" : ".";
        return r;
    }
}

// FULLSCREEN CMATRIX ANIMASYON CANVAS'I
class MatrixCanvas extends Canvas implements Runnable {
    private Display display;
    private Displayable lastScreen;
    private boolean running = true;
    private Random rand = new Random();
    private int width, height, numCols;
    private int[] yPos;

    public MatrixCanvas(Display d, Displayable last) {
        this.display = d;
        this.lastScreen = last;
        setFullScreenMode(true);
        width = getWidth();
        height = getHeight();
        numCols = width / 14;
        if (numCols <= 0) numCols = 10;
        yPos = new int[numCols];
        for (int i = 0; i < numCols; i++) {
            yPos[i] = rand.nextInt(height);
        }
    }

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        while (running) {
            repaint();
            try {
                Thread.sleep(70);
            } catch (Exception e) {}
        }
    }

    protected void paint(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, width, height);

        g.setColor(0, 255, 0);
        for (int i = 0; i < numCols; i++) {
            char ch = (char) (33 + rand.nextInt(90));
            g.drawString("" + ch, i * 14, yPos[i], Graphics.TOP | Graphics.LEFT);
            yPos[i] += 16;
            if (yPos[i] > height) {
                yPos[i] = 0;
            }
        }
    }

    protected void keyPressed(int keyCode) { stopAndReturn(); }
    protected void pointerPressed(int x, int y) { stopAndReturn(); }

    private void stopAndReturn() {
        running = false;
        display.setCurrent(lastScreen);
    }
}
