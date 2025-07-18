package pomodoro;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;


public final class PomoFrame extends JFrame{
    
    private JPanel panel;
    
    private final CentralPanel central;
    private final NumDisplay display;
    
    //final Color bgColor = new Color(24, 24, 24);
    private final Color bgColor = new Color(40, 40, 40);
    private final int L = 700; //longitud de los lados
    
    private Font textFont;
    private Font numFont;
    
    private final CustomButton start;
    private final CustomButton stop;
    
    private SouthButton pause;
    private SouthButton alarmButton;
    private JLabel separator;
    
    private Clip alarm;
    private JPanel soundPanel;
    
    private final TimeLogic tl;
    
    private boolean isWork = true;
    private Timer timer;
    private int time = 0;
    
    
    public PomoFrame() {

        setIcon();
        setPanel();
        loadFont();
        loadAlarm();
        setTitleLabel();
        setSouthButtons();
        setTimer();
        
        tl = new TimeLogic();
        
        central = new CentralPanel();
        display = new NumDisplay(textFont, "2:25:00");
        
        start = new CustomButton(CustomButton.START);
        stop = new CustomButton(CustomButton.STOP);
        
        
        /*CustomLabel title = new CustomLabel("Pomodoro Timer");
        CustomLabel down = new CustomLabel();

        panel.add(title,BorderLayout.NORTH);
        panel.add(down,BorderLayout.SOUTH);*/
        
        panel.add(start,BorderLayout.WEST);
        panel.add(stop,BorderLayout.EAST);
        
        panel.add(central, BorderLayout.CENTER);
        //panel.add(display, BorderLayout.CENTER);
        
        
        this.setVisible(true);
        
        this.setButtonEvents();
    }
    
    
    private void setIcon() {
        try {
            Image icon = ImageIO.read(getClass().getResource("/res/icon.png"));
            this.setIconImage(icon);
        } catch(Exception e) {
            System.out.println("no se pudo cargar el icono");
        }
        
        this.setTitle("Pomodoro Timer");
    }
    
    
    private void setPanel() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(L, L));
        panel.setBounds(50, 0, L, L);
        panel.setBackground(bgColor);
        
        //panel apra agregar bordes a la izquierda y derecha
        JPanel borderPanel = new JPanel();
        borderPanel.setLayout(null);
        borderPanel.setBackground(bgColor);
        
        //
        borderPanel.setPreferredSize(new Dimension(L + 100, L));
        borderPanel.add(panel);
                
        this.add(borderPanel);
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    
    protected void loadFont() {
        try{
            InputStream is = getClass().getResourceAsStream("/res/Anton-Regular.ttf");
            textFont = Font.createFont(Font.TRUETYPE_FONT, is);
            textFont = textFont.deriveFont(Font.PLAIN, 40f);
            
            numFont = textFont.deriveFont(Font.PLAIN, 25f);

        }catch(Exception e) {
            System.out.println("error al cargar la fuente: " + e);
        }
    }
    
    
    private void loadAlarm() {
        try {
            alarm = AudioSystem.getClip();
            
            InputStream input = getClass().getResourceAsStream("/res/alarm.wav");
            BufferedInputStream buffered = new BufferedInputStream(input); // ✅ solució
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(buffered);
            alarm.open(audioStream);
            
            alarm.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    hideAlarmButton();
                    timer.start();
                }
            });

        } catch (Exception e) {
            System.out.println("No se pudo cargar el sonido. " + e);
        }
    }
    
    
    private void setTitleLabel() {
        JLabel title = new JLabel();
        
        title.setPreferredSize(new Dimension(800, 150));
        title.setBackground(null);
        
        title.setVerticalAlignment(JLabel.CENTER);
        title.setHorizontalAlignment(JLabel.CENTER);
        
        title.setFont(textFont);
        title.setForeground(Color.LIGHT_GRAY);
        
        title.setText("Pomodoro Timer");
        
        panel.add(title, BorderLayout.NORTH);
    }
    
    
    private void setSouthButtons() {
        soundPanel = new JPanel();
        
        alarmButton = new AlarmButton();
        pause = new PauseButton();
        
        separator = new JLabel();
        separator.setPreferredSize(new Dimension(50, 1));
        separator.setOpaque(false);
        
        soundPanel.setPreferredSize(new Dimension(800, 150));
        soundPanel.setBackground(null);
        
        soundPanel.setLayout(new GridBagLayout());
        
        soundPanel.add(pause);
        panel.add(soundPanel, BorderLayout.SOUTH);
        
        //showAlarmButton();
    }
    
    private void showAlarmButton(){
        soundPanel.add(separator);
        soundPanel.add(alarmButton);
        soundPanel.revalidate();
        soundPanel.repaint();
    }
    
    private void hideAlarmButton() {
        soundPanel.remove(separator);
        soundPanel.remove(alarmButton);
        soundPanel.revalidate();
        soundPanel.repaint();
    }
    
    
    private void setTimer() {
        timer = new Timer(1000, e -> {
            time --;
            
            if (time == 0) {
                time = central.getTime(tl);
                isWork = !isWork;
                display.setTitleLabel(isWork ? "Work" : "Rest");
                refocus();
                
                ringAlarm();
            }
            
            display.setTimeLabel(tl.formatTime(time));
            display.repaint();
            this.repaint();
        });
    }
    
    
    private void setButtonEvents() {
        start.addActionListener((ActionEvent e) -> {
            if (time == 0) {
                time = central.getTime(tl);
                timer.start();
                
                panel.remove(central);
                panel.add(display, BorderLayout.CENTER);
                
                display.setTimeLabel(tl.formatTime(time));
                display.setTitleLabel(isWork ? "work" : "rest");
                
                panel.revalidate();
                panel.repaint();
            }
            
            System.out.println("boton start presionado");
            
        });
        
        stop.addActionListener((ActionEvent e) -> {
            if (!(time == 0)) {
                time = 0;
                timer.stop();
                
                panel.remove(display);
                panel.add(central, BorderLayout.CENTER);
                
                isWork = true;
                central.setIsWork(true);
               
                
                panel.revalidate();
                panel.repaint();
            }
            
            System.out.println("boton stop presionado");
        });
        
        alarmButton.addActionListener(e -> {
            alarm.stop();
        });
        
        pause.addActionListener(e -> {
            if (timer.isRunning()) {
                timer.stop();
            } else if(!timer.isRunning() && time != 0) {
                timer.start();
                System.out.println("timer reanudado");
            }
        });
    }
    
    
    private void refocus() {
        this.setExtendedState(JFrame.NORMAL);
        this.setAlwaysOnTop(true);
        this.toFront();
        this.requestFocus();
        this.setAlwaysOnTop(false);
    }
    
    
    private void ringAlarm() {
        timer.stop();
        
        alarm.setFramePosition(0);
        alarm.start();
        
        showAlarmButton();
        
        /*alarm tiene un line listener que hace que el timer se reanude en el momento en el que 
        se termina de reproducir, por lo que en esta funcion solo es necesario parar el timer
        e iniciar la reproducción de alarm*/
    }
    
    
    
    
    
    
    
    private class CustomButton extends JButton {
        
        private boolean pressed = false;
        
        //tipos de botones
        public static final int STOP = 0;
        public static final int START = 1;
        
        private final int buttonType;
        
        private final int width;
        private final int height;
        
        //simbolo del boton
        private final int symbolSide;
        int symbolX, symbolY; //posición
        
        private Color buttonColor;
        private Color symbolColor;
        
        
        public CustomButton(int type) {
            
            this.buttonType = type;
            
            //switch para asignar el color
            switch(type) {
                case STOP:
                    buttonColor = new Color(205, 86, 86);
                    symbolColor = new Color(218, 108, 108);
                    break;
                    
                case START:
                    buttonColor = new Color(129, 116, 160);
                    symbolColor = new Color(168, 136, 181);
                    break;
                 
                default:
                    buttonColor = Color.GRAY;
                    symbolColor = Color.LIGHT_GRAY;
                    break;
            }
            
            //dimensiones para el dibujo del boton 
            width = 120;
            height = 400;
            symbolSide = 50;
            
            symbolX = (width / 2) - (symbolSide  / 2);
            symbolY = (height / 2) - (symbolSide  / 2);
            
            this.setPreferredSize(new Dimension(width, height));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(true);
            setBackground(null);
            
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            //repaint();
        }
        
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            //castea el objeto
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(buttonColor);
            g2d.fillRoundRect(0, 0, width, height, 50, 50);
            
            drawSymbol(g2d);
            
            //si el boton está presionado hace una sombra
            if (getModel().isPressed()) {
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillRoundRect(0, 0, width, height, 50, 50);
            }
            
        }
        
        //dibuja los simbolos del boton
        protected void drawSymbol(Graphics2D g2d) {
            g2d.setColor(symbolColor);
            
            switch(buttonType) {
                case 0:
                    g2d.fillOval(symbolX, symbolY, symbolSide, symbolSide);
                    break;
                
                case 1:
                    int[] x = {symbolX + (symbolSide / 2), symbolX, symbolX + symbolSide};
                    int[] y = {symbolY, symbolY + symbolSide, symbolY + symbolSide};
                    g2d.fillPolygon(x, y, 3);
                    break;
                    
                default:
                    break;
            }
        }
    }
    
    
    
    private class CentralPanel extends JPanel {
        
        NumberSelector work = new NumberSelector("Work", textFont, 25, Color.GREEN);
        NumberSelector rest = new NumberSelector("Rest", textFont, 5, Color.BLUE);
        
        private boolean isWork = true;
        
        public CentralPanel() {
            this.setLayout(new GridLayout(1, 2));
            this.setBackground(null);
            
            this.add(work);
            this.add(rest);
        }
        
        public int getTime(TimeLogic tl) {
            int timeInMin = isWork ? work.getTime() : rest.getTime();
            isWork = !isWork;
            return tl.toSeconds(0, timeInMin, 0);
        }
        
        public void setIsWork(boolean isWork) {
            this.isWork = isWork;
        }
    }
    
    
    
    private class NumDisplay extends JPanel {
        
        private final Font font;
        
        private String time;
        private JLabel timeLabel;
        private JLabel titleLabel;
        private String title = "placeholder";
        
        public NumDisplay(Font font, String time) {
            this.font = font.deriveFont(100f);
            this.time = time;
            
            setDisplay();
        }
        
        private void setDisplay() {
            this.setLayout(new BorderLayout());
            this.setBackground(null);
            
            timeLabel = new JLabel(time);
            timeLabel.setForeground(Color.LIGHT_GRAY);
            timeLabel.setFont(font);
            
            timeLabel.setHorizontalAlignment(JLabel.CENTER);
            timeLabel.setVerticalAlignment(JLabel.CENTER);
            
            this.add(timeLabel, BorderLayout.CENTER);
            
            titleLabel = new JLabel(title);
            
            titleLabel.setForeground(Color.LIGHT_GRAY);
            titleLabel.setFont(font.deriveFont(50f));
            
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            titleLabel.setVerticalAlignment(JLabel.CENTER);
            
            this.add(titleLabel, BorderLayout.NORTH);
            
        }
        
        public void setTimeLabel(String newTime) {
            this.time = newTime;
            timeLabel.setText(time);
        }
        
        public void setTitleLabel(String newTitle) {
            this.title = newTitle;
            titleLabel.setText(title);
        }
        
    }
    
    
    private abstract class SouthButton extends JButton {
        
        protected int width;
        protected int height;
        
        protected int symbolSide;
        protected int symbolX;
        protected int symbolY;
        
        protected Color buttonColor;
        protected Color symbolColor;
        
        
        public SouthButton() {
            
            //dimensiones para el dibujo del boton 
            width = 300;
            height = 100;
            symbolSide = 50;
            
            symbolX = (width / 2) - (symbolSide  / 2);
            symbolY = (height / 2) - (symbolSide  / 2);
            
            this.setPreferredSize(new Dimension(width, height));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(true);
            setBackground(null);
            
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            //castea el objeto
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(buttonColor);
            g2d.fillRoundRect(0, 0, width, height, 50, 50);
            
            drawSymbol(g2d);
            
            //si el boton está presionado hace una sombra
            if (getModel().isPressed()) {
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillRoundRect(0, 0, width, height, 50, 50);
            }
            
        }
        
        //dibuja los simbolos del boton
        protected abstract void drawSymbol(Graphics2D g2d);
    }
    
    
    private class PauseButton extends SouthButton {
        
        public PauseButton() {
            super();            
            buttonColor = new Color(174, 200, 164);
            symbolColor = new Color(231, 239, 199);
        }
        
        @Override
        protected void drawSymbol(Graphics2D g2d) {
            g2d.setColor(symbolColor);
            
            g2d.fillRect(symbolX, symbolY, 20, symbolSide);
            g2d.fillRect(symbolX + 30, symbolY, 20, symbolSide);
        }
    }
    
    
    private class AlarmButton extends SouthButton {
        
        public AlarmButton() {
            super();
            buttonColor = new Color(116, 141, 174);
            symbolColor = new Color(158, 202, 214);
        }
        
        @Override
        protected void drawSymbol(Graphics2D g2d) {
            g2d.setColor(symbolColor);
            
            g2d.fillRect(symbolX, symbolY, symbolSide, symbolSide);
        }
    }
    
}







