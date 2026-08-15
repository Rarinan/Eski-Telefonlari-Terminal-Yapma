import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class MiniLinuxTerminal extends MIDlet implements CommandListener {
    private Display display;
    private TerminalCanvas terminalCanvas;

    public MiniLinuxTerminal() {
        display = Display.getDisplay(this);
        terminalCanvas = new TerminalCanvas(this);
    }

    protected void startApp() {
        display.setCurrent(terminalCanvas);
    }

    protected void pauseApp() {}
    protected void destroyApp(boolean u) {}

    public void commandAction(Command c, Displayable d) {}
}

class TerminalCanvas extends Canvas implements Runnable, CommandListener {
    private MiniLinuxTerminal midlet;
    private Vector lines = new Vector();
    private Vector pkgs = new Vector();
    private Vector history = new Vector();
    
    private String currentInput = "";
    private boolean cursorVisible = true;
    private boolean isRunning = false;
    private long startTime;
    private Command exitCmd, clearCmd, inputCmd;
    private Font terminalFont;

    // Dokunmatik ve Kaydırma Değişkenleri
    private int startTouchY = 0;
    private int totalDragDist = 0;
    private int scrollOffset = 0;

    // CMatrix Modu Değişkenleri
    private boolean isCmatrixRunning = false;
    private Random random = new Random();
    private int[] matrixCols;

    public TerminalCanvas(MiniLinuxTerminal midlet) {
        this.midlet = midlet;
        this.startTime = System.currentTimeMillis();
        
        try {
            terminalFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        } catch (Exception e) {
            terminalFont = Font.getDefaultFont();
        }

        // Varsayılan Paketler
        pkgs.addElement("base");
        pkgs.addElement("bash");
        pkgs.addElement("net-tools");

        // Başlangıç Yazıları
        lines.addElement("TermiSymbian Arch-Linux v2.0 (TTY1)");
        lines.addElement("Type 'help' for available commands.");
        lines.addElement("------------------------------------");
        lines.addElement("");

        inputCmd = new Command("Komut Yaz", Command.OK, 1);
        clearCmd = new Command("Temizle", Command.SCREEN, 2);
        exitCmd = new Command("Çıkış", Command.EXIT, 3);
        
        addCommand(inputCmd);
        addCommand(clearCmd);
        addCommand(exitCmd);
        setCommandListener(this);
    }

    protected void showNotify() {
        setFullScreenMode(true);
        if (!isRunning) {
            isRunning = true;
            new Thread(this).start();
        }
    }

    protected void hideNotify() {
        isRunning = false;
    }

    public void run() {
        while (isRunning) {
            if (isCmatrixRunning) {
                updateCmatrix();
            } else {
                cursorVisible = !cursorVisible;
            }
            repaint();
            try { Thread.sleep(isCmatrixRunning ? 80 : 500); } catch (Exception e) {}
        }
    }

    protected void paint(Graphics g) {
        try {
            int width = getWidth();
            int height = getHeight();

            // Simsiyah Arka Plan
            g.setColor(0, 0, 0);
            g.fillRect(0, 0, width, height);

            // CMatrix Modu Aktifse
            if (isCmatrixRunning) {
                drawCmatrix(g, width, height);
                return;
            }

            // Normal Terminal Modu
            g.setColor(0, 255, 65);
            g.setFont(terminalFont);

            int lineHeight = terminalFont.getHeight() + 2;
            if (lineHeight <= 0) lineHeight = 14;

            int maxLines = (height - 20) / lineHeight;
            if (maxLines <= 0) maxLines = 1;

            int maxScroll = Math.max(0, lines.size() - maxLines);
            if (scrollOffset > maxScroll) scrollOffset = maxScroll;
            if (scrollOffset < 0) scrollOffset = 0;

            int startIdx = Math.max(0, lines.size() - maxLines - scrollOffset);
            int endIdx = Math.min(lines.size(), startIdx + maxLines);

            int y = 5;

            for (int i = startIdx; i < endIdx; i++) {
                g.drawString((String) lines.elementAt(i), 5, y, Graphics.TOP | Graphics.LEFT);
                y += lineHeight;
            }

            // Komut İstemi ve İmleç
            String prompt = "archuser@c5-03:~$ " + currentInput + (cursorVisible ? "_" : " ");
            g.setColor(255, 255, 255);
            g.drawString(prompt, 5, y, Graphics.TOP | Graphics.LEFT);

        } catch (Exception e) {}
    }