final class NumberSelector extends JPanel {
        
    JPanel interior;
    JPanel buttonsPanel;
    JLabel textLabel;
    JLabel numLabel;
    
    TriangleButton upButton;
    TriangleButton downButton;
    
    Font numFont;
    Font textFont;
    
    private int time;
    
    
    public NumberSelector(String text, Font font, int startTime, Color color) {
        
        this.time = startTime;
        this.numFont = font.deriveFont(35f);
        this.textFont = font.deriveFont(30f);
        
        this.setForeground(Color.LIGHT_GRAY);
        
        this.setLayout(null);
        this.setPreferredSize(new Dimension(230, 400));
        
        this.setBackground(null);
        
        setInterior();
        setButtons();
        setLabel(text);
        setNumLabel();
        
        addListeners();
        
    }
    
    
    protected void setInterior() {
        interior = new JPanel();
        interior.setLayout(null);
        interior.setBackground(null);
        //interior.setBounds(25, 110, 180, 180);
        interior.setBounds(25, 130, 180, 180);
        
        
        this.add(interior);
    }
    
    protected void setButtons() {
        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(2, 1));
        buttonsPanel.setBounds(0, 0, 60, 180);
        
        buttonsPanel.setBackground(null);
        interior.add(buttonsPanel);
        
        upButton = new TriangleButton(true);
        downButton = new TriangleButton(false);
        