    // CMatrix Çizim Fonksiyonu
    private void drawCmatrix(Graphics g, int w, int h) {
        g.setFont(terminalFont);
        int charW = terminalFont.charWidth('A');
        if (charW <= 0) charW = 10;
        int numCols = w / charW;

        if (matrixCols == null || matrixCols.length != numCols) {
            matrixCols = new int[numCols];
            for (int i = 0; i < numCols; i++) {
                matrixCols[i] = random.nextInt(h);
            }
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@#$%&*";
        for (int i = 0; i < numCols; i++) {
            int x = i * charW;
            int y = matrixCols[i];

            g.setColor(255, 255, 255);
            char ch = chars.charAt(random.nextInt(chars.length()));
            g.drawString(String.valueOf(ch), x, y, Graphics.TOP | Graphics.LEFT);

            g.setColor(0, 255, 65);
            for (int j = 1; j <= 5; j++) {
                char prevCh = chars.charAt(random.nextInt(chars.length()));
                g.drawString(String.valueOf(prevCh), x, y - (j * 12), Graphics.TOP | Graphics.LEFT);
            }
        }
    }

    private void updateCmatrix() {
        if (matrixCols == null) return;
        int h = getHeight();
        for (int i = 0; i < matrixCols.length; i++) {
            matrixCols[i] += 12;
            if (matrixCols[i] > h + 60) {
                matrixCols[i] = 0;
            }
        }
    }

    // Dokunma Başlangıcı
    protected void pointerPressed(int x, int y) {
        if (isCmatrixRunning) {
            isCmatrixRunning = false; // Ekrana dokunarak CMatrix'ten çık
            repaint();
            return;
        }
        startTouchY = y;
        totalDragDist = 0;
    }

    // Parmağı Kaydırma (Scroll)
    protected void pointerDragged(int x, int y) {
        if (isCmatrixRunning) return;

        int dy = y - startTouchY;
        totalDragDist += Math.abs(dy);

        int lineHeight = terminalFont.getHeight() + 2;
        if (lineHeight <= 0) lineHeight = 14;

        int linesMoved = dy / lineHeight;
        if (linesMoved != 0) {
            scrollOffset += linesMoved;
            startTouchY = y;
            repaint();
        }
    }

    // Dokunma Bitişi
    protected void pointerReleased(int x, int y) {
        if (isCmatrixRunning) return;
        if (totalDragDist < 15) {
            openInputBox();
        }
    }

    private void openInputBox() {
        final TextBox tb = new TextBox("Komut Girin:", "", 100, TextField.ANY);
        final Command okCmd = new Command("Gönder", Command.OK, 1);
        final Command cancelCmd = new Command("İptal", Command.CANCEL, 2);
        
        tb.addCommand(okCmd);
        tb.addCommand(cancelCmd);
        
        tb.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == okCmd) {
                    String input = tb.getString();
                    if (input != null && input.trim().length() > 0) {
                        executeCommand(input.trim());
                        history.addElement(input.trim());
                    }
                }
                Display.getDisplay(midlet).setCurrent(TerminalCanvas.this);
            }
        });
        
        Display.getDisplay(midlet).setCurrent(tb);
    }

    private void executeCommand(String fullCmd) {
        lines.addElement("archuser@c5-03:~$ " + fullCmd);
        String cmd = fullCmd.toLowerCase().trim();

        if (cmd.startsWith("pacman -s ")) handlePacman(cmd);
        else if (cmd.equals("cmatrix")) handleCmatrix();
        else if (cmd.equals("fastfetch") || cmd.equals("neofetch")) handleFetch();
        else if (cmd.equals("htop")) handleHtop();
        else if (cmd.equals("netstat")) handleNetstat();
        else if (cmd.equals("free") || cmd.equals("free -h")) handleFree();
        else if (cmd.equals("uptime")) lines.addElement("Uptime: " + getUptime());
        else if (cmd.equals("whoami")) lines.addElement("archuser");
        else if (cmd.equals("uname -a")) lines.addElement("Linux arch-c503 6.1.0-arch1-1 armv6l GNU/Linux");
        else if (cmd.equals("ls")) lines.addElement("welcome.txt   notes.txt   script.sh");
        else if (cmd.equals("clear")) lines.removeAllElements();
        else if (cmd.equals("help")) {
            lines.addElement("Available: pacman -S, cmatrix, fastfetch, htop,");
            lines.addElement("           netstat, free, uptime, ls, clear, whoami");
        } else {
            lines.addElement("bash: " + fullCmd + ": command not found");
        }
        lines.addElement("");
        
        scrollOffset = 0;
        repaint();
    }

    private String getUptime() {
        long sec = (System.currentTimeMillis() - startTime) / 1000;
        return (sec / 60) + "m " + (sec % 60) + "s";
    }

    private void handlePacman(String c) {
        String p = c.substring(10).trim();
        if (pkgs.contains(p)) {
            lines.addElement("warning: " + p + " is up to date -- skipping");
            return;
        }
        pkgs.addElement(p);
        lines.addElement(":: Resolving dependencies...");
        lines.addElement(":: Downloading " + p + " (1.2 MB)...");
        lines.addElement(":: Installing " + p + "...");
        lines.addElement(":: " + p + " installed successfully!");
    }

    private void handleCmatrix() {
        if (!pkgs.contains("cmatrix")) {
            lines.addElement("Error: cmatrix not installed. Try: pacman -S cmatrix");
            return;
        }
        isCmatrixRunning = true;
    }

    private void handleFetch() {
        if (!pkgs.contains("neofetch") && !pkgs.contains("fastfetch")) {
            lines.addElement("Error: fastfetch not installed. Try: pacman -S fastfetch");
            return;
        }
        lines.addElement("  /\\       archuser@Nokia-C5-03");
        lines.addElement(" /  \\      --------------------");
        lines.addElement("/ /\\ \\     OS: Arch Linux ARM");
        lines.addElement("\\ \\/ /     Kernel: 6.1.0-ARCH");
        lines.addElement("  \\/       Uptime: " + getUptime());
        lines.addElement("           Packages: " + pkgs.size() + " (pacman)");
        lines.addElement("           Shell: TermiSymbian v2.0");
        lines.addElement("           CPU: ARM1136JF-S @ 600MHz");
        lines.addElement("           RAM: " + (Runtime.getRuntime().totalMemory() / 1024) + "KB / 128MB");
    }

    private void handleHtop() {
        if (!pkgs.contains("htop")) {
            lines.addElement("Error: htop not installed. Try: pacman -S htop");
            return;
        }
        long tot = Runtime.getRuntime().totalMemory() / 1024;
        long free = Runtime.getRuntime().freeMemory() / 1024;
        lines.addElement("CPU [||||||||||||||...... 42.0%]");
        lines.addElement("Mem [" + (tot - free) + "K/" + tot + "K]");
        lines.addElement("PID USER  CPU% COMMAND");
        lines.addElement("  1 root   1%  init");
        lines.addElement("  2 arch  41%  java_vm");
    }

    private void handleNetstat() {
        lines.addElement("Proto Local Address      Foreign Address    State");
        lines.addElement("tcp   192.168.1.105:443  104.26.10.23:80    ESTABLISHED");
        lines.addElement("tcp   127.0.0.1:8080     0.0.0.0:*          LISTEN");
        lines.addElement("udp   0.0.0.0:68         0.0.0.0:*");
    }

    private void handleFree() {
        long tot = Runtime.getRuntime().totalMemory() / 1024;
        long free = Runtime.getRuntime().freeMemory() / 1024;
        lines.addElement("      total    used    free");
        lines.addElement("Mem:  " + tot + "K   " + (tot - free) + "K   " + free + "K");
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCmd) midlet.notifyDestroyed();
        else if (c == clearCmd) { lines.removeAllElements(); scrollOffset = 0; repaint(); }
        else if (c == inputCmd) { openInputBox(); }
    }
}