        buttonsPanel.add(upButton);
        buttonsPanel.add(downButton);
    }
    
    
    protected void setLabel(String text) {
        textLabel = new JLabel(text);
        
        textLabel.setBounds(0, 10, 230, 50);
        textLabel.setBackground(null); //lo hace invisible

        //alinea el texto
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        
        textLabel.setFont(textFont);
        textLabel.setForeground(Color.LIGHT_GRAY);
        
        this.add(textLabel);
    }
    
    
    protected void setNumLabel() {
        numLabel = new JLabel(time + " min");
        numLabel.setBounds(60, 60, 120, 60);
        numLabel.setBackground(null);
        numLabel.setOpaque(true);
        
        //alinea el texto
        numLabel.setHorizontalAlignment(JLabel.CENTER);
        numLabel.setHorizontalAlignment(JLabel.CENTER);
        
        numLabel.setFont(numFont);
        numLabel.setForeground(Color.LIGHT_GRAY);
        
        interior.add(numLabel);
    }
    
    protected void updateTime() {
        numLabel.setText(time + " min");
    }
    
    protected void addListeners() {
        
        //listener para el boton up
        upButton.addActionListener((ActionEvent e) -> {
            time ++;
            updateTime();
            numLabel.repaint();
        });
        
        //listener para el boton down
        downButton.addActionListener((ActionEvent e) -> {
            if (time > 1) {
                time --;
            }
            
            updateTime();
            numLabel.repaint();
        });
    }
    
    public int getTime() {
        return time;
    }
    
    
    private class TriangleButton extends JButton {
        
        private final int width = 60;
        private final int height = 90;
        
        private final boolean upTriangle;
        
        public TriangleButton(Boolean up) {
            this.setPreferredSize(new Dimension(width, height));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            //setBackground(null);
            
            upTriangle = up;
        }
        
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            //castea el objeto
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(Color.LIGHT_GRAY);
            
            if (upTriangle) {
                drawUpTriangle(g2d);
            }else {
                drawDownTriangle(g2d);
            }
            
            //si el boton está presionado hace una sombra
            if (getModel().isPressed()) {
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillRect(0, 0, width, height);
            }
            
        }
        
        private void drawUpTriangle(Graphics2D g2d) {
            int[] x = {30, 10, 50};
            int[] y = {25, 65, 65};
            
            g2d.fillPolygon(x, y, 3);
        }
        
        
        private void drawDownTriangle(Graphics2D g2d) {
            int[] x = {10, 50, 30};
            int[] y = {25, 25, 65};
            
            g2d.fillPolygon(x, y, 3);
        }
    }
    
    
    private class Interior extends JPanel {
        
        Color color;
        
        public Interior(Color color) {
            this.color = color;
        }
        
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            //castea el objeto
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(color);
            
            g2d.fillOval(0, 0, 180, 180);
        }
    }

}


class TimeLogic {
    
    public TimeLogic(){
    }
    
    public String formatTime(int timeInSec) {
        int sec;
        int min;
        int hrs;
        
        sec = timeInSec % 60;
        
        min = timeInSec / 60;
        
        if (min >= 60) {
            hrs = min / 60;
            min = min % 60;
        } else {
            hrs = 0;
        }
        
        String secStr = "" + sec;
        String minStr = "" + min;
        String hrsStr = "" + hrs;
        
        
        //forza los tres valores a tener dos digitos
        if (secStr.length() < 2) {
            secStr = "0" + secStr;
        }
        if (minStr.length() < 2) {
            minStr = "0" + minStr;
        }
       
        if (hrs == 0) {
            return minStr + ":" + secStr;
        } else {
            return hrs + ":" + minStr + ":" + secStr;
        }
    }
    
    
    public int toSeconds(int hrs, int min, int sec) {
        return (hrs * 3600) + (min * 60) + sec;
    }
}




